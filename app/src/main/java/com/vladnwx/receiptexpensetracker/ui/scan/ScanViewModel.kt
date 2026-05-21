package com.vladnwx.receiptexpensetracker.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.util.FnsQrData
import com.vladnwx.receiptexpensetracker.data.util.FnsQrParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannedData(
    val raw: String,
    val sum: Double,
    val date: String? = null,
    val fn: String? = null,
    val fd: String? = null,
    val fp: String? = null
)

data class ScanUiState(
    val scanned: ScannedData? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
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

    fun save(description: String, categoryId: Long) {
        val scanned = _state.value.scanned ?: return
        viewModelScope.launch {
            try {
                expenseDao.insert(ExpenseEntity(
                    amount = scanned.sum,
                    categoryId = categoryId,
                    description = description.ifBlank { "QR: ${scanned.raw.take(30)}" },
                    date = System.currentTimeMillis()
                ))
                _state.value = _state.value.copy(saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun clear() {
        _state.value = _state.value.copy(scanned = null, saved = false, error = null)
    }
}
