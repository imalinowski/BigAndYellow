package com.malinowski.bigandyellow.domain.usecase

import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.Topic
import com.malinowski.bigandyellow.view.mvi.events.ChatEvent.SearchTopics
import io.reactivex.Observable
import javax.inject.Inject

interface SearchTopicsUseCase : (SearchTopics) -> Observable<List<Topic>> {

    override fun invoke(search: SearchTopics): Observable<List<Topic>>
}

internal class SearchTopicsUseCaseImpl @Inject constructor(
    val dataProvider: Repository
) : SearchTopicsUseCase {

    override fun invoke(search: SearchTopics): Observable<List<Topic>> {
        return dataProvider.loadTopics(search.streamId)
            .map { topics ->
                if (search.query.isNotEmpty())
                    topics.filter { topic -> topic.name.contains(search.query, ignoreCase = true) }
                else
                    listOf()
            }.toObservable()
    }
}