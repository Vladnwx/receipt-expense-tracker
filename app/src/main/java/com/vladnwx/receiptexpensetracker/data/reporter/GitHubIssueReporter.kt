package com.vladnwx.receiptexpensetracker.data.reporter

import android.util.Log
import com.vladnwx.receiptexpensetracker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object GitHubIssueReporter {

    private const val TAG = "GitHubIssueReporter"
    private const val MAX_BODY_LENGTH = 60_000
    private const val MAX_LOG_CHUNK = 50_000
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun reportError(
        token: String,
        title: String,
        details: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val body = JSONObject().apply {
                put("title", title)
                put("body", buildString {
                    appendLine("## Отчёт об ошибке")
                    appendLine()
                    appendLine("- **Версия:** ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    appendLine("- **Время:** $dateStr")
                    appendLine()
                    appendLine("### Описание")
                    appendLine()
                    appendLine(details)
                })
            }

            val bodyStr = body.toString()
            val request = Request.Builder()
                .url("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/issues")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "ReceiptExpenseTracker/1.0")
                .post(bodyStr.toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            response.close()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("errors")?.optString("message") ?: responseBody
                } catch (_: Exception) { responseBody }
                return@withContext "Ошибка ${response.code}: ${errorMsg.take(200)}"
            }

            val issueNumber = try {
                JSONObject(responseBody).optInt("number")
            } catch (_: Exception) { 0 }
            val issueUrl = try {
                JSONObject(responseBody).optString("html_url", "")
            } catch (_: Exception) { "" }

            val logText = AppLogger.getLogText()
            if (logText.isNotBlank() && issueNumber > 0) {
                logText.chunked(MAX_LOG_CHUNK).forEachIndexed { i, chunk ->
                    val comment = JSONObject().apply {
                        put("body", "**Лог (часть ${i + 1})**\n\n```\n$chunk\n```")
                    }
                    val commentRequest = Request.Builder()
                        .url("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/issues/$issueNumber/comments")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("User-Agent", "ReceiptExpenseTracker/1.0")
                        .post(comment.toString().toRequestBody(jsonMediaType))
                        .build()
                    try {
                        client.newCall(commentRequest).execute().close()
                    } catch (_: Exception) { }
                }
            }

            "Issue создан: $issueUrl"
        } catch (e: Exception) {
            Log.e(TAG, "Error reporting issue", e)
            "Ошибка: ${e.localizedMessage ?: "неизвестная"}"
        }
    }
}
