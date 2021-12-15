package com.malinowski.bigandyellow.model.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class BottomSheetResult: Parcelable

@Parcelize
data class AddEmoji(val messageId: Int, val unicode: String, val name: String) : BottomSheetResult()

@Parcelize
data class Copy(val messageId: Int) : BottomSheetResult()

@Parcelize
data class Delete(val messageId: Int) : BottomSheetResult()
