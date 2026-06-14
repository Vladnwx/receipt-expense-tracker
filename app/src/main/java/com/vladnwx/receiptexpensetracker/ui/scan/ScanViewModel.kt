package com.vladnwx.receiptexpensetracker.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptRawDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptItemDao
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptRawEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptItemEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptStatus
import com.vladnwx.receiptexpensetracker.data.util.FnsJsonData
import com.vladnwx.receiptexpensetracker.data.util.FnsJsonItem
import com.vladnwx.receiptexpensetracker.data.util.FnsJsonParser
import com.vladnwx.receiptexpensetracker.data.util.FnsQrData
import com.vladnwx.receiptexpensetracker.data.util.FnsQrParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class ScannedData(
    val raw: String,
    val sum: Double,
    val date: String? = null,
    val fn: String? = null,
    val fd: String? = null,
    val fp: String? = null,
    val isJson: Boolean = false
)

data class ScanUiState(
    val scanned: ScannedData? = null,
    val fnsJson: FnsJsonData? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val receiptRawDao: ReceiptRawDao,
    private val receiptDao: ReceiptDao,
    private val receiptItemDao: ReceiptItemDao
) : ViewModel() {

    private val _state = MutableStateFlow(ScanUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(categories = categoryDao.getAll())
        }
    }

    fun onQrDetected(raw: String) {
        val fns = FnsQrParser.parse(raw)
        if (fns != null) {
            _state.value = _state.value.copy(
                scanned = ScannedData(
                    raw = raw,
                    sum = fns.sum ?: 0.0,
                    date = fns.date,
                    fn = fns.fn,
                    fd = fns.fd,
                    fp = fns.fp
                )
            )
        } else {
            _state.value = _state.value.copy(
                scanned = ScannedData(raw = raw, sum = 0.0)
            )
        }
    }

    fun onJsonReceived(jsonStr: String) {
        val parsed = FnsJsonParser.parse(jsonStr)
        if (parsed != null) {
            val dateStr = parsed.dateTime?.let { formatFnsDate(it) }
            _state.value = _state.value.copy(
                scanned = ScannedData(
                    raw = jsonStr,
                    sum = parsed.totalSum,
                    date = dateStr ?: parsed.dateTime,
                    fn = parsed.fn,
                    fd = parsed.fd,
                    fp = parsed.fp,
                    isJson = true
                ),
                fnsJson = parsed
            )
        } else {
            _state.value = _state.value.copy(
                scanned = ScannedData(raw = jsonStr, sum = 0.0, isJson = true),
                error = "Не удалось распарсить JSON чек"
            )
        }
    }

    fun save(description: String, categoryId: Long) {
        val scanned = _state.value.scanned ?: return
        val fnsJson = _state.value.fnsJson
        viewModelScope.launch {
            try {
                if (fnsJson != null) {
                    saveFullReceipt(scanned, fnsJson, description, categoryId)
                } else {
                    expenseDao.insert(ExpenseEntity(
                        amount = scanned.sum,
                        categoryId = categoryId,
                        description = description.ifBlank {
                            if (scanned.isJson) "Чек: ${scanned.raw.take(30)}"
                            else "QR: ${scanned.raw.take(30)}"
                        },
                        date = parseDate(scanned.date) ?: System.currentTimeMillis()
                    ))
                }
                _state.value = _state.value.copy(saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    private suspend fun saveFullReceipt(
        scanned: ScannedData,
        fnsJson: FnsJsonData,
        description: String,
        categoryId: Long
    ) {
        val fn = fnsJson.fn ?: ""
        val fd = fnsJson.fd ?: ""
        val fp = fnsJson.fp ?: ""

        val existing = if (fn.isNotBlank() && fd.isNotBlank() && fp.isNotBlank()) {
            receiptDao.findByFiscalInfo(fn, fd, fp)
        } else null

        if (existing != null) {
            _state.value = _state.value.copy(error = "Чек уже сохранён")
            return
        }

        val rawId = receiptRawDao.insert(ReceiptRawEntity(
            rawData = scanned.raw,
            format = "json",
            isParsed = true
        ))

        val receiptId = receiptDao.insert(ReceiptEntity(
            rawId = rawId,
            fiscalDriveNumber = fn,
            fiscalDocumentNumber = fd,
            fiscalSign = fp,
            amount = scanned.sum,
            date = parseDate(scanned.date) ?: System.currentTimeMillis(),
            retailerName = fnsJson.retailerName,
            retailerInn = fnsJson.retailerInn,
            operationType = fnsJson.operationType,
            status = ReceiptStatus.Pending.name
        ))

        val items = fnsJson.items.map { item ->
            ReceiptItemEntity(
                receiptId = receiptId,
                name = item.name,
                price = item.price,
                quantity = item.quantity,
                amount = item.sum,
                categoryId = if (categoryId != 0L) categoryId else null
            )
        }
        if (items.isNotEmpty()) {
            receiptItemDao.insertAll(items)
        }

        val storeName = fnsJson.retailPlace?.takeIf { it.isNotBlank() }
            ?: fnsJson.retailerName
        expenseDao.insert(ExpenseEntity(
            amount = scanned.sum,
            categoryId = categoryId,
            accountId = null,
            description = description.ifBlank {
                storeName ?: "Чек ${fn.take(8)}"
            },
            date = parseDate(scanned.date) ?: System.currentTimeMillis(),
            receiptId = receiptId,
            tags = fnsJson.items.joinToString(", ") { it.name.take(30) }.ifBlank { null }
        ))
    }

    private fun parseDate(dateStr: String?): Long? {
        if (dateStr == null) return null
        return try {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss",
                "dd.MM.yyyy",
                "yyyy-MM-dd"
            )
            for (fmt in formats) {
                try {
                    val sdf = SimpleDateFormat(fmt, Locale.getDefault())
                    return sdf.parse(dateStr)?.time
                } catch (_: Exception) { }
            }
            null
        } catch (_: Exception) { null }
    }

    private fun formatFnsDate(dateStr: String): String? {
        val ms = parseDate(dateStr) ?: return null
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return sdf.format(ms)
    }

    fun clear() {
        _state.value = _state.value.copy(scanned = null, fnsJson = null, saved = false, error = null)
    }
}
