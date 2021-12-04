package com.malinowski.bigandyellow.domain.mapper

import com.malinowski.bigandyellow.model.data.MessageData
import com.malinowski.bigandyellow.model.data.MessageItem
import com.malinowski.bigandyellow.model.data.net_entities.MessageNET


internal class MessageToItemMapper : (List<MessageData>, Int) -> (List<MessageItem>) {

    override fun invoke(messages: List<MessageData>, myId: Int): List<MessageItem> {
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