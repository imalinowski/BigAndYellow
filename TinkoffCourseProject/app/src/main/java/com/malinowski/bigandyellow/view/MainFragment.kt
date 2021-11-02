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
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}