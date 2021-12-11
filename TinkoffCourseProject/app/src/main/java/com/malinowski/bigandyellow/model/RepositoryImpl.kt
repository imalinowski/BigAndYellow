package com.malinowski.bigandyellow.model

import android.util.Log
import com.malinowski.bigandyellow.domain.mapper.MessageNetToDbMapper
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.model.data.db_entities.MessageDB
import com.malinowski.bigandyellow.model.data.net_entities.MessageNET
import com.malinowski.bigandyellow.model.db.AppDatabase
import com.malinowski.bigandyellow.model.network.ZulipChat
import com.malinowski.bigandyellow.model.network.ZulipChat.Companion.NEWEST_MES
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryImpl @Inject constructor() : Repository {

    @Inject
    lateinit var service: ZulipChat

    @Inject
    lateinit var format: Json

    @Inject
    lateinit var db: AppDatabase

    @Inject
    internal lateinit var messageNetToDbMapper: MessageNetToDbMapper

    override fun loadStreams(): Observable<List<Stream>> {

        val dbCall = db.streamDao().getAll()
            .observeOn(Schedulers.io())
            .flatMap { topicsPreload(it) }
            .doOnSuccess { Log.d("STREAMS_DB", "$it") }

        val netCall = service.getStreams()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val streamsJSA =
                    format.decodeFromString<JsonObject>(body.string())[streamsRoute]
                format.decodeFromString<List<Stream>>(streamsJSA.toString())
            }.flatMap { streams ->
                Log.d("STREAMS_NET", "$streams")
                db.streamDao().insert(streams).toSingleDefault(streams)
            }
            .flatMap { topicsPreload(it) }
            .doOnError { error: Throwable ->
                Log.e("STREAMS_NET", "${error.message}")
            }

        return Single.concat(dbCall, netCall).toObservable()
    }

    override fun loadSubscribedStreams(): Observable<List<Stream>> {

        val dbCall = db.streamDao().getSubscribed()
            .observeOn(Schedulers.io())
            .doOnSuccess { Log.d("STREAMS_DB", "$it") }
            .flatMap { topicsPreload(it) }

        val netCall = service.getSubscribedStreams()
            .subscribeOn(Schedulers.io())
            .map { body ->
                val subscriptionsJSA =
                    format.decodeFromString<JsonObject>(body.string())[subscriptionsRoute]
                format.decodeFromString<List<Stream>>(subscriptionsJSA.toString()).onEach {
                    it.subscribed = true
                }
            }.flatMap { streams ->
                Log.d("STREAMS_NET", "$streams")
                db.streamDao().insert(streams).toSingleDefault(streams)
            }
            .flatMap { topicsPreload(it) }
            .doOnError { error: Throwable ->
                Log.e("STREAMS_NET", "${error.message}")
            }

        return Single.concat(dbCall, netCall).toObservable()
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
            }
            .flatMap { topics ->
                Log.d("TOPICS_NET", "stream $id > $topics")
                db.topicDao().insert(topics).toSingleDefault(topics)
            }
            .flatMap { topics -> messageNumPreload(topics) }
            .onErrorResumeNext {
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
            }
            .flatMap { usersStatusPreload(it) }
            .flatMap { users ->
                db.userDao().insert(users)
                    .mergeWith(db.userDao().insert(User.ME))
                    .toSingleDefault(users)
            }.doOnError { error: Throwable ->
                Log.e("USERS_NET", "${error.message}")
            }

        return Single.concat(dbCall, netCall).toObservable()
    }

    private fun usersStatusPreload(users: List<User>): Single<List<User>> {
        val statusLoaders = users.map { user ->
            loadStatus(user)
        }
        return Single.concatEager(statusLoaders).toList()
    }

    private fun loadStatus(user: User) =
        service.getPresence(user.id)
            .subscribeOn(Schedulers.io())
            .map { body ->
                val jso = Json.decodeFromString<JsonObject>(body.string())
                    .jsonObject["presence"]?.jsonObject?.get("aggregated")?.jsonObject?.get(
                    statusRoute
                )
                jso?.jsonPrimitive?.content?.let { status ->
                    Log.d("LoadUserStatus", "${user.name} $status")
                    user.apply { this.status = UserStatus.decodeFromString(status) }
                }
            }.onErrorReturn { user }
            .doOnError {
                Log.e("LoadUserStatus", "${user.name} ${it.message}")
            }

    override fun loadOwnUser(): Single<User> {
        val dbCall = db.userDao().getOwnUser()
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                Log.d("LOAD_OWN_USER_DB", "$it")
            }

        val netCall = service.getOwnUser()
            .subscribeOn(Schedulers.io())
            .map { body ->
                format.decodeFromString<User>(body.string()).apply { isMe = true }
            }
            .flatMap { loadStatus(it) }
            .flatMap {
                Log.d("LOAD_OWN_USER", "DB SAVED")
                db.userDao().insert(it).toSingleDefault(it)
            }.onErrorResumeNext {
                Log.e("LOAD_OWN_USER", it.message.toString())
                dbCall
            }

        return netCall
    }

    override fun setMessageNum(topicName: String, messageNum: Int): Single<Topic> {
        return db.topicDao().getTopicByName(topicName)
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                it.messageNum = messageNum
                db.topicDao().update(it)
            }
    }

    override fun loadMessages(
        stream: Int,
        topicName: String,
        anchor: String
    ): Observable<List<MessageData>> {
        val narrow = listOf(
            NarrowInt("stream", stream),
            NarrowStr("topic", topicName)
        ).map {
            Json.encodeToJsonElement(it)
        }.let {
            JsonArray(it).toString()
        }

        val dbCall: Single<List<MessageData>> = db.messageDao()
            .getMessages(stream, topicName)
            .map { clearMessages(it) }
            .flatMap {
                Log.i("MESSAGES_DB", "$it")
                loadReactionsDB(it)
            }

        val netCall: Single<List<MessageData>> = service.getMessages(
            anchor = anchor,
            narrow = narrow
        ).map { body ->
            val jso = Json.decodeFromString<JsonObject>(body.string())[messagesRoute]
            format.decodeFromString<List<MessageNET>>(jso.toString())
        }.map {
            if (anchor != NEWEST_MES) // api send n+1 message
                it.subList(0, it.size - 1)
            else it
        }.flatMap { messages ->
            Log.i("MESSAGES_NET", "$messages")
            saveMessagesToDB(messages)
        }

        val flow =
            if (anchor == NEWEST_MES) // when paging no db call needed
                Single.concat(dbCall, netCall).toObservable()
            else
                netCall.toObservable()

        // change order for newest to oldest -> add oldest in the end of list
        return flow.subscribeOn(Schedulers.io()).map { it.reversed() }
    }

    override fun loadMessages(
        userEmail: String,
        anchor: String
    ): Observable<List<MessageData>> {
        val narrow = listOf(
            NarrowStr("pm-with", userEmail)
        ).map {
            Json.encodeToJsonElement(it)
        }.let {
            JsonArray(it).toString()
        }

        val dbCall: Single<List<MessageData>> = db.messageDao()
            .getMessages(userEmail)
            .map { clearMessages(it) }
            .flatMap {
                Log.i("MESSAGES_DB", "$it")
                loadReactionsDB(it)
            }

        val netCall: Single<List<MessageData>> = service.getMessages(
            anchor = anchor,
            narrow = narrow
        ).map { body ->
            val jso = Json.decodeFromString<JsonObject>(body.string())[messagesRoute]
            format.decodeFromString<List<MessageNET>>(jso.toString())
        }.map {
            if (anchor != NEWEST_MES)
                it.subList(0, it.size - 1) // api send n+1 message
            else it
        }.flatMap { messages ->
            Log.i("MESSAGES_NET", "$messages")
            saveMessagesToDB(messages)
        }

        val flow = if (anchor == NEWEST_MES) // when paging no db call needed
            Single.concat(dbCall, netCall).toObservable()
        else
            netCall.toObservable()
        //change order for newest to oldest -> add oldest in the end of list
        return flow.subscribeOn(Schedulers.io()).map { it.reversed() }
    }

    private fun loadReactionsDB(messages: List<MessageDB>): Single<List<MessageDB>> {
        val messageReactionLoader = messages.map { message ->
            db.reactionDao().getByMessageId(message.id).map {
                Log.d(
                    "REACTIONS_DB",
                    "messageId > ${message.id} ${message.message} reactions > $it"
                )
                message.apply { initEmoji(it) }
            }.doOnError {
                Log.e("REACTIONS_DB", "${it.message}")
            }
        }
        return Single.concatEager(messageReactionLoader).toList()
    }

    private fun saveMessagesToDB(messages: List<MessageNET>): Single<List<MessageData>> {
        var flow = db.messageDao().insert(messageNetToDbMapper(messages))
        messages.onEach { message ->
            flow = Completable.mergeArrayDelayError(flow,
                db.reactionDao().deleteByMessageId(message.id), // update deleted messages
                message.reactions // save reactions
                    .map { it.apply { messageId = message.id } }
                    .let { db.reactionDao().insert(it) }
            )
        }
        return flow.toSingleDefault(messages)
    }

    private fun clearMessages(messages: List<MessageDB>): List<MessageDB> {
        if (messages.size <= 50) return messages
        messages.subList(0, messages.size - 50).onEach { message ->
            db.messageDao().delete(message)
            db.reactionDao().deleteByMessageId(message.id)
        }
        return messages.subList(messages.size - 50, messages.size)
    }

    enum class SendType(val type: String) {
        PRIVATE("private"),
        STREAM("stream")
    }

    override fun sendMessage(
        type: SendType,
        to: String,
        content: String,
        topic: String
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


    override fun addEmoji(messageId: Int, emojiName: String): Completable =
        service.addEmojiReaction(messageId, name = emojiName)
            .subscribeOn(Schedulers.io())

    override fun deleteEmoji(messageId: Int, emojiName: String): Completable =
        service.deleteEmojiReacction(messageId, name = emojiName)
            .subscribeOn(Schedulers.io())

    companion object {
        private const val idRoute: String = "id"
        private const val messagesRoute: String = "messages"
        private const val statusRoute: String = "status"
        private const val membersRoute: String = "members"
        private const val topicsRoute: String = "topics"
        private const val subscriptionsRoute: String = "subscriptions"
        private const val streamsRoute: String = "streams"
    }

}