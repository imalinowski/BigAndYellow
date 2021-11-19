package com.malinowski.bigandyellow

import android.app.Application
import com.malinowski.bigandyellow.model.RepositoryImpl

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        RepositoryImpl.loadOwnUser()
    }
}