package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.EmojiClickParcel
import com.malinowski.bigandyellow.databinding.MessageItemBinding
import com.malinowski.bigandyellow.model.data.Message


class MessagesAdapter(
    private val dataSet: MutableList<Message>,
    val emojiClickListener: (EmojiClickParcel) -> Unit,
    val longClickListener: (position: Int) -> Unit
) :
    RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    class ViewHolder(val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MessageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        dataSet[position].apply {
            viewHolder.binding.messageItem.setMessage(this)
            viewHolder.binding.messageItem.setOnEmojiClickListener(emojiClickListener)
            viewHolder.binding.messageItem.setMessageOnLongClick {
                longClickListener(position)
            }
            viewHolder.binding.date.text = dataSet[position].getDate()
            viewHolder.binding.date.isVisible = isPlaceForDate(position)
        }
    }

    private fun isPlaceForDate(position: Int): Boolean {
        if (position == 0) return true
        val prevDay = dataSet[position - 1].timestamp / SECONDS_IN_DAY
        val curDay = dataSet[position].timestamp / SECONDS_IN_DAY
        return prevDay < curDay
    }

    override fun getItemCount() = dataSet.size

    companion object {
        const val SECONDS_IN_DAY = 86400
    }

}