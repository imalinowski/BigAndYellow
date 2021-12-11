package com.malinowski.bigandyellow.domain.mapper

import com.malinowski.bigandyellow.model.data.Stream
import com.malinowski.bigandyellow.model.data.StreamItem
import javax.inject.Inject

internal class StreamToItemMapper @Inject constructor() : (Stream) -> (StreamItem) {

    private val topicToItemMapper: TopicToItemMapper = TopicToItemMapper()

    override fun invoke(stream: Stream): StreamItem {
        return StreamItem(
            id = stream.id,
            name = stream.name,
            topics = topicToItemMapper(stream.topics, stream.id),
            expanded = false
        )
    }
}