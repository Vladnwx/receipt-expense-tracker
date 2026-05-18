package com.qrcode.scanner.ui.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.repository.ReceiptRepository
import com.qrcode.scanner.domain.parser.FnsReceiptParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScannerEvent {
    data class QrFound(val rawData: String) : ScannerEvent()
    data class Parsed(val receiptId: Long) : ScannerEvent()
    data class AlreadyExists(val receiptId: Long) : ScannerEvent()
    object Saving : ScannerEvent()
    object Error : ScannerEvent()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val parser: FnsReceiptParser
) : ViewModel() {

    private val _event = MutableLiveData<ScannerEvent>()
    val event: LiveData<ScannerEvent> = _event

    private val _isProcessing = MutableLiveData(false)
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _isScanning = MutableLiveData(true)
    val isScanning: LiveData<Boolean> = _isScanning

    fun onQrDetected(rawData: String) {
        if (_isProcessing.value == true) return
        viewModelScope.launch {
            _isProcessing.value = true
            _event.value = ScannerEvent.Saving
            try {
                val qrData = parser.parse(rawData)
                if (qrData != null) {
                    val existing = receiptRepository.findExistingReceipt(
                        fn = qrData.fiscalNumber,
                        fd = qrData.fiscalDocument,
                        fp = qrData.fiscalSign
                    )
                    if (existing != null) {
                        _event.value = ScannerEvent.AlreadyExists(existing.id)
                        return@launch
                    }
                }

                val raw = receiptRepository.saveRaw(rawData)
                _event.value = ScannerEvent.QrFound(rawData)
                val result = receiptRepository.parseAndFetch(raw.id)
                if (result?.receipt != null) {
                    _event.value = ScannerEvent.Parsed(result.receipt.id)
                }
            } catch (e: Exception) {
                _event.value = ScannerEvent.Error
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun toggleScanning() {
        _isScanning.value = _isScanning.value != true
    }
}
