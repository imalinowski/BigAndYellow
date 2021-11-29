package com.malinowski.bigandyellow.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.model.RepositoryImpl
import com.malinowski.bigandyellow.model.data.MessageItem
import com.malinowski.bigandyellow.model.data.StreamTopicItem
import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.model.mapper.MessageToItemMapper
import com.malinowski.bigandyellow.usecase.SearchTopicsUseCase
import com.malinowski.bigandyellow.usecase.SearchTopicsUseCaseImpl
import com.malinowski.bigandyellow.usecase.SearchUsersUseCase
import com.malinowski.bigandyellow.usecase.SearchUsersUseCaseImpl
import com.malinowski.bigandyellow.view.ChatFragment
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

    private val dataProvider = RepositoryImpl
    val chat = MutableLiveData<Bundle>()

    private val _mainScreenState: MutableLiveData<MainScreenState> = MutableLiveData()
    val mainScreenState: LiveData<MainScreenState>
        get() = _mainScreenState

    val streamsSubscribed = MutableLiveData<List<StreamTopicItem>>()
    val streams = MutableLiveData<List<StreamTopicItem>>()
    val users = MutableLiveData<List<User>>()

    private val searchTopicsUseCase: SearchTopicsUseCase = SearchTopicsUseCaseImpl()
    private val searchUserUseCase: SearchUsersUseCase = SearchUsersUseCaseImpl()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val searchStreamSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private val searchUsersSubject: BehaviorSubject<String> = BehaviorSubject.create()

    private val messageToItemMapper: MessageToItemMapper = MessageToItemMapper()

    fun searchStreams(query: String) {
        searchStreamSubject.onNext(query)
    }

    fun searchUsers(query: String) {
        searchUsersSubject.onNext(query)
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
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    users.value = it
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
                    streamsSubscribed.value = it
                    _mainScreenState.value = MainScreenState.Result
                },
                onError = { _mainScreenState.value = MainScreenState.Error(it) }
            )
            .addTo(compositeDisposable)

        flow
            .switchMap { searchQuery ->
                searchTopicsUseCase(searchQuery, dataProvider.loadStreams())
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    streams.value = it
                    _mainScreenState.value = MainScreenState.Result
                },
                onError = { _mainScreenState.value = MainScreenState.Error(it) }
            )
            .addTo(compositeDisposable)
    }

    fun openChat(streamId: Int, topic: String) {
        Bundle().apply {
            putInt(ChatFragment.STREAM, streamId)
            putString(ChatFragment.TOPIC, topic)
            chat.postValue(this)
        }
    }

    fun openChat(user: User) {
        Bundle().apply {
            putString(ChatFragment.USER, user.email)
            putString(ChatFragment.USER_NAME, user.name)
            chat.postValue(this)
        }
    }

    fun getTopics(streamId: Int): Single<List<Topic>> =
        dataProvider.loadTopics(streamId)

    fun getMessages(stream: Int, topic: String): Single<List<MessageItem>> =
        dataProvider.loadMessages(stream, topic).map {
            messageToItemMapper(it, User.ME.id)
        }

    fun getMessagesCount(stream: Int, topic: String): Single<Int> =
        RepositoryImpl.loadMessages(stream, topic).map { it.size }

    fun getMessages(user: String): Single<List<MessageItem>> =
        dataProvider.loadMessages(user).map {
            messageToItemMapper(it, User.ME.id)
        }

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