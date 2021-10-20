package com.malinowski.bigandyellow.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.ActivityMainBinding
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.messagesRecyclerView.DateItemDecorator
import com.malinowski.bigandyellow.viewmodel.messagesRecyclerView.MessagesAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val model: MainViewModel by viewModels()

    private val modalBottomSheet = SmileBottomSheet()

    private val layoutManager = LinearLayoutManager(this).apply {
        stackFromEnd = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.messageRecycler.apply {
            adapter = MessagesAdapter(model.messages) { flow ->
                modalBottomSheet.show(flow, supportFragmentManager)
            }
            layoutManager = this@MainActivity.layoutManager
            addItemDecoration(
                DateItemDecorator()
            )
        }

        binding.sendMessageButton.setOnClickListener {
            binding.sendMessageText.apply {
                if (this.length() == 0) return@apply
                model.addMessage(Message(text.toString(), User.INSTANCE))
                setText("")
                layoutManager.scrollToPosition(model.messages.size - 1)
            }
            binding.messageRecycler.adapter?.notifyItemInserted(model.messages.size)
        }

        binding.sendMessageText.doAfterTextChanged {
            if (it?.length == 0)
                binding.sendMessageButton.setImageResource(R.drawable.ic_add_file_to_message)
            else
                binding.sendMessageButton.setImageResource(R.drawable.ic_send_message)
        }
    }


}