package com.malinowski.bigandyellow.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Stream(
    @ColumnInfo(name = "name") @SerialName("name") val name: String,
    @ColumnInfo(name = "stream_id") @SerialName("stream_id") val id: Int,
    var topics: MutableList<Topic> = mutableListOf()
)