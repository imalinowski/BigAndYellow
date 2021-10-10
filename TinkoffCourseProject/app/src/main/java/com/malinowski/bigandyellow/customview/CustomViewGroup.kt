package com.malinowski.bigandyellow.customview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.malinowski.bigandyellow.R

class CustomViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    init {
        inflate(context, R.layout.custom_view_group_layout, this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        require(childCount == 3) { "Child count should be 3 but was $childCount" }
        val imageView = getChildAt(0)
        val textView = getChildAt(1)
        val flexBoxView = getChildAt(2)

        var totalWidth = 0
        var totalHeight = 0

        measureChildWithMargins(imageView, widthMeasureSpec, 0, heightMeasureSpec, 0)

        val marginLeft = (imageView.layoutParams as MarginLayoutParams).leftMargin
        val marginRight = (imageView.layoutParams as MarginLayoutParams).rightMargin
        totalWidth += imageView.measuredWidth + marginLeft + marginRight
        totalHeight = maxOf(totalHeight, imageView.measuredHeight)

        measureChildWithMargins(
            textView,
            widthMeasureSpec,
            imageView.measuredWidth,
            heightMeasureSpec,
            0
        )
        val textMarginLeft = (textView.layoutParams as MarginLayoutParams).leftMargin
        val textMarginRight = (textView.layoutParams as MarginLayoutParams).rightMargin
        val textWidth = textView.measuredWidth + textMarginLeft + textMarginRight
        totalHeight = maxOf(totalHeight, textView.measuredHeight)

        measureChildWithMargins(
            flexBoxView,
            widthMeasureSpec,
            imageView.measuredWidth,
            heightMeasureSpec,
            totalHeight
        )

        val topMargin = (flexBoxView.layoutParams as MarginLayoutParams).topMargin
        totalHeight += flexBoxView.measuredHeight + topMargin
        totalWidth += maxOf(flexBoxView.measuredWidth, textWidth)

        val resultWidth = resolveSize(totalWidth + paddingRight + paddingLeft, widthMeasureSpec)
        val resultHeight = resolveSize(totalHeight + paddingTop + paddingBottom, heightMeasureSpec)
        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val imageView = getChildAt(0)
        val textView = getChildAt(1)
        val flexBoxView = getChildAt(2)

        imageView.layout(
            paddingLeft,
            paddingTop,
            paddingLeft + imageView.measuredWidth,
            paddingTop + imageView.measuredHeight
        )

        val marginRight = (imageView.layoutParams as MarginLayoutParams).rightMargin

        textView.layout(
            imageView.right + marginRight,
            paddingTop,
            imageView.right + textView.measuredWidth,
            paddingTop + textView.measuredHeight
        )

        val topMargin = (flexBoxView.layoutParams as MarginLayoutParams).topMargin

        flexBoxView.layout(
            imageView.right + marginRight,
            textView.bottom + topMargin,
            imageView.right + flexBoxView.measuredWidth,
            textView.bottom + flexBoxView.measuredHeight
        )
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