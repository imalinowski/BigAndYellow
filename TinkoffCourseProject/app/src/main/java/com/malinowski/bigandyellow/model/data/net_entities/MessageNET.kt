package com.malinowski.bigandyellow.model.data.net_entities

import com.malinowski.bigandyellow.model.data.MessageData
import com.malinowski.bigandyellow.model.data.Reaction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class MessageNET(
    @SerialName("id")
    override val id: Int,
    @SerialName("content")
    override val message: String,
    @SerialName("sender_id")
    override val userId: Int,
    @SerialName("sender_full_name")
    override val senderName: String = "",
    @SerialName("timestamp")
    override val timestamp: Int = (Date().time / 1000).toInt(),
    @SerialName("avatar_url")
    override val avatarUrl: String = "",
    @SerialName("sender_email")
    override val senderEmail: String = "",
    @SerialName("stream_id")
    override val streamId: Int = 0,
    @SerialName("subject")
    override val topicName: String = "",
) : MessageData {
    @SerialName("reactions")
    override var reactions: List<Reaction> = listOf() // init all reactions by one

    override fun initEmoji(reactions: List<Reaction>) {
        this.reactions = reactions
    }
}
