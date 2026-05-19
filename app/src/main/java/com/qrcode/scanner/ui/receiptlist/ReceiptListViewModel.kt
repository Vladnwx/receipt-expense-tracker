package com.qrcode.scanner.ui.receiptlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.ReceiptEntity
import com.qrcode.scanner.data.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptListUiState(
    val receipts: List<ReceiptEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val checkResult: String? = null
)

@HiltViewModel
class ReceiptListViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptListUiState())
    val uiState: StateFlow<ReceiptListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(
                receipts = receiptRepository.getAllReceipts(),
                isLoading = false
            )
        }
    }

    fun checkReceipts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true, checkResult = null)
            val checked = receiptRepository.checkUncheckedReceipts()
            _uiState.value = _uiState.value.copy(
                receipts = receiptRepository.getAllReceipts(),
                isChecking = false,
                checkResult = "Проверено: $checked"
            )
        }
    }

    fun consumeCheckResult() {
        _uiState.value = _uiState.value.copy(checkResult = null)
    }
}
