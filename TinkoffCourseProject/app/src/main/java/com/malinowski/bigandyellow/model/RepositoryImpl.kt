package com.malinowski.bigandyellow.model

import android.util.Log
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.malinowski.bigandyellow.App
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.db.AppDatabase
import com.malinowski.bigandyellow.model.network.AuthInterceptor
import com.malinowski.bigandyellow.model.network.ZulipChat
import com.malinowski.bigandyellow.model.network.ZulipChat.Companion.NEWEST_MES
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object RepositoryImpl : Repository {

    private const val URL = "https://tinkoff-android-fall21.zulipchat.com/api/v1/"
    private const val idRoute: String = "id"
    private const val messagesRoute: String = "messages"
    private const val statusRoute: String = "status"
    private const val membersRoute: String = "members"
    private const val topicsRoute: String = "topics"
    private const val subscriptionsRoute: String = "subscriptions"
    private const val streamsRoute: String = "streams"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(AuthInterceptor())
        .build()

    private var retrofit = Retrofit.Builder()
        .baseUrl(URL)
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

    override fun loadStreams(): Observable<List<Stream>> {

        val dbCall = db.streamDao().getAll()
            .observeOn(Schedulers.io())
            .doOnSuccess { Log.d("STREAMS_DB", "$it") }
            .toObservable() //TODO offline topic search

        val netCall = service.getStreams()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val streamsJSA =
                    format.decodeFromString<JsonObject>(body.string())[streamsRoute]
                format.decodeFromString<List<Stream>>(streamsJSA.toString())
            }.doOnSuccess { streams ->
                Log.d("STREAMS_NET", "$streams")
                streams.onEach { it.subscribed = false }
                db.streamDao().insert(streams)
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
            .toObservable() //TODO offline topic search

        val netCall = service.getSubscribedStreams()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val subscriptionsJSA =
                    format.decodeFromString<JsonObject>(body.string())[subscriptionsRoute]
                format.decodeFromString<List<Stream>>(subscriptionsJSA.toString())
            }.doOnSuccess { streams ->
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
        val topicLoaders = streams.map { stream ->
            loadTopics(stream.id)
                .subscribeOn(Schedulers.io())
                .map {
                    stream.apply { topics = it.toMutableList() }
                }
        }
        return Single.concatEager(topicLoaders).toList().toObservable()
    }

    override fun loadTopics(id: Int): Single<List<Topic>> {

        val dbCall = db.topicDao()
            .getTopicsInStream(id)
            .doOnSuccess { Log.d("TOPICS_DB", "stream $id > $it") }

        val netCall = service.getTopicsInStream(id)
            .map { body ->
                val topicsJSA =
                    format.decodeFromString<JsonObject>(body.string())[topicsRoute]
                format.decodeFromString<List<Topic>>(topicsJSA.toString()).apply {
                    map { it.streamId = id }
                }
            }.flatMap { topics ->
                Log.d("TOPICS_NET", "stream $id > $topics")
                db.topicDao().insert(topics)
                messageNumPreload(topics)
            }.onErrorResumeNext {
                Log.e("TOPICS_NET", "${it.message}")
                dbCall
            }

        return netCall.subscribeOn(Schedulers.io())
    }

    private fun messageNumPreload(topics: List<Topic>): Single<List<Topic>> {
        val messageNumLoaders = topics.map { topic ->
            db.topicDao().getTopicByName(topic.name).doOnSuccess {
                topic.messageNum = it.messageNum
            }
        }
        return Single.concatEager(messageNumLoaders).toList()
    }

    override fun loadUsers(): Observable<List<User>> {
        val dbCall = db.userDao()
            .getAll()
            .subscribeOn(Schedulers.io())
        val netCall = service.getUsers()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val membersJSA =
                    format.decodeFromString<JsonObject>(body.string())[membersRoute]
                format.decodeFromString<List<User>>(membersJSA.toString())
            }.doOnSuccess {
                db.userDao().insert(it)
                db.userDao().insert(User.ME)
            }.onErrorResumeNext { error: Throwable ->
                Log.e("USERS_NET", "${error.message}")
                dbCall
            }
        return Single.concat(dbCall, netCall).toObservable()
    }

    fun loadStatus(user: User) =
        service.getPresence(user.id)
            .subscribeOn(Schedulers.io())
            .map { body ->
                val jso = Json.decodeFromString<JsonObject>(body.string())
                    .jsonObject["presence"]?.jsonObject?.get("aggregated")?.jsonObject?.get(
                    statusRoute
                )
                jso?.jsonPrimitive?.content?.let { status ->
                    user.status = UserStatus.decodeFromString(status)
                    user.status
                }
            }.doOnError {
                Log.e("LoadUserStatus", "${user.name} ${it.message}")
            }

    fun loadOwnUser(): Single<User> {
        val dbCall = db.userDao().getOwnUser()
            .subscribeOn(Schedulers.io())

        val netCall = service.getOwnUser()
            .subscribeOn(Schedulers.io())
            .map { body ->
                format.decodeFromString<User>(body.string()).apply {
                    this.isMe = true
                    db.userDao().insert(this)
                }
            }.onErrorResumeNext {
                Log.e("LoadOwnUser", it.message.toString())
                dbCall
            }

        return netCall
    }

    fun setMessageNum(topicName: String, messageNum: Int): Single<Topic> {
        return db.topicDao().getTopicByName(topicName)
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                it.messageNum = messageNum
                db.topicDao().update(it)
            }
    }

    fun loadMessages(
        stream: Int,
        topicName: String,
        anchor: String = NEWEST_MES
    ): Observable<List<Message>> {
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
            .map { clearMessages(it) }
            .flatMap {
                Log.i("MESSAGES_DB", "$it")
                loadReactionsDB(it)
            }

        val netCall = service.getMessages(
            anchor = anchor,
            narrow = narrow
        ).map { body ->
            val jso = Json.decodeFromString<JsonObject>(body.string())[messagesRoute]
            format.decodeFromString<List<Message>>(jso.toString())
        }.map {
            if (anchor != NEWEST_MES) // api send n+1 message
                it.subList(0, it.size - 1)
            else it
        }.doOnSuccess {
            saveMessagesToDB(it)
        }

        val flow = if (anchor == NEWEST_MES) // when paging no db call needed
            Single.concat(dbCall, netCall).toObservable()
        else
            netCall.toObservable()

        return flow.subscribeOn(Schedulers.io())
    }

    fun loadMessages(
        userEmail: String,
        anchor: String = NEWEST_MES
    ): Observable<List<Message>> {
        val narrow = listOf(
            NarrowStr("pm-with", userEmail)
        ).map {
            Json.encodeToJsonElement(it)
        }.let {
            JsonArray(it).toString()
        }

        val dbCall = db.messageDao()
            .getMessages(userEmail)
            .map { clearMessages(it) }
            .flatMap {
                Log.i("MESSAGES_DB", "$it")
                loadReactionsDB(it)
            }

        val netCall = service.getMessages(
            anchor = anchor,
            narrow = narrow
        ).map { body ->
            val jso = Json.decodeFromString<JsonObject>(body.string())[messagesRoute]
            format.decodeFromString<List<Message>>(jso.toString())
        }.map {
            if (anchor != NEWEST_MES)
                it.subList(0, it.size - 1) // api send n+1 message
            else it
        }.doOnSuccess { messages ->
            Log.i("MESSAGES_NET", "$messages")
            saveMessagesToDB(messages)
        }

        val flow = if (anchor == NEWEST_MES) // when paging no db call needed
            Single.concat(dbCall, netCall).toObservable()
        else
            netCall.toObservable()

        return flow.subscribeOn(Schedulers.io())
    }

    private fun loadReactionsDB(messages: List<Message>): Single<List<Message>> {
        val messageReactionLoader = messages.map { message ->
            db.reactionDao().getByMessageId(message.id).map {
                Log.d("REACTIONS_DB", "$it")
                message.apply { initEmoji(it) }
            }.doOnError {
                Log.e("REACTIONS_DB", "${it.message}")
            }
        }
        return Single.concatEager(messageReactionLoader).toList()
    }

    private fun saveMessagesToDB(messages: List<Message>) {
        db.messageDao().insert(messages)
        messages.onEach { message ->
            message.reactions  // reactions save
                .map { it.apply { messageId = message.id } }
                .let { db.reactionDao().insert(it) }
        }

    }

    private fun clearMessages(messages: List<Message>): List<Message> {
        if (messages.size <= 50) return messages
        messages.subList(0, messages.size - 50).onEach { message ->
            Log.i("CLEAR_MESSAGES", db.messageDao().delete(message).toString())
            Log.i(
                "CLEAR_MESSAGES",
                db.reactionDao().deleteByMessageId(message.id).toString()
            )
        }
        return messages.subList(messages.size - 50, messages.size)
    }

    enum class SendType(val type: String) {
        PRIVATE("private"),
        STREAM("stream")
    }

    fun sendMessage(
        type: SendType,
        to: String,
        content: String,
        topic: String = ""
    ): Single<Int> =
        service.sendMessage(type.type, to, content, topic)
            .subscribeOn(Schedulers.io())
            .map { body ->
                Json.decodeFromString<JsonObject>(body.string())[idRoute]
                    ?.jsonPrimitive
                    ?.content.let {
                        it?.toInt()
                    } ?: -1
            }


    fun addEmoji(messageId: Int, emojiName: String): Completable =
        service.addEmojiReaction(messageId, name = emojiName)
            .subscribeOn(Schedulers.io())

    fun deleteEmoji(messageId: Int, emojiName: String): Completable =
        service.deleteEmojiReacction(messageId, name = emojiName)
            .subscribeOn(Schedulers.io())

}