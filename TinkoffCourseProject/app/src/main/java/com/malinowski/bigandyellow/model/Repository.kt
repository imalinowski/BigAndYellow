package com.malinowski.bigandyellow.model

import android.util.Log
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.malinowski.bigandyellow.App
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.db.AppDatabase
import com.malinowski.bigandyellow.model.network.AuthInterceptor
import com.malinowski.bigandyellow.model.network.ZulipChat
import com.malinowski.bigandyellow.model.network.ZulipChat.NarrowInt
import com.malinowski.bigandyellow.model.network.ZulipChat.NarrowStr
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object Repository : IRepository {

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(AuthInterceptor())
        .build()

    private var retrofit = Retrofit.Builder()
        .baseUrl("https://tinkoff-android-fall21.zulipchat.com/api/v1/") // http://192.168.0.21:8081/
        .client(client)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    private var service = retrofit.create(ZulipChat::class.java)
    private val format = Json { ignoreUnknownKeys = true }

    private val db = Room.databaseBuilder(
        App.appContext,
        AppDatabase::class.java, AppDatabase.DB_NAME
    ).build()

    private val compositeDisposable = CompositeDisposable()

    override fun loadStreams(): Observable<List<Stream>> {

        val dbCall = db.streamDao().getAll()
            .observeOn(Schedulers.io())
            .doOnSuccess { Log.d("STREAMS_DB", "$it") }
            .toObservable()

        val netCall = service.getStreams()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val streamsJSA =
                    format.decodeFromString<JsonObject>(body.string())["streams"]
                format.decodeFromString<List<Stream>>(streamsJSA.toString())
            }.doOnSuccess {
                Log.d("STREAMS_NET", "$it")
                db.streamDao().insert(it)
            }
            .toObservable()
            .flatMap { topicsPreload(it) }
            .onErrorResumeNext { error: Throwable ->
                Log.e("STREAMS_NET", "${error.message}")
                dbCall
            }

        return Observable.concat(dbCall, netCall)
    }

    override fun loadSubscribedStreams(): Observable<List<Stream>> {

        val dbCall = db.streamDao().getSubscribed()
            .observeOn(Schedulers.io())
            .toObservable()

        val netCall = service.getSubscribedStreams()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val subscriptionsJSA =
                    format.decodeFromString<JsonObject>(body.string())["subscriptions"]
                format.decodeFromString<List<Stream>>(subscriptionsJSA.toString())
            }.doOnSuccess { streams ->
                streams.onEach { it.subscribed = true }
                db.streamDao().insert(streams)
            }.toObservable()
            .flatMap { topicsPreload(it) }
            .onErrorResumeNext { error: Throwable ->
                Log.e("STREAMS_NET", "${error.message}")
                dbCall
            }

        return Observable.concat(dbCall, netCall)
    }

    private fun topicsPreload(streams: List<Stream>): Observable<List<Stream>> {
        val subject = PublishSubject.create<List<Stream>>()
        var count = 0
        streams.onEach { stream ->
            loadTopics(stream.id)
                .doFinally {
                    count += 1
                    if (count % streams.size == 0)
                        subject.onNext(streams)
                }.subscribe({
                    stream.topics = it.toMutableList()
                }, { Log.e("TopicsPreload", it.message.toString()) }
                ).addTo(compositeDisposable)
        }
        if (streams.isEmpty()) subject.onNext(streams)
        return subject
    }

    override fun loadTopics(id: Int): Observable<List<Topic>> {

        val dbCall = db.topicDao()
            .getTopicsInStream(id)
            .doOnSuccess { Log.d("TOPICS_DB", "stream $id > $it") }

        val netCall = service.getTopicsInStream(id)
            .map { body ->
                val topicsJSA =
                    format.decodeFromString<JsonObject>(body.string())["topics"]
                format.decodeFromString<List<Topic>>(topicsJSA.toString()).apply {
                    map { it.streamId = id }
                }
            }.flatMap { topics ->
                Log.d("TOPICS_NET", "stream $id > $topics")
                db.topicDao().insert(topics)
                messageNumPreload(topics)
                Single.just(topics)
            }

        return Single.concat(dbCall, netCall)
            .toObservable()
            .subscribeOn(Schedulers.io())
    }

    private fun messageNumPreload(topics: List<Topic>) {
        topics.onEach { topic ->
            db.topicDao().getTopicByName(topic.name).subscribeBy(
                onSuccess = { topic.messageNum = it.messageNum },
                onError = { Log.e("TOPICS_NET", "topic ${topic.name} error message preload") }
            ).addTo(compositeDisposable)
        }
    }

    override fun loadUsers(): Single<List<User>> {
        return service.getUsers()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val membersJSA =
                    format.decodeFromString<JsonObject>(body.string())["members"]
                format.decodeFromString<List<User>>(membersJSA.toString())
            }
    }

    fun loadStatus(user: User) =
        service.getPresence(user.id)
            .subscribeOn(Schedulers.io())
            .map { body ->
                val jso = Json.decodeFromString<JsonObject>(body.string())
                    .jsonObject["presence"]?.jsonObject?.get("aggregated")?.jsonObject?.get("status")
                jso?.jsonPrimitive?.content?.let { status ->
                    user.status = UserStatus.decodeFromString(status)
                    user.status
                }
            }.doOnError {
                Log.e("LoadUserStatus", "${user.name} ${it.message}")
            }

    fun loadOwnUser() {
        db.userDao().getOwnUser()
            .subscribeOn(Schedulers.io())
            .subscribe(
                { User.ME = it },
                { Log.e("LoadOwnUser", it.message.toString()) }
            ).addTo(compositeDisposable)

        service.getOwnUser()
            .subscribeOn(Schedulers.io())
            .subscribe(
                { body ->
                    User.ME = format.decodeFromString(body.string())
                    db.userDao().insert(User.ME.apply { isMe = true })
                },
                { Log.e("LoadOwnUser", it.message.toString()) }
            ).addTo(compositeDisposable)
    }

    private fun setMessageNum(topicName: String, messageNum: Int) {
        db.topicDao().getTopicByName(topicName).subscribeBy(
            onSuccess = {
                it.messageNum = messageNum
                db.topicDao().update(it)
            },
            onError = { Log.e("LOAD_TOPIC_BY_NAME", "${it.message}") }
        ).addTo(compositeDisposable)
    }

    fun loadMessages(stream: Int, topicName: String): Observable<List<Message>> {
        val narrow = listOf(
            NarrowInt("stream", stream),
            NarrowStr("topic", topicName)
        ).map {
            Json.encodeToJsonElement(it)
        }.let {
            JsonArray(it).toString()
        }

        val dbCall = db.messageDao()
            .getMessages(stream, topicName)
            .doOnSuccess { Log.i("MESSAGES_DB", "$it") }

        val netCall = service.getMessages(narrow = narrow)
            .subscribeOn(Schedulers.io())
            .map { body ->
                val jso = Json.decodeFromString<JsonObject>(body.string())["messages"]
                format.decodeFromString<List<Message>>(jso.toString())
            }.doOnSuccess {
                setMessageNum(topicName, it.size)
                db.messageDao().insert(it)
            }

        return Single.concat(dbCall, netCall)
            .subscribeOn(Schedulers.io())
            .toObservable()
    }

    fun loadMessages(userEmail: String): Observable<List<Message>> {
        val narrow = listOf(
            NarrowStr("pm-with", userEmail)
        ).map {
            Json.encodeToJsonElement(it)
        }.let {
            JsonArray(it).toString()
        }

        val dbCall = db.messageDao()
            .getMessages(userEmail)
            .doOnSuccess { Log.i("MESSAGES_DB", "$it") }

        val netCall = service.getMessages(narrow = narrow)
            .map { body ->
                val jso = Json.decodeFromString<JsonObject>(body.string())["messages"]
                format.decodeFromString<List<Message>>(jso.toString())
            }.doOnSuccess {
                Log.i("MESSAGES_NET", "$it")
                db.messageDao().insert(it)
            }

        return Single.concat(dbCall, netCall)
            .subscribeOn(Schedulers.io())
            .toObservable()
    }

    enum class SendType(val type: String) {
        PRIVATE("private"),
        STREAM("stream")
    }

    fun sendMessage(type: SendType, to: String, content: String, topic: String = ""): Single<Int> =
        service.sendMessage(type.type, to, content, topic)
            .subscribeOn(Schedulers.io())
            .map { body ->
                Json.decodeFromString<JsonObject>(body.string())["id"]
                    ?.jsonPrimitive
                    ?.content.let {
                        it?.toInt()
                    } ?: -1
            }


    fun addEmoji(messageId: Int, emojiName: String): Completable {
        val complete = CompletableSubject.create()
        service.addEmojiReaction(messageId, name = emojiName)
            .subscribeOn(Schedulers.io())
            .subscribe(
                { complete.onComplete() },
                { e -> complete.onError(e) }
            ).addTo(compositeDisposable)
        return complete
    }

    fun deleteEmoji(messageId: Int, emojiName: String): Completable {
        val complete = CompletableSubject.create()
        service.deleteEmojiReacction(messageId, name = emojiName)
            .subscribeOn(Schedulers.io())
            .subscribe(
                { complete.onComplete() },
                { e -> complete.onError(e) }
            ).addTo(compositeDisposable)
        return complete
    }

}