package com.qrcode.scanner.ui.scanner

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.qrcode.scanner.ReceiptExpenseApp
import com.qrcode.scanner.data.repository.ReceiptRepository
import com.qrcode.scanner.domain.parser.FnsReceiptParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class ScannerEvent {
    data class QrFound(val rawData: String) : ScannerEvent()
    data class Parsed(val receiptId: Long) : ScannerEvent()
    data class AlreadyExists(val receiptId: Long) : ScannerEvent()
    object Saving : ScannerEvent()
    data class Error(val message: String) : ScannerEvent()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val parser: FnsReceiptParser
) : ViewModel() {

    private val _event = MutableLiveData<Event<ScannerEvent>>()
    val event: LiveData<Event<ScannerEvent>> = _event

    private val _isProcessing = MutableLiveData(false)
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _isScanning = MutableLiveData(true)
    val isScanning: LiveData<Boolean> = _isScanning

    fun onQrDetected(rawData: String) {
        if (_isProcessing.value == true) return
        viewModelScope.launch {
            _isProcessing.value = true
            _event.value = Event(ScannerEvent.Saving)
            try {
                val qrData = parser.parse(rawData)
                if (qrData != null) {
                    val existing = receiptRepository.findExistingReceipt(
                        fn = qrData.fiscalNumber,
                        fd = qrData.fiscalDocument,
                        fp = qrData.fiscalSign
                    )
                    if (existing != null) {
                        _event.value = Event(ScannerEvent.AlreadyExists(existing.id))
                        return@launch
                    }
                }

                val raw = receiptRepository.saveRaw(rawData)
                _event.value = Event(ScannerEvent.QrFound(rawData))

                if (qrData == null) {
                    _event.value = Event(ScannerEvent.Error("Формат QR-кода не распознан"))
                    return@launch
                }

                val result = receiptRepository.parseAndFetch(raw.id)
                if (result?.receipt != null) {
                    _event.value = Event(ScannerEvent.Parsed(result.receipt.id))
                } else {
                    _event.value = Event(ScannerEvent.Error("Не удалось получить данные чека"))
                }
            } catch (e: Exception) {
                _event.value = Event(ScannerEvent.Error("Ошибка: ${e.localizedMessage ?: "неизвестная"}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun onImagePicked(uri: String) {
        if (_isProcessing.value == true) return
        _isScanning.value = false
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(ReceiptExpenseApp.instance, Uri.parse(uri))
                val barcodeScanner = BarcodeScanning.getClient()
                val barcodes = barcodeScanner.process(image).await()
                barcodeScanner.close()
                val rawValue = barcodes.firstOrNull()?.rawValue
                if (!rawValue.isNullOrBlank()) {
                    onQrDetected(rawValue)
                } else {
                    _event.value = Event(ScannerEvent.Error("QR-код не найден на изображении"))
                }
            } catch (e: Exception) {
                _event.value = Event(ScannerEvent.Error("Ошибка обработки изображения: ${e.localizedMessage ?: "неизвестная"}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun toggleScanning() {
        _isScanning.value = _isScanning.value != true
    }
}
