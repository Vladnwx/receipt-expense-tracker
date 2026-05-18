package com.qrcode.scanner.ui.receiptlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.ReceiptEntity
import com.qrcode.scanner.data.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptListViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _receipts = MutableLiveData<List<ReceiptEntity>>(emptyList())
    val receipts: LiveData<List<ReceiptEntity>> = _receipts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isChecking = MutableLiveData(false)
    val isChecking: LiveData<Boolean> = _isChecking

    private val _checkResult = MutableLiveData<String?>(null)
    val checkResult: LiveData<String?> = _checkResult

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _receipts.value = receiptRepository.getAllReceipts()
            _isLoading.value = false
        }
    }

    fun checkReceipts() {
        viewModelScope.launch {
            _isChecking.value = true
            _checkResult.value = null
            val checked = receiptRepository.checkUncheckedReceipts()
            _receipts.value = receiptRepository.getAllReceipts()
            _isChecking.value = false
            _checkResult.value = "Проверено: $checked"
        }
    }

    fun consumeCheckResult() {
        _checkResult.value = null
    }
}
