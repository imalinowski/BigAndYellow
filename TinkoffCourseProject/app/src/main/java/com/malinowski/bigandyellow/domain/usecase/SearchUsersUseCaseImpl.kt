package com.malinowski.bigandyellow.domain.usecase

import com.malinowski.bigandyellow.model.Repository
import com.malinowski.bigandyellow.model.data.User
import io.reactivex.Observable
import javax.inject.Inject

interface SearchUsersUseCase : (String) -> Observable<List<User>> {

    override fun invoke(searchQuery: String): Observable<List<User>>
}

internal class SearchUsersUseCaseImpl @Inject constructor() : SearchUsersUseCase {

    @Inject
    lateinit var dataProvider: Repository

    override fun invoke(searchQuery: String): Observable<List<User>> {
        return dataProvider.loadUsers()
            .map { users ->
                if (searchQuery.isNotEmpty())
                    users.filter { user -> user.name.contains(searchQuery, ignoreCase = true) }
                else
                    users
            }
    }
}