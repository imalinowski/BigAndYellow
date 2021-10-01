package com.malinowski.project

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlin.random.Random

class UsefulService : Service() {

    companion object {
        const val TAG = "UsefulServiceBroadcast"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            for (i in 1..3) {
                Thread.sleep(1000)
                Log.i("RASP", "$i")
            }
            val data = Intent(TAG).putExtra(
                getString(R.string.data),
                "random result -> ${Random.nextInt()}"
            )
            LocalBroadcastManager.getInstance(this).sendBroadcast(data)
            stopSelf()
        }.start()
        return START_STICKY
    }
}