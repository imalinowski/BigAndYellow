package com.malinowski.bigandyellow.view.mvi.states

sealed class MainScreenState {

    object Result : MainScreenState()

    object Loading : MainScreenState()

    class Error(val error: Throwable) : MainScreenState()
}
