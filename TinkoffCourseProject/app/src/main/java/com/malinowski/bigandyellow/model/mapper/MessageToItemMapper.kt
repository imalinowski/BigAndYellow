package com.malinowski.bigandyellow.model.mapper

import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.MessageItem


internal class MessageToItemMapper : (List<Message>, Int) -> (List<MessageItem>) {

    override fun invoke(messages: List<Message>, myId: Int): List<MessageItem> {
        return messages.map { message ->
            MessageItem(
                id = message.id,
                message = message.message,
                userId = message.userId,
                isMine = message.userId == myId,
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