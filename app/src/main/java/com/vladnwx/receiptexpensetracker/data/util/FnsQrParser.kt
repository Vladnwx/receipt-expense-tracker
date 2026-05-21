package com.vladnwx.receiptexpensetracker.data.util

data class FnsQrData(
    val raw: String,
    val date: String? = null,
    val sum: Double? = null,
    val fn: String? = null,
    val fd: String? = null,
    val fp: String? = null,
    val type: String? = null
)

object FnsQrParser {
    fun parse(raw: String): FnsQrData? {
        val params = raw.split("&").associate {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
        }

        val sumStr = params["s"] ?: return null
        val sum = sumStr.toDoubleOrNull() ?: return null

        return FnsQrData(
            raw = raw,
            date = params["t"],
            sum = sum,
            fn = params["fn"],
            fd = params["i"],
            fp = params["fp"],
            type = params["n"]
        )
    }
}
