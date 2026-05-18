package com.qrcode.scanner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReceiptExpenseApp : Application() {
    companion object {
        lateinit var instance: ReceiptExpenseApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
