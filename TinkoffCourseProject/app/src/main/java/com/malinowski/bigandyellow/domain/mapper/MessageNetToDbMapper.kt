package com.malinowski.bigandyellow.domain.mapper

import com.malinowski.bigandyellow.model.data.db_entities.MessageDB
import com.malinowski.bigandyellow.model.data.net_entities.MessageNET
import javax.inject.Inject

class MessageNetToDbMapper @Inject constructor() :
        (List<MessageNET>) -> (List<MessageDB>) {

    override fun invoke(messages: List<MessageNET>): List<MessageDB> {
        return messages.map { message ->
            MessageDB(
                id = message.id,
                message = message.message,
                userId = message.userId,
                senderName = message.senderName,
                timestamp = message.timestamp,
                avatarUrl = message.avatarUrl,
                senderEmail = message.senderEmail,
                streamId = message.streamId,
                topicName = message.topicName,
            ).apply {
                reactions = message.reactions
            }
        }
    }
}