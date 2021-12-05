package com.malinowski.bigandyellow.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.domain.mapper.MessageToItemMapper
import com.malinowski.bigandyellow.domain.usecase.SearchTopicsUseCase
import com.malinowski.bigandyellow.domain.usecase.SearchTopicsUseCaseImpl
import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCase
import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCaseImpl
import com.malinowski.bigandyellow.model.RepositoryImpl
import com.malinowski.bigandyellow.model.data.MessageData
import com.malinowski.bigandyellow.model.data.MessageItem
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.utils.SingleLiveEvent
import com.malinowski.bigandyellow.view.ChatFragment
import com.malinowski.bigandyellow.view.mvi.events.Event
import com.malinowski.bigandyellow.view.mvi.events.Event.*
import com.malinowski.bigandyellow.view.mvi.states.MainScreenState
import com.malinowski.bigandyellow.view.mvi.states.State
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {

    private val dataProvider = RepositoryImpl
    private val _mainScreenState: MutableLiveData<MainScreenState> = MutableLiveData()

    val mainScreenState: LiveData<MainScreenState>
        get() = _mainScreenState

    //event
    val navigateChat = SingleLiveEvent<Bundle>()
    val scrollToPos = SingleLiveEvent<Int>()

    // states
    val usersState = MutableLiveData<State.Users>()
    val streamsAllState = MutableLiveData(State.Streams(listOf()))
    val streamsSubscribedState = MutableLiveData(State.Streams(listOf()))
    val chatState = MutableLiveData<State.Chat>()

    // use case
    private val searchTopicsUseCase: SearchTopicsUseCase = SearchTopicsUseCaseImpl()
    private val searchUserUseCase: SearchUsersUseCase = SearchUsersUseCaseImpl()

    private val searchStreamSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private val searchUsersSubject: BehaviorSubject<String> = BehaviorSubject.create()

    private val messageToItemMapper: MessageToItemMapper = MessageToItemMapper()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

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

    fun processEvent(event: Event) {
        when (event) {
            is SearchUsers ->
                searchUsersSubject.onNext(event.query)
            is SearchStreams ->
                searchStreamSubject.onNext(event.query)
            is OpenChat.WithUser ->
                openChat(event.user)
            is OpenChat.OfTopic ->
                openChat(event.streamId, event.topic)
            is SendMessage.ToUser ->
                sendMessageToUser(event.userEmail, event.content)
            is SendMessage.ToTopic ->
                sendMessageToTopic(event.streamId, event.topicName, event.content)
            is LoadMessages.ForUser ->
                processGetMessages(getMessages(event.userEmail, event.anchor))
            is LoadMessages.ForTopic ->
                processGetMessages(getMessages(event.streamId, event.topicName, event.anchor))
            is SetMessageNum ->
                setMessageNum(event.topicName, event.messageNum)
            is Reaction.Add ->
                addReaction(event.messageId, event.emojiName)
            is Reaction.Remove ->
                deleteReaction(event.messageId, event.emojiName)
        }
    }

    private fun subscribeToSearchUser() {
        searchUsersSubject
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .doOnNext { _mainScreenState.postValue(MainScreenState.Loading) }
            .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
            .switchMap { searchQuery -> searchUserUseCase(searchQuery) }
            .observeOn(AndroidSchedulers.mainThread(), true)
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
            .doOnNext { _mainScreenState.postValue(MainScreenState.Loading) }
            .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
            .share()

        // subscribed flow
        flow
            .switchMap { searchQuery ->
                searchTopicsUseCase(searchQuery, dataProvider.loadSubscribedStreams())
            }.observeOn(AndroidSchedulers.mainThread(), true)
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
            }.observeOn(AndroidSchedulers.mainThread(), true)
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

    fun result() {
        _mainScreenState.postValue(MainScreenState.Result)
    }

    fun error(error: Throwable) {
        _mainScreenState.postValue(MainScreenState.Error(error))
    }

    private fun loading() {
        _mainScreenState.postValue(MainScreenState.Loading)
    }

    private fun openChat(streamId: Int, topicName: String) {
        Bundle().apply {
            putInt(ChatFragment.STREAM, streamId)
            putString(ChatFragment.TOPIC, topicName)
            navigateChat.postValue(this)
        }
        chatState.value = State.Chat(name = topicName, messages = listOf())
    }

    private fun openChat(user: User) {
        Bundle().apply {
            putString(ChatFragment.USER_EMAIL, user.email)
            putString(ChatFragment.USER_NAME, user.name)
            navigateChat.postValue(this)
        }
        chatState.value = State.Chat(name = user.name, messages = listOf())
    }

    private fun getMessages(
        stream: Int, topicName: String, anchor: String
    ): Observable<List<MessageData>> =
        dataProvider.loadMessages(stream, topicName, anchor)

    private fun getMessages(
        user: String, anchor: String
    ): Observable<List<MessageData>> =
        dataProvider.loadMessages(user, anchor)

    private fun processGetMessages(flow: Observable<List<MessageData>>) {
        loading()
        val state = chatState.value!!.copy()
        flow.map { messageToItemMapper(it, User.ME.id) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { messagesPage ->
                    result()
                    val lastPage = messagesPage.isEmpty()
                    val messages = state.messages.toMutableList().apply { addAll(messagesPage) }
                    chatState.value = state.copy(
                        messages = messages,
                        loaded = lastPage
                    )
                    if (state.messages.isEmpty())
                        scrollToPos.value = 0
                },
                onError = { error(it) }
            )
            .addTo(compositeDisposable)
    }

    private fun setMessageNum(topicName: String, messageNum: Int) =
        dataProvider.setMessageNum(topicName, messageNum).subscribeBy(
            onSuccess = { },
            onError = { error(it) }
        ).addTo(compositeDisposable)

    private fun sendMessage(
        type: RepositoryImpl.SendType, to: String, content: String, topic: String = ""
    ) {
        loading()
        dataProvider.sendMessage(type, to, content, topic)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { id ->
                    Log.d("MESSAGE_SEND", "$id $content")
                    val message = MessageItem(id, content, User.ME.id, true)
                    val state = chatState.value!!
                    val list = state.messages.toMutableList().apply { add(0, message) }
                    chatState.value = state.copy(messages = list)
                    scrollToPos.value = 0
                    result()
                },
                onError = { error(it) }
            ).addTo(compositeDisposable)
    }

    private fun sendMessageToUser(userEmail: String, content: String) {
        sendMessage(RepositoryImpl.SendType.PRIVATE, userEmail, content)
    }

    private fun sendMessageToTopic(stream: Int, topic: String, content: String) {
        sendMessage(
            RepositoryImpl.SendType.STREAM, "[$stream]", content, topic
        )
    }

    private fun addReaction(messageId: Int, emojiName: String) {
        dataProvider.addEmoji(messageId, emojiName).subscribeBy(
            onComplete = { },
            onError = { error(it) }
        ).addTo(compositeDisposable)
    }

    private fun deleteReaction(messageId: Int, emojiName: String) {
        dataProvider.deleteEmoji(messageId, emojiName).subscribeBy(
            onComplete = { },
            onError = { error(it) }
        ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}