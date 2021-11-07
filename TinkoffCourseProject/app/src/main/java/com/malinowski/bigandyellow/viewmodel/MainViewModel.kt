package com.malinowski.bigandyellow.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.StreamTopicItem
import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.usecase.ISearchTopicsUseCase
import com.malinowski.bigandyellow.usecase.ISearchUsersUseCase
import com.malinowski.bigandyellow.usecase.SearchTopicsUseCase
import com.malinowski.bigandyellow.usecase.SearchUsersUseCase
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

    private val dataProvider = Repository
    val chat = MutableLiveData<Bundle>()

    private val _mainScreenState: MutableLiveData<MainScreenState> = MutableLiveData()
    val mainScreenState: LiveData<MainScreenState>
        get() = _mainScreenState

    val streamsSubscribed = MutableLiveData<List<StreamTopicItem>>()
    val streams = MutableLiveData<List<StreamTopicItem>>()
    val users = MutableLiveData<List<User>>()

    private val searchTopicsUseCase: ISearchTopicsUseCase = SearchTopicsUseCase()
    private val searchUserUseCase: ISearchUsersUseCase = SearchUsersUseCase()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val searchStreamSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private val searchUsersSubject: BehaviorSubject<String> = BehaviorSubject.create()

    fun searchStreams(query: String) {
        searchStreamSubject.onNext(query)
    }

    fun searchUsers(query: String) {
        searchUsersSubject.onNext(query)
    }

    init {
        subscribeToSearchStreams()
        subscribeToSearchUser()
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

    fun openChat(userEmail: String) {
        Bundle().apply {
            putString(ChatFragment.USER, userEmail)
            chat.postValue(this)
        }
    }

    fun getTopics(streamId: Int): Single<List<Topic>> =
        dataProvider.loadTopics(streamId)

    fun getMessages(stream: Int, topic: String): Single<List<Message>> =
        dataProvider.loadMessages(stream, topic)

    fun getMessages(user: String): Single<List<Message>> =
        dataProvider.loadMessages(user)

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