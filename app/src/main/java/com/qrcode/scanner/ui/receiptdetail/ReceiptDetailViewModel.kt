package com.qrcode.scanner.ui.receiptdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.CategoryEntity
import com.qrcode.scanner.data.local.entity.ReceiptEntity
import com.qrcode.scanner.data.local.entity.ReceiptItemEntity
import com.qrcode.scanner.data.local.entity.ReceiptRawEntity
import com.qrcode.scanner.data.repository.CategoryRepository
import com.qrcode.scanner.data.repository.ExpenseRepository
import com.qrcode.scanner.data.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptDetailState(
    val receipt: ReceiptEntity? = null,
    val raw: ReceiptRawEntity? = null,
    val items: List<ReceiptItemEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val receiptRepository: ReceiptRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val receiptId: Long = savedStateHandle["receiptId"] ?: -1

    private val _state = MutableStateFlow(ReceiptDetailState())
    val state: StateFlow<ReceiptDetailState> = _state.asStateFlow()

    init {
        if (receiptId > 0) load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val receipt = receiptRepository.getReceiptById(receiptId)
            val items = receiptRepository.getItemsByReceiptId(receiptId)
            val raw = receipt?.let { receiptRepository.getRawById(it.rawId) }
            val categories = categoryRepository.getAll()
            _state.value = ReceiptDetailState(
                receipt = receipt,
                raw = raw,
                items = items,
                categories = categories,
                isLoading = false
            )
        }
    }

    fun setItemCategory(itemId: Long, categoryId: Long?) {
        viewModelScope.launch {
            receiptRepository.setItemCategory(itemId, categoryId)
            val items = receiptRepository.getItemsByReceiptId(receiptId)
            _state.value = _state.value.copy(items = items)
        }
    }
}
