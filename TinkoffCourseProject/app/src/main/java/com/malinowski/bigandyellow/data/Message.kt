package com.malinowski.bigandyellow.data

import io.reactivex.rxjava3.subjects.PublishSubject

data class Message(
    val message: String,
    val name: String = "Aldous Huxley",
    val reactions: MutableList<Reaction> = mutableListOf(),
    val flow: PublishSubject<Reaction> = PublishSubject.create()
) {
    init {
        flow.subscribe {
            reactions.add(it)
        }
    }
}