package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.databinding.FragmentPeopleBinding
import com.malinowski.bigandyellow.model.RepositoryImpl
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.UserAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class UsersFragment : Fragment() {
    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!

    private val model: MainViewModel by activityViewModels()
    private var adapter = UserAdapter() {
        model.openChat(it)
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
            model.searchUsers(it.toString())
        }

        if (savedInstanceState == null)
            model.searchUsers("")
        model.users.observe(viewLifecycleOwner) { users ->
            updateUsersStatus(users)
            adapter.submitList(users) {
                binding.usersRecycler.scrollToPosition(0)
            }
        }

        binding.usersRecycler.apply {
            adapter = this@UsersFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private val compositeDisposable = CompositeDisposable()
    private fun updateUsersStatus(users: List<User>) {
        users.forEachIndexed { index, user ->
            RepositoryImpl.loadStatus(user).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onSuccess = {
                    adapter.notifyItemChanged(index)
                },
                onError = {
                    Log.e("LoadUserError", it.message.toString())
                }
            ).addTo(compositeDisposable)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.dispose()
    }

}