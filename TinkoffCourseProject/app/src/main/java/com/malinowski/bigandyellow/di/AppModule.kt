package com.malinowski.bigandyellow.di

import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCase
import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCaseImpl
import dagger.Module

@Module
interface AppModule {
    fun getSearchUsersUseCase(impl: SearchUsersUseCaseImpl): SearchUsersUseCase
}