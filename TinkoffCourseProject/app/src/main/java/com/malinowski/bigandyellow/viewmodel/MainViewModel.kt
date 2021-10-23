package com.malinowski.bigandyellow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.model.DataProvider
import com.malinowski.bigandyellow.model.data.Chat

class MainViewModel : ViewModel() {

    private val dataProvider = DataProvider
    val chat = MutableLiveData<Pair<Int, Int>>() // <topic num> to <chat num in topic>

    fun getTopics(subscribed: Boolean = false): List<String> =
        dataProvider.getTopicsNames().filterIndexed { index, _ ->
            !subscribed || dataProvider.getTopic(index).subscribed
        }

    fun getChatNames(topic: Int): List<String> =
        dataProvider.getTopic(topic).chats.map {
            it.name
        }

    fun openChat(topicNum: Int, chatNum: Int) {
        if (topicNum !in 0..dataProvider.getTopicsSize())
            throw IllegalArgumentException("getChat > topicNum is out of indices")
        if (chatNum !in dataProvider.getTopic(topicNum).chats.indices)
            throw IllegalArgumentException("getChat > chatNum is out of indices")
        chat.postValue(topicNum to chatNum)
    }

    fun getChat(topicNum: Int, chatNum: Int): Chat = dataProvider.getTopic(topicNum).chats[chatNum]

}