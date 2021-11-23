package com.malinowski.bigandyellow.view.mvi.states

import com.malinowski.bigandyellow.model.data.MessageItem
import com.malinowski.bigandyellow.model.data.StreamTopicItem
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.viewmodel.StreamsType

sealed class State {

    data class Chat(
        val name: String,
        val messages: List<MessageItem>
    ) : State()

    data class Users(
        val users: List<User>
    ) : State()

    data class Streams(
        val items: List<StreamTopicItem>,
    ) : State()
}

