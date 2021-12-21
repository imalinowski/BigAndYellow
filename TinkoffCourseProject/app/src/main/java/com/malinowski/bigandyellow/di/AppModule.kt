package com.malinowski.bigandyellow.di

import com.malinowski.bigandyellow.domain.usecase.*
import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
internal interface AppModule {

    @Binds
    fun getSearchUsersUseCase(impl: SearchUsersUseCaseImpl): SearchUsersUseCase

    @Binds
    fun getSearchStreamsUseCase(impl: SearchStreamUseCaseImpl): SearchStreamUseCase

    @Binds
    fun getSearchTopicsUseCase(impl: SearchTopicsUseCaseImpl): SearchTopicsUseCase

    @Binds
    fun getRepository(impl: RepositoryImpl): Repository

    companion object {
        @Singleton
        @Provides
        fun getJsonFormat(): Json = Json { ignoreUnknownKeys = true }
    }

}