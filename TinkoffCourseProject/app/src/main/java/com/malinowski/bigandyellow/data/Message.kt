package com.malinowski.bigandyellow.data

data class Message(
    val id: Int,
    val message: String,
    val name: String = "Aldous Huxley",
    val reactions: MutableList<Reaction> = mutableListOf(),
)