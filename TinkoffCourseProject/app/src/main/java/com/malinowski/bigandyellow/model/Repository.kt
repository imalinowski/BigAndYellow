package com.malinowski.bigandyellow.model

import com.malinowski.bigandyellow.model.data.MessageData
import com.malinowski.bigandyellow.model.data.Stream
import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.User
import com.malinowski.bigandyellow.model.network.ZulipChat
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface Repository {

    fun loadStreams(): Observable<List<Stream>>

    fun loadSubscribedStreams(): Observable<List<Stream>>

    fun loadTopics(streamId: Int): Single<List<Topic>>

    fun loadUsers(): Observable<List<User>>

    fun loadOwnUser(): Single<User>

    fun loadMessages(
        stream: Int,
        topicName: String = "",
        anchor: String = ZulipChat.NEWEST_MES
    ): Observable<List<MessageData>>

    fun loadMessages(
        userEmail: String,
        anchor: String = ZulipChat.NEWEST_MES
    ): Observable<List<MessageData>>

    fun setMessageNum(topicName: String, messageNum: Int): Single<Topic>

    fun sendMessage(
        type: RepositoryImpl.SendType,
        to: String,
        content: String,
        topic: String = ""
    ): Single<Int>

    fun addEmoji(messageId: Int, emojiName: String): Completable

    fun deleteEmoji(messageId: Int, emojiName: String): Completable

    fun deleteMessage(messageId: Int): Completable

    fun editMessageTopic(messageId: Int, topic: String): Completable

    fun editMessage(messageId: Int, content: String): Completable

    fun loadTopics(): Single<List<Topic>>

    fun createStream(name: String, description: String): Completable
}