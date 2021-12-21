package com.malinowski.bigandyellow.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val TOPIC_TABLE = "topic"

@Entity(tableName = TOPIC_TABLE, primaryKeys = ["name", "stream_id"])
@Serializable
data class Topic(
    @ColumnInfo(name = "name") @SerialName("name") val name: String,
    @ColumnInfo(name = "stream_id") var streamId: Int = 0,
    @ColumnInfo(name = "message_num") var messageNum: Int = 0,
)
