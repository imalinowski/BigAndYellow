package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.ActivityMainBinding
import com.malinowski.bigandyellow.getComponent
import com.malinowski.bigandyellow.view.mvi.states.ScreenState
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val model: MainViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getComponent().mainComponent().create().inject(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.activity_fragment_container_view, MainFragment.newInstance())
                .commit()

        model.mainScreenState.observe(this) {
            when (it) {
                is ScreenState.Loading -> binding.progressBar.visibility = VISIBLE
                is ScreenState.Error -> {
                    Toast.makeText(this, it.error.message.toString(), Toast.LENGTH_LONG).show()
                    Log.e("BigAndYellow", it.error.message.toString())
                    binding.progressBar.visibility = GONE
                }
                is ScreenState.Result -> {
                    if (it.text.isNotEmpty())
                        Toast.makeText(this, it.text, Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = GONE
                }
            }
        }

        model.navigateChat.observe(this) { bundle ->
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.activity_fragment_container_view,
                    ChatFragment.newInstance(bundle)
                )
                .addToBackStack(null)
                .commit()
        }
    }

}