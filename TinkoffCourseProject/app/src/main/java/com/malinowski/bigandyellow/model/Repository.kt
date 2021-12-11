package com.malinowski.bigandyellow.model

import com.malinowski.bigandyellow.model.data.Stream
import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.User
import io.reactivex.Observable
import io.reactivex.Single

interface Repository { // todo inject repository instead of impl

    fun loadStreams(): Observable<List<Stream>>

    fun loadSubscribedStreams(): Observable<List<Stream>>

    fun loadTopics(id: Int): Single<List<Topic>>

    fun loadUsers(): Observable<List<User>>

    fun loadOwnUser(): Single<User>

}