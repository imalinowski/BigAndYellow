package com.malinowski.bigandyellow.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.malinowski.bigandyellow.model.network.AuthInterceptor
import com.malinowski.bigandyellow.model.network.ZulipChat
import dagger.Module
import dagger.Provides
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetModule {

    @Provides
    @Named("URL")
    fun provideURL() = "https://tinkoff-android-fall21.zulipchat.com/api/v1/"

    @Provides
    fun provideOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(AuthInterceptor())
        .build()

    @Singleton
    @Provides
    @ExperimentalSerializationApi
    fun provideRetrofit(@Named("URL") url: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Singleton
    @Provides
    fun provideService(retrofit: Retrofit): ZulipChat =
        retrofit.create(ZulipChat::class.java)
}