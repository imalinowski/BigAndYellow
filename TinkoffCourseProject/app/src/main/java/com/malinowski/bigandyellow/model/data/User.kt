package com.malinowski.bigandyellow.model.data

data class User(val id: Int = 1, val name: String) {
    companion object {
        val INSTANCE by lazy {
            User(id = 0, name = "me")
        }
    }
}