package com.malinowski.bigandyellow.view.events

import com.malinowski.bigandyellow.model.data.User

sealed class Event {

    data class SearchUsers(
        val query: String
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