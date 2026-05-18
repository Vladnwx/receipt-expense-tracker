package com.qrcode.scanner

import android.app.Application
import com.qrcode.scanner.data.reporter.AppLogger
import com.qrcode.scanner.data.reporter.GitHubIssueReporter
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
        AppLogger.init(this)
        AppLogger.i("App", "App started, version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            GitHubIssueReporter.reportError(
                title = "Unhandled exception in ${thread.name}",
                details = "Необработанное исключение в потоке ${thread.name}: ${throwable.localizedMessage}",
                throwable = throwable
            )
        }
    }
}
