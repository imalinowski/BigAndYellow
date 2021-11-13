package com.malinowski.bigandyellow.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.malinowski.bigandyellow.model.data.*

@Database(entities = [Stream::class, Topic::class, Message::class, User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun streamDao(): StreamDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {
        const val DB_NAME = "app_db"
    }
}