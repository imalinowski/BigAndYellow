package com.malinowski.bigandyellow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.model.DataProvider
import com.malinowski.bigandyellow.model.data.Chat
import com.malinowski.bigandyellow.model.data.ChatItem

class MainViewModel : ViewModel() {

    private val dataProvider = DataProvider
    val chat = MutableLiveData<Pair<Int, Int>>() // <topic num> to <chat num in topic>

    fun getTopics(subscribed: Boolean = false): List<Pair<Int, String>> =
        dataProvider.getTopicsNames().mapIndexed { index, s ->
            index to s
        }.filter {
            !subscribed || dataProvider.getTopic(it.first).subscribed
        }

    fun getChatsByTopic(topicNum: Int): List<ChatItem> =
        dataProvider.getTopic(topicNum).chats.mapIndexed { index, chat ->
            ChatItem(chat.name, chat.messages.size, topicNum, index)
        }

    fun openChat(topicId: Int, chatId: Int) {
        if (topicId !in 0..dataProvider.getTopicsSize())
            throw IllegalArgumentException("getChat > topicNum is out of indices")
        if (chatId !in dataProvider.getTopic(topicId).chats.indices)
            throw IllegalArgumentException("getChat > chatNum is out of indices")
        chat.postValue(topicId to chatId)
    }

    fun getChat(topicNum: Int, chatNum: Int): Chat = dataProvider.getTopic(topicNum).chats[chatNum]

}