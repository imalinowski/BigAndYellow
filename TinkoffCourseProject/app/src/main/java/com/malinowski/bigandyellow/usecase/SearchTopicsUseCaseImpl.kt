package com.malinowski.bigandyellow.usecase

import com.malinowski.bigandyellow.model.data.Stream
import com.malinowski.bigandyellow.model.data.StreamTopicItem
import com.malinowski.bigandyellow.model.data.TopicItem
import com.malinowski.bigandyellow.model.mapper.StreamToItemMapper
import com.malinowski.bigandyellow.model.mapper.TopicToItemMapper
import io.reactivex.Observable

interface SearchTopicsUseCase :
        (String, Observable<List<Stream>>) -> Observable<List<StreamTopicItem>> {

    override fun invoke(
        searchQuery: String,
        streams: Observable<List<Stream>>
    ): Observable<List<StreamTopicItem>>
}

internal class SearchTopicsUseCaseImpl : SearchTopicsUseCase {

    private val streamToItemMapper: StreamToItemMapper = StreamToItemMapper()
    private val topicToItemMapper: TopicToItemMapper = TopicToItemMapper()

    override fun invoke(
        searchQuery: String,
        streams: Observable<List<Stream>>
    ): Observable<List<StreamTopicItem>> {
        return streams.map { topics ->
            if (searchQuery.isNotEmpty())
                topics.search(searchQuery)
            else
                topics.map(streamToItemMapper)
        }
    }

    private fun List<Stream>.search(query: String): List<StreamTopicItem> {
        val streamTopicItem = mutableListOf<StreamTopicItem>()
        this.forEach { stream ->
            val topics = stream.search(query)
            if (topics.isNotEmpty() || stream.name.contains(query, ignoreCase = true)) {
                streamTopicItem.add(streamToItemMapper(stream).apply {
                    if (topics.isNotEmpty())
                        expanded = true
                })
                streamTopicItem.addAll(topics)
            }
        }
        return streamTopicItem
    }

    private fun Stream.search(query: String): List<TopicItem> {
        val satisfyChats = topics.filter { topic ->
            topic.name.contains(query, ignoreCase = true)
        }
        return topicToItemMapper(satisfyChats, this.id)
    }

}