package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.FragmentProfileBinding
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.model.data.UserStatus
import io.reactivex.disposables.CompositeDisposable

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.userName.text = User.ME.name
        updateStatus(User.ME)
        binding.image.apply {
            Glide.with(this).load(User.ME.avatarUrl).into(this)
        }
    }

    private fun updateStatus(user: User) {
        binding.userStatus.apply {
            val color = ContextCompat.getColor(
                context,
                when (user.status) {
                    UserStatus.Online -> R.color.custom_green_primary
                    UserStatus.Idle -> R.color.idle_orange
                    UserStatus.Offline -> R.color.offline_red
                }
            )
            setTextColor(color)
            text = user.status.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.dispose()
    }

}