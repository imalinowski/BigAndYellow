package com.malinowski.bigandyellow.view.customview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.malinowski.bigandyellow.R


class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var paddingRows: Int
    var paddingColumns: Int

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

    var sumHeight = 0
        private set

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth =
            MeasureSpec.getSize(widthMeasureSpec) // максимальное доступное пространсвто px

        var widthLine = paddingRight + paddingLeft
        var heightLine = 0

        var totalWidth = availableWidth
        var totalHeight = paddingTop + paddingBottom

        sumHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, totalHeight)
            if (widthLine + child.measuredWidth >= availableWidth) { // перенос детей на следующую строку
                totalHeight += heightLine + paddingRows
                widthLine = paddingRight + paddingLeft
                heightLine = 0
            }
            widthLine += child.measuredWidth + paddingColumns
            if (child.isVisible)
                heightLine = maxOf(heightLine, child.measuredHeight)
            sumHeight += child.measuredHeight
        }

        if (totalHeight == paddingTop + paddingBottom) // нет переноса строки
            totalWidth = widthLine
        totalHeight += heightLine + paddingRows * 2

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