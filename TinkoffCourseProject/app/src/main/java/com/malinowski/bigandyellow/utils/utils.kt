package com.malinowski.bigandyellow.utils

sealed class EmojiClickParcel(val id: Int, open val name: String)
data class EmojiAddParcel(
    val messageId: Int,
    val emojiName: String
) : EmojiClickParcel(messageId, emojiName)

data class EmojiDeleteParcel(
    val messageId: Int,
    val emojiName: String
) : EmojiClickParcel(messageId, emojiName)
