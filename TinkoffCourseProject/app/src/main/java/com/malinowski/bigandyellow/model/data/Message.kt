package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class Message(
    @SerialName("id") val id: Int,
    @SerialName("content") val message: String,
    @SerialName("is_me_message") val isMine: Boolean = false,
    @SerialName("sender_full_name") val senderName: String = "",
    @SerialName("timestamp") val timestamp: Int = (Date().time / 1000).toInt(),
    @SerialName("avatar_url") val avatarUrl: String = "",
    val reactions: MutableList<Reaction> = mutableListOf(),
) {
    fun getDate(): String {
        val date = Date(timestamp.toLong() * 1000)
        return SimpleDateFormat("d MMM", Locale("ru", "RU")).format(date)
    }
}