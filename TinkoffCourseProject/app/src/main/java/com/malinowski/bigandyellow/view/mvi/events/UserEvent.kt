package com.malinowski.bigandyellow.view.mvi.events

sealed class UsersEvent {

    data class SearchUsers(
        val query: String = ""
    ) : UsersEvent()

}