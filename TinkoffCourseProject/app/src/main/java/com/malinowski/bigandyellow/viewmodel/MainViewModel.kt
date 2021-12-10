package com.malinowski.bigandyellow.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.domain.usecase.SearchTopicsUseCase
import com.malinowski.bigandyellow.domain.usecase.SearchTopicsUseCaseImpl
import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCase
import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCaseImpl
import com.malinowski.bigandyellow.model.RepositoryImpl
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.utils.SingleLiveEvent
import com.malinowski.bigandyellow.view.ChatFragment
import com.malinowski.bigandyellow.view.mvi.events.Event
import com.malinowski.bigandyellow.view.mvi.events.Event.*
import com.malinowski.bigandyellow.view.mvi.states.MainScreenState
import com.malinowski.bigandyellow.view.mvi.states.State
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

    // states
    val usersState = MutableLiveData<State.Users>()
    val streamsAllState = MutableLiveData(State.Streams(listOf()))
    val streamsSubscribedState = MutableLiveData(State.Streams(listOf()))

    // use case
    private val searchTopicsUseCase: SearchTopicsUseCase = SearchTopicsUseCaseImpl()
    private val searchUserUseCase: SearchUsersUseCase = SearchUsersUseCaseImpl()

    private val searchStreamSubject: BehaviorSubject<String> = BehaviorSubject.create()
    private val searchUsersSubject: BehaviorSubject<String> = BehaviorSubject.create()

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

    private fun error(error: Throwable) {
        _mainScreenState.postValue(MainScreenState.Error(error))
    }

    fun setScreenState(state: MainScreenState) {
        _mainScreenState.postValue(state)
    }

    private fun openChat(streamId: Int, topicName: String) {
        Bundle().apply {
            putInt(ChatFragment.STREAM, streamId)
            putString(ChatFragment.TOPIC, topicName)
            navigateChat.postValue(this)
        }
        //chatState.value = State.Chat(name = topicName, messages = listOf())
    }

    private fun openChat(user: User) {
        Bundle().apply {
            putString(ChatFragment.USER_EMAIL, user.email)
            putString(ChatFragment.USER_NAME, user.name)
            navigateChat.postValue(this)
        }
        //chatState.value = State.Chat(name = user.name, messages = listOf())
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}