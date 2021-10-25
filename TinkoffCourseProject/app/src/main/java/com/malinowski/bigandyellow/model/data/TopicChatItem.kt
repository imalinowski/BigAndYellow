package com.malinowski.bigandyellow.model.data

sealed class TopicChatItem

data class TopicItem(val name: String, val id: Int, var expanded: Boolean = false) :
    TopicChatItem()

data class ChatItem(val name: String, val messageNum: Int, val topicId: Int, val chatId: Int) :
    TopicChatItem()