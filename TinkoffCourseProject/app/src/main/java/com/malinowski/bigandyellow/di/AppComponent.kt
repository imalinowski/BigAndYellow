package com.malinowski.bigandyellow.di

import android.content.Context
import com.malinowski.bigandyellow.di.channels.ChannelComponent
import com.malinowski.bigandyellow.di.chat.ChatComponent
import com.malinowski.bigandyellow.di.main.MainComponent
import com.malinowski.bigandyellow.di.streams.StreamsComponent
import com.malinowski.bigandyellow.di.users.UserComponent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        NetModule::class,
        DbModule::class,
        ViewModelBuilderModule::class,
        SubcomponentsModule::class,
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): AppComponent
    }

    fun mainComponent(): MainComponent.Factory
    fun chatComponent(): ChatComponent.Factory
    fun userComponent(): UserComponent.Factory
    fun channelComponent(): ChannelComponent.Factory
    fun streamsComponent(): StreamsComponent.Factory

}

@Module(
    subcomponents = [
        MainComponent::class,
        ChatComponent::class,
        UserComponent::class,
        ChannelComponent::class,
        StreamsComponent::class
    ]
)
object SubcomponentsModule