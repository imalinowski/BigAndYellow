package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.databinding.SimpleItemBinding

class SimpleItemsAdapter(
    val onClick: (Int) -> Unit
) : ListAdapter<String, SimpleItemsAdapter.ViewHolder>(InterestingItemDiffUtilCallback()) {

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
        holder.binding.item.text = item
        holder.binding.root.setOnClickListener {
            onClick(position)
        }
    }

    class InterestingItemDiffUtilCallback : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

}