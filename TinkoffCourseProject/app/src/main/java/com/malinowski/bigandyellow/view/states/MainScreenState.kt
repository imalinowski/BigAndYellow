package com.malinowski.bigandyellow.view.states

sealed class MainScreenState {

    object Result : MainScreenState()

    object Loading : MainScreenState()

    class Error(val error: Throwable) : MainScreenState()
}
