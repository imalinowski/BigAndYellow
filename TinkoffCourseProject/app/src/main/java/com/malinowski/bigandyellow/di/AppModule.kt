package com.malinowski.bigandyellow.di

import com.malinowski.bigandyellow.domain.usecase.*
import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json

@Module(includes = [AppModule.SubModule::class])
abstract class AppModule {

    @Binds
    internal abstract fun getSearchUsersUseCase(impl: SearchUsersUseCaseImpl): SearchUsersUseCase

    @Binds
    internal abstract fun getSearchStreamsUseCase(impl: SearchStreamUseCaseImpl): SearchStreamUseCase

    @Binds
    internal abstract fun getSearchTopicsUseCase(impl: SearchTopicsUseCaseImpl): SearchTopicsUseCase

    @Binds
    abstract fun getRepository(impl: RepositoryImpl): Repository

    @Module
    class SubModule {
        @Provides
        fun getJsonFormat(): Json = Json { ignoreUnknownKeys = true }

    }

}