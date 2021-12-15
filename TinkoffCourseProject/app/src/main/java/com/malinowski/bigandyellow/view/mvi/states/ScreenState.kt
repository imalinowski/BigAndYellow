package com.malinowski.bigandyellow.view.mvi.states

sealed class ScreenState {

    class Result(val text: String = "") : ScreenState()

    object Loading : ScreenState()

    class Error(val error: Throwable) : ScreenState()
}
