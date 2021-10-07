package com.malinowski.project

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract.Contacts
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlin.math.max

class UsefulService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            val result = mutableListOf<String>()

            contentResolver.query(
                Contacts.CONTENT_URI,
                arrayOf(Contacts.DISPLAY_NAME, Contacts.CONTACT_LAST_UPDATED_TIMESTAMP),
                null, null, null
            )?.use { cursor ->
                val name = cursor.getColumnIndex(Contacts.DISPLAY_NAME)
                val status = cursor.getColumnIndex(Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
                while (cursor.moveToNext())
                    result.add(
                        cursor.getString(name)?.let {
                            it + ".".repeat(
                                maxOf(3.0, 60 - it.length * 2.15).toInt()
                            )
                        } + cursor.getString(status)
                    )
            }

            val data = Intent(TAG).putExtra(
                EXTRA_DATA,
                result.toTypedArray()
            )

            Thread.sleep(1000) // useful long task run
            LocalBroadcastManager.getInstance(this).sendBroadcast(data)

            stopSelf()
        }.start()
        return START_STICKY
    }

    companion object {
        const val TAG = "UsefulServiceBroadcast"
        const val EXTRA_DATA = "data"
    }
}