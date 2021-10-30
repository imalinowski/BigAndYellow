package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.databinding.FragmentPeopleBinding
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.UserAdapter

class PeopleFragment : Fragment() {
    private lateinit var binding: FragmentPeopleBinding

    private val model: MainViewModel by activityViewModels()
    private var adapter = UserAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeopleBinding.inflate(layoutInflater)

        binding.searchQuery.doAfterTextChanged {
            model.searchUsers(it.toString())
        }

        if (savedInstanceState == null)
            model.searchUsers("")
        model.users.observe(viewLifecycleOwner) {
            adapter.submitList(it) {
                binding.usersRecycler.scrollToPosition(0)
            }
        }

        binding.usersRecycler.apply {
            adapter = this@PeopleFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }

        return binding.root
    }

}