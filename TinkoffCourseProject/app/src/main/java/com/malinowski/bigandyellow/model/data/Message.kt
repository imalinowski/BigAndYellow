package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

@Serializable
data class Message(
    @SerialName("id") val id: Int,
    @SerialName("content") val message: String,
    @SerialName("is_me_message") val isMine: Boolean = false,
    @SerialName("sender_full_name") val senderName: String = "",
    @SerialName("timestamp") val timestamp: Int = (Date().time / 1000).toInt(),
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("reactions") private val reactions: List<Reaction> = listOf(), // init all reactions by one
) {
    @kotlinx.serialization.Transient
    val emoji: HashMap<String, UnitedReaction> = HashMap() // united reactions unicode to reaction

    init {
        for (reaction in reactions)
            addEmoji(reaction)
    }

    fun addEmoji(reaction: Reaction) {
        val code = reaction.getUnicode()
        if (code in emoji) {
            emoji[code]?.usersId?.add(reaction.userId)
        } else {
            emoji[code] = UnitedReaction(
                mutableListOf(reaction.userId),
                reaction.getUnicode(),
                reaction.name
            )
        }
    }

    fun getDate(): String {
        val date = Date(timestamp.toLong() * 1000)
        return SimpleDateFormat("d MMM", Locale("ru", "RU")).format(date)
    }
}