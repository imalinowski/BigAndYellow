package com.malinowski.bigandyellow.model.mapper

import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.TopicItem

internal class TopicToItemMapper : (List<Topic>, Int) -> (List<TopicItem>) {

    override fun invoke(topics: List<Topic>, streamId: Int): List<TopicItem> {
        return topics.mapIndexed { index, chat ->
            TopicItem(
                topicId = index,
                streamId = streamId,
                name = chat.name,
                messageNum = chat.lastMesID
            )
        }
    }
}