package com.malinowski.bigandyellow.model.data.net_entities

import kotlinx.serialization.Serializable

@Serializable
data class StreamRequest(
    val name: String,
    val description: String
)