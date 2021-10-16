package com.malinowski.bigandyellow.data

import io.reactivex.rxjava3.subjects.PublishSubject

data class Message(
    val message: String,
    val name: String = "James Huxley",
    val emojis: MutableList<Pair<Int, Int>> = mutableListOf(),
    val flow: PublishSubject<Int> = PublishSubject.create()
) {
    init {
        flow.subscribe {
            emojis.add(Pair(it,0))
        }
    }
}