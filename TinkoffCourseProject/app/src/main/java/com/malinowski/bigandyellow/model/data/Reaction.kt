package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Reaction(var userId: String = "me", val smile: Int, var num: Int)