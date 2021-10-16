package com.malinowski.bigandyellow.messagesRecyclerView

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.customview.MessageViewGroup
import com.malinowski.bigandyellow.data.Message
import com.malinowski.bigandyellow.data.Reaction
import io.reactivex.rxjava3.subjects.PublishSubject

class MessagesAdapter(
    private val dataSet: MutableList<Message>,
    val openBottomSheet: (PublishSubject<Reaction>) -> Unit
) :
    RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    class ViewHolder(val view: MessageViewGroup) : RecyclerView.ViewHolder(view) {}

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val view = MessageViewGroup(viewGroup.context)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        dataSet[position].apply {
            viewHolder.view.setMessage(name, message, emojis.toList(), flow)
            viewHolder.view.setMessageOnLongClick {
                openBottomSheet(flow)
            }
        }
    }

    override fun getItemCount() = dataSet.size

}