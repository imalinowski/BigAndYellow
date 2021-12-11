package com.malinowski.bigandyellow.di

import com.malinowski.bigandyellow.di.chat.ChatComponent
import com.malinowski.bigandyellow.di.main.MainComponent
import com.malinowski.bigandyellow.di.users.UserComponent
import dagger.Component
import dagger.Module

@Component(
    modules = [
        AppModule::class,
        NetModule::class,
        ViewModelBuilderModule::class,
        SubcomponentsModule::class,
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(): AppComponent
    }

    fun mainComponent(): MainComponent.Factory
    fun chatComponent(): ChatComponent.Factory
    fun userComponent(): UserComponent.Factory

}

@Module(
    subcomponents = [
        MainComponent::class,
        ChatComponent::class,
        UserComponent::class
    ]
)
object SubcomponentsModule