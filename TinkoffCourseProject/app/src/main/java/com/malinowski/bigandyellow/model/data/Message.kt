package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Message(
    @SerialName("id") val id: Int,
    @SerialName("content") val message: String,
    @SerialName("sender_id") val userId: Int,
    @SerialName("sender_full_name") val senderName: String = "",
    @SerialName("timestamp") val timestamp: Int = (Date().time / 1000).toInt(),
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("reactions") val reactions: List<Reaction> = listOf(), // init all reactions by one
)