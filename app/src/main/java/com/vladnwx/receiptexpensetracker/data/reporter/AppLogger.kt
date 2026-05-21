package com.vladnwx.receiptexpensetracker.data.reporter

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object AppLogger {

    private var pid: String? = null

    fun init(context: Context) {
        pid = android.os.Process.myPid().toString()
    }

    fun getLogText(): String {
        val p = pid ?: return "Лог недоступен"
        return try {
            val process = Runtime.getRuntime().exec("logcat -d -v threadtime --pid=$p")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.appendLine(line)
            }
            reader.close()
            process.waitFor()
            sb.toString()
        } catch (e: Exception) {
            "Ошибка чтения лога: ${e.localizedMessage}"
        }
    }

    fun clearLog() {
        try {
            Runtime.getRuntime().exec("logcat -c").waitFor()
        } catch (_: Exception) { }
    }
}
