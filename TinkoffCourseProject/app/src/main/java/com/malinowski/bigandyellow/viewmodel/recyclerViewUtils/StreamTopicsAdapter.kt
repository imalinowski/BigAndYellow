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
import com.malinowski.bigandyellow.model.data.StreamItem
import com.malinowski.bigandyellow.model.data.StreamTopicItem
import com.malinowski.bigandyellow.model.data.TopicItem

class TopicsChatsAdapter(
    private val onClick: (position: Int) -> Unit
) : ListAdapter<StreamTopicItem, TopicsChatsAdapter.ViewHolder>(InterestingItemDiffUtilCallback()) {

    class ViewHolder(private val viewBinding: TopicAndChatsItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {

        val topicArrow by lazy {
            viewBinding.topicArrow
        }

        fun bind(item: StreamTopicItem) {
            when (item) {
                is StreamItem -> with(viewBinding) {
                    chatLinear.visibility = GONE
                    topicConstraint.visibility = VISIBLE
                    topicName.text = item.name
                    topicArrow.rotation = if (item.expanded) 180f else 0f
                    progressBar.isVisible = item.loading
                    topicArrow.isVisible = !item.loading
                }
                is TopicItem -> with(viewBinding) {
                    chatLinear.visibility = VISIBLE
                    topicConstraint.visibility = GONE
                    chatName.text = item.name
                    messagesNum.text =
                        item.messageNum.let { if (it > 0) "$it mes" else "" }
                    chatLinear.setBackgroundResource(
                        if (item.topicId % 2 == 0) R.drawable.bg_green else R.drawable.bg_purple
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
            if (item is StreamItem) holder.topicArrow.let { arrow ->
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

    class InterestingItemDiffUtilCallback : DiffUtil.ItemCallback<StreamTopicItem>() {

        override fun areItemsTheSame(oldItem: StreamTopicItem, newItem: StreamTopicItem): Boolean {
            if (oldItem is TopicItem && newItem is TopicItem)
                return oldItem.topicId == newItem.topicId
            if (oldItem is StreamItem && newItem is StreamItem)
                return oldItem.id == newItem.id
            return false
        }

        override fun areContentsTheSame(
            oldItem: StreamTopicItem,
            newItem: StreamTopicItem
        ): Boolean {
            return oldItem == newItem
        }

    }
}