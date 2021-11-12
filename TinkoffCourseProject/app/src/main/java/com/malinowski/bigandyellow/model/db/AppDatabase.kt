package com.malinowski.bigandyellow.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.malinowski.bigandyellow.model.data.Stream
import com.malinowski.bigandyellow.model.data.StreamDao
import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.TopicDao

@Database(entities = [Stream::class, Topic::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun streamDao(): StreamDao
    companion object {
        const val DB_NAME = "app_db"
    }
}