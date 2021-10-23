package com.malinowski.bigandyellow.model

import com.malinowski.bigandyellow.model.data.*

object DataProvider {
    // backend in future
    private val topics: MutableList<Topic> = mutableListOf()

    init {
        topics.addAll(mutableListOf(
            Topic(
                "#general", true,
                chats = mutableListOf(
                    Chat("Literature"),
                    Chat("Testing"),
                    Chat("Bruh"),
                )
            ),
            Topic("#development", true),
            Topic("#design", true),
            Topic("#pr", true),
        ))
        topics[0].chats[0].messages.addAll(with(User(name = "Nikolay Nekrasov")) {
            mutableListOf(
                Message("Вчерашний день, часу в шестом,\nЗашел я на Сенную;", this),
                Message("Там били женщину кнутом,\nКрестьянку молодую.", this),
                Message("Ни звука из ее груди,\nЛишь бич свистал, играя...", this),
                Message(
                    "И Музе я сказал: «Гляди!\nСестра твоя родная!».", this,
                    mutableListOf(Reaction("other", 34, 3))
                ),
            )
        })
    }

    fun getTopic(num: Int) = topics[num]

    fun getTopicsNames(): List<String> =
        topics.map {
            it.name
        }

    fun getTopicsSize() = topics.size
}