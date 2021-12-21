package com.malinowski.bigandyellow.model.db.dao

import androidx.room.*
import com.malinowski.bigandyellow.model.data.TOPIC_TABLE
import com.malinowski.bigandyellow.model.data.Topic
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TopicDao {
    @Query("SELECT * FROM $TOPIC_TABLE")
    fun getAll(): Single<List<Topic>>

    @Query("SELECT * FROM $TOPIC_TABLE WHERE stream_id = :streamId")
    fun getTopicsInStream(streamId: Int): Single<List<Topic>>

    @Query("SELECT * FROM $TOPIC_TABLE WHERE name = :name")
    fun getTopicByName(name: String): Single<Topic>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(topic: List<Topic>): Completable

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(topic: Topic): Completable

    @Update
    fun update(topic: Topic)

    @Delete
    fun delete(topic: Topic): Single<Int>
}