package com.qrcode.scanner.ui.scanner

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.qrcode.scanner.data.local.entity.ReceiptStatus
import com.qrcode.scanner.data.reporter.AppLogger
import com.qrcode.scanner.data.repository.FetchReceiptResult
import com.qrcode.scanner.data.repository.PreferencesRepository
import com.qrcode.scanner.data.repository.ReceiptRepository
import com.qrcode.scanner.domain.parser.FnsReceiptParser
import com.qrcode.scanner.domain.parser.FnsShareParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class ScannerEvent {
    data class Saved(val receiptId: Long) : ScannerEvent()
    data class AlreadyExists(val receiptId: Long) : ScannerEvent()
    object CheckStarted : ScannerEvent()
    object CheckSuccess : ScannerEvent()
    data class CheckWarning(val message: String) : ScannerEvent()
    data class CheckError(val message: String) : ScannerEvent()
    data class Error(val message: String) : ScannerEvent()
    data class GalleryQrList(val qrs: List<String>) : ScannerEvent()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val preferencesRepository: PreferencesRepository,
    private val parser: FnsReceiptParser,
    private val fnsShareParser: FnsShareParser,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _event = MutableLiveData<Event<ScannerEvent>>()
    val event: LiveData<Event<ScannerEvent>> = _event

    private val _isProcessing = MutableLiveData(false)
    val isProcessing: LiveData<Boolean> = _isProcessing
    val isProcessingNow: Boolean get() = _isProcessing.value == true

    private val _isScanning = MutableLiveData(true)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _lastQrDetectedMs = MutableLiveData(0L)
    val lastQrDetectedMs: LiveData<Long> = _lastQrDetectedMs

    private var galleryQrList: List<String> = emptyList()

    fun onQrDetected(rawData: String) {
        if (_isProcessing.value == true) {
            AppLogger.w("Scanner", "onQrDetected skipped — already processing")
            return
        }
        AppLogger.i("Scanner", "QR detected: ${rawData.take(80)}...")
        _lastQrDetectedMs.value = System.currentTimeMillis()
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                processQr(rawData)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun onImagePicked(uri: String) {
        if (_isProcessing.value == true) {
            AppLogger.w("Scanner", "onImagePicked skipped — already processing")
            return
        }
        AppLogger.i("Scanner", "Image picked: ${uri.take(60)}...")
        _isScanning.value = false
        viewModelScope.launch {
            _isProcessing.value = true
            galleryQrList = emptyList()
            try {
                val barcodeScanner = BarcodeScanning.getClient()
                val barcodes = barcodeScanner.process(InputImage.fromFilePath(context, Uri.parse(uri))).await()
                barcodeScanner.close()

                val qrCodes = barcodes
                    .filter { it.format == Barcode.FORMAT_QR_CODE }
                    .mapNotNull { it.rawValue }
                    .filter { it.isNotBlank() }

                AppLogger.i("Scanner", "Image scan: ${barcodes.size} barcodes, ${qrCodes.size} QRs")
                when {
                    qrCodes.isEmpty() -> _event.value = Event(ScannerEvent.Error("QR-код не найден на изображении"))
                    qrCodes.size == 1 -> processQr(qrCodes.first())
                    else -> {
                        galleryQrList = qrCodes
                        _event.value = Event(ScannerEvent.GalleryQrList(qrCodes.map { it.take(60) }))
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("Scanner", "Image scan failed", e)
                _event.value = Event(ScannerEvent.Error("Ошибка обработки изображения: ${e.localizedMessage ?: "неизвестная"}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun selectGalleryQr(index: Int) {
        val rawData = galleryQrList.getOrNull(index)
        if (rawData == null) {
            AppLogger.w("Scanner", "selectGalleryQr: invalid index $index")
            return
        }
        AppLogger.i("Scanner", "Gallery QR selected: index=$index")
        galleryQrList = emptyList()
        _lastQrDetectedMs.value = System.currentTimeMillis()
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                processQr(rawData)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private suspend fun processQr(rawData: String) {
        val qrData = parser.parse(rawData)
        if (qrData == null) {
            AppLogger.w("Scanner", "QR parse failed — not a FNS receipt: ${rawData.take(60)}...")
            _event.value = Event(ScannerEvent.Error("Это не чек"))
            return
        }
        AppLogger.i("Scanner", "QR parsed: fn=${qrData.fiscalNumber} fd=${qrData.fiscalDocument} fp=${qrData.fiscalSign} sum=${qrData.sum}")

        val existing = receiptRepository.findExistingReceipt(
            fn = qrData.fiscalNumber,
            fd = qrData.fiscalDocument,
            fp = qrData.fiscalSign
        )
        if (existing != null) {
            AppLogger.i("Scanner", "Duplicate: receipt #${existing.id} status=${existing.status}")
            if (existing.status == ReceiptStatus.Pending.name) {
                AppLogger.i("Scanner", "Auto-retry: re-fetching Pending receipt #${existing.id}")
                _event.value = Event(ScannerEvent.Saved(existing.id))
                launchFetch(existing.id)
            } else {
                AppLogger.i("Scanner", "Already exists (${existing.status}), showing dialog")
                _event.value = Event(ScannerEvent.AlreadyExists(existing.id))
            }
            return
        }

        AppLogger.d("Scanner", "Saving raw data and creating receipt...")
        val raw = receiptRepository.saveRaw(rawData)
        val receipt = receiptRepository.createReceiptFromQr(raw.id, qrData)
        if (receipt == null) {
            AppLogger.e("Scanner", "Failed to create receipt from QR")
            _event.value = Event(ScannerEvent.Error("Не удалось сохранить чек"))
            return
        }

        AppLogger.i("Scanner", "Receipt #${receipt.id} created (Pending), launching fetch")
        _event.value = Event(ScannerEvent.Saved(receipt.id))
        launchFetch(receipt.id)
    }

    private fun launchFetch(receiptId: Long) {
        AppLogger.i("Scanner", "launchFetch receiptId=$receiptId")
        viewModelScope.launch {
            try {
                _event.value = Event(ScannerEvent.CheckStarted)

                val accountId = preferencesRepository.getDefaultAccountId()
                AppLogger.d("Scanner", "Fetching receipt #$receiptId, accountId=$accountId")
                val startTime = System.currentTimeMillis()

                when (val result = receiptRepository.fetchAndUpdate(receiptId, accountId)) {
                    is FetchReceiptResult.Success -> {
                        val elapsed = System.currentTimeMillis() - startTime
                        AppLogger.i("Scanner", "Fetch #$receiptId succeeded in ${elapsed}ms, items=${result.items.size}")
                        if (accountId != null) {
                            _event.value = Event(ScannerEvent.CheckSuccess)
                        } else {
                            AppLogger.w("Scanner", "Fetch #$receiptId: no default account, expenses without account")
                            _event.value = Event(ScannerEvent.CheckWarning("Счёт не выбран — расходы добавлены без счёта"))
                        }
                    }
                    is FetchReceiptResult.Unauthorized -> {
                        AppLogger.w("Scanner", "Fetch #$receiptId: unauthorized — token not set")
                        _event.value = Event(ScannerEvent.CheckError("Требуется токен в настройках"))
                    }
                    is FetchReceiptResult.NotFound -> {
                        AppLogger.w("Scanner", "Fetch #$receiptId: not found (404)")
                        _event.value = Event(ScannerEvent.CheckError("Чек не найден в сервисе"))
                    }
                    is FetchReceiptResult.Error -> {
                        AppLogger.w("Scanner", "Fetch #$receiptId failed: ${result.message}")
                        _event.value = Event(ScannerEvent.CheckError("Ошибка проверки: ${result.message}"))
                    }
                    is FetchReceiptResult.RateLimited -> {
                        AppLogger.w("Scanner", "Fetch #$receiptId: rate limited")
                        _event.value = Event(ScannerEvent.CheckError("Сервис временно недоступен, повторите позже"))
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("Scanner", "Fetch #$receiptId crashed", e)
                _event.value = Event(ScannerEvent.CheckError("Ошибка проверки: ${e.localizedMessage ?: "неизвестная"}"))
            }
        }
    }

    fun toggleScanning() {
        _isScanning.value = _isScanning.value != true
        if (_isScanning.value == true) {
            _lastQrDetectedMs.value = System.currentTimeMillis()
        }
    }

    fun onFnsJsonReceived(json: String) {
        if (_isProcessing.value == true) return
        AppLogger.i("Scanner", "FNS JSON received: ${json.take(100)}...")
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val response = fnsShareParser.parse(json)
                if (response == null) {
                    AppLogger.w("Scanner", "FNS JSON parse failed")
                    _event.value = Event(ScannerEvent.Error("Не удалось обработать JSON"))
                    return@launch
                }
                val qrData = fnsShareParser.toFnsQrData(response)
                AppLogger.i("Scanner", "FNS parsed: fn=${qrData.fiscalNumber} fd=${qrData.fiscalDocument}")

                val existing = receiptRepository.findExistingReceipt(
                    fn = qrData.fiscalNumber,
                    fd = qrData.fiscalDocument,
                    fp = qrData.fiscalSign
                )
                if (existing != null) {
                    AppLogger.i("Scanner", "FNS duplicate: receipt #${existing.id}")
                    _event.value = Event(ScannerEvent.AlreadyExists(existing.id))
                    return@launch
                }

                val raw = receiptRepository.saveRaw(json)
                val receipt = receiptRepository.createReceiptFromQr(raw.id, qrData)
                if (receipt == null) {
                    _event.value = Event(ScannerEvent.Error("Не удалось сохранить чек"))
                    return@launch
                }

                AppLogger.i("Scanner", "FNS receipt #${receipt.id} created, updating directly")
                _event.value = Event(ScannerEvent.Saved(receipt.id))

                val fetched = fnsShareParser.toFetchedReceipt(response)
                receiptRepository.saveFetchedData(receipt, fetched)
                _event.value = Event(ScannerEvent.CheckSuccess)
            } catch (e: Exception) {
                AppLogger.e("Scanner", "FNS JSON processing failed", e)
                _event.value = Event(ScannerEvent.Error("Ошибка обработки: ${e.localizedMessage ?: "неизвестная"}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
