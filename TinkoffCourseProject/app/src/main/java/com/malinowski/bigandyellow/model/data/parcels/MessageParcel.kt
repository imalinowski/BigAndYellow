package com.malinowski.bigandyellow.model.data.parcels

sealed class MessageParcel

sealed class EmojiClickParcel(val id: Int, open val name: String) : MessageParcel()

data class EmojiAddParcel(
    val messageId: Int,
    val emojiName: String
) : EmojiClickParcel(messageId, emojiName)

data class EmojiDeleteParcel(
    val messageId: Int,
    val emojiName: String
) : EmojiClickParcel(messageId, emojiName)

data class ShowBottomSheet(
    val messageId: Int
) : MessageParcel()

data class ShowSmileBottomSheet(
    val messageId: Int
) : MessageParcel()

data class OnBind(
    val position: Int
) : MessageParcel()

data class OpenTopic(
    val streamId: Int,
    val topic: String,
) : MessageParcel()

