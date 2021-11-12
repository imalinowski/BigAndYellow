package com.malinowski.bigandyellow.model.data

import androidx.room.*
import io.reactivex.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val TABLE_NAME = "topic"

@Entity(tableName = TABLE_NAME)
@Serializable
data class Topic(
    @PrimaryKey @ColumnInfo(name = "name") @SerialName("name") val name: String,
    @ColumnInfo(name = "stream_id") var streamId: Int = 0
)

@Dao
interface TopicDao {
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): Single<List<Topic>>

    @Query("SELECT * FROM $TABLE_NAME WHERE stream_id = :streamId")
    fun getTopicsInStream(streamId: Int): Single<List<Topic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(topic: List<Topic>)

    @Delete
    fun delete(topic: Topic): Single<Int>
}