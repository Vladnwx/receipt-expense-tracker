package com.qrcode.scanner.data.reporter

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {

    private const val TAG = "AppLogger"
    private const val MAX_FILE_SIZE = 512 * 1024
    private const val LOG_FILE = "app.log"

    private var logDir: File? = null

    fun init(context: Context) {
        logDir = File(context.filesDir, "logs")
        logDir?.mkdirs()
        trimLogFile()
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        writeToFile("D", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        val stack = if (throwable != null) Log.getStackTraceString(throwable) else ""
        writeToFile("E", tag, "$message\n$stack")
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        writeToFile("W", tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        writeToFile("I", tag, message)
    }

    private fun writeToFile(level: String, tag: String, message: String) {
        val dir = logDir ?: return
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val line = "$dateStr [$level] $tag: $message"
        try {
            val file = File(dir, LOG_FILE)
            FileWriter(file, true).use { it.write("$line\n") }
            trimLogFile()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write log", e)
        }
    }

    private fun trimLogFile() {
        try {
            val file = File(logDir, LOG_FILE)
            if (file.exists() && file.length() > MAX_FILE_SIZE) {
                val lines = file.readLines()
                val trimmed = lines.takeLast(2000)
                file.writeText(trimmed.joinToString("\n") + "\n")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trim log", e)
        }
    }

    fun getLogText(): String {
        val file = File(logDir, LOG_FILE)
        if (!file.exists()) return "Лог пуст"
        return try {
            val sb = StringBuilder()
            BufferedReader(FileReader(file)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.appendLine(line)
                }
            }
            sb.toString()
        } catch (e: Exception) {
            "Ошибка чтения лога: ${e.localizedMessage}"
        }
    }

    fun getErrorLogText(): String {
        val file = File(logDir, LOG_FILE)
        if (!file.exists()) return ""
        return try {
            val sb = StringBuilder()
            BufferedReader(FileReader(file)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    if (line.contains(" [E] ")) {
                        sb.appendLine(line)
                    }
                    line = reader.readLine()
                }
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    fun clearLog() {
        try {
            File(logDir, LOG_FILE).writeText("")
            i(TAG, "Log cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear log", e)
        }
    }

    fun getLogFile(): File? {
        val file = File(logDir, LOG_FILE)
        return if (file.exists()) file else null
    }
}
