package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentStreamsBinding
import com.malinowski.bigandyellow.model.data.StreamItem
import com.malinowski.bigandyellow.model.data.StreamTopicItem
import com.malinowski.bigandyellow.model.data.TopicItem
import com.malinowski.bigandyellow.view.mvi.FragmentMVI
import com.malinowski.bigandyellow.view.mvi.events.Event
import com.malinowski.bigandyellow.view.mvi.states.State
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.StreamsType
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.TopicsChatsAdapter

class StreamsRecyclerFragment : FragmentMVI<State.Streams>(R.layout.fragment_streams) {

    private val viewBinding: FragmentStreamsBinding by lazy {
        FragmentStreamsBinding.inflate(layoutInflater)
    }
    private val model: MainViewModel by activityViewModels()
    private var items: MutableList<StreamTopicItem> = mutableListOf()
    private val streamType =
        arguments?.getSerializable(STREAMS_TYPE)?.let { it as StreamsType }
            ?: StreamsType.AllStreams

    private val adapter = TopicsChatsAdapter { position -> // on item click
        when (val item = items[position]) {
            is TopicItem -> item.also {
                model.processEvent(
                    Event.OpenChat.OfTopic(item.streamId, item.name)
                )
                closeStreams()
            }
            is StreamItem -> {
                model.processEvent(
                    if (item.expanded)
                        Event.Remove.Topics(item, position, streamType)
                    else
                        Event.Load.Topics(item, position, streamType)
                )
            }
        }
    }

    override fun render(state: State.Streams) {
        items = state.items.toMutableList()
        adapter.submitList(items) {
            viewBinding.topicsChatsRecycler.scrollToPosition(0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (streamType == StreamsType.SubscribedStreams)
            model.streamsSubscribedState
                .observe(viewLifecycleOwner) { state -> render(state) }
        else model.streamsAllState
            .observe(viewLifecycleOwner) { state -> render(state) }

        viewBinding.topicsChatsRecycler.let { recycler ->
            recycler.adapter = adapter
            recycler.layoutManager = LinearLayoutManager(context)
        }

        return viewBinding.root
    }

    override fun onStop() {
        super.onStop()
        closeStreams()
    }

    private fun closeStreams() {
        for (item in items)
            if (item is StreamItem)
                item.expanded = false
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