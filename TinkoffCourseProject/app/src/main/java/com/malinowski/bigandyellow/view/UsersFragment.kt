package com.malinowski.bigandyellow.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.databinding.FragmentPeopleBinding
import com.malinowski.bigandyellow.getComponent
import com.malinowski.bigandyellow.view.mvi.events.Event
import com.malinowski.bigandyellow.view.mvi.events.UsersEvent
import com.malinowski.bigandyellow.view.mvi.states.State
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.UsersViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.UserAdapter
import javax.inject.Inject

class UsersFragment : Fragment() {

    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mainModel: MainViewModel by activityViewModels { viewModelFactory }
    private val model: UsersViewModel by viewModels { viewModelFactory }

    private var adapter = UserAdapter(
        onClick = { user ->
            mainModel.processEvent(
                Event.OpenChat.WithUser(user)
            )
        }
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getComponent().userComponent().create().inject(this)
    }

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
                UsersEvent.SearchUsers(query = it.toString())
            )
        }

        if (savedInstanceState == null)
            model.processEvent(
                UsersEvent.SearchUsers()
            )

        model.usersState.observe(viewLifecycleOwner) { state -> render(state) }

        binding.usersRecycler.apply {
            adapter = this@UsersFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }

        model.screenState.observe(viewLifecycleOwner) {
            mainModel.setScreenState(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun render(state: State.Users) {
        adapter.submitList(state.users) {
            binding.usersRecycler.scrollToPosition(0)
        }
    }

}