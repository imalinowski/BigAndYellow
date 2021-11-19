package com.malinowski.bigandyellow.model.data

import java.text.SimpleDateFormat
import java.util.*

data class MessageItem(
    val id: Int,
    val message: String,
    val userId: Int,
    var isMine: Boolean = false,
    val senderName: String = "",
    val timestamp: Int = (Date().time / 1000).toInt(),
    val avatarUrl: String = "",
    val emoji: HashMap<String, UnitedReaction> = HashMap()
) {

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