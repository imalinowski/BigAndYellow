package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersResponse(val result: String, val msg: String, val members: List<User>)

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

@Serializable
data class User(
    @SerialName("user_id") val id: Int,
    @SerialName("full_name") val name: String,
    @SerialName("delivery_email") val email: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
    var status: UserStatus = UserStatus.Offline
) {
    companion object {
        val INSTANCE by lazy {
            User(id = 0, name = "me")
        }
    }
}


