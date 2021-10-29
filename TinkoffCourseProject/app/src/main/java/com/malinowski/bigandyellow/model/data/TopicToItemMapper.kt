package com.malinowski.bigandyellow.model.data

internal class TopicToItemMapper : (List<Topic>) -> (List<TopicItem>) {

    override fun invoke(topics: List<Topic>): List<TopicItem> {
        return topics.map { topic ->
            TopicItem(
                name = topic.name,
                id = topic.id,
                subscribed = topic.subscribed,
                expanded = false
            )
        }
    }
}