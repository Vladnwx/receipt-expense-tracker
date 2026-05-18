package com.qrcode.scanner.ui.receiptdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.CategoryEntity
import com.qrcode.scanner.data.local.entity.ReceiptEntity
import com.qrcode.scanner.data.local.entity.ReceiptItemEntity
import com.qrcode.scanner.data.local.entity.ReceiptRawEntity
import com.qrcode.scanner.data.repository.CategoryRepository
import com.qrcode.scanner.data.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val receiptId: Long = savedStateHandle["receiptId"] ?: -1

    private val _state = MutableLiveData(ReceiptDetailState())
    val state: LiveData<ReceiptDetailState> = _state

    init {
        if (receiptId > 0) load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value?.copy(isLoading = true)
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
}
