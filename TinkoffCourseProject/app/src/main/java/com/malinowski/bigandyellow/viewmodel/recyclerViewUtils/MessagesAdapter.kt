package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.Reaction
import com.malinowski.bigandyellow.view.customview.MessageViewGroup
import io.reactivex.rxjava3.subjects.PublishSubject

class MessagesAdapter(
    private val dataSet: MutableList<Message>,
    val openBottomSheet: (PublishSubject<Reaction>) -> Unit
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    class ViewHolder(val view: MessageViewGroup) : RecyclerView.ViewHolder(view) {}

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = MessageViewGroup(viewGroup.context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        dataSet[position].apply {
            viewHolder.view.setMessage(this)
            viewHolder.view.setMessageOnLongClick {
                openBottomSheet(flow)
            }
        }
    }

    override fun getItemCount() = dataSet.size

}