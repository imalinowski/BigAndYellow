package com.malinowski.bigandyellow.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCase
import com.malinowski.bigandyellow.domain.usecase.SearchUsersUseCaseImpl
import com.malinowski.bigandyellow.view.mvi.events.UsersEvent
import com.malinowski.bigandyellow.view.mvi.states.ScreenState
import com.malinowski.bigandyellow.view.mvi.states.State
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class UsersViewModel : ViewModel() {

    //state
    val usersState = MutableLiveData<State.Users>()

    private val _screenState: MutableLiveData<ScreenState> = MutableLiveData()
    val screenState: LiveData<ScreenState>
        get() = _screenState

    //flow
    private val searchUsersSubject: BehaviorSubject<String> = BehaviorSubject.create()

    //use case
    private val searchUserUseCase: SearchUsersUseCase = SearchUsersUseCaseImpl()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    init {
        subscribeToSearchUser()
    }

    fun processEvent(event: UsersEvent) {
        when (event) {
            is UsersEvent.SearchUsers ->
                searchUsersSubject.onNext(event.query)
        }
    }

    private fun subscribeToSearchUser() {
        searchUsersSubject
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .doOnNext { _screenState.postValue(ScreenState.Loading) }
            .debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
            .switchMap { searchQuery -> searchUserUseCase(searchQuery) }
            .observeOn(AndroidSchedulers.mainThread(), true)
            .subscribeBy(
                onNext = {
                    usersState.value = State.Users(it)
                    _screenState.value = ScreenState.Result
                },
                onError = { _screenState.value = ScreenState.Error(it) }
            )
            .addTo(compositeDisposable)
    }

}