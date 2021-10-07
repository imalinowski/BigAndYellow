package com.malinowski.project

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.malinowski.project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val startForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringArrayExtra(UsefulService.EXTRA_DATA)
                binding.txtResult.apply {
                    text = data?.joinToString("\n")
                    visibility = View.VISIBLE
                }
            } else binding.txtResult.text = getString(R.string.error)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnOpen2ndActivity.setOnClickListener {
            startForResult.launch(Activity2.createIntent(this))
        }
    }
}