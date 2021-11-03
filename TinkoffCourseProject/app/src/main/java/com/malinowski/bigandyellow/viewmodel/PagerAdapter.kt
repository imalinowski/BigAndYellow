package com.malinowski.bigandyellow.viewmodel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.malinowski.bigandyellow.view.StreamsRecyclerFragment

enum class Streams {
    SubscribedStreams, AllStreams
}

class PagerAdapter(
    private val pages: List<Streams>,
    fragmentManager: FragmentManager, lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment =
        StreamsRecyclerFragment.newInstance(pages[position])

}