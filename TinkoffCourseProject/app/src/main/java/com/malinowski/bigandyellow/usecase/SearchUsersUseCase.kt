package com.malinowski.bigandyellow.usecase

import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.User
import io.reactivex.Observable

interface ISearchUsersUseCase : (String) -> Observable<List<User>> {

    override fun invoke(searchQuery: String): Observable<List<User>>
}

internal class SearchUsersUseCase : ISearchUsersUseCase {

    private val dataProvider = Repository

    override fun invoke(searchQuery: String): Observable<List<User>> {
        return dataProvider.loadUsers().toObservable()
            .map { users ->
                if (searchQuery.isNotEmpty())
                    users.filter { user -> user.name.contains(searchQuery, ignoreCase = true) }
                else
                    users
            }
    }
}