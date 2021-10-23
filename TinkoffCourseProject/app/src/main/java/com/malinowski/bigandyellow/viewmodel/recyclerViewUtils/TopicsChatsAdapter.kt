package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.model.data.ChatItem
import com.malinowski.bigandyellow.model.data.TopicChatItem
import com.malinowski.bigandyellow.model.data.TopicItem

class TopicsChatsAdapter(
    private val dataSet: MutableList<TopicChatItem>,
    private val onClick: (position: Int) -> Unit
) : RecyclerView.Adapter<TopicsChatsAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val topicName: TextView = view.findViewById(R.id.topic_name)
        val topicArrow: ImageView = view.findViewById(R.id.topic_arrow)
        val chatName: TextView = view.findViewById(R.id.chat_name)
        val messagesNum: TextView = view.findViewById(R.id.messages_num)
        val topic: ConstraintLayout = view.findViewById(R.id.topic_constraint)
        val chat: LinearLayout = view.findViewById(R.id.chat_linear)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.topic_and_chats_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        when (item) {
            is TopicItem -> holder.apply {
                chat.visibility = GONE
                topic.visibility = VISIBLE
                topicName.text = item.name
            }
            is ChatItem -> holder.apply {
                chat.visibility = VISIBLE
                topic.visibility = GONE
                chatName.text = item.name
                messagesNum.text = item.messageNum.toString()
                chat.setBackgroundResource(if (position % 2 == 0) R.drawable.bg_green else R.drawable.bg_purple)
            }
        }

        holder.view.setOnClickListener {
            if (position >= itemCount) return@setOnClickListener
            onClick(position)
            if (item is TopicItem) holder.topicArrow.let { arrow ->
                ObjectAnimator.ofFloat(
                    arrow, "rotation",
                    if (item.expanded) 0f else 180f,
                    if (item.expanded) 180f else 0f
                ).apply {
                    duration = 300
                }.start()
            }
        }
    }

    override fun getItemCount(): Int = dataSet.size
}