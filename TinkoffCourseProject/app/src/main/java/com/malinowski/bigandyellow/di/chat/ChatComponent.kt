package com.malinowski.bigandyellow.di.chat

import com.malinowski.bigandyellow.view.ChatFragment
import dagger.Subcomponent

@Subcomponent(modules = [ChatModule::class])
abstract class ChatComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ChatComponent
    }

    abstract fun inject(fragment: ChatFragment)

}