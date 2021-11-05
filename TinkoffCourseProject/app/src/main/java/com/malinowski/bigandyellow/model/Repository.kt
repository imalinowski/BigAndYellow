package com.malinowski.bigandyellow.model

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.malinowski.bigandyellow.model.data.*
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit


object Repository : IRepository {
    // backend in future
    private val topics: MutableList<Topic> = mutableListOf()

    private val users: MutableList<User> = mutableListOf()

    private val client = OkHttpClient.Builder().addInterceptor(
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    ).build()

    private var retrofit = Retrofit.Builder()
        .baseUrl("https://tinkoff-android-fall21.zulipchat.com/api/v1/") // http://192.168.0.21:8081/
        .client(client)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    private var service = retrofit.create(ZulipChat::class.java)

    override fun loadTopics(): Observable<List<Topic>> =
        Observable.fromArray(topics.toList()).subscribeOn(Schedulers.io())
            .delay(1000, TimeUnit.MILLISECONDS)

    override fun loadTopic(id: Int): Observable<Topic> =
        Observable.just(topics[id]).subscribeOn(Schedulers.io())
            .delay(1000, TimeUnit.MILLISECONDS)

    override fun loadUsers(): Observable<List<User>> {
        val format = Json { ignoreUnknownKeys = true }
        return service.getUsers().observeOn(Schedulers.io())
            .map {
                format.decodeFromString<GetUsersResponse>(it.string()).members
            }.toObservable()/*.subscribe({ list ->
            Log.i("BigAndYellow", list.size.toString())
        }, {
            Log.e("BigAndYellow", it.message.toString())
        }).let { }*/
        //return Observable.just(users.toList()).subscribeOn(Schedulers.io())//.toObservable()
    }

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

        users.addAll(
            mutableListOf(
                User(1, "Taylan Colon"),
                User(2, "Priya Roth"),
                User(3, "Luisa Pennington"),
                User(4, "Olli Cairns"),
                User(5, "Jimmy Lee"),
                User(6, "Murat Coffey"),
            )
        )
    }

    class ExpectedError : Throwable("Expected Random Error")

}