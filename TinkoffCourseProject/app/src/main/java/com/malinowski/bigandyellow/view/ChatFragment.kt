package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentChatBinding
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.network.ZulipChat
import com.malinowski.bigandyellow.utils.EmojiAddParcel
import com.malinowski.bigandyellow.utils.EmojiClickParcel
import com.malinowski.bigandyellow.utils.EmojiDeleteParcel
import com.malinowski.bigandyellow.view.mvi.FragmentMVI
import com.malinowski.bigandyellow.view.mvi.events.Event
import com.malinowski.bigandyellow.view.mvi.states.State
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.MessagesAdapter

class ChatFragment : FragmentMVI<State.Chat>(R.layout.fragment_chat) {

    private val binding by lazy { FragmentChatBinding.inflate(layoutInflater) }
    private val model: MainViewModel by activityViewModels()

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
            onLongClick = { position ->
                showBottomSheet(position)
            },
            onBind = { position ->
                if (position == 5 && !state.loaded) loadMessages()
            }
        )
    }

    private val layoutManager = LinearLayoutManager(context).apply {
        stackFromEnd = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMessages()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initUI()
        model.chatState.observe(viewLifecycleOwner) { render(it) }
    }

    override fun render(state: State.Chat) {
        this.state = state
        if (state.loaded && topicName != null)
            model.processEvent(Event.SetMessageNum(topicName!!, messages.size))
        binding.chatName.text = state.name
        adapter.submitList(messages)
    }

    private fun showBottomSheet(position: Int) {
        SmileBottomSheet()
            .apply { arguments = bundleOf(SmileBottomSheet.MESSAGE_KEY to position) }
            .show(childFragmentManager, SmileBottomSheet.TAG)
    }

    private fun processEmojiClick(parcel: EmojiClickParcel) {
        when (parcel) {
            is EmojiAddParcel ->
                model.addReaction(parcel.messageId, parcel.name)
            is EmojiDeleteParcel ->
                model.deleteReaction(parcel.messageId, parcel.name)
        }
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
            SmileBottomSheet.SMILE_RESULT,
            this
        ) { _, bundle ->
            val messagePosition = bundle.getInt(SmileBottomSheet.MESSAGE_KEY)
            val unicode = bundle.getString(SmileBottomSheet.SMILE_KEY)!!
            val name = bundle.getString(SmileBottomSheet.SMILE_NAME)!!

            if (messagePosition >= messages.size) { // since there two source of messages collisions happens
                model.error(java.lang.IllegalStateException(getString(R.string.error_data_expired)))
                return@setFragmentResultListener
            }

            val emoji = Reaction(userId = User.ME.id, unicode = unicode, name = name)

            // add emoji an case emoji haven't exist before or it has been added by other users
            val emojiGroup: UnitedReaction? =
                messages[messagePosition].emoji[emoji.getUnicode()]
            if (emojiGroup == null || !emojiGroup.usersId.contains(User.ME.id)) {
                messages[messagePosition].addEmoji(emoji) // data update
                model.addReaction(messages[messagePosition].id, emoji.name) // net call
                adapter.notifyItemChanged(messagePosition) // ui update
            } else {
                model.error(IllegalStateException(getString(R.string.error_emoji_added)))
            }
        }
    }

    private fun loadMessages() {
        val anchor = if (messages.isNotEmpty()) "${messages[0].id}" else ZulipChat.NEWEST_MES
        Log.d("LOAD_MESSAGES", anchor)
        model.processEvent(
            if (userEmail != null)
                Event.LoadMessages.ForUser(userEmail!!, anchor)
            else if (streamId != null && topicName != null)
                Event.LoadMessages.ForTopic(streamId!!, topicName!!, anchor)
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
                Event.SendMessage.ToUser(userEmail!!, content)
            else
                Event.SendMessage.ToTopic(streamId!!, topicName!!, content)
        )
    }

    companion object {
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val STREAM = "stream"
        const val TOPIC = "topic"

        fun newInstance(bundle: Bundle) = ChatFragment().apply { arguments = bundle }

    }
}