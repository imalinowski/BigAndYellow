package com.malinowski.bigandyellow.view.mvi.states

sealed class ScreenState {

    object Result : ScreenState()

    object Loading : ScreenState()

    class Error(val error: Throwable) : ScreenState()
}
