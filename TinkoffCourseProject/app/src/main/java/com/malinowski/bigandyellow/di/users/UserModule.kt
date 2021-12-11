package com.malinowski.bigandyellow.di.users

import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.di.ViewModelKey
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import com.malinowski.bigandyellow.viewmodel.UsersViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class UserModule {

    @Binds
    @IntoMap
    @ViewModelKey(UsersViewModel::class)
    abstract fun bindViewModel(viewModel: UsersViewModel): ViewModel

}
