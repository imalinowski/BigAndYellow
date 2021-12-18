package com.malinowski.bigandyellow.model.data

import kotlinx.serialization.Serializable

@Serializable
sealed class Narrow

@Serializable
data class NarrowStr(val operator: String, val operand: String) : Narrow()

@Serializable
data class NarrowInt(val operator: String, val operand: Int) : Narrow()