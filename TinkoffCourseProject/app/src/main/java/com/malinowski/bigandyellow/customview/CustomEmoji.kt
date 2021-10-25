package com.malinowski.bigandyellow.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.data.Reaction

class CustomEmoji @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var emoji: String = ":)"
        private set(value) {
            field = value
            invalidate()
        }

    private fun setEmoji(num: Int) {
        resources.getStringArray(R.array.smiles).apply {
            if (num < size) emoji = get(num)
        }
    }

    private var num = 0
        set(value) {
            if (value < 0) return
            field = value
            reaction?.num = num
            requestLayout()
        }

    private var userId = ""
        set(value) {
            reaction?.userId = value
            isSelected = value == "me"
            field = value
        }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = context.resources.getDimension(R.dimen.custom_default_text_size)
        textAlign = Paint.Align.CENTER
    }

    var reaction: Reaction? = null
        private set

    fun setReaction(reaction: Reaction) {
        this.reaction = reaction
        setEmoji(reaction.smile)
        num = reaction.num
        userId = reaction.userId
    }

    var clickCallback = { }

    private val textBounds = Rect()
    private val textCoordinate = PointF()
    private val tempFontMetrics = Paint.FontMetrics()

    private val smiles: Array<String> = resources.getStringArray(R.array.smiles)

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CustomEmoji,
            defStyleAttr,
            defStyleRes
        )
        emoji = smiles[typedArray.getInt(R.styleable.CustomEmoji_emoji, 1)]
        num = typedArray.getInt(R.styleable.CustomEmoji_customNum, num)

        textPaint.color =
            typedArray.getColor(R.styleable.CustomEmoji_customTextColor, textPaint.color)

        textPaint.textSize =
            typedArray.getDimension(R.styleable.CustomEmoji_customTextSize, textPaint.textSize)

        setBackgroundResource(R.drawable.bg_custom_emoji)

        setOnClickListener {
            isSelected = !isSelected
            if (userId == "me") {
                num -= 1
                userId = "other"
            } else {
                num += 1
                userId = "me"
            }
            clickCallback()
        }

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        textPaint.getTextBounds("$emoji $num", 0, "$emoji $num".length, textBounds)

        val textHeight = textBounds.height()
        val textWidth = textBounds.width()

        val totalWidth = textWidth + paddingRight + paddingLeft + DEFAULT_PADDINGS
        val totalHeight = textHeight + paddingTop + paddingBottom + DEFAULT_PADDINGS

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
        private const val DEFAULT_PADDINGS = 50
    }
}