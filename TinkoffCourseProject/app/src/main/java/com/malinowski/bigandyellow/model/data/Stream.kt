package com.malinowski.bigandyellow.model.data

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val TABLE_NAME = "Streams"

@Entity(tableName = TABLE_NAME, primaryKeys = ["name", "subscribed"])
@Serializable
data class Stream(
    @SerialName("name") val name: String,
    @SerialName("stream_id") val id: Int,
    var subscribed: Boolean = false,
) {
    @Ignore
    var topics: MutableList<Topic> = mutableListOf()
}


@Dao
interface StreamDao {
    @Query("SELECT * FROM $TABLE_NAME WHERE subscribed = 0")
    fun getAll(): Single<List<Stream>>

    @Query("SELECT * FROM $TABLE_NAME WHERE subscribed = 1")
    fun getSubscribed(): Single<List<Stream>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(streams: List<Stream>): Completable

    @Delete
    fun delete(topic: Stream): Single<Int>
}