package com.malinowski.bigandyellow.model.mapper

import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.MessageItem
import com.malinowski.bigandyellow.model.data.User


internal class MessageToItemMapper : (List<Message>) -> (List<MessageItem>) {

    override fun invoke(messages: List<Message>): List<MessageItem> {
        return messages.map { message ->
            MessageItem(
                id = message.id,
                message = message.message,
                userId = message.userId,
                isMine = message.userId == User.ME.id,
                senderName = message.senderName,
                timestamp = message.timestamp,
                avatarUrl = message.avatarUrl,
            ).apply {
                for (reaction in message.reactions)
                    addEmoji(reaction)
            }
        }
    }
}