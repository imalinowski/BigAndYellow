package com.malinowski.bigandyellow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.data.Message
import com.malinowski.bigandyellow.data.Reaction
import com.malinowski.bigandyellow.databinding.ActivityMainBinding
import com.malinowski.bigandyellow.messagesRecyclerView.DateItemDecorator
import com.malinowski.bigandyellow.messagesRecyclerView.MessagesAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // backend in future
    private val messages: MutableList<Message> = with("Nikolay Nekrasov") {
        mutableListOf(
            Message(0, "Вчерашний день, часу в шестом,\nЗашел я на Сенную;", this),
            Message(1, "Там били женщину кнутом,\nКрестьянку молодую.", this),
            Message(2, "Ни звука из ее груди,\nЛишь бич свистал, играя...", this),
            Message(
                3, "И Музе я сказал: «Гляди!\nСестра твоя родная!».", this,
                mutableListOf(Reaction("other", 34, 3))
            ),
        )
    }

    private val modalBottomSheet = SmileBottomSheet()
    private val adapter = MessagesAdapter(messages) { position ->
        modalBottomSheet.show(supportFragmentManager, SmileBottomSheet.TAG)
        modalBottomSheet.arguments = bundleOf(SmileBottomSheet.MESSAGE_KEY to position)
    }
    private val layoutManager = LinearLayoutManager(this).apply {
        stackFromEnd = true
    }

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
                if (this.length() == 0) return@apply
                messages.add(Message(messages.size, text.toString()))
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

        supportFragmentManager.setFragmentResultListener(
            SmileBottomSheet.SMILE_RESULT,
            this
        ) { _, bundle ->
            val messagePosition = bundle.getInt(SmileBottomSheet.MESSAGE_KEY)
            val smileNum = bundle.getInt(SmileBottomSheet.SMILE_KEY)
            messages[messagePosition].reactions.add(Reaction(smile = smileNum, num = 1))
            adapter.notifyItemChanged(messagePosition)
        }

    }


}