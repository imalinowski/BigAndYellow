package com.malinowski.bigandyellow.model.mapper

import com.malinowski.bigandyellow.model.data.MessageItem
import com.malinowski.bigandyellow.model.data.db_entities.MessageDB
import com.malinowski.bigandyellow.model.data.net_entities.MessageNET


internal class MessageDbToItemMapper : (List<MessageDB>, Int) -> (List<MessageItem>) {

    override fun invoke(messages: List<MessageDB>, myId: Int): List<MessageItem> {
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