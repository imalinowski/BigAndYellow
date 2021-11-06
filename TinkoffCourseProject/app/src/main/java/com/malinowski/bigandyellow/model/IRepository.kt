package com.malinowski.bigandyellow.model

import com.malinowski.bigandyellow.model.data.Stream
import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.User
import io.reactivex.Single


interface IRepository {

    fun loadStreams(): Single<List<Stream>>

    fun loadSubscribedStreams(): Single<List<Stream>>

    fun loadTopics(id: Int): Single<List<Topic>>

    fun loadUsers(): Single<List<User>>

}