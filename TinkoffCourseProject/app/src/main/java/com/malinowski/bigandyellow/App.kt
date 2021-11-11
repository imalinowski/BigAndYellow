package com.malinowski.bigandyellow

import android.app.Application
import android.content.Context
import com.malinowski.bigandyellow.model.Repository

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        Repository.loadOwnUser()
    }

    companion object {
        lateinit var appContext: Context
    }
}