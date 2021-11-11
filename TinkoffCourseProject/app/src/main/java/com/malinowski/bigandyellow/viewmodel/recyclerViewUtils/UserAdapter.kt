package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.UserItemBinding
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.model.data.UserStatus

class UserAdapter(
    val callback: (User)->Unit
) :
    ListAdapter<User, UserAdapter.ViewHolder>(InterestingItemDiffUtilCallback()) {

    class ViewHolder(val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            UserItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.binding.name.text = user.name
        holder.binding.email.text = user.email
        holder.binding.userStatus.setImageResource(
            when (user.status) {
                UserStatus.Online -> R.drawable.ic_green_dot
                UserStatus.Idle -> R.drawable.ic_orange_dot
                UserStatus.Offline -> R.drawable.ic_red_dot
            }
        )
        holder.binding.image.apply {
            Glide.with(context).load(user.avatarUrl).into(this)
        }
        holder.binding.root.setOnClickListener{
            callback(user)
        }
    }

    class InterestingItemDiffUtilCallback : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }

}