package com.malinowski.bigandyellow.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.domain.mapper.MessageToItemMapper
import com.malinowski.bigandyellow.domain.mapper.TopicToItemMapper
import com.malinowski.bigandyellow.domain.usecase.SearchTopicsUseCase
import com.malinowski.bigandyellow.domain.usecase.SearchTopicsUseCaseImpl
import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCase
import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCaseImpl
import com.malinowski.bigandyellow.model.RepositoryImpl
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.network.ZulipChat
import com.malinowski.bigandyellow.view.ChatFragment
import com.malinowski.bigandyellow.view.mvi.events.Event
import com.malinowski.bigandyellow.view.mvi.states.MainScreenState
import com.malinowski.bigandyellow.view.mvi.states.State
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {

    private val dataProvider = RepositoryImpl
    val chat = MutableLiveData<Bundle>()

    private val _mainScreenState: MutableLiveData<MainScreenState> = MutableLiveData()
    val mainScreenState: LiveData<MainScreenState>
        get() = _mainScreenState

    // states
    val usersState = MutableLiveData<State.Users>()
    val streamsAllState = MutableLiveData(State.Streams(listOf()))
    val streamsSubscribedState = MutableLiveData(State.Streams(listOf()))

    // use case
    private val searchTopicsUseCase: SearchTopicsUseCase = SearchTopicsUseCaseImpl()
    private val searchUserUseCase: SearchUsersUseCase = SearchUsersUseCaseImpl()

    private val searchStreamSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private val searchUsersSubject: BehaviorSubject<String> = BehaviorSubject.create()

    // mapper
    private val messageToItemMapper: MessageToItemMapper = MessageToItemMapper()
    private val topicToItemMapper: TopicToItemMapper = TopicToItemMapper()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun searchStreams(query: String) {
        searchStreamSubject.onNext(query)
    }

    init {
        subscribeToSearchStreams()
        subscribeToSearchUser()
        initUser()
    }

    private fun initUser() {
        dataProvider.loadOwnUser().subscribeBy(
            onSuccess = { user -> User.ME = user },
            onError = { error(it) }
        ).addTo(compositeDisposable)
    }

    private fun subscribeToSearchUser() {
        searchUsersSubject
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .doOnNext { _mainScreenState.postValue(MainScreenState.Loading) }
            .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
            .switchMap { searchQuery -> searchUserUseCase(searchQuery) }
            .observeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    usersState.value = State.Users(it)
                    _mainScreenState.value = MainScreenState.Result
                },
                onError = { _mainScreenState.value = MainScreenState.Error(it) }
            )
            .addTo(compositeDisposable)
    }

    private fun subscribeToSearchStreams() {
        val flow = searchStreamSubject
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .doOnNext { _mainScreenState.postValue(MainScreenState.Loading) }
            .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
            .share()

        // subscribed flow
        flow
            .switchMap { searchQuery ->
                searchTopicsUseCase(searchQuery, dataProvider.loadSubscribedStreams())
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    streamsSubscribedState.value = State.Streams(it)
                    _mainScreenState.value = MainScreenState.Result
                },
                onError = {
                    _mainScreenState.value = MainScreenState.Error(it)
                }
            )
            .addTo(compositeDisposable)

        flow
            .switchMap { searchQuery ->
                searchTopicsUseCase(searchQuery, dataProvider.loadStreams())
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    streamsAllState.value = State.Streams(it)
                    _mainScreenState.value = MainScreenState.Result
                },
                onError = {
                    _mainScreenState.value = MainScreenState.Error(it)
                }
            )
            .addTo(compositeDisposable)
    }

    fun processEvent(event: Event) {
        when (event) {
            is Event.SearchUsers -> searchUsersSubject.onNext(event.query)
            is Event.OpenChat.WithUser -> openChat(event.user) // todo single live event
            is Event.OpenChat.OfTopic -> openChat(
                event.streamId,
                event.topic
            )
        }
    }

    private fun openChat(streamId: Int, topicName: String) {
        Bundle().apply {
            putInt(ChatFragment.STREAM, streamId)
            putString(ChatFragment.TOPIC, topicName)
            chat.postValue(this)
        }
    }

    private fun openChat(user: User) {
        Bundle().apply {
            putString(ChatFragment.USER_EMAIL, user.email)
            putString(ChatFragment.USER_NAME, user.name)
            chat.postValue(this)
        }
    }

    fun getMessages(
        stream: Int,
        topicName: String,
        anchor: String = ZulipChat.NEWEST_MES
    ): Observable<List<MessageItem>> =
        dataProvider.loadMessages(stream, topicName, anchor).map {
            messageToItemMapper(it)
        }

    fun getMessages(
        user: String,
        anchor: String = ZulipChat.NEWEST_MES
    ): Observable<List<MessageItem>> =
        dataProvider.loadMessages(user, anchor).map {
            messageToItemMapper(it)
        }

    fun setMessageNum(topicName: String, messageNum: Int) =
        dataProvider.setMessageNum(topicName, messageNum).subscribeBy(
            onSuccess = { },
            onError = { Log.e("LOAD_TOPIC_BY_NAME", "${it.message}") }
        ).addTo(compositeDisposable)

    private fun sendMessage(
        type: RepositoryImpl.SendType, to: String, content: String, topic: String = ""
    ): Single<Int> {
        return dataProvider.sendMessage(type, to, content, topic)
    }

    fun sendMessageToUser(userEmail: String, content: String) =
        sendMessage(RepositoryImpl.SendType.PRIVATE, userEmail, content)

    fun sendMessageToTopic(stream: Int, topic: String, content: String) =
        sendMessage(RepositoryImpl.SendType.STREAM, "[$stream]", content, topic)

    fun addReaction(messageId: Int, emojiName: String) {
        dataProvider.addEmoji(messageId, emojiName).subscribeBy(
            onComplete = {

            }, onError = { error(it) }
        ).addTo(compositeDisposable)
    }

    fun deleteReaction(messageId: Int, emojiName: String) {
        dataProvider.deleteEmoji(messageId, emojiName).subscribeBy(
            onComplete = {}, onError = { error(it) }
        ).addTo(compositeDisposable)
    }

    fun result() {
        _mainScreenState.postValue(MainScreenState.Result)
    }

    fun error(error: Throwable) {
        _mainScreenState.postValue(MainScreenState.Error(error))
    }

    fun loading() {
        _mainScreenState.postValue(MainScreenState.Loading)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}