package com.vladnwx.receiptexpensetracker.data.reporter

import android.util.Log
import com.vladnwx.receiptexpensetracker.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {

    private const val TAG = "AppLogger"
    private const val BUFFER_SIZE = 500

    private var pid: String? = null
    private val buffer = ArrayDeque<String>(BUFFER_SIZE)

    fun init() {
        pid = android.os.Process.myPid().toString()
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        appendToBuffer("D", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        val stack = if (throwable != null) Log.getStackTraceString(throwable) else ""
        appendToBuffer("E", tag, "$message\n$stack")
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        appendToBuffer("I", tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        appendToBuffer("W", tag, message)
    }

    private fun appendToBuffer(level: String, tag: String, message: String) {
        val dateStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        buffer.addLast("$dateStr [$level] $tag: $message")
        if (buffer.size > BUFFER_SIZE) buffer.removeFirst()
    }

    fun getLogText(): String {
        if (!BuildConfig.DEBUG) return "Лог доступен только в debug-сборке"

        val appLog = buffer.joinToString("\n")

        val logcatLog = try {
            val p = pid ?: return appLog
            val process = Runtime.getRuntime().exec("logcat -d -v threadtime --pid=$p")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (sb.length > 200_000) break
                sb.appendLine(line)
            }
            reader.close()
            process.waitFor()
            sb.toString()
        } catch (_: Exception) { "" }

        return buildString {
            if (appLog.isNotBlank()) {
                appendLine("=== Лог приложения ===")
                appendLine(appLog)
                appendLine()
            }
            if (logcatLog.isNotBlank()) {
                appendLine("=== System logcat ===")
                append(logcatLog)
            }
            if (appLog.isBlank() && logcatLog.isBlank()) {
                append("Лог пуст")
            }
        }
    }

    fun clearLog() {
        if (!BuildConfig.DEBUG) return
        buffer.clear()
        try {
            Runtime.getRuntime().exec("logcat -c").waitFor()
        } catch (_: Exception) { }
    }
}
