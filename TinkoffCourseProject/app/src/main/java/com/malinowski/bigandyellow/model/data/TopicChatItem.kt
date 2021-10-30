package com.malinowski.bigandyellow.model.data

sealed class TopicChatItem()

data class TopicItem(
    val topicId: Int,
    val name: String,
    val chats: List<ChatItem>,
    val subscribed: Boolean = false,
    var expanded: Boolean = false,
    var loading: Boolean = false
) : TopicChatItem()

data class ChatItem(
    val chatId: Int,
    val topicId: Int,
    val name: String,
    val messageNum: Int
) : TopicChatItem()