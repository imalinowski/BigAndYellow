package com.malinowski.bigandyellow.model.mapper

import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.TopicItem

internal class TopicToItemMapper : (Topic) -> (TopicItem) {

    private val chatToItemMapper: ChatToItemMapper = ChatToItemMapper()

    override fun invoke(topic: Topic): TopicItem {
        return TopicItem(
            name = topic.name,
            id = topic.id,
            chats = chatToItemMapper(topic.chats, topic.id),
            subscribed = topic.subscribed,
            expanded = false
        )
    }
}