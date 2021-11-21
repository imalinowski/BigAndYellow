package com.malinowski.bigandyellow.view.states

import com.malinowski.bigandyellow.model.data.MessageItem
import com.malinowski.bigandyellow.model.data.User

sealed class State {
    data class Chat(
        val name: String,
        val messages: List<MessageItem>
    ) : State()

    data class Users(
        val users: List<User>
    ) : State()
}

