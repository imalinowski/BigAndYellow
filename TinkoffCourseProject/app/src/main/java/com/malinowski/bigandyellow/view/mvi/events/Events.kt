package com.malinowski.bigandyellow.view.mvi.events

import com.malinowski.bigandyellow.model.data.StreamItem
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.viewmodel.StreamsType

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

    sealed class Load : Event() {
        data class Topics(
            val stream: StreamItem,
            val type: StreamsType
        ) : Load()
    }
}