package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.Serializable

@Serializable
data class StreamRequest(
    val name: String,
    val description: String
)