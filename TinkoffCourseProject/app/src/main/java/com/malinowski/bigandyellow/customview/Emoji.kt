package com.malinowski.bigandyellow.customview

enum class Emoji(private val unicode: String) {
    SMILING("\uD83D\uDE0A"),
    WINKING("\uD83D\uDE09"),
    HEART("\uD83D\uDE0D"),
    SAD("\uD83D\uDE22"),
    CAT_JOY("\uD83D\uDE39");

    override fun toString(): String {
        return unicode
    }
}