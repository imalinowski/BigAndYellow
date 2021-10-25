package com.malinowski.bigandyellow.model.data

data class Message(
    val id: Int,
    val message: String,
    val user: User,
    val reactions: MutableList<Reaction> = mutableListOf(),
)