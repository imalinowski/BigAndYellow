package com.malinowski.bigandyellow.model

import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.model.data.User
import io.reactivex.Observable


interface IRepository {

    fun loadTopics(): Observable<List<Topic>>

    fun loadTopic(id: Int): Observable<Topic>

    fun loadUsers(): Observable<List<User>>

}