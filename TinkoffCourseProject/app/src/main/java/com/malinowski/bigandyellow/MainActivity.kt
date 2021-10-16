package com.malinowski.bigandyellow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.databinding.ActivityMainBinding
import com.malinowski.bigandyellow.messagesRecyclerView.DateItemDecorator
import com.malinowski.bigandyellow.messagesRecyclerView.MessagesAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val messages: MutableList<String> = MutableList(10) { "$it" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.messageRecycler.apply {
            adapter = MessagesAdapter(messages)
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(
                DateItemDecorator()
            )
        }

        binding.sendMessageButton.setOnClickListener {
            binding.sendMessageText.apply {
                messages.add(text.toString())
                setText("")
            }
            binding.messageRecycler.adapter?.notifyItemInserted(messages.size)
        }

        binding.sendMessageText.doAfterTextChanged {
            if (it?.length == 0)
                binding.sendMessageButton.setImageResource(R.drawable.ic_add_file_to_message)
            else
                binding.sendMessageButton.setImageResource(R.drawable.ic_send_message)
        }
    }
}