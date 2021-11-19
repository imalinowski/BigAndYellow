package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.EmojiAddParcel
import com.malinowski.bigandyellow.EmojiClickParcel
import com.malinowski.bigandyellow.EmojiDeleteParcel
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentChatBinding
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.network.ZulipChat
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.MessagesAdapter
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.Reaction
import com.malinowski.bigandyellow.model.data.UnitedReaction
import com.malinowski.bigandyellow.model.data.User

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val binding by lazy { FragmentChatBinding.inflate(layoutInflater) }
    private val model: MainViewModel by activityViewModels()
    private var messages: MutableList<MessageItem> = mutableListOf()
    private var messagesLoaded = false

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val topicName: String? by lazy { arguments?.getString(TOPIC) }
    private val userName: String? by lazy { arguments?.getString(USER_NAME) }
    private val userEmail: String? by lazy { arguments?.getString(USER_EMAIL) }
    private val streamId: Int? by lazy { arguments?.getInt(STREAM) }

    private val adapter: MessagesAdapter by lazy {
        MessagesAdapter(
            onEmojiClick = { parcel: EmojiClickParcel ->
                processEmojiClick(parcel)
            },
            onLongClick = { position ->
                showBottomSheet(position)
            },
            onBind = { position ->
                if (position == 5 && !messagesLoaded) loadMessages()
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
        val anchor = if (messages.size > 0) "${messages[0].id}" else ZulipChat.NEWEST_MES

        val flow: Observable<List<MessageItem>> =
            if (userEmail != null)
                model.getMessages(userEmail!!, anchor)
            else if (streamId != null && topicName != null)
                model.getMessages(streamId!!, topicName!!, anchor)
            else {
                model.error(IllegalArgumentException("open chat -> Invalid arguments"))
                parentFragmentManager.popBackStack()
                return
            }

        val itemsCopy = messages.toMutableList()
        flow.observeOn(AndroidSchedulers.mainThread(), true)
            .subscribeBy(
                onNext = { messagesPage ->
                    model.result()
                    messagesLoaded = messagesPage.isEmpty()
                    if (!messagesLoaded) {
                        messages = itemsCopy.toMutableList().apply { addAll(0, messagesPage) }
                        adapter.submitList(messages)
                    } else topicName?.let { topicName ->
                        model.setMessageNum(topicName, messages.size)
                    }
                },
                onError = { e ->
                    Log.d("MESSAGES_DEBUG", "${e.message}")
                    model.error(e)
                }
            ).addTo(compositeDisposable)

        model.loading()
    }

    private fun sendMessage(content: String) {
        model.loading()
        val singleId: Single<Int> =
            if (userEmail != null)
                model.sendMessageToUser(userEmail!!, content)
            else
                model.sendMessageToTopic(streamId!!, topicName!!, content)
        singleId
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    messages.add(MessageItem(id, content, User.ME.id, true))
                    adapter.submitList(messages)
                    adapter.notifyItemInserted(messages.size - 1)
                    layoutManager.scrollToPosition(messages.size - 1)
                    model.result()
                },
                { model.error(it) }
            ).addTo(compositeDisposable)
    }

    companion object {
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val STREAM = "stream"
        const val TOPIC = "topic"

        fun newInstance(bundle: Bundle) = ChatFragment().apply { arguments = bundle }

    }
}