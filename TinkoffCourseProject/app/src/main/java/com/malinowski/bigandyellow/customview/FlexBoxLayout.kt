package com.malinowski.bigandyellow.customview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.isVisible
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.data.Reaction

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var paddingRows: Int
    var paddingColumns: Int
    var plus = ImageButton(context).apply {
        setImageResource(R.drawable.ic_plus)
        setBackgroundResource(R.drawable.bg_gray_round)
        visibility = GONE
    }

    fun addEmoji(reaction: Reaction) {
        addView(CustomEmoji(context).apply {
            setReaction(reaction)
        }, 0)
        plus.visibility = View.VISIBLE
    }

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.FlexBoxLayout,
            defStyleAttr,
            defStyleRes
        )
        paddingRows =
            typedArray.getDimension(R.styleable.FlexBoxLayout_paddingRows, 20f).toInt()
        paddingColumns =
            typedArray.getDimension(R.styleable.FlexBoxLayout_paddingColumns, 20f).toInt()
        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (childCount == 0 || getChildAt(childCount - 1) !== plus) plus.apply {
            addView(this)
            layoutParams.apply {
                height = 100
                width = 100
            }
        }
        if (childCount > 1 && getChildAt(childCount - 2) is CustomEmoji)
            plus.visibility = VISIBLE

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth =
            MeasureSpec.getSize(widthMeasureSpec) // максимальное доступное пространсвто px

        var widthLine = paddingRight + paddingLeft
        var heightLine = 0

        var totalWidth = availableWidth
        var totalHeight = paddingTop + paddingBottom

        var sumHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, totalHeight)
            if (widthLine + child.measuredWidth >= availableWidth) { // перенос детей на следующую строку
                totalHeight += heightLine + paddingRows
                widthLine = paddingRight + paddingLeft
                heightLine = 0
            }
            widthLine += child.measuredWidth + paddingColumns
            heightLine =
                if (child.isVisible) maxOf(heightLine, child.measuredHeight) else heightLine
            sumHeight += child.measuredHeight
        }

        if (totalHeight == paddingTop + paddingBottom) // нет переноса строки
            totalWidth = widthLine
        totalHeight += heightLine

        if (plus.isVisible) plus.layoutParams.apply {
            height = sumHeight / childCount // average height
            width = height
            totalHeight += height
            getChildAt(childCount - 1).layoutParams = this
        }

        val resultWidth = resolveSize(totalWidth, widthMeasureSpec)
        val resultHeight = resolveSize(totalHeight, heightMeasureSpec)
        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentTop = paddingTop
        var currentStart = paddingLeft
        var heightLine = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (currentStart + child.measuredWidth + paddingRight >= measuredWidth) { // перенос детей на следующую строку
                currentTop += heightLine + paddingRows
                heightLine = 0
                currentStart = paddingLeft
            }
            child.layout(
                currentStart,
                currentTop,
                currentStart + child.measuredWidth,
                currentTop + child.measuredHeight
            )
            currentStart += child.measuredWidth + paddingColumns
            heightLine = maxOf(heightLine, child.measuredHeight)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        plus.visibility = View.GONE
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is MarginLayoutParams
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }
}