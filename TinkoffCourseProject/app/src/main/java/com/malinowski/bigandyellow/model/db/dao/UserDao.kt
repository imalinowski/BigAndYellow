package com.malinowski.bigandyellow.model.db.dao

import androidx.room.*
import com.malinowski.bigandyellow.model.data.USERS_TABLE
import com.malinowski.bigandyellow.model.data.User
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface UserDao {
    @Query("SELECT * FROM $USERS_TABLE")
    fun getAll(): Single<List<User>>

    @Query("SELECT * FROM $USERS_TABLE WHERE is_me = 1")
    fun getOwnUser(): Single<User>

    @Query("SELECT * FROM $USERS_TABLE WHERE id = :id")
    fun getById(id: Int): Single<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: List<User>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: User): Completable

    @Delete
    fun delete(topic: User): Single<Int>
}
