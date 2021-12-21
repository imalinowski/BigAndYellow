package com.malinowski.bigandyellow.di.streams

import com.malinowski.bigandyellow.di.main.MainViewModelModule
import com.malinowski.bigandyellow.view.StreamsRecyclerFragment
import dagger.Subcomponent

@Subcomponent(modules = [MainViewModelModule::class])
abstract class StreamsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): StreamsComponent
    }

    abstract fun inject(fragment: StreamsRecyclerFragment)

}