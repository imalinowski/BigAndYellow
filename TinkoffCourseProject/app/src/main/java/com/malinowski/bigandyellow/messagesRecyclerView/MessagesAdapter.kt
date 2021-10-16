package com.malinowski.bigandyellow.messagesRecyclerView

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.customview.MessageViewGroup

class MessagesAdapter(private val dataSet: Array<String>) :
    RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {


    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val view = MessageViewGroup(viewGroup.context)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

    }

    override fun getItemCount() = dataSet.size

}