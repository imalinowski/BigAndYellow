package com.malinowski.bigandyellow.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentStreamsBinding
import com.malinowski.bigandyellow.getComponent
import com.malinowski.bigandyellow.model.data.StreamItem
import com.malinowski.bigandyellow.model.data.StreamTopicItem
import com.malinowski.bigandyellow.model.data.TopicItem
import com.malinowski.bigandyellow.view.mvi.events.Event
import com.malinowski.bigandyellow.view.mvi.states.State
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.StreamsType
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.TopicsChatsAdapter
import javax.inject.Inject

class StreamsRecyclerFragment : Fragment(R.layout.fragment_streams) {

    private val viewBinding: FragmentStreamsBinding by lazy {
        FragmentStreamsBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: MainViewModel by activityViewModels { viewModelFactory }

    private var items: MutableList<StreamTopicItem> = mutableListOf()
    private lateinit var streamType: StreamsType

    private var state = State.Streams(listOf())

    private val adapter = TopicsChatsAdapter(
        onClick = { position -> // on item click
            when (val item = items[position]) {
                is TopicItem -> item.also {
                    model.processEvent(
                        Event.OpenChat.OfTopic(item.streamId, item.name, item.name)
                    )
                    closeStreams()
                }
                is StreamItem -> {
                    if (item.expanded)
                        deleteItems(position)
                    else {
                        addItems(item, position)
                    }
                    item.expanded = !item.expanded
                }
            }
        },
        onLongClick = { position ->
            when (val item = items[position]) {
                is StreamItem -> {
                    model.processEvent(
                        Event.OpenChat.OfStream(item.id, item.name)
                    )
                }
                else -> {}
            }
        }
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getComponent().streamsComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        streamType = arguments?.getSerializable(STREAMS_TYPE)?.let { it as StreamsType }
            ?: StreamsType.AllStreams

        if (streamType == StreamsType.SubscribedStreams)
            model.streamsSubscribedState
                .observe(viewLifecycleOwner) { state -> render(state) }
        else model.streamsAllState
            .observe(viewLifecycleOwner) { state -> render(state) }

        viewBinding.topicsChatsRecycler.let { recycler ->
            recycler.adapter = adapter
            recycler.layoutManager = LinearLayoutManager(context)
        }

        viewBinding.refresh.setOnRefreshListener {
            model.processEvent(Event.SearchStreams())
            viewBinding.refresh.isRefreshing = false
        }

        return viewBinding.root
    }

    override fun onStop() {
        super.onStop()
        closeStreams()
    }

    private fun render(state: State.Streams) {
        this.state = state
        items = state.items.toMutableList()
        adapter.submitList(items) {
            viewBinding.topicsChatsRecycler.scrollToPosition(0)
        }
    }

    private fun addItems(streamItem: StreamItem, initPos: Int) {
        items.addAll(initPos + 1, streamItem.topics)
        adapter.notifyItemRangeInserted(initPos + 1, streamItem.topics.size)
        adapter.notifyItemRangeChanged(initPos, items.size)
        model.processEvent(Event.UpdateStream(streamItem.id))
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

    private fun closeStreams() {
        for (item in items)
            if (item is StreamItem)
                item.expanded = false
        items = items.filterIsInstance<StreamItem>().toMutableList()
    }

    companion object {
        private const val STREAMS_TYPE = "subscribed"
        fun newInstance(streamType: StreamsType) =
            StreamsRecyclerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(STREAMS_TYPE, streamType)
                }
            }
    }
}