package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View.*
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.TopicAndChatsItemBinding
import com.malinowski.bigandyellow.model.data.ChatItem
import com.malinowski.bigandyellow.model.data.TopicChatItem
import com.malinowski.bigandyellow.model.data.TopicItem

class TopicsChatsAdapter(
    private val onClick: (position: Int) -> Unit
) : ListAdapter<TopicChatItem, TopicsChatsAdapter.ViewHolder>(InterestingItemDiffUtilCallback()) {

    class ViewHolder(private val viewBinding: TopicAndChatsItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        val topicArrow by lazy {
            viewBinding.topicArrow
        }

        fun bind(item: TopicChatItem) {
            when (item) {
                is TopicItem -> {
                    viewBinding.chatLinear.visibility = GONE
                    viewBinding.topicConstraint.visibility = VISIBLE
                    viewBinding.topicName.text = item.name
                    viewBinding.topicArrow.rotation = if (item.expanded) 180f else 0f
                    viewBinding.progressBar.isVisible = item.loading
                    viewBinding.topicArrow.isVisible = !item.loading
                }
                is ChatItem -> {
                    viewBinding.chatLinear.visibility = VISIBLE
                    viewBinding.topicConstraint.visibility = GONE
                    viewBinding.chatName.text = item.name
                    viewBinding.messagesNum.text = item.messageNum.toString()
                    viewBinding.chatLinear.setBackgroundResource(
                        if (item.chatId % 2 == 0) R.drawable.bg_green else R.drawable.bg_purple
                    )
                }
            }
        }

        fun setOnClickListener(i: OnClickListener) {
            viewBinding.root.setOnClickListener(i)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TopicAndChatsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        holder.setOnClickListener {
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

    class InterestingItemDiffUtilCallback : DiffUtil.ItemCallback<TopicChatItem>() {

        override fun areItemsTheSame(oldItem: TopicChatItem, newItem: TopicChatItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: TopicChatItem, newItem: TopicChatItem): Boolean {
            return oldItem == newItem
        }

    }
}