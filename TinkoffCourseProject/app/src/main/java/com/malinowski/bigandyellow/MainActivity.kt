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
    val adapter = MessagesAdapter(messages)
    val layoutManager = LinearLayoutManager(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.messageRecycler.apply {
            adapter = this@MainActivity.adapter
            layoutManager = this@MainActivity.layoutManager
            addItemDecoration(
                DateItemDecorator()
            )
        }

        binding.sendMessageButton.setOnClickListener {
            binding.sendMessageText.apply {
                if(this.length() == 0) return@apply
                messages.add(text.toString())
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
    }
}