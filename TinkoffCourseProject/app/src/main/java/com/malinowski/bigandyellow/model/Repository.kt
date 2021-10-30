package com.malinowski.bigandyellow.model

import com.malinowski.bigandyellow.model.data.*
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object Repository : IRepository {
    // backend in future
    private val topics: MutableList<Topic> = mutableListOf()

    override fun loadData(): Observable<List<Topic>> =
        Observable.fromArray(topics.toList()).subscribeOn(Schedulers.io())
            .delay(1000, TimeUnit.MILLISECONDS)

    override fun loadItem(id: Int): Observable<Topic> =
        Observable.just(topics[id]).subscribeOn(Schedulers.io())
            .delay(1000, TimeUnit.MILLISECONDS)

    init {
        topics.addAll(
            mutableListOf(
                Topic(
                    "#general", 0, true,
                    chats = mutableListOf(
                        Chat("Literature"),
                        Chat("Testing"),
                        Chat("Bruh"),
                    )
                ),
                Topic(
                    "#development", 1, true, chats = mutableListOf(
                        Chat("Kotlin")
                    )
                ),
                Topic("#design", 2, true),
                Topic("#PR", 3),
                Topic(
                    "#unsubscribed stream", 4, chats = mutableListOf(
                        Chat("SomeChat")
                    )
                ),
            )
        )
        topics[0].chats[0].messages.addAll(with(User(name = "Nikolay Nekrasov")) {
            mutableListOf(
                Message(1, "Вчерашний день, часу в шестом,\nЗашел я на Сенную;", this),
                Message(2, "Там били женщину кнутом,\nКрестьянку молодую.", this),
                Message(3, "Ни звука из ее груди,\nЛишь бич свистал, играя...", this),
                Message(
                    4, "И Музе я сказал: «Гляди!\nСестра твоя родная!».", this,
                    mutableListOf(Reaction("other", 34, 3))
                ),
            )
        })
    }

}