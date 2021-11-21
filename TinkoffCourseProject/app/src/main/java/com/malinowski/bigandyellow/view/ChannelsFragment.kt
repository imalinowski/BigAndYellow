package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentChannelsBinding
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.view.events.Event
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.PagerAdapter
import com.malinowski.bigandyellow.viewmodel.Streams

class ChannelsFragment : Fragment() {
    private val model: MainViewModel by activityViewModels()

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            model.searchStreams("")

        binding.searchQuery.doAfterTextChanged {
            model.searchStreams(it.toString())
        }

        binding.searchLoop.setOnLongClickListener {
            model.processEvent(Event.OpenChat.WithUser(User.ME))
            false
        }

        val tabs: List<String> =
            listOf(getString(R.string.subscribed), getString(R.string.all_streams))

        val pagerAdapter = PagerAdapter(
            pages = listOf(Streams.SubscribedStreams, Streams.AllStreams),
            childFragmentManager, lifecycle
        )
        binding.fragmentViewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.fragmentViewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}