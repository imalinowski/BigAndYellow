package com.malinowski.bigandyellow.model

import com.malinowski.bigandyellow.model.data.Topic
import io.reactivex.Observable


interface IRepository {

    fun loadData(): Observable<List<Topic>>

}