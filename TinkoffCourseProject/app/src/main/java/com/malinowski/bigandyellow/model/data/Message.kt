package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int,
    @SerialName("content") val message: String,
    @SerialName("is_me_message") val isMine: Boolean = false,
    @SerialName("sender_full_name") val senderName: String = "",
    val reactions: MutableList<Reaction> = mutableListOf(),
)