package com.malinowski.bigandyellow.di.main

import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.di.ViewModelKey
import com.malinowski.bigandyellow.viewmodel.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindViewModel(viewModel: MainViewModel): ViewModel

}
