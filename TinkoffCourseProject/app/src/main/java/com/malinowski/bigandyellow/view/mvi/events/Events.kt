package com.malinowski.bigandyellow.view.mvi.events

import com.malinowski.bigandyellow.model.data.User

sealed class Event {

    data class SearchStreams(
        val query: String = ""
    ) : Event()

    data class UpdateStream(
        val streamId: Int
    ) : Event()

    data class CreateStream(
        val name: String,
        val description: String
    ) : Event()

    sealed class OpenChat : Event() {
        data class WithUser(
            val user: User
        ) : OpenChat()

        data class OfTopic(
            val streamId: Int,
            val streamName: String,
            val topic: String,
        ) : OpenChat()

        data class OfStream(
            val streamId: Int,
            val streamName: String,
        ) : OpenChat()
    }

}