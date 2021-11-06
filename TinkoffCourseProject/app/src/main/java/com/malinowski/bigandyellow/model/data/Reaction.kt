package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Reaction(
    @Transient var userId: String = "me",
    @Transient val smile: Int = 0,
    @Transient var num: Int = 1
)