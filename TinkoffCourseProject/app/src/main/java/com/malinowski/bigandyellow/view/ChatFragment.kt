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
import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.Reaction
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.DateItemDecorator
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.MessagesAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlin.random.Random

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val model: MainViewModel by activityViewModels()
    private lateinit var messages: MutableList<Message>

    private val modalBottomSheet = SmileBottomSheet()

    private lateinit var adapter: MessagesAdapter

    private val layoutManager = LinearLayoutManager(context).apply {
        stackFromEnd = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            model.getMessages(bundle.getInt(STREAM)!!, bundle.getString(TOPIC)!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    messages = it.toMutableList()
                    model.result()
                    initUI()
                }, { e ->
                    model.error(e)
                })
        }
        model.loading()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun initUI() {
        //binding.chatName.text = topic.name

        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = MessagesAdapter(messages) { position ->
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
                if (Random.nextInt() % 13 == 0) {
                    model.error(Repository.ExpectedError())
                    return@apply
                }
                messages.add(Message(messages.size, text.toString(), true))
                setText("")
                layoutManager.scrollToPosition(messages.size - 1)
            }
            binding.messageRecycler.adapter?.notifyItemInserted(messages.size)
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
            messages[messagePosition].reactions.add(Reaction(smile = smileNum, num = 1))
            adapter.notifyItemChanged(messagePosition)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val STREAM = "stream"
        private const val TOPIC = "topic"
        fun newInstance(streamId: Int, topic: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putInt(STREAM, streamId)
                    putString(TOPIC, topic)
                }
            }
    }
}