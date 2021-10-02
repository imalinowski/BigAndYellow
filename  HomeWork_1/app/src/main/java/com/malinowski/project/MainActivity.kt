package com.malinowski.project

import android.app.Activity
import android.content.Intent
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
                val data = result.data?.getStringArrayExtra("data")
                binding.txtResult.text = data?.joinToString("\n")
                binding.txtResult.visibility = View.VISIBLE
            }
            else binding.txtResult.text = getString(R.string.error)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //binding.txtResult.movementMethod = ScrollingMovementMethod();
        binding.btnOpen2ndActivity.setOnClickListener {
            startForResult.launch(Intent(this, Activity2::class.java))
        }
    }
}