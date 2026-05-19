package com.qrcode.scanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.qrcode.scanner.data.repository.AppUpdateRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var updateRepository: AppUpdateRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "MainActivity + Hilt"
            setTextColor(0xFFFF0000.toInt())
            textSize = 24f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
        setContentView(tv)
        window.decorView.setBackgroundColor(0xFFFFE4B5.toInt()) // moccasin
    }
}
