package com.malinowski.bigandyellow.domain.mapper

import com.malinowski.bigandyellow.model.data.Stream
import com.malinowski.bigandyellow.model.data.StreamItem

internal class StreamToItemMapper : (Stream) -> (StreamItem) {

    private val topicToItemMapper: TopicToItemMapper = TopicToItemMapper()

    override fun invoke(stream: Stream): StreamItem {
        return StreamItem(
            streamId = stream.id,
            name = stream.name,
            topics = topicToItemMapper(stream.topics, stream.id),
            expanded = false
        )
    }
}