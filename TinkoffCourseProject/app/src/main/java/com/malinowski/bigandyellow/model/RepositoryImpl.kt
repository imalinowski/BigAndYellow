package com.malinowski.bigandyellow.model

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.network.AuthInterceptor
import com.malinowski.bigandyellow.model.network.ZulipChat
import io.reactivex.Completable
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

    override fun loadStreams(): Single<List<Stream>> {
        return service.getStreams()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val streamsJSA =
                    format.decodeFromString<JsonObject>(body.string())[streamsRoute]
                format.decodeFromString<List<Stream>>(streamsJSA.toString())
            }.flatMap { topicsPreload(it) }
    }

    override fun loadSubscribedStreams(): Single<List<Stream>> {
        return service.getSubscribedStreams()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val subscriptionsJSA =
                    format.decodeFromString<JsonObject>(body.string())[subscriptionsRoute]
                format.decodeFromString<List<Stream>>(subscriptionsJSA.toString())
            }.flatMap { topicsPreload(it) }
    }

    private fun topicsPreload(streams: List<Stream>): Single<List<Stream>> {
        val topicLoaders = streams.map { stream ->
            loadTopics(stream.id)
                .subscribeOn(Schedulers.io())
                .map {
                    stream.apply { topics = it.toMutableList() }
                }
        }
        return Single.concatEager(topicLoaders).toList()
    }

    override fun loadTopics(id: Int): Single<List<Topic>> {
        return service.getTopicsInStream(id)
            .subscribeOn(Schedulers.io())
            .map { body ->
                val topicsJSA =
                    format.decodeFromString<JsonObject>(body.string())[topicsRoute]
                format.decodeFromString<List<Topic>>(topicsJSA.toString())
            }
    }

    override fun loadUsers(): Single<List<User>> {
        return service.getUsers()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val membersJSA =
                    format.decodeFromString<JsonObject>(body.string())[membersRoute]
                format.decodeFromString<List<User>>(membersJSA.toString())
            }
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
        val format = Json { ignoreUnknownKeys = true }
        return service.getOwnUser()
            .subscribeOn(Schedulers.io())
            .map { body ->
                format.decodeFromString<User>(body.string())
            }
    }

    fun loadMessages(stream: Int, topic: String): Single<List<Message>> {
        val narrow = listOf(
            NarrowInt("stream", stream),
            NarrowStr("topic", topic)
        ).map {
            Json.encodeToJsonElement(it)
        }.let {
            JsonArray(it).toString()
        }

        return service.getMessages(narrow = narrow)
            .subscribeOn(Schedulers.io())
            .map { body ->
                val jso = Json.decodeFromString<JsonObject>(body.string())[messagesRoute]
                format.decodeFromString<List<Message>>(jso.toString())
            }
    }

    fun loadMessages(userEmail: String): Single<List<Message>> {
        val narrow = listOf(
            NarrowStr("pm-with", userEmail)
        ).map {
            Json.encodeToJsonElement(it)
        }.let {
            JsonArray(it).toString()
        }

        return service.getMessages(narrow = narrow)
            .subscribeOn(Schedulers.io())
            .map { body ->
                val jso = Json.decodeFromString<JsonObject>(body.string())[messagesRoute]
                format.decodeFromString<List<Message>>(jso.toString())
            }
    }

    enum class SendType(val type: String) {
        PRIVATE("private"),
        STREAM("stream")
    }

    fun sendMessage(type: SendType, to: String, content: String, topic: String = ""): Single<Int> =
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