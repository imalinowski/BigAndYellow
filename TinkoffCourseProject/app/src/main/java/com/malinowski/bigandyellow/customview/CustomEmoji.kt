package com.malinowski.bigandyellow.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.malinowski.bigandyellow.R

class CustomEmoji @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    var emoji: Emoji = Emoji.SMILING
        set(value) {
            field = value
            invalidate()
        }

    var num = 0
        set(value) {
            if (value < 0) return
            field = value
            requestLayout()
        }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 70f
        textAlign = Paint.Align.CENTER
    }

    private val textBounds = Rect()
    private val textCoordinate = PointF()
    private val tempFontMetrics = Paint.FontMetrics()

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CustomEmoji,
            defStyleAttr,
            defStyleRes
        )

        emoji = when (typedArray.getInt(R.styleable.CustomEmoji_emoji, 1)) {
            1 -> Emoji.SMILING
            2 -> Emoji.WINKING
            3 -> Emoji.HEART
            4 -> Emoji.SAD
            5 -> Emoji.CAT_JOY
            else -> Emoji.SMILING
        }
        num = typedArray.getInt(R.styleable.CustomEmoji_customNum, num)
        textPaint.color =
            typedArray.getColor(R.styleable.CustomEmoji_customTextColor, textPaint.color)
        textPaint.textSize =
            typedArray.getDimension(R.styleable.CustomEmoji_customTextSize, textPaint.textSize)

        setOnClickListener {
            isSelected = !isSelected
            emoji = if(isSelected) Emoji.CAT_JOY else Emoji.HEART
            num = if(isSelected) 2 else 1
        }

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        textPaint.getTextBounds("$emoji $num", 0, "$emoji $num".length, textBounds)

        val textHeight = textBounds.height()
        val textWidth = textBounds.width()

        val totalWidth = textWidth + paddingRight + paddingLeft
        val totalHeight = textHeight + paddingTop + paddingBottom

        val resultWidth = resolveSize(totalWidth, widthMeasureSpec)
        val resultHeight = resolveSize(totalHeight, heightMeasureSpec)

        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        textPaint.getFontMetrics(tempFontMetrics)
        textCoordinate.x = w / 2f
        textCoordinate.y = h / 2f + textBounds.height() / 2 - tempFontMetrics.descent
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + SUPPORTED_DRAWABLE_STATE.size)
        if (isSelected) {
            mergeDrawableStates(drawableState, SUPPORTED_DRAWABLE_STATE)
        }
        return drawableState
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawText("$emoji $num", textCoordinate.x, textCoordinate.y, textPaint)
    }

    companion object {
        private val SUPPORTED_DRAWABLE_STATE = intArrayOf(android.R.attr.state_selected)
    }
}