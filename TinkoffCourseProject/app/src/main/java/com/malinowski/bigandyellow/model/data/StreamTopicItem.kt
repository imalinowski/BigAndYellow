package com.malinowski.bigandyellow.model.data

sealed class StreamTopicItem()

data class StreamItem(
    val streamId: Int,
    val name: String,
    var topics: List<TopicItem>,
    var expanded: Boolean = false,
    var loading: Boolean = false
) : StreamTopicItem()

data class TopicItem(
    val chatId: Int,
    val topicId: Int,
    val name: String,
    val messageNum: Int
) : StreamTopicItem()