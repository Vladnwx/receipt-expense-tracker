package com.qrcode.scanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "Test: app is running"
            setTextColor(0xFFFF0000.toInt())
            textSize = 24f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
        setContentView(tv)
        window.decorView.setBackgroundColor(0xFFFFC0CB.toInt()) // pink
    }
}
