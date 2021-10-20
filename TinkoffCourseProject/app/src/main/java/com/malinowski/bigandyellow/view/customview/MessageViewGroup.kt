package com.malinowski.bigandyellow.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.model.data.Message
import com.malinowski.bigandyellow.model.data.User
import io.reactivex.rxjava3.disposables.Disposable

class MessageViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    init {
        inflate(context, R.layout.message_view_group_layout, this)
    }

    private val message: TextView = findViewById(R.id.message)
    private val name: TextView = findViewById(R.id.name)
    private var subscription: Disposable? = null
    var messageData: Message? = null

    fun setMessage(message: Message) {
        this.messageData = message
        this.message.text = message.message
        this.name.text = message.user.name
        if (message.user === User.INSTANCE) {
            name.visibility = GONE
            getChildAt(0).visibility = GONE
            getChildAt(1).setBackgroundResource(R.drawable.bg_green_round)

        } else {
            name.visibility = VISIBLE
            getChildAt(0).visibility = VISIBLE
            getChildAt(1).setBackgroundResource(R.drawable.bg_gray_round)
        }
        (getChildAt(2) as FlexBoxLayout).apply {
            removeAllViews()
            for (reaction in message.reactions) {
                addEmoji(reaction)
            }
            subscription?.dispose()
            subscription = message.flow.subscribe {
                addEmoji(it)
            }
        }
    }

    fun setMessageOnLongClick(callback: () -> Unit) {
        getChildAt(1).setOnLongClickListener {
            callback()
            true
        }
        (getChildAt(2) as FlexBoxLayout).plus.setOnClickListener {
            callback()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        require(childCount == 3) { "Child count should be 3 but was $childCount" }
        val imageView = getChildAt(0)
        val textBox = getChildAt(1)
        val flexBoxView = getChildAt(2)

        var totalWidth = 0
        var totalHeight = 0

        /*setPadding( // max width of message
            paddingLeft,
            paddingTop,
            maxOf(paddingRight, MeasureSpec.getSize(widthMeasureSpec) / 6),
            paddingBottom
        )*/

        measureChildWithMargins(
            imageView,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            0
        )
        val marginLeft = (imageView.layoutParams as MarginLayoutParams).leftMargin
        val marginRight = (imageView.layoutParams as MarginLayoutParams).rightMargin
        totalWidth += imageView.measuredWidth + marginLeft + marginRight
        totalHeight = maxOf(totalHeight, imageView.measuredHeight)

        measureChildWithMargins(
            textBox,
            widthMeasureSpec,
            imageView.measuredWidth,
            heightMeasureSpec,
            0
        )
        val textMarginLeft = (textBox.layoutParams as MarginLayoutParams).leftMargin
        val textMarginRight = (textBox.layoutParams as MarginLayoutParams).rightMargin
        val textWidth = textBox.measuredWidth + textMarginLeft + textMarginRight
        totalHeight = maxOf(totalHeight, textBox.measuredHeight)

        measureChildWithMargins(
            flexBoxView,
            widthMeasureSpec,
            imageView.measuredWidth,
            heightMeasureSpec,
            totalHeight
        )
        val topMargin = (flexBoxView.layoutParams as MarginLayoutParams).topMargin
        totalHeight += flexBoxView.measuredHeight + topMargin // bottom of message - всегда FlexBox
        totalWidth += maxOf(flexBoxView.measuredWidth, textWidth)
        // width of message - ширина текста или FlexBox

        if(messageData?.user === User.INSTANCE)
            totalWidth = MeasureSpec.getSize(widthMeasureSpec)
        val resultWidth = resolveSize(totalWidth + paddingRight + paddingLeft, widthMeasureSpec)
        val resultHeight = resolveSize(totalHeight + paddingTop + paddingBottom, heightMeasureSpec)
        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val imageView = getChildAt(0)
        val textBox = getChildAt(1)
        val flexBoxView = getChildAt(2)

        imageView.layout(
            paddingLeft,
            paddingTop,
            paddingLeft + imageView.measuredWidth,
            paddingTop + imageView.measuredHeight
        )
        val topMargin = (flexBoxView.layoutParams as MarginLayoutParams).topMargin

        if (messageData?.user === User.INSTANCE) {
            textBox.layout(
                measuredWidth - textBox.measuredWidth,
                paddingTop,
                measuredWidth,
                paddingTop + textBox.measuredHeight
            )
            flexBoxView.layout(
                measuredWidth - paddingRight - flexBoxView.measuredWidth,
                textBox.bottom + topMargin,
                measuredWidth - paddingRight,
                textBox.bottom + flexBoxView.measuredHeight
            )
        } else {
            val marginRight = (imageView.layoutParams as MarginLayoutParams).rightMargin

            textBox.layout(
                imageView.right + marginRight,
                paddingTop,
                imageView.right + textBox.measuredWidth,
                paddingTop + textBox.measuredHeight
            )

            flexBoxView.layout(
                imageView.right + marginRight,
                textBox.bottom + topMargin,
                imageView.right + flexBoxView.measuredWidth,
                textBox.bottom + flexBoxView.measuredHeight
            )
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