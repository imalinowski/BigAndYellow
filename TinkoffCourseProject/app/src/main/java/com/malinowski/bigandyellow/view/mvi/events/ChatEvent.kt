package com.malinowski.bigandyellow.view.mvi.events

sealed class ChatEvent {

    data class SearchTopics(
        val query: String,
        val streamId: Int
    ) : ChatEvent()

    data class SetMessageID(
        val messageId: Int
    ) : ChatEvent()

    data class DeleteMessage(
        val messageId: Int
    ) : ChatEvent()

    data class EditMessage(
        val messageId: Int,
        val content: String
    ) : ChatEvent()

    data class ChangeMessageTopic(
        val messageId: Int,
        val topic: String
    ) : ChatEvent()

    data class SetMessageNum(
        val topicName: String?,
        val messageNum: Int
    ) : ChatEvent()

    data class LoadTopics(
        val messageId: Int,
        val streamId: Int,
    ) : ChatEvent()

    sealed class LoadMessages : ChatEvent() {
        data class ForUser(
            val userEmail: String,
            val anchor: String
        ) : LoadMessages()

        data class ForTopic(
            val streamId: Int,
            val topicName: String,
            val anchor: String
        ) : LoadMessages()

        data class ForStream(
            val streamId: Int,
            val anchor: String
        ) : LoadMessages()
    }

    sealed class SendMessage : ChatEvent() {
        data class ToUser(
            val userEmail: String,
            val content: String
        ) : SendMessage()

        data class ToTopic(
            val streamId: Int,
            val topicName: String,
            val content: String
        ) : SendMessage()
    }

    sealed class Reaction : ChatEvent() {
        data class Add(
            val messageId: Int,
            val emojiName: String
        ) : Reaction()

        data class Remove(
            val messageId: Int,
            val emojiName: String
        ) : Reaction()
    }
}