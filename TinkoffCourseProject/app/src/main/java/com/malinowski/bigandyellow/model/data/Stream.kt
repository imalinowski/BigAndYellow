package com.malinowski.bigandyellow.model.data

import androidx.room.*
import io.reactivex.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val TABLE_NAME = "Streams"

@Entity(tableName = TABLE_NAME)
@Serializable
data class Stream(
    @PrimaryKey
    @SerialName("name") val name: String,
    @SerialName("stream_id") val id: Int,
    var subscribed: Boolean = true,
) {
    @Ignore
    var topics: MutableList<Topic> = mutableListOf()
}


@Dao
interface StreamDao {
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): Single<List<Stream>>

    @Query("SELECT * FROM $TABLE_NAME WHERE subscribed = 1")
    fun getSubscribed(): Single<List<Stream>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(streams: List<Stream>)

    @Delete
    fun delete(topic: Stream): Single<Int>
}