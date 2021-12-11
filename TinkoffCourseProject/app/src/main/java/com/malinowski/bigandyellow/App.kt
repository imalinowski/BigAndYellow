package com.malinowski.bigandyellow

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import com.malinowski.bigandyellow.di.AppComponent
import com.malinowski.bigandyellow.di.DaggerAppComponent

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        appComponent = DaggerAppComponent.create()
    }

    companion object {
        lateinit var appContext: Context
    }
}

fun Activity.getComponent(): AppComponent = (application as App).appComponent

fun Fragment.getComponent(): AppComponent = (requireActivity().application as App).appComponent