package com.malinowski.bigandyellow.domain.usecase

import com.malinowski.bigandyellow.domain.mapper.StreamToItemMapper
import com.malinowski.bigandyellow.domain.mapper.TopicToItemMapper
import com.malinowski.bigandyellow.model.data.Stream
import com.malinowski.bigandyellow.model.data.StreamTopicItem
import com.malinowski.bigandyellow.model.data.TopicItem
import io.reactivex.Observable
import javax.inject.Inject

interface SearchStreamUseCase :
        (String, Observable<List<Stream>>) -> Observable<List<StreamTopicItem>> {

    override fun invoke(
        searchQuery: String,
        flow: Observable<List<Stream>>
    ): Observable<List<StreamTopicItem>>
}

internal class SearchStreamUseCaseImpl @Inject constructor() : SearchStreamUseCase {

    @Inject
    lateinit var streamToItemMapper: StreamToItemMapper
    @Inject
    lateinit var topicToItemMapper: TopicToItemMapper

    override fun invoke(
        searchQuery: String,
        flow: Observable<List<Stream>>
    ): Observable<List<StreamTopicItem>> {
        return flow
            .map { streams ->
                if (searchQuery.isNotEmpty())
                    streams.search(searchQuery)
                else
                    streams.map(streamToItemMapper)
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