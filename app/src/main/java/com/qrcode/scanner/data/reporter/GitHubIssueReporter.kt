package com.qrcode.scanner.data.reporter

import android.util.Log
import com.qrcode.scanner.BuildConfig
import com.qrcode.scanner.data.repository.TokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubIssueReporter @Inject constructor(
    private val tokenRepository: TokenRepository
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun resolveToken(): String? {
        val fromPrefs = tokenRepository.getGitHubIssuesToken()
        if (!fromPrefs.isNullOrBlank()) return fromPrefs
        val fromBuildConfig = BuildConfig.GITHUB_ISSUES_TOKEN
        if (fromBuildConfig.isNotBlank()) {
            tokenRepository.saveGitHubIssuesToken(fromBuildConfig)
            return fromBuildConfig
        }
        return null
    }

    fun reportError(
        title: String,
        details: String,
        throwable: Throwable? = null
    ) {
        AppLogger.e("GitHubIssueReporter", "$title: $details", throwable)
        val token = resolveToken()
        if (token.isNullOrBlank()) {
            AppLogger.w(TAG, "GitHub token not configured, skipping issue report")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                createIssueOnGithub(token, formatTitle(title), formatBody(details, throwable))
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error reporting issue", e)
            }
        }
    }

    suspend fun reportIssue(
        title: String,
        details: String,
        throwable: Throwable? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val token = resolveToken()
        if (token.isNullOrBlank()) {
            AppLogger.w(TAG, "GitHub token not configured, skipping issue report")
            return@withContext false
        }
        try {
            val success = createIssueOnGithub(token, formatTitle(title), formatBody(details, throwable))
            if (success) {
                AppLogger.i(TAG, "Issue created successfully")
            }
            success
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error reporting issue", e)
            false
        }
    }

    private fun formatTitle(title: String): String = "[Crash] $title"

    private fun formatBody(
        details: String,
        throwable: Throwable? = null
    ): String {
        val stackTrace = throwable?.let { Log.getStackTraceString(it) } ?: ""
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        return buildString {
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
        }
    }

    private fun createIssueOnGithub(token: String, issueTitle: String, body: String): Boolean {
        val jsonBody = JSONObject().apply {
            put("title", issueTitle)
            put("body", body)
        }

        val request = Request.Builder()
            .url("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/issues")
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "ReceiptExpenseTracker/1.0")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val success = response.isSuccessful
        if (!success) {
            AppLogger.e(TAG, "Failed to create issue: ${response.code} ${response.body?.string()}")
        }
        response.close()
        return success
    }

    companion object {
        private const val TAG = "GitHubIssueReporter"
    }
}
