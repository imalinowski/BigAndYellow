package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentChatBinding
import com.malinowski.bigandyellow.model.data.Chat
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.Reaction
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.DateItemDecorator
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.MessagesAdapter
import io.reactivex.android.schedulers.AndroidSchedulers

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private val model: MainViewModel by activityViewModels()
    private lateinit var chat: Chat

    private val modalBottomSheet = SmileBottomSheet()

    private lateinit var adapter: MessagesAdapter

    private val layoutManager = LinearLayoutManager(context).apply {
        stackFromEnd = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            model.getChat(bundle.getInt(TOPIC_NUM), bundle.getInt(CHAT_NUM))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    chat = it
                    model.result()
                    initUI()
                }, { e -> model.error(e) })
        }
        model.loading()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    fun initUI() {
        binding.chatName.text = chat.name

        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = MessagesAdapter(chat.messages) { position ->
            modalBottomSheet.show(childFragmentManager, SmileBottomSheet.TAG)
            modalBottomSheet.arguments = bundleOf(SmileBottomSheet.MESSAGE_KEY to position)
        }

        binding.messageRecycler.apply {
            adapter = this@ChatFragment.adapter
            layoutManager = this@ChatFragment.layoutManager
            addItemDecoration(
                DateItemDecorator()
            )
        }

        binding.sendMessageButton.setOnClickListener {
            binding.sendMessageText.apply {
                if (this.length() == 0) return@apply
                chat.messages.add(Message(chat.messages.size, text.toString(), User.INSTANCE))
                setText("")
                layoutManager.scrollToPosition(chat.messages.size - 1)
            }
            binding.messageRecycler.adapter?.notifyItemInserted(chat.messages.size)
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
            val smileNum = bundle.getInt(SmileBottomSheet.SMILE_KEY)
            chat.messages[messagePosition].reactions.add(Reaction(smile = smileNum, num = 1))
            adapter.notifyItemChanged(messagePosition)
        }

    }

    companion object {
        private const val TOPIC_NUM = "topic_num"
        private const val CHAT_NUM = "chat_num"
        fun newInstance(topicNum: Int, chatNum: Int) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putInt(TOPIC_NUM, topicNum)
                    putInt(CHAT_NUM, chatNum)
                }
            }
    }
}