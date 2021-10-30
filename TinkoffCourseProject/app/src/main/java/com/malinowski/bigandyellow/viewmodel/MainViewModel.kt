package com.malinowski.bigandyellow.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.Chat
import com.malinowski.bigandyellow.model.data.TopicChatItem
import com.malinowski.bigandyellow.usecase.FilterSubscribedUseCase
import com.malinowski.bigandyellow.usecase.IFilterSubscribedUseCase
import com.malinowski.bigandyellow.usecase.ISearchTopicsUseCase
import com.malinowski.bigandyellow.usecase.SearchTopicsUseCase
import com.malinowski.bigandyellow.view.MainScreenState
import io.reactivex.Single
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
                onError = { _mainScreenState.value = MainScreenState.Error(it) }
            )
            .addTo(compositeDisposable)
    }

    fun openChat(topicId: Int, chatId: Int) {
        chat.postValue(topicId to chatId)
    }

    fun getChats(topicId: Int): Single<List<Chat>> =
        dataProvider.loadItem(topicId).singleOrError().map { it.chats }

    fun getChat(topicId: Int, chatId: Int): Single<Chat> =
        getChats(topicId).map { it[chatId] }

    fun result() {
        _mainScreenState.postValue(MainScreenState.Result)
    }

    fun error(error: Throwable) {
        _mainScreenState.postValue(MainScreenState.Error(error))
    }

    fun loading() {
        _mainScreenState.postValue(MainScreenState.Loading)
    }

}