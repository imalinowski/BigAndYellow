package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reaction(
    @Transient var userId: String = "me",
    @SerialName("emoji_code") val smile: String,
    @Transient var num: Int = 1
)