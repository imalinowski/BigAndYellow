package com.malinowski.bigandyellow.model.mapper

import com.malinowski.bigandyellow.model.data.Chat
import com.malinowski.bigandyellow.model.data.ChatItem

internal class ChatToItemMapper : (List<Chat>, Int) -> (List<ChatItem>) {

    override fun invoke(chats: List<Chat>, topicId: Int): List<ChatItem> {
        return chats.mapIndexed { index, chat ->
            ChatItem(
                chatId = index,
                topicId = topicId,
                name = chat.name,
                messageNum = chat.messages.size
            )
        }
    }
}