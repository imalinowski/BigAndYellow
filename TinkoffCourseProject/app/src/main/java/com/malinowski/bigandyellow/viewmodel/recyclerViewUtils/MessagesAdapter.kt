package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.databinding.MessageItemBinding
import com.malinowski.bigandyellow.model.data.MessageItem
import com.malinowski.bigandyellow.model.data.parcels.*


class MessagesAdapter(
    private val onAction: (parcel: MessageParcel) -> Unit = {},
) : ListAdapter<MessageItem, MessagesAdapter.ViewHolder>(InterestingItemDiffUtilCallback()) {

    class ViewHolder(val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MessageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        onAction(OnBind(position))
        val message = getItem(position)
        viewHolder.binding.messageItem.apply {
            setMessage(message)
            setOnEmojiClickListener {
                onAction(it)
            }
            setMessageOnLongClick {
                onAction(ShowBottomSheet(message.id))
            }
            setPlusClickListener {
                onAction(ShowSmileBottomSheet(message.id))
            }
        }
        viewHolder.binding.date.apply {
            text = message.getDate()
            isVisible = isPlaceForDate(position)
        }
        viewHolder.binding.topicName.apply {
            text = message.topic
            isVisible = isPlaceForTopic(position)
            setOnClickListener {
                onAction(OpenTopic(message.streamId, message.topic))
            }
        }
    }

    private fun isPlaceForDate(position: Int): Boolean {
        if (position + 1 == itemCount) return true
        val curDay = getItem(position).timestamp / SECONDS_IN_DAY
        val prevDay = getItem(position + 1).timestamp / SECONDS_IN_DAY
        return curDay > prevDay
    }

    private fun isPlaceForTopic(position: Int): Boolean {
        if (position + 1 == itemCount) return false
        val curDay = getItem(position).topic
        val prevDay = getItem(position + 1).topic
        return curDay != prevDay
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