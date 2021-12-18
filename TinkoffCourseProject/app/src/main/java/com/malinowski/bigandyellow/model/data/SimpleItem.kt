package com.malinowski.bigandyellow.model.data

import android.graphics.Color
import androidx.annotation.ColorInt

data class SimpleItem(
    val name: String,
    @ColorInt val color: Int = Color.parseColor("#000000"),
    val textSize: Float = 21f// sp
)