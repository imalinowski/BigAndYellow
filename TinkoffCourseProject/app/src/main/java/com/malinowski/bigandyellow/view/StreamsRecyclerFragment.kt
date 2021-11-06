package com.malinowski.bigandyellow.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentStreamsBinding
import com.malinowski.bigandyellow.model.data.StreamItem
import com.malinowski.bigandyellow.model.data.StreamTopicItem
import com.malinowski.bigandyellow.model.data.TopicItem
import com.malinowski.bigandyellow.model.mapper.TopicToItemMapper
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.Streams
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.TopicsChatsAdapter
import io.reactivex.android.schedulers.AndroidSchedulers

class StreamsRecyclerFragment : Fragment(R.layout.fragment_streams) {

    private val viewBinding: FragmentStreamsBinding by lazy {
        FragmentStreamsBinding.inflate(layoutInflater)
    }

    private val topicToItemMapper: TopicToItemMapper = TopicToItemMapper()
    private val model: MainViewModel by activityViewModels()
    private var items: MutableList<StreamTopicItem> = mutableListOf()

    private val adapter = TopicsChatsAdapter { position -> // on item click
        when (val item = items[position]) {
            is TopicItem -> item.also {
                model.openChat(item.topicId, item.chatId)
            }
            is StreamItem -> {
                if (item.expanded)
                    deleteItems(position)
                else
                    addItems(item, position)
                item.expanded = !item.expanded
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val streamType =
            arguments?.getSerializable(SUBSCRIBED)?.let { it as Streams } ?: Streams.AllStreams

        (if (streamType == Streams.SubscribedStreams) model.streamsSubscribed else model.streams)
            .observe(viewLifecycleOwner) {
                items = it.toMutableList()
                adapter.submitList(items) {
                    viewBinding.topicsChatsRecycler.scrollToPosition(0)
                }
            }

        viewBinding.topicsChatsRecycler.let { recycler ->
            recycler.adapter = adapter
            recycler.layoutManager = LinearLayoutManager(context)
        }

        return viewBinding.root
    }

    @SuppressLint("CheckResult")
    private fun addItems(stream: StreamItem, listPosition: Int) {
        stream.loading = true
        adapter.notifyItemChanged(listPosition)

        model.getTopics(stream.streamId)
            .map { topicToItemMapper(it, stream.streamId) }
            .doOnSuccess { stream.topics = it }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ chats ->
                stream.loading = false
                items.addAll(listPosition + 1, chats)
                adapter.notifyItemChanged(listPosition)
                adapter.notifyItemRangeInserted(listPosition + 1, chats.size)
                adapter.notifyItemRangeChanged(listPosition + chats.size + 1, adapter.itemCount)
            }, { e ->
                stream.loading = false
                model.error(e)
            })
    }

    private fun deleteItems(listPosition: Int) {
        var count = 0
        while (listPosition + 1 < items.size && items[listPosition + 1] is TopicItem) {
            items.removeAt(listPosition + 1)
            count += 1
        }
        adapter.notifyItemRangeRemoved(listPosition + 1, count)
        adapter.notifyItemRangeChanged(listPosition + 1, adapter.itemCount)

    }

    companion object {
        private const val SUBSCRIBED = "subscribed"
        fun newInstance(streamType: Streams) =
            StreamsRecyclerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(SUBSCRIBED, streamType)
                }
            }
    }
}