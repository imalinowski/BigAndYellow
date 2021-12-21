package com.malinowski.bigandyellow.di.channels

import com.malinowski.bigandyellow.di.main.MainViewModelModule
import com.malinowski.bigandyellow.view.ChannelsFragment
import dagger.Subcomponent

@Subcomponent(modules = [MainViewModelModule::class])
abstract class ChannelComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ChannelComponent
    }

    abstract fun inject(fragment: ChannelsFragment)

}