package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.ActivityMainBinding
import com.malinowski.bigandyellow.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.activity_fragment_container_view, MainFragment.newInstance())
                .commitAllowingStateLoss()

        model.mainScreenState.observe(this) {
            when (it) {
                is MainScreenState.Loading -> binding.progressBar.visibility = VISIBLE
                is MainScreenState.Error -> {
                    Toast.makeText(this, it.error.message.toString(), Toast.LENGTH_LONG).show()
                    Log.e("BigAndYellow", it.error.message.toString())
                    binding.progressBar.visibility = GONE
                }
                is MainScreenState.Result -> binding.progressBar.visibility = GONE
            }
        }

        model.chat.observe(this) { bundle ->
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.activity_fragment_container_view,
                    ChatFragment.newInstance(bundle)
                )
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }
    }

}