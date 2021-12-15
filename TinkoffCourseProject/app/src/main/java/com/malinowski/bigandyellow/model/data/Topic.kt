package com.malinowski.bigandyellow.model.data

import androidx.room.*
import androidx.room.Delete
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val TABLE_NAME = "topic"

@Entity(tableName = TABLE_NAME)
@Serializable
data class Topic(
    @PrimaryKey @ColumnInfo(name = "name") @SerialName("name") val name: String,
    @ColumnInfo(name = "stream_id") var streamId: Int = 0,
    @ColumnInfo(name = "message_num") var messageNum: Int = 0,
)

@Dao
interface TopicDao {
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): Single<List<Topic>>

    @Query("SELECT * FROM $TABLE_NAME WHERE stream_id = :streamId")
    fun getTopicsInStream(streamId: Int): Single<List<Topic>>

    @Query("SELECT * FROM $TABLE_NAME WHERE name = :name")
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
