package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.viewmodel.MainViewModel

class SubscribedFragment : Fragment(R.layout.fragment_subscribed){

    private val model: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragmentManager
        view.findViewById<Button>(R.id.btn).setOnClickListener {
            model.openChat()
        }
    }
    companion object {
        const val TAG = "Subscribed"
    }
}