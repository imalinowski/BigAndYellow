package com.malinowski.bigandyellow.model.data

import androidx.room.*
import io.reactivex.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

private const val TABLE_NAME = "Messages"

@Entity(tableName = TABLE_NAME)
@Serializable
data class Message(
    @PrimaryKey @SerialName("id")
    val id: Int,
    @SerialName("content")
    val message: String,
    @ColumnInfo(name = "user_id") @SerialName("sender_id")
    val userId: Int,
    @ColumnInfo(name = "sender_name") @SerialName("sender_full_name")
    val senderName: String = "",
    @SerialName("timestamp")
    val timestamp: Int = (Date().time / 1000).toInt(),
    @ColumnInfo(name = "avatar_url") @SerialName("avatar_url")
    val avatarUrl: String = "",
    @ColumnInfo(name = "sender_email") @SerialName("sender_email")
    val senderEmail: String = "",
    @ColumnInfo(name = "stream_id") @SerialName("stream_id")
    val streamId: Int = 0,
    @ColumnInfo(name = "topic_name") @SerialName("subject")
    val topicName: String = "",
) {
    @Ignore
    @SerialName("reactions")
    var reactions: List<Reaction> = listOf() // init all reactions by one

    fun initEmoji(reactions: List<Reaction>) {
        this.reactions = reactions
    }
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): Single<List<Message>>

    @Query("SELECT * FROM $TABLE_NAME WHERE sender_email = :email")
    fun getMessages(email: String): Single<List<Message>>

    @Query("SELECT * FROM $TABLE_NAME WHERE stream_id = :streamId AND topic_name = :topicName")
    fun getMessages(streamId: Int, topicName: String): Single<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(streams: List<Message>)

    @Delete
    fun delete(message: Message): Single<Int>
}
