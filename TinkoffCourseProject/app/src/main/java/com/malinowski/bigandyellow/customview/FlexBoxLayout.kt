package com.malinowski.bigandyellow.customview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.setPadding

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth =
            MeasureSpec.getSize(widthMeasureSpec) // максимальное доступное пространсвто px

        var widthLine = 0
        var heightLine = 0

        var totalWidth = availableWidth
        var totalHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, totalHeight)
            if (widthLine + child.measuredWidth >= availableWidth) { // перенос детей на следующую строку
                totalHeight += heightLine
                widthLine = 0
                heightLine = 0
            }
            widthLine += child.measuredWidth
            heightLine = maxOf(heightLine, child.measuredHeight)
        }

        if (totalHeight == 0) // totalHeight == 0 если не было переноса по строке
            totalWidth = widthLine

        totalHeight += heightLine

        val resultWidth = resolveSize(totalWidth, widthMeasureSpec)
        val resultHeight = resolveSize(totalHeight, heightMeasureSpec)
        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentTop = 0
        var currentStart = 0
        var heightLine = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (currentStart + child.measuredWidth >= measuredWidth) { // перенос детей на следующую строку
                currentTop += heightLine
                heightLine = 0
                currentStart = 0
            }
            child.layout(
                currentStart,
                currentTop,
                currentStart + child.measuredWidth,
                currentTop + child.measuredHeight
            )
            currentStart += child.measuredWidth
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
