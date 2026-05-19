package com.qrcode.scanner

import android.app.Application
import com.qrcode.scanner.data.reporter.AppLogger
import com.qrcode.scanner.data.reporter.GitHubIssueReporter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ReceiptExpenseApp : Application() {
    @Inject lateinit var githubIssueReporter: GitHubIssueReporter

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
            githubIssueReporter.reportError(
                title = "Unhandled exception in ${thread.name}",
                details = "Необработанное исключение в потоке ${thread.name}: ${throwable.localizedMessage}",
                throwable = throwable
            )
        }
    }
}
