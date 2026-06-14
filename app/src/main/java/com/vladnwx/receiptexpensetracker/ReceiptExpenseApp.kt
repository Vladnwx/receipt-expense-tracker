package com.vladnwx.receiptexpensetracker

import android.app.Application
import android.content.Context
import com.vladnwx.receiptexpensetracker.data.reporter.AppLogger
import com.vladnwx.receiptexpensetracker.data.reporter.GitHubIssueReporter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class ReceiptExpenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init()
        setupCrashHandler()
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            reportCrash(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun reportCrash(throwable: Throwable) {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val token = prefs.getString("github_token", null)
        if (token.isNullOrBlank()) return

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope.launch {
            GitHubIssueReporter.reportError(
                token = token.trim(),
                title = "[Crash] ${throwable.localizedMessage ?: "Неизвестная ошибка"}".take(80),
                details = buildString {
                    appendLine("**Автоматический отчёт о краше**")
                    appendLine()
                    appendLine("${throwable::class.java.simpleName}: ${throwable.localizedMessage}")
                    appendLine()
                    appendLine("### Stacktrace")
                    appendLine("```")
                    throwable.stackTraceToString().let { appendLine(it) }
                    appendLine("```")
                }
            )
        }
    }
}
