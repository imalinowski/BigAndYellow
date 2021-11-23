package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.databinding.FragmentPeopleBinding
import com.malinowski.bigandyellow.view.mvi.FragmentMVI
import com.malinowski.bigandyellow.view.mvi.events.Event
import com.malinowski.bigandyellow.view.mvi.states.State
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.UserAdapter

class UsersFragment : FragmentMVI<State.Users>() {
    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!

    private val model: MainViewModel by activityViewModels()
    private var adapter = UserAdapter(
        onClick = { user ->
            model.processEvent(
                Event.OpenChat.WithUser(user)
            )
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeopleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchQuery.doAfterTextChanged {
            model.processEvent(
                Event.SearchUsers(query = it.toString())
            )
        }

        if (savedInstanceState == null)
            model.processEvent(
                Event.SearchUsers(query = "")
            )

        model.usersState.observe(viewLifecycleOwner) { state -> render(state) }

        binding.usersRecycler.apply {
            adapter = this@UsersFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun render(state: State.Users) {
        adapter.submitList(state.users) {
            binding.usersRecycler.scrollToPosition(0)
        }
    }

}