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
        when (dataSet[position]) {
            is TopicItem -> holder.apply {
                chat.visibility = GONE
                topic.visibility = VISIBLE
                topicName.text = (dataSet[position] as TopicItem).name
            }
            is ChatItem -> holder.apply {
                chat.visibility = VISIBLE
                topic.visibility = GONE
                chatName.text = (dataSet[position] as ChatItem).name
                messagesNum.text = (dataSet[position] as ChatItem).messageNum.toString()
            }
        }
        holder.view.setOnClickListener {
            if(position >= itemCount) return@setOnClickListener
            onClick(position)
            val item = dataSet[position]
            if ( item is TopicItem) {
                holder.topicArrow.also { arrow ->
                    val animation = ObjectAnimator.ofFloat(
                        arrow,
                        "rotation",
                        if(item.expanded) 180f else 0f,
                        if(item.expanded) 0f else 180f
                    )
                    animation.duration = 300
                    animation.start()
                }
            }
        }
    }

    override fun getItemCount(): Int = dataSet.size
}