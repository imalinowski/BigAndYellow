package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentChatBinding
import com.malinowski.bigandyellow.model.data.Chat
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.DateItemDecorator
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.MessagesAdapter

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private val model: MainViewModel by activityViewModels()
    private lateinit var chat: Chat

    private val modalBottomSheet = SmileBottomSheet()

    private val layoutManager = LinearLayoutManager(context).apply {
        stackFromEnd = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chat = model.getChat(it.getInt(TOPIC_NUM), it.getInt(CHAT_NUM))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(layoutInflater)

        binding.chatName.text = chat.name

        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.messageRecycler.apply {
            adapter = MessagesAdapter(chat.messages) { flow ->
                modalBottomSheet.show(flow, childFragmentManager)
            }
            layoutManager = this@ChatFragment.layoutManager
            addItemDecoration(
                DateItemDecorator()
            )
        }

        binding.sendMessageButton.setOnClickListener {
            binding.sendMessageText.apply {
                if (this.length() == 0) return@apply
                chat.messages.add(Message(text.toString(), User.INSTANCE))
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

        return binding.root
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