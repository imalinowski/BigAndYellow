package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int,
    val message: String,
    val user: User,
    val reactions: MutableList<Reaction> = mutableListOf(),
)