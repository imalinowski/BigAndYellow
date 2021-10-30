package com.malinowski.bigandyellow.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.Chat
import com.malinowski.bigandyellow.model.data.ChatItem
import com.malinowski.bigandyellow.model.data.TopicChatItem
import com.malinowski.bigandyellow.usecase.FilterSubscribedUseCase
import com.malinowski.bigandyellow.usecase.IFilterSubscribedUseCase
import com.malinowski.bigandyellow.usecase.ISearchTopicsUseCase
import com.malinowski.bigandyellow.usecase.SearchTopicsUseCase
import com.malinowski.bigandyellow.view.MainScreenState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {

    private val dataProvider = Repository
    val chat = MutableLiveData<Pair<Int, Int>>() // <topic num> to <chat num in topic>

    private val _mainScreenState: MutableLiveData<MainScreenState> = MutableLiveData()
    val mainScreenState: LiveData<MainScreenState>
        get() = _mainScreenState

    val topicsSubscribed = MutableLiveData<List<TopicChatItem>>()
    val topics = MutableLiveData<List<TopicChatItem>>()

    private val searchTopicsUseCase: ISearchTopicsUseCase = SearchTopicsUseCase()
    private val filterSubscribedUseCase: IFilterSubscribedUseCase = FilterSubscribedUseCase()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val searchSubject: BehaviorSubject<String> = BehaviorSubject.create()

    fun search(query: String) {
        searchSubject.onNext(query)
    }

    init {
        subscribeToSearchChanges()
    }

    private fun subscribeToSearchChanges() {
        val flow = searchSubject
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .doOnNext { _mainScreenState.postValue(MainScreenState.Loading) }
            .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
            .switchMap { searchQuery -> searchTopicsUseCase(searchQuery) }
            .share()

        // subscribed flow
        flow.map(filterSubscribedUseCase).observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    topicsSubscribed.value = it
                    _mainScreenState.value = MainScreenState.Result
                },
                onError = { _mainScreenState.value = MainScreenState.Error(it) }
            )
            .addTo(compositeDisposable)

        flow.observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    topics.value = it
                    _mainScreenState.value = MainScreenState.Result
                },
                onError = { _mainScreenState.value = MainScreenState.Error(it) })
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