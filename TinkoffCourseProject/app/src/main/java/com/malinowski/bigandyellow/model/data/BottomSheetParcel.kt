package com.malinowski.bigandyellow.model.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class BottomSheetResult : Parcelable

@Parcelize
data class AddEmoji(val messageId: Int, val name: String, val unicode: String) : BottomSheetResult()

@Parcelize
data class ChangeTopic(val messageId: Int, val topic: String) : BottomSheetResult()

sealed class MessageIntent: BottomSheetResult() {

    @Parcelize
    data class AddEmoji(val messageId: Int) : MessageIntent()

    @Parcelize
    data class Copy(val messageId: Int) : MessageIntent()

    @Parcelize
    data class Edit(val messageId: Int) : MessageIntent()

    @Parcelize
    data class ChangeTopic(val messageId: Int) : MessageIntent()

    @Parcelize
    data class Delete(val messageId: Int) : MessageIntent()
}