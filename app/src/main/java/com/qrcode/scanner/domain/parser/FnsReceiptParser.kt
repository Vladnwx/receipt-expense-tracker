package com.qrcode.scanner.domain.parser

import javax.inject.Inject

data class FnsQrData(
    val fiscalNumber: String = "",
    val fiscalDocument: String = "",
    val fiscalSign: String = "",
    val sum: Double? = null,
    val date: String? = null,
    val operationType: Int = 1,
    val rawUrl: String? = null
)

class FnsReceiptParser @Inject constructor() {

    fun parse(rawData: String): FnsQrData? {
        val urlMatch = URL_PATTERN.find(rawData)
        if (urlMatch != null) {
            return parseUrl(rawData)
        }

        val dataMatch = DATA_PATTERN.find(rawData)
        if (dataMatch != null) {
            return parseData(rawData)
        }

        return null
    }

    private fun parseUrl(raw: String): FnsQrData? {
        val fn = extractParam(raw, "fn") ?: return null
        val fd = extractParam(raw, "i") ?: return null
        val fp = extractParam(raw, "fp") ?: return null
        val n = extractParam(raw, "n")?.toIntOrNull() ?: 1
        val s = extractParam(raw, "s")?.toDoubleOrNull()
        val t = extractParam(raw, "t")

        return FnsQrData(
            fiscalNumber = fn,
            fiscalDocument = fd,
            fiscalSign = fp,
            sum = s,
            date = t,
            operationType = n,
            rawUrl = raw
        )
    }

    private fun parseData(raw: String): FnsQrData? {
        val fn = extractParam(raw, "fn") ?: return null
        val fd = extractParam(raw, "i") ?: return null
        val fp = extractParam(raw, "fp") ?: return null
        val s = extractParam(raw, "s")?.toDoubleOrNull()
        val t = extractParam(raw, "t")
        val n = extractParam(raw, "n")?.toIntOrNull() ?: 1

        return FnsQrData(
            fiscalNumber = fn,
            fiscalDocument = fd,
            fiscalSign = fp,
            sum = s,
            date = t,
            operationType = n,
            rawUrl = raw
        )
    }

    private fun extractParam(input: String, key: String): String? {
        val regex = Regex("[?&]$key=([^&]+)")
        return regex.find(input)?.groupValues?.getOrNull(1)
    }

    fun detectFormat(rawData: String): String {
        return when {
            rawData.startsWith("http") -> "url"
            rawData.contains("fn=") -> "data"
            else -> "unknown"
        }
    }

    companion object {
        private val URL_PATTERN = Regex("^https?://.*[?&]fn=\\d+.*")
        private val DATA_PATTERN = Regex("^(?:t=\\d+T\\d+&)?s=[\\d.]+&fn=\\d+.*")
    }
}
