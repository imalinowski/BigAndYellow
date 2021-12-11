package com.malinowski.bigandyellow.di.main

import com.malinowski.bigandyellow.view.MainActivity
import dagger.Subcomponent

@Subcomponent(modules = [MainModule::class])
abstract class MainComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }

    abstract fun inject(activity: MainActivity)

}