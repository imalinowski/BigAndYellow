package com.malinowski.bigandyellow.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.TopicDao

@Database(entities = [Topic::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    companion object {
        const val DB_NAME = "app_db"
    }
}