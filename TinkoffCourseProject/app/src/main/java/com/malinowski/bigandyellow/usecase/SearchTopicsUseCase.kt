package com.malinowski.bigandyellow.usecase

import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.Topic
import io.reactivex.Observable

interface SearchTopicsUseCase : (String) -> Observable<List<Topic>> {

    override fun invoke(searchQuery: String): Observable<List<Topic>>
}

internal class SearchTopicsUseCaseImpl : SearchTopicsUseCase {

    private val dataProvider = Repository

    override fun invoke(searchQuery: String): Observable<List<Topic>> {
        return dataProvider.loadData()
            .map { topics ->
                topics.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
    }

}