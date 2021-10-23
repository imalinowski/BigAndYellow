package com.malinowski.bigandyellow.model.data

data class Chat(val name: String, val messages: MutableList<Message> = mutableListOf())