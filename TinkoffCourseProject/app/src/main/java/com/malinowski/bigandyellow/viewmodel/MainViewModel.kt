package com.malinowski.bigandyellow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.Chat
import com.malinowski.bigandyellow.model.data.ChatItem
import com.malinowski.bigandyellow.model.data.TopicChatItem
import com.malinowski.bigandyellow.model.data.TopicItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class MainViewModel : ViewModel() {

    private val dataProvider = Repository
    val chat = MutableLiveData<Pair<Int, Int>>() // <topic num> to <chat num in topic>
    val topics = MutableLiveData<List<TopicChatItem>>()

    private val searchSubject: PublishSubject<String> = PublishSubject.create()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun search(query: String) {
        searchSubject.onNext(query)
    }

    init {
        subscribeToSearchChanges()
    }

    private fun subscribeToSearchChanges() {
        searchSubject
            .subscribeOn(Schedulers.io())
            //.distinctUntilChanged()
            //.debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
            .switchMap { searchQuery ->
                dataProvider.loadData()
                    .map { topics ->
                        topics.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }
            }
            .map {
                it.map { topic ->
                    TopicItem(
                        name = topic.name,
                        id = topic.id,
                        subscribed = topic.subscribed,
                        expanded = false
                    )
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { topics.value = it }
            )
            .addTo(compositeDisposable)
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