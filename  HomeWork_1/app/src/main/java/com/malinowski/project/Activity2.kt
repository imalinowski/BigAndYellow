package com.malinowski.project

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class Activity2 : AppCompatActivity() {

    private val READ_CONTACT_PERMISSION = 0

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            val data = intent?.getStringArrayExtra(getString(R.string.data))
            setResult(RESULT_OK, Intent().putExtra(getString(R.string.data), data))
            onBackPressed()
        }
    }

    private lateinit var launchService: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        launchService = Intent(this, UsefulService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        )
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), READ_CONTACT_PERMISSION)
        else
            startService(launchService)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(UsefulService.TAG))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_CONTACT_PERMISSION ->
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    startService(launchService)
                else
                    Toast.makeText(this, getString(R.string.permissions_denied), Toast.LENGTH_SHORT)
                        .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(receiver)
    }
}