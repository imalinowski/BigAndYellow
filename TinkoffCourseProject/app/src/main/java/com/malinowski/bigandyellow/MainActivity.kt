package com.malinowski.bigandyellow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.malinowski.bigandyellow.customview.CustomEmoji
import com.malinowski.bigandyellow.customview.Emoji

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CustomEmoji(this).num = 1
        CustomEmoji(this).emoji = Emoji.SAD
    }
}