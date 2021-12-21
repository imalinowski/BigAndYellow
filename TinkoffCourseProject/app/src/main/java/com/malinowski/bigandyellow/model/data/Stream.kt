package com.malinowski.bigandyellow.model.data

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val STREAMS_TABLE = "Streams"

@Entity(tableName = STREAMS_TABLE, primaryKeys = ["name", "subscribed"])
@Serializable
data class Stream(
    @SerialName("name") val name: String,
    @SerialName("stream_id") val id: Int,
    var subscribed: Boolean = false,
) {
    @Ignore
    var topics: MutableList<Topic> = mutableListOf()
}