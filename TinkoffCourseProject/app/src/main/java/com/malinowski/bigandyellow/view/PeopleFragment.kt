package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentPeopleBinding
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.UserAdapter

class PeopleFragment : Fragment() {
    private lateinit var binding: FragmentPeopleBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeopleBinding.inflate(layoutInflater)

        binding.usersRecycler.apply {
            adapter = UserAdapter(MutableList(3) {
                User(0, resources.getString(R.string.app_name))
            })
            layoutManager = LinearLayoutManager(context)
        }

        return binding.root
    }

}