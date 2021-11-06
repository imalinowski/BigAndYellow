package com.malinowski.bigandyellow

import android.app.Application
import com.malinowski.bigandyellow.model.Repository

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.loadOwnUser()
    }
}