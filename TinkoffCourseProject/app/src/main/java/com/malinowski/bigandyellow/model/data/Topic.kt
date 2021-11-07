package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Topic(
    val name: String,
    @SerialName("max_id") val lastMesID:Int = 0,
)