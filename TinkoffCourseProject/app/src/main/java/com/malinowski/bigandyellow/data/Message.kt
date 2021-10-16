package com.malinowski.bigandyellow.data

import io.reactivex.rxjava3.subjects.PublishSubject

data class Message(
    val message: String,
    val name: String = "James Huxley",
    val emojis: MutableList<Reaction> = mutableListOf(),
    val flow: PublishSubject<Reaction> = PublishSubject.create()
) {
    init {
        flow.subscribe {
            emojis.add(it)
        }
    }
}