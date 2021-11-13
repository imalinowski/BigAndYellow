package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.EmojiClickParcel
import com.malinowski.bigandyellow.databinding.MessageItemBinding
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.StreamTopicItem


class MessagesAdapter(
    private val emojiClickListener: (EmojiClickParcel) -> Unit,
    private val longClickListener: (position: Int) -> Unit
) : ListAdapter<Message, MessagesAdapter.ViewHolder>(InterestingItemDiffUtilCallback()) {

    class ViewHolder(val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MessageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        getItem(position).apply {
            viewHolder.binding.messageItem.setMessage(this)
            viewHolder.binding.messageItem.setOnEmojiClickListener(emojiClickListener)
            viewHolder.binding.messageItem.setMessageOnLongClick {
                longClickListener(position)
            }
            viewHolder.binding.date.text = getItem(position).getDate()
            viewHolder.binding.date.isVisible = isPlaceForDate(position)
        }
    }

    private fun isPlaceForDate(position: Int): Boolean {
        if (position == 0) return true
        val prevDay = getItem(position - 1).timestamp / SECONDS_IN_DAY
        val curDay = getItem(position).timestamp / SECONDS_IN_DAY
        return prevDay < curDay
    }

    class InterestingItemDiffUtilCallback : DiffUtil.ItemCallback<Message>() {

        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(
            oldItem: Message,
            newItem: Message
        ): Boolean {
            return oldItem == newItem && (oldItem.emoji == newItem.emoji)
        }

    }

    companion object {
        const val SECONDS_IN_DAY = 86400
    }

}