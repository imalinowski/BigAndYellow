package com.malinowski.bigandyellow.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.activity_fragment_container_view, MainFragment.newInstance())
                .commitAllowingStateLoss()

    }

}