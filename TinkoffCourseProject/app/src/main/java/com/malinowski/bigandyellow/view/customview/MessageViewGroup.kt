package com.malinowski.bigandyellow.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.model.data.*


class MessageViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    init {
        inflate(context, R.layout.message_view_group_layout, this)
    }

    private val imageView: ImageView = findViewById(R.id.image)
    private val messageTextView: TextView = findViewById(R.id.message)
    private val nameTextView: TextView = findViewById(R.id.name)
    var plus = ImageButton(context).apply {
        setImageResource(R.drawable.ic_plus)
        setBackgroundResource(R.drawable.bg_gray_round)
        visibility = GONE
    }

    private lateinit var message: MessageItem

    fun setMessage(message: MessageItem) {
        this.message = message

        this.messageTextView.text =
            HtmlCompat.fromHtml(message.message, HtmlCompat.FROM_HTML_MODE_LEGACY).trim()
        this.nameTextView.text = message.senderName
        Glide.with(context).load(message.avatarUrl).into(this.imageView)

        if (message.isMine) {
            nameTextView.visibility = GONE
            getChildAt(0).visibility = GONE
            getChildAt(1).setBackgroundResource(R.drawable.bg_green_round)
        } else {
            nameTextView.visibility = VISIBLE
            getChildAt(0).visibility = VISIBLE
            getChildAt(1).setBackgroundResource(R.drawable.bg_gray_round)
        }

        (getChildAt(2) as FlexBoxLayout).apply {
            removeAllViews()
            if (message.emoji.isNotEmpty())
                plus.visibility = VISIBLE
            else plus.visibility = GONE
        }

        for (reaction in message.emoji)
            addEmoji(reaction.value)
    }

    private fun addEmoji(reaction: UnitedReaction) {
        val flexbox = (getChildAt(2) as FlexBoxLayout)
        val emoji = CustomEmoji(context).apply {
            this.reaction = reaction
            clickCallback = {
                if (reaction.usersId.size == 0) {
                    flexbox.removeView(this)
                    message.emoji.remove(reaction.getUnicode())
                }
                if (message.emoji.size == 0)
                    plus.visibility = GONE

                val parcel =
                    if (reaction.usersId.contains(User.ME.id))
                        EmojiAddParcel(message.id, reaction.name)
                    else
                        EmojiDeleteParcel(message.id, reaction.name)
                emojiClickListener(parcel)
            }
        }
        plus.visibility = VISIBLE
        flexbox.addView(emoji, 0)
    }

    private var emojiClickListener: (EmojiClickParcel) -> Unit = { _ -> }
    fun setOnEmojiClickListener(callback: (EmojiClickParcel) -> Unit) {
        emojiClickListener = callback
    }

    fun setMessageOnLongClick(callback: () -> Unit) {
        getChildAt(1).setOnLongClickListener { // text message layout
            callback()
            true
        }
    }

    fun setPlusClickListener(callback: () -> Unit){
        plus.setOnClickListener {
            callback()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        plus.apply {
            if (parent == null)
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
        val textBox = getChildAt(1)
        val flexBoxView = getChildAt(2) as FlexBoxLayout

        var totalWidth = 0
        var totalHeight = 0

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

        if (message.isMine)
            totalWidth = MeasureSpec.getSize(widthMeasureSpec)

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
        val textBox = getChildAt(1)
        val flexBoxView = getChildAt(2)

        imageView.layout(
            paddingLeft,
            paddingTop,
            paddingLeft + imageView.measuredWidth,
            paddingTop + imageView.measuredHeight
        )
        val topMargin = (flexBoxView.layoutParams as MarginLayoutParams).topMargin

        if (message.isMine) {
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