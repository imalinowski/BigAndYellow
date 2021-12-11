package com.malinowski.bigandyellow.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.domain.usecase.SearchTopicsUseCase
import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.utils.SingleLiveEvent
import com.malinowski.bigandyellow.view.ChatFragment
import com.malinowski.bigandyellow.view.mvi.events.Event
import com.malinowski.bigandyellow.view.mvi.events.Event.OpenChat
import com.malinowski.bigandyellow.view.mvi.events.Event.SearchStreams
import com.malinowski.bigandyellow.view.mvi.states.ScreenState
import com.malinowski.bigandyellow.view.mvi.states.State
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private var dataProvider: Repository
) : ViewModel() {

    private val _mainScreenState: MutableLiveData<ScreenState> = MutableLiveData()
    val mainScreenState: LiveData<ScreenState>
        get() = _mainScreenState

    //event
    val navigateChat = SingleLiveEvent<Bundle>()

    // states
    val streamsAllState = MutableLiveData(State.Streams(listOf()))
    val streamsSubscribedState = MutableLiveData(State.Streams(listOf()))

    // use case
    @Inject
    lateinit var searchTopicsUseCase: SearchTopicsUseCase

    //flow
    private val searchStreamSubject: BehaviorSubject<String> = BehaviorSubject.create()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()


    init {
        subscribeToSearchStreams()
        initUser()
    }

    private fun initUser() {
        dataProvider.loadOwnUser().subscribeBy(
            onSuccess = { user ->
                User.ME = user
            },
            onError = {
                error(it)
            }
        ).addTo(compositeDisposable)
    }

    fun processEvent(event: Event) {
        when (event) {
            is SearchStreams ->
                searchStreamSubject.onNext(event.query)
            is OpenChat.WithUser ->
                openChat(event.user)
            is OpenChat.OfTopic ->
                openChat(event.streamId, event.topic)
        }
    }

    private fun subscribeToSearchStreams() {
        val flow = searchStreamSubject
            .subscribeOn(Schedulers.io())
            .doOnNext { _mainScreenState.postValue(ScreenState.Loading) }
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
                    _mainScreenState.value = ScreenState.Result
                },
                onError = {
                    _mainScreenState.value = ScreenState.Error(it)
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
                    _mainScreenState.value = ScreenState.Result
                },
                onError = {
                    _mainScreenState.value = ScreenState.Error(it)
                }
            )
            .addTo(compositeDisposable)
    }

    private fun error(error: Throwable) {
        _mainScreenState.postValue(ScreenState.Error(error))
    }

    fun setScreenState(state: ScreenState) {
        _mainScreenState.postValue(state)
    }

    private fun openChat(streamId: Int, topicName: String) {
        Bundle().apply {
            putInt(ChatFragment.STREAM, streamId)
            putString(ChatFragment.TOPIC, topicName)
            navigateChat.postValue(this)
        }
    }

    private fun openChat(user: User) {
        Bundle().apply {
            putString(ChatFragment.USER_EMAIL, user.email)
            putString(ChatFragment.USER_NAME, user.name)
            navigateChat.postValue(this)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}