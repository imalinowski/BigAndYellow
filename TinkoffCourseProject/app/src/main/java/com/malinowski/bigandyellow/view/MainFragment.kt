package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.page_1 -> childFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container_view, ChannelsFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
                R.id.page_2 -> childFragmentManager.commit {
                    replace(R.id.main_fragment_container_view, PeopleFragment())
                    addToBackStack(null)
                }
                R.id.page_3 -> childFragmentManager.commit {
                    replace(R.id.main_fragment_container_view, ProfileFragment())
                    addToBackStack(null)
                }
            }
            true
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container_view, ChannelsFragment())
            .addToBackStack(null)
            .commitAllowingStateLoss()

        return binding.root
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}