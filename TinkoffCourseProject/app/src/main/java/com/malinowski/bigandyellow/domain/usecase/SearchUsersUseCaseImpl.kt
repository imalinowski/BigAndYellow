package com.malinowski.bigandyellow.domain.usecase

import com.malinowski.bigandyellow.model.RepositoryImpl
import com.malinowski.bigandyellow.model.data.User
import io.reactivex.Observable
import javax.inject.Inject

interface SearchUsersUseCase : (String) -> Observable<List<User>> {

    override fun invoke(searchQuery: String): Observable<List<User>>
}

class SearchUsersUseCaseImpl @Inject constructor() : SearchUsersUseCase {

    @Inject
    lateinit var dataProvider: RepositoryImpl

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