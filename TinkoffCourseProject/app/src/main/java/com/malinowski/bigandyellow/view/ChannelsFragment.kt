package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.malinowski.bigandyellow.databinding.FragmentChannelsBinding
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.PagerAdapter

class ChannelsFragment : Fragment() {
    private lateinit var binding: FragmentChannelsBinding
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChannelsBinding.inflate(layoutInflater)

        if(savedInstanceState == null)
            model.searchTopics("")
        binding.searchQuery.doAfterTextChanged {
            model.searchTopics(it.toString())
        }

        val tabs: List<String> = listOf("Subscribed", "All Streams")
        val pagerAdapter = PagerAdapter(childFragmentManager, lifecycle)
        binding.fragmentViewPager.adapter = pagerAdapter
        pagerAdapter.update(
            listOf(
                StreamsRecyclerFragment.newInstance(true),
                StreamsRecyclerFragment.newInstance(false)
            )
        )

        TabLayoutMediator(binding.tabLayout, binding.fragmentViewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()

        return binding.root
    }
}