package com.malinowski.bigandyellow.view.mvi.events

import com.malinowski.bigandyellow.model.data.User

sealed class Event {

    data class SearchStreams(
        val query: String = ""
    ) : Event()

    sealed class OpenChat : Event() {
        data class WithUser(
            val user: User
        ) : OpenChat()

        data class OfTopic(
            val streamId: Int,
            val topic: String,
        ) : OpenChat()
    }

}

sealed class UsersEvent {

    data class SearchUsers(
        val query: String = ""
    ) : UsersEvent()

}

sealed class ChatEvent {

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
        val messageId: Int
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