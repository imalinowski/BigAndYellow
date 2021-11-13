package com.malinowski.bigandyellow.model

import com.malinowski.bigandyellow.model.data.Stream
import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.User
import io.reactivex.Observable
import io.reactivex.Single


interface IRepository {

    fun loadStreams(): Observable<List<Stream>>

    fun loadSubscribedStreams(): Observable<List<Stream>>

    fun loadTopics(id: Int): Observable<List<Topic>>

    fun loadUsers(): Observable<List<User>>

}