package com.malinowski.bigandyellow.model.data

data class Topic(
    val name: String,
    val id: Int,
    val subscribed: Boolean = false,
    val chats: MutableList<Chat> = mutableListOf()
)