package com.vladnwx.receiptexpensetracker

import android.app.Application
import com.vladnwx.receiptexpensetracker.data.reporter.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReceiptExpenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
    }
}
