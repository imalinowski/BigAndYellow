package com.malinowski.bigandyellow.model.db.dao

import androidx.room.*
import com.malinowski.bigandyellow.model.data.STREAMS_TABLE
import com.malinowski.bigandyellow.model.data.Stream
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface StreamDao {
    @Query("SELECT * FROM $STREAMS_TABLE WHERE subscribed = 0")
    fun getAll(): Single<List<Stream>>

    @Query("SELECT * FROM $STREAMS_TABLE WHERE subscribed = 1")
    fun getSubscribed(): Single<List<Stream>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(streams: List<Stream>): Completable

    @Delete
    fun delete(topic: Stream): Single<Int>
}