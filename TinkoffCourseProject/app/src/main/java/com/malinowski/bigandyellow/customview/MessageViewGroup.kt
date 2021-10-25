package com.malinowski.bigandyellow.customview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.data.Message
import com.malinowski.bigandyellow.data.Reaction
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

    private val messageTextView: TextView = findViewById(R.id.message)
    private val nameTextView: TextView = findViewById(R.id.name)
    var plus = ImageButton(context).apply {
        setImageResource(R.drawable.ic_plus)
        setBackgroundResource(R.drawable.bg_gray_round)
        visibility = GONE
    }

    private var subscription: Disposable? = null
    private lateinit var message: Message

    fun setMessage(message: Message) {
        this.message = message
        this.messageTextView.text = message.message
        this.nameTextView.text = message.name
        (getChildAt(2) as FlexBoxLayout).apply {
            removeAllViews()
            if (message.reactions.isNotEmpty())
                plus.visibility = VISIBLE
            else plus.visibility = GONE
        }
        for (reaction in message.reactions)
            addEmoji(reaction)
        subscription?.dispose()
    }

    fun addEmoji(reaction: Reaction) {
        val flexbox = (getChildAt(2) as FlexBoxLayout)
        val emoji = CustomEmoji(context).apply {
            setReaction(reaction)
            clickCallback = {
                if (reaction.num == 0) {
                    flexbox.removeView(this)
                    message.reactions.remove(reaction)
                    if (message.reactions.size == 0)
                        plus.visibility = GONE
                }
            }
        }
        if(!message.reactions.contains(reaction))
            message.reactions.add(reaction)
        plus.visibility = VISIBLE
        flexbox.addView(emoji, 0)
    }

    fun setMessageOnLongClick(callback: () -> Unit) {
        getChildAt(1).setOnLongClickListener { // text message layout
            callback()
            true
        }
        plus.setOnClickListener {
            callback()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        plus.apply {
            if(parent == null)
                (getChildAt(2) as FlexBoxLayout).addView(this)
            layoutParams.apply {
                height = 100
                width = 100
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        require(childCount == 3) { "Child count should be 3 but was $childCount" }
        val imageView = getChildAt(0)
        val textView = getChildAt(1)
        val flexBoxView = getChildAt(2) as FlexBoxLayout

        var totalWidth = 0
        var totalHeight = 0

        setPadding( // max width of message
            paddingLeft,
            paddingTop,
            maxOf(paddingRight, MeasureSpec.getSize(widthMeasureSpec) / 6),
            paddingBottom
        )

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
        totalHeight += flexBoxView.measuredHeight + topMargin // bottom of message - всегда FlexBox
        totalWidth += maxOf(flexBoxView.measuredWidth, textWidth)
        // width of message - ширина текста или FlexBox

        plus.layoutParams = plus.layoutParams.apply {
            height = flexBoxView.sumHeight / flexBoxView.childCount // average height
            width = height
        }

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