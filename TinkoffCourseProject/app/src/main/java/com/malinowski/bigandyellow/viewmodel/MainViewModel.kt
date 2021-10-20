package com.malinowski.bigandyellow.viewmodel

import androidx.lifecycle.ViewModel
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.Reaction
import com.malinowski.bigandyellow.model.data.User

class MainViewModel : ViewModel() {
    // backend in future
    val messages: MutableList<Message> = mutableListOf()

    init {
        messages.addAll(with(User(name = "Nikolay Nekrasov")) {
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

    fun addMessage(message: Message) {
        messages.add(message)
    }
}