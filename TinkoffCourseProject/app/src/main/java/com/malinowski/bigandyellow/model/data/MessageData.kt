package com.malinowski.bigandyellow.model.data

interface MessageData {
    val id: Int
    val message: String
    val userId: Int
    val senderName: String
    val timestamp: Int
    val avatarUrl: String
    val senderEmail: String
    val streamId: Int
    val topicName: String
    var reactions: List<Reaction>
    fun initEmoji(reactions: List<Reaction>)
}