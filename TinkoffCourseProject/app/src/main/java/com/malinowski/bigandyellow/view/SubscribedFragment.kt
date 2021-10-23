package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import java.lang.IllegalArgumentException

class SubscribedFragment : Fragment(R.layout.fragment_subscribed){

    private val model: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentFragmentManager
        view.findViewById<Button>(R.id.btn).setOnClickListener {
            try {
                model.openChat(0,0)
            } catch (e: IllegalArgumentException){
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    companion object {
        const val TAG = "Subscribed"
    }
}