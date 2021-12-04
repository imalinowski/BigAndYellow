package com.malinowski.bigandyellow.model.data.db_entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.malinowski.bigandyellow.model.data.MessageData
import com.malinowski.bigandyellow.model.data.Reaction
import java.util.*

const val TABLE_MESSAGES = "Messages"

@Entity(tableName = TABLE_MESSAGES)
data class MessageDB(
    @PrimaryKey
    override val id: Int,
    @ColumnInfo(name = "message")
    override val message: String,
    @ColumnInfo(name = "user_id")
    override val userId: Int,
    @ColumnInfo(name = "sender_name")
    override val senderName: String = "",
    @ColumnInfo(name = "timestamp")
    override val timestamp: Int = (Date().time / 1000).toInt(),
    @ColumnInfo(name = "avatar_url")
    override val avatarUrl: String = "",
    @ColumnInfo(name = "sender_email")
    override val senderEmail: String = "",
    @ColumnInfo(name = "stream_id")
    override val streamId: Int = 0,
    @ColumnInfo(name = "topic_name")
    override val topicName: String = "",
) : MessageData {
    @Ignore
    override var reactions: List<Reaction> = listOf() // init all reactions by one

    override fun initEmoji(reactions: List<Reaction>) {
        this.reactions = reactions
    }
}
