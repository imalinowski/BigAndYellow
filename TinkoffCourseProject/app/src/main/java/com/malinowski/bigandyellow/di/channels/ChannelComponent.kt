package com.malinowski.bigandyellow.di.channels

import com.malinowski.bigandyellow.di.main.MainModule
import com.malinowski.bigandyellow.view.ChannelsFragment
import com.malinowski.bigandyellow.view.ChatFragment
import dagger.Subcomponent

@Subcomponent(modules = [MainModule::class])
abstract class ChannelComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ChannelComponent
    }

    abstract fun inject(fragment: ChannelsFragment)

}