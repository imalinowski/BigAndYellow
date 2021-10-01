package com.malinowski.project

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class Activity2 : AppCompatActivity() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            val data = intent?.getStringExtra(getString(R.string.data))
            setResult(RESULT_OK, Intent().putExtra(getString(R.string.data), data))
            onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        Intent(this, UsefulService::class.java).apply {
            startService(this)
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(UsefulService.TAG))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)
    }
}