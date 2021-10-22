package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.model.data.User

class UserAdapter(
    private val dataSet: MutableList<User>,
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = dataSet[position].name
    }

    override fun getItemCount(): Int = dataSet.size

}