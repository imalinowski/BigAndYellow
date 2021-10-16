package com.malinowski.bigandyellow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.messagesRecyclerView.DateItemDecorator
import com.malinowski.bigandyellow.messagesRecyclerView.MessagesAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<RecyclerView>(R.id.message_recycler).apply {
            adapter = MessagesAdapter(Array(10) { "$it" })
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(
                DateItemDecorator()
            )
        }
    }
}