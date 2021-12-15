package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.databinding.MessageItemBinding
import com.malinowski.bigandyellow.model.data.EmojiClickParcel
import com.malinowski.bigandyellow.model.data.MessageItem


class MessagesAdapter(
    private val onEmojiClick: (EmojiClickParcel) -> Unit = {},
    private val onLongClick: (messageId: Int) -> Unit = {},
    private val onPlusClick: (messageId: Int) -> Unit = {},
    private val onBind: (position: Int) -> Unit = {}
) : ListAdapter<MessageItem, MessagesAdapter.ViewHolder>(InterestingItemDiffUtilCallback()) {

    class ViewHolder(val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MessageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        onBind(position)
        val message = getItem(position)
        viewHolder.binding.messageItem.apply {
            setMessage(message)
            setOnEmojiClickListener(onEmojiClick)
            setMessageOnLongClick { onLongClick(message.id) }
            setPlusClickListener { onPlusClick(message.id) }
        }
        viewHolder.binding.date.apply {
            text = message.getDate()
            isVisible = isPlaceForDate(position)
        }
    }

    private fun isPlaceForDate(position: Int): Boolean {
        if (position + 1 == itemCount) return true
        val curDay = getItem(position).timestamp / SECONDS_IN_DAY
        val prevDay = getItem(position + 1).timestamp / SECONDS_IN_DAY
        return curDay > prevDay
    }

    class InterestingItemDiffUtilCallback : DiffUtil.ItemCallback<MessageItem>() {

        override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MessageItem,
            newItem: MessageItem
        ): Boolean {
            return oldItem == newItem && (oldItem.emoji == newItem.emoji)
        }

    }

    companion object {
        const val SECONDS_IN_DAY = 86400
    }

}