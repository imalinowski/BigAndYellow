package com.malinowski.bigandyellow.usecase

import com.malinowski.bigandyellow.model.data.ChatItem
import com.malinowski.bigandyellow.model.data.TopicChatItem
import com.malinowski.bigandyellow.model.data.TopicItem

interface IFilterSubscribedUseCase : (List<TopicChatItem>) -> List<TopicChatItem> {

    override fun invoke(items: List<TopicChatItem>): List<TopicChatItem>
}

internal class FilterSubscribedUseCase : IFilterSubscribedUseCase {
    override fun invoke(items: List<TopicChatItem>): List<TopicChatItem> {
        val filteredItems = mutableListOf<TopicChatItem>()

        var lastTopic: TopicItem? = null
        loop@ for (item in items) when (item) {
            is TopicItem -> {
                if (item.subscribed) filteredItems.add(item)
                lastTopic = item
            }
            is ChatItem -> {
                if (lastTopic?.subscribed == false) continue@loop
                filteredItems.add(item)
            }
        }

        return filteredItems
    }
}