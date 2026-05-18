package com.qrcode.scanner.data.reporter

import android.util.Log
import com.qrcode.scanner.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GitHubIssueReporter {

    private const val TAG = "GitHubIssueReporter"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun reportError(
        title: String,
        details: String,
        throwable: Throwable? = null
    ) {
        val token = BuildConfig.GITHUB_ISSUES_TOKEN
        if (token.isBlank()) {
            Log.w(TAG, "GITHUB_ISSUES_TOKEN not configured, skipping issue report")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val stackTrace = throwable?.let { Log.getStackTraceString(it) } ?: ""
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val body = JSONObject().apply {
                    put("title", "[Crash] $title")
                    put("body", buildString {
                        appendLine("## Автоматический отчёт об ошибке")
                        appendLine()
                        appendLine("- **Версия:** ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                        appendLine("- **Время:** $dateStr")
                        appendLine()
                        appendLine("### Описание")
                        appendLine()
                        appendLine(details)
                        if (stackTrace.isNotBlank()) {
                            appendLine()
                            appendLine("### Stacktrace")
                            appendLine()
                            appendLine("```")
                            appendLine(stackTrace)
                            appendLine("```")
                        }
                    })
                }

                val request = Request.Builder()
                    .url("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/issues")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "ReceiptExpenseTracker/1.0")
                    .post(body.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed to create issue: ${response.code} ${response.body?.string()}")
                } else {
                    Log.i(TAG, "Issue created successfully")
                }
                response.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error reporting issue", e)
            }
        }
    }
}
