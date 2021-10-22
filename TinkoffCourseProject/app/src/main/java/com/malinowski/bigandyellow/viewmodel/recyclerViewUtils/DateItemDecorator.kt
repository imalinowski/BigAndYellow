package com.malinowski.bigandyellow.viewmodel.recyclerViewUtils

import android.graphics.Canvas
import android.graphics.Color.parseColor
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class DateItemDecorator :
    RecyclerView.ItemDecoration() {

    private val textBounds = Rect()
    private val textCoordinate = PointF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = parseColor("#999999")
        textSize = 40f
        textAlign = Paint.Align.CENTER
        text
    }
    private val padding = 45

    private val text: String = "1 Фев"

    override fun getItemOffsets(
        rect: Rect,
        view: View,
        parent: RecyclerView,
        s: RecyclerView.State
    ) {
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val position = parent.getChildAdapterPosition(view)
            .let { if (it == RecyclerView.NO_POSITION) return else it }
        rect.bottom =
            if (position % 2 == 0) 2
            else abs(textBounds.height()) * 2 + padding
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.children.forEach { view ->
            val position = parent.getChildAdapterPosition(view)
                .let { if (it == RecyclerView.NO_POSITION) return else it }
            if (position % 2 != 0) {
                textCoordinate.x = parent.width / 2f
                textCoordinate.y = view.bottom.toFloat() + textBounds.height() / 2 + padding
                if (textCoordinate.y > parent.height - parent.paddingBottom)
                    return@forEach
                canvas.drawRoundRect(
                    textCoordinate.x - textBounds.width() / 2 - padding,
                    textCoordinate.y - textBounds.height() * 1.5f,
                    textCoordinate.x + textBounds.width() / 2 + padding,
                    textCoordinate.y + textBounds.height() / 2,
                    50f, 50f,
                    Paint().apply { color = parseColor("#070707") })
                canvas.drawText(
                    text, textCoordinate.x, textCoordinate.y, textPaint
                )
            }
        }
    }

}