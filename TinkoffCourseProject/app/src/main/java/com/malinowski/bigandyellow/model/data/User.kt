package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUsersResponse(val result: String, val msg: String, val members: List<User>)

@Serializable
data class User( @SerialName("user_id") val id: Int = 1, @SerialName("full_name") val name: String) {
    companion object {
        val INSTANCE by lazy {
            User(id = 0, name = "me")
        }
    }
}

