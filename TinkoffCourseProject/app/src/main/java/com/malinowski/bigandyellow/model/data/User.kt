package com.malinowski.bigandyellow.model.data

import androidx.room.*
import androidx.room.Delete
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class UserStatus {
    Online, Offline, Idle;

    companion object {
        fun decodeFromString(string: String): UserStatus = when (string) {
            "active" -> Online
            "idle" -> Idle
            else -> Offline
        }
    }
}

private const val TABLE_NAME = "Users"

@Entity(tableName = TABLE_NAME)
@Serializable
data class User(
    @PrimaryKey
    @SerialName("user_id") val id: Int,
    @SerialName("full_name") val name: String,
    @SerialName("email") val email: String = "",
    @ColumnInfo(name = "avatar_url") @SerialName("avatar_url")
    val avatarUrl: String = "",
    var status: UserStatus = UserStatus.Offline,
    @Transient @ColumnInfo(name = "is_me")
    var isMe: Boolean = false
) {
    companion object {
        var ME: User = User(0, "") // since there is no authorization
    }
}

@Dao
interface UserDao {
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): Single<List<User>>

    @Query("SELECT * FROM $TABLE_NAME WHERE is_me = 1")
    fun getOwnUser(): Single<User>

    @Query("SELECT * FROM $TABLE_NAME WHERE id = :id")
    fun getById(id: Int): Single<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: List<User>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: User): Completable

    @Delete
    fun delete(topic: User): Single<Int>
}


