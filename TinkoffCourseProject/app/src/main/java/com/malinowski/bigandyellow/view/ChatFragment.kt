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
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.Reaction
import com.malinowski.bigandyellow.model.data.UnitedReaction
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.MessagesAdapter
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val binding by lazy {
        FragmentChatBinding.inflate(layoutInflater)
    }

    private val model: MainViewModel by activityViewModels()
    private var messages: MutableList<Message> = mutableListOf()

    private val modalBottomSheet = SmileBottomSheet()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val adapter: MessagesAdapter by lazy {
        MessagesAdapter(
            { parcel: EmojiClickParcel ->
                when (parcel) {
                    is EmojiAddParcel ->
                        model.addReaction(parcel.messageId, parcel.name)
                    is EmojiDeleteParcel ->
                        model.deleteReaction(parcel.messageId, parcel.name)
                }
            })
        { position ->
            modalBottomSheet.show(childFragmentManager, SmileBottomSheet.TAG)
            modalBottomSheet.arguments = bundleOf(SmileBottomSheet.MESSAGE_KEY to position)
        }
    }

    private val layoutManager = LinearLayoutManager(context).apply {
        stackFromEnd = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->

            val flow: Observable<List<Message>> =
                if (bundle.containsKey(USER))
                    model.getMessages(bundle.getString(USER)!!)
                else if (bundle.containsKey(STREAM) && bundle.containsKey(TOPIC))
                    model.getMessages(
                        bundle.getInt(STREAM),
                        bundle.getString(TOPIC)!!
                    )
                else {
                    model.error(IllegalArgumentException("open chat -> Invalid arguments"))
                    parentFragmentManager.popBackStack()
                    return@let
                }

            flow
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribeBy(
                    onNext = {
                        Log.d("MESSAGES_DEBUG", "$it")
                        messages = it.toMutableList()
                        model.result()
                        adapter.submitList(it){
                            binding.messageRecycler.scrollToPosition(it.size - 1)
                        }
                    },
                    onError = { e ->
                        Log.d("MESSAGES_DEBUG", "${e.message}")
                        model.error(e)
                    }
                ).addTo(compositeDisposable)

        }
        model.loading()
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

    private fun initUI() {
        binding.chatName.text = "#%s".format(
            arguments?.getString(TOPIC) ?: arguments?.getString(USER_NAME)
        )

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

            val emoji = Reaction(userId = User.ME.id, code = unicode, name = name)

            // add emoji an case emoji haven't exist before or it has been added by other users
            val emojiGroup: UnitedReaction? = messages[messagePosition].emoji[emoji.getUnicode()]
            if (emojiGroup == null || !emojiGroup.usersId.contains(User.ME.id)) {
                messages[messagePosition].addEmoji(emoji) // data update
                model.addReaction(messages[messagePosition].id, emoji.name) // net call
                adapter.notifyItemChanged(messagePosition) // ui update
            } else {
                model.error(IllegalStateException(getString(R.string.error_emoji_added)))
            }
        }
    }

    private fun sendMessage(content: String) {
        val bundle = requireArguments()
        model.loading()
        val singleId: Single<Int> = if (bundle.containsKey(USER)) {
            val userEmail = bundle.getString(USER)!!
            model.sendMessageToUser(userEmail, content)
        } else {
            val streamId = bundle.getInt(STREAM)
            val topicName = bundle.getString(TOPIC)!!
            model.sendMessageToTopic(streamId, topicName, content)
        }
        singleId
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    messages.add(Message(id, content, User.ME.id))
                    adapter.submitList(messages)
                    adapter.notifyItemInserted(messages.size - 1)
                    layoutManager.scrollToPosition(messages.size - 1)
                    model.result()
                },
                { model.error(it) }
            ).addTo(compositeDisposable)
    }

    companion object {
        const val USER = "user_email"
        const val USER_NAME = "user_name"
        const val STREAM = "stream"
        const val TOPIC = "topic"

        fun newInstance(bundle: Bundle) = ChatFragment().apply { arguments = bundle }

    }
}