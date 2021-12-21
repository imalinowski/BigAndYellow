package com.malinowski.bigandyellow.domain.mapper

import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.TopicItem
import javax.inject.Inject

class TopicToItemMapper @Inject constructor() : (List<Topic>, Int) -> (List<TopicItem>) {

    override fun invoke(topics: List<Topic>, streamId: Int): List<TopicItem> {
        return topics.mapIndexed { index, topic ->
            TopicItem(
                topicId = index,
                streamId = streamId,
                name = topic.name,
                messageNum = topic.messageNum
            )
        }
    }
}