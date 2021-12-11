package com.malinowski.bigandyellow.di.streams

import com.malinowski.bigandyellow.di.main.MainModule
import com.malinowski.bigandyellow.view.ChannelsFragment
import com.malinowski.bigandyellow.view.ChatFragment
import com.malinowski.bigandyellow.view.StreamsRecyclerFragment
import dagger.Subcomponent

@Subcomponent(modules = [MainModule::class])
abstract class StreamsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): StreamsComponent
    }

    abstract fun inject(fragment: StreamsRecyclerFragment)

}