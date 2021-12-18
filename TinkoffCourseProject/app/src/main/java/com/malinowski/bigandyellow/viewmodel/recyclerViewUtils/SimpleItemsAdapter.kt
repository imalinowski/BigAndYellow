package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.databinding.SimpleItemBinding
import com.malinowski.bigandyellow.model.data.SimpleItem

class SimpleItemsAdapter(
    val onClick: (Int) -> Unit
) : ListAdapter<SimpleItem, SimpleItemsAdapter.ViewHolder>(InterestingItemDiffUtilCallback()) {

    class ViewHolder(val binding: SimpleItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SimpleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.item.apply {
            text = item.name
            setTextColor(item.color)
            textSize = item.textSize
        }
        holder.binding.root.setOnClickListener {
            onClick(position)
        }
    }

    class InterestingItemDiffUtilCallback : DiffUtil.ItemCallback<SimpleItem>() {

        override fun areItemsTheSame(oldItem: SimpleItem, newItem: SimpleItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: SimpleItem, newItem: SimpleItem): Boolean {
            return oldItem == newItem
        }

    }

}