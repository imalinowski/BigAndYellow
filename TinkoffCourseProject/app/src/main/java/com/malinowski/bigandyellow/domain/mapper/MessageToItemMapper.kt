package com.malinowski.bigandyellow.domain.mapper

import com.malinowski.bigandyellow.model.data.MessageData
import com.malinowski.bigandyellow.model.data.MessageItem
import javax.inject.Inject


internal class MessageToItemMapper @Inject constructor() :
        (List<MessageData>, Int) -> (List<MessageItem>) {

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
                topic = message.topicName,
                streamId = message.streamId
            ).apply {
                for (reaction in message.reactions)
                    addEmoji(reaction)
            }
        }
    }
}