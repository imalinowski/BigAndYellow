package com.malinowski.bigandyellow.di

import android.content.Context
import androidx.room.Room
import com.malinowski.bigandyellow.model.db.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DbModule {

    @Singleton
    @Provides
    fun getDb(applicationContext: Context) = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, AppDatabase.DB_NAME
    ).build()

}