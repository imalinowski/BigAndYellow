package com.malinowski.bigandyellow.model.db.dao

import androidx.room.*
import com.malinowski.bigandyellow.model.data.db_entities.MessageDB
import com.malinowski.bigandyellow.model.data.db_entities.TABLE_MESSAGES
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface MessageDao {
    @Query("SELECT * FROM $TABLE_MESSAGES")
    fun getAll(): Single<List<MessageDB>>

    @Query("SELECT * FROM $TABLE_MESSAGES WHERE sender_email = :email")
    fun getMessages(email: String): Single<List<MessageDB>>

    @Query("SELECT * FROM $TABLE_MESSAGES WHERE stream_id = :streamId AND topic_name = :topicName")
    fun getMessages(streamId: Int, topicName: String): Single<List<MessageDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(streams: List<MessageDB>): Completable

    @Delete
    fun delete(message: MessageDB): Single<Int>
}