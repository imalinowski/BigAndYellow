package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stream(
    val name: String,
    @SerialName("stream_id") val id: Int,
    var topics: MutableList<Topic> = mutableListOf()
)