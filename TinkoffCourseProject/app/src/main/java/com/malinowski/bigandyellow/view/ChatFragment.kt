package com.malinowski.bigandyellow.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentChatBinding
import com.malinowski.bigandyellow.getComponent
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.network.ZulipChat
import com.malinowski.bigandyellow.view.mvi.events.ChatEvent
import com.malinowski.bigandyellow.view.mvi.states.State
import com.malinowski.bigandyellow.viewmodel.ChatViewModel
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.MessagesAdapter
import javax.inject.Inject

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val binding by lazy { FragmentChatBinding.inflate(layoutInflater) }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mainModel: MainViewModel by activityViewModels { viewModelFactory }
    private val model: ChatViewModel by viewModels { viewModelFactory }

    private val topicName: String? by lazy { arguments?.getString(TOPIC) }
    private val userName: String? by lazy { arguments?.getString(USER_NAME) }
    private val userEmail: String? by lazy { arguments?.getString(USER_EMAIL) }
    private val streamId: Int? by lazy { arguments?.getInt(STREAM) }

    private var state = State.Chat("", listOf())
    private val messages
        get() = state.messages

    private val adapter: MessagesAdapter by lazy {
        MessagesAdapter(
            onEmojiClick = { parcel: EmojiClickParcel ->
                processEmojiClick(parcel)
            },
            onLongClick = { messageId ->
                showBottomSheet(messageId)
            },
            onPlusClick = { messageId ->
                showSmileBottomSheet(messageId)
            },
            onBind = { position ->
                if (position == messages.size - 5 && !state.loaded) loadMessages()
            }
        )
    }

    private val layoutManager = LinearLayoutManager(context).apply {
        reverseLayout = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getComponent().chatComponent().create().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.setName(userName ?: topicName!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loadMessages()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI()
        model.chatState.observe(viewLifecycleOwner) { render(it) }
        model.scrollToPos.observe(viewLifecycleOwner) {
            adapter.submitList(messages) {
                binding.messageRecycler.scrollToPosition(it)
            }
        }
        model.chatScreenState.observe(viewLifecycleOwner) {
            mainModel.setScreenState(it)
        }
    }

    private fun render(state: State.Chat) {
        this.state = state
        if (state.loaded)
            model.processEvent(ChatEvent.SetMessageNum(topicName, messages.size))
        binding.chatName.text = state.name
        adapter.submitList(messages)
    }

    private fun showBottomSheet(position: Int) {
        BottomSheet()
            .apply { arguments = bundleOf(BottomSheet.MESSAGE_KEY to position) }
            .show(childFragmentManager, BottomSheet.TAG)
    }

    private fun showSmileBottomSheet(position: Int) {
        SmileBottomSheet()
            .apply { arguments = bundleOf(SmileBottomSheet.MESSAGE_KEY to position) }
            .show(childFragmentManager, SmileBottomSheet.TAG)
    }

    private fun processEmojiClick(parcel: EmojiClickParcel) {
        model.processEvent(
            when (parcel) {
                is EmojiAddParcel ->
                    ChatEvent.Reaction.Add(parcel.messageId, parcel.name)
                is EmojiDeleteParcel ->
                    ChatEvent.Reaction.Remove(parcel.messageId, parcel.name)
            }
        )
    }

    private fun initUI() {
        binding.chatName.text = "#%s".format(topicName ?: userName)

        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.messageRecycler.apply {
            adapter = this@ChatFragment.adapter
            layoutManager = this@ChatFragment.layoutManager
        }

        binding.sendMessageButton.setOnClickListener {
            binding.sendMessageText.apply {
                if (this.length() == 0) return@apply
                sendMessage(text.toString())
                setText("")
            }
        }

        binding.sendMessageText.doAfterTextChanged {
            if (it?.length == 0)
                binding.sendMessageButton.setImageResource(R.drawable.ic_add_file_to_message)
            else
                binding.sendMessageButton.setImageResource(R.drawable.ic_send_message)
        }

        childFragmentManager.setFragmentResultListener(
            BOTTOM_SHEET_RES, this
        ) { _, bundle ->
            try {
                when (val data = bundle.getParcelable<BottomSheetResult>(BOTTOM_SHEET_RES)) {
                    is AddEmoji ->
                        onAddEmojiResult(
                            getMessageById(data.messageId),
                            data.unicode,
                            data.name
                        )
                    is Copy ->
                        copyMessage(getMessageById(data.messageId))
                    is Delete ->
                        deleteMessage(getMessageById(data.messageId))
                }

            } catch (e: java.lang.IllegalStateException) {
                model.error(e)
            }
        }
    }

    private fun getMessageById(messageId: Int): MessageItem {
        return messages.find { it.id == messageId } // since there two source of messages collisions happens
            ?: throw java.lang.IllegalStateException(getString(R.string.error_data_expired))
    }

    private fun deleteMessage(message: MessageItem) {
        model.processEvent(ChatEvent.DeleteMessage(message.id))
    }

    private fun copyMessage(message: MessageItem) {
        val text = HtmlCompat.fromHtml(message.message, HtmlCompat.FROM_HTML_MODE_LEGACY).trim()
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData =
            ClipData.newPlainText(getString(R.string.message_copy), text)
        clipboard.setPrimaryClip(clip)
        model.result(getString(R.string.copied))
    }

    private fun onAddEmojiResult(message: MessageItem, unicode: String, name: String) {
        val emoji = Reaction(userId = User.ME.id, unicode = unicode, name = name)

        // add emoji an case emoji haven't exist before or it has been added by other users
        val emojiGroup: UnitedReaction? =
            message.emoji[emoji.getUnicode()]
        if (emojiGroup == null || !emojiGroup.usersId.contains(User.ME.id)) {
            message.addEmoji(emoji) // data update
            model.processEvent(ChatEvent.Reaction.Add(message.id, emoji.name)) // net call
            adapter.notifyItemChanged(messages.indexOf(message)) // ui update
        } else {
            model.error(IllegalStateException(getString(R.string.error_emoji_added)))
        }
    }

    private fun loadMessages() {
        val anchor = if (messages.isNotEmpty()) "${messages.last().id}" else ZulipChat.NEWEST_MES
        Log.d("LOAD_MESSAGES", anchor)
        model.processEvent(
            if (userEmail != null)
                ChatEvent.LoadMessages.ForUser(userEmail!!, anchor)
            else if (streamId != null && topicName != null)
                ChatEvent.LoadMessages.ForTopic(streamId!!, topicName!!, anchor)
            else {
                model.error(IllegalArgumentException("open chat -> Invalid arguments"))
                parentFragmentManager.popBackStack()
                return
            }
        )
    }

    private fun sendMessage(content: String) {
        model.processEvent(
            if (userEmail != null)
                ChatEvent.SendMessage.ToUser(userEmail!!, content)
            else
                ChatEvent.SendMessage.ToTopic(streamId!!, topicName!!, content)
        )
    }

    companion object {
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val STREAM = "stream"
        const val TOPIC = "topic"
        const val BOTTOM_SHEET_RES = "bottom sheet result"

        fun newInstance(bundle: Bundle) = ChatFragment().apply { arguments = bundle }

    }
}