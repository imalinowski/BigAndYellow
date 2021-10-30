package com.malinowski.bigandyellow.usecase

import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.mapper.ChatToItemMapper
import com.malinowski.bigandyellow.model.mapper.TopicToItemMapper
import io.reactivex.Observable

interface ISearchTopicsUseCase : (String) -> Observable<List<TopicChatItem>> {

    override fun invoke(searchQuery: String): Observable<List<TopicChatItem>>
}

internal class SearchTopicsUseCase : ISearchTopicsUseCase {

    private val topicToItemMapper: TopicToItemMapper = TopicToItemMapper()
    private val chatToItemMapper: ChatToItemMapper = ChatToItemMapper()
    private val dataProvider = Repository

    override fun invoke(searchQuery: String): Observable<List<TopicChatItem>> {
        return dataProvider.loadData()
            .map { topics ->
                if (searchQuery.isNotEmpty())
                    topics.search(searchQuery)
                else
                    topics.map(topicToItemMapper)
            }
    }

    private fun List<Topic>.search(query: String): List<TopicChatItem> {
        val topicChatItems = mutableListOf<TopicChatItem>()
        this.forEach { topic ->
            val chats = topic.search(query)
            if(chats.isNotEmpty() || topic.name.contains(query, ignoreCase = true)) {
                topicChatItems.add(topicToItemMapper(topic).apply {
                    if(chats.isNotEmpty()) expanded = true
                })
                topicChatItems.addAll(chats)
            }
        }
        return topicChatItems
    }

    private fun Topic.search(query: String): List<ChatItem> {
        val satisfyChats = mutableListOf<Chat>()
        this.chats.forEach { chat ->
            if (chat.name.contains(query, ignoreCase = true)) satisfyChats.add(chat)
        }
        return chatToItemMapper(satisfyChats, this.id)
    }

}