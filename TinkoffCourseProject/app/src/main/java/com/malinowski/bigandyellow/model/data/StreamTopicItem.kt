package com.malinowski.bigandyellow.model.data

sealed class StreamTopicItem()

data class StreamItem(
    val id: Int,
    val name: String,
    var topics: List<TopicItem>,
    var expanded: Boolean = false,
    var loading: Boolean = false
) : StreamTopicItem()

data class TopicItem(
    val topicId: Int,
    val streamId: Int,
    val name: String,
    var messageNum: Int
) : StreamTopicItem()