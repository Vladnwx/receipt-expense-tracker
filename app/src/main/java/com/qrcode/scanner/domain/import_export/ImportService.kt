package com.qrcode.scanner.domain.import_export

interface ImportService {

    suspend fun importCsv(fileUri: String, accountId: Long? = null): ImportResult

    suspend fun importExcel(fileUri: String, accountId: Long? = null): ImportResult

    suspend fun getSupportedFormats(): List<String>

    data class ImportResult(
        val importedCount: Int,
        val skippedCount: Int,
        val errors: List<String>,
        val totalAmount: Double,
        val importedItems: List<ImportedItem>
    )

    data class ImportedItem(
        val name: String,
        val amount: Double,
        val date: String?,
        val categoryName: String?,
        val description: String?
    )

    sealed class ImportError {
        data class ParseError(val line: Int, val message: String) : ImportError()
        data class FileError(val message: String) : ImportError()
        data object UnsupportedFormat : ImportError()
        data object Cancelled : ImportError()
    }
}
