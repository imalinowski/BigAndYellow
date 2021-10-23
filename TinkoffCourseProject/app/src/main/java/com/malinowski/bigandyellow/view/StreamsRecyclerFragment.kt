package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.model.data.ChatItem
import com.malinowski.bigandyellow.model.data.TopicChatItem
import com.malinowski.bigandyellow.model.data.TopicItem
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.TopicsChatsAdapter

class StreamsRecyclerFragment : Fragment(R.layout.fragment_streams) {

    private val model: MainViewModel by activityViewModels()
    private val items: MutableList<TopicChatItem> = mutableListOf()

    private val adapter = TopicsChatsAdapter(items) { position ->
        when (val item = items[position]) {
            is ChatItem -> item.also {
                model.openChat(item.topicId, item.chatId)
            }
            is TopicItem -> {
                if (item.expanded)
                    deleteItems(position)
                else
                    addItems(item.id, position)
                item.expanded = !item.expanded
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val subscribed = arguments?.getBoolean(SUBSCRIBED) ?: false
        items.addAll(model.getTopics(subscribed).map {
            TopicItem(name = it.second, id = it.first)
        })

        view.findViewById<RecyclerView>(R.id.topics_chats_recycler)?.let { recycler ->
            recycler.adapter = adapter
            recycler.layoutManager = LinearLayoutManager(context)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun addItems(topicNum: Int, listPosition: Int) {
        val chats = model.getChatsByTopic(topicNum)
        if (chats.isEmpty()) return

        items.addAll(listPosition + 1, chats)
        adapter.notifyItemRangeInserted(
            listPosition + 1,
            chats.size
        )
        adapter.notifyItemRangeChanged(listPosition + chats.size + 1, adapter.itemCount)

    }

    private fun deleteItems(listPosition: Int) {
        var count = 0
        while (listPosition + 1 < items.size && items[listPosition + 1] is ChatItem) {
            items.removeAt(listPosition + 1)
            count += 1
        }
        adapter.notifyItemRangeRemoved(listPosition + 1, count)
        adapter.notifyItemRangeChanged(listPosition + 1, adapter.itemCount)

    }

    companion object {
        private const val SUBSCRIBED = "subscribed"
        fun newInstance(subscribed: Boolean) =
            StreamsRecyclerFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(SUBSCRIBED, subscribed)
                }
            }
    }
}