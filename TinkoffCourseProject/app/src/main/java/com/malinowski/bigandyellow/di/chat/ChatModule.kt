package com.malinowski.bigandyellow.di.chat

import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.di.ViewModelKey
import com.malinowski.bigandyellow.viewmodel.ChatViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ChatModule {

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    abstract fun bindViewModel(viewmodel: ChatViewModel): ViewModel

}
