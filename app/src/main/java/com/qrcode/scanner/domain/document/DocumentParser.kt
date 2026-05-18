package com.qrcode.scanner.domain.document

interface DocumentParser {

    suspend fun parse(fileUri: String): ParsedDocument

    suspend fun parsePdf(fileUri: String): ParsedDocument

    suspend fun parseImage(fileUri: String): ParsedDocument

    suspend fun supportedMimeTypes(): List<String>

    data class ParsedDocument(
        val rawText: String,
        val qrCodes: List<String>,
        val barcodes: List<String>,
        val confidence: Float,
        val mimeType: String
    )

    sealed class ParseError {
        data object UnsupportedFormat : ParseError()
        data object FileNotFound : ParseError()
        data object OcrFailed : ParseError()
        data class ProcessingError(val message: String) : ParseError()
    }
}
