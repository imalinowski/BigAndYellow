package com.malinowski.bigandyellow.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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

const val USERS_TABLE = "Users"

@Entity(tableName = USERS_TABLE)
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

