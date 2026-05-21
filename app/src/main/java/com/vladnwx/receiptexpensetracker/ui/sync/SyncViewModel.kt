package com.vladnwx.receiptexpensetracker.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.dao.CurrencyDao
import com.vladnwx.receiptexpensetracker.data.local.entity.CurrencyEntity
import com.vladnwx.receiptexpensetracker.data.repository.CurrencyRepository
import com.vladnwx.receiptexpensetracker.data.sync.NextCloudClient
import com.vladnwx.receiptexpensetracker.data.sync.SyncManager
import com.vladnwx.receiptexpensetracker.data.sync.SyncPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val currencies: List<CurrencyEntity> = emptyList(),
    val currencyLoading: Boolean = false,
    val currencyError: String? = null,
    val syncLoading: Boolean = false,
    val syncError: String? = null,
    val syncSuccess: String? = null,
    val connectionStatus: String? = null,
    val lastSyncAt: Long = 0L
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val syncManager: SyncManager,
    private val syncPreferences: SyncPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SyncUiState())
    val state = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(
            serverUrl = syncPreferences.serverUrl,
            username = syncPreferences.username,
            password = syncPreferences.password,
            lastSyncAt = syncPreferences.lastSyncAt
        )
        loadCurrencies()
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            _state.value = _state.value.copy(currencies = currencyRepository.getAll())
        }
    }

    fun updateUrl(value: String) { _state.value = _state.value.copy(serverUrl = value) }
    fun updateUsername(value: String) { _state.value = _state.value.copy(username = value) }
    fun updatePassword(value: String) { _state.value = _state.value.copy(password = value) }

    fun saveSettings() {
        val s = _state.value
        syncPreferences.serverUrl = s.serverUrl
        syncPreferences.username = s.username
        syncPreferences.password = s.password
    }

    fun testConnection() {
        val s = _state.value
        if (s.serverUrl.isBlank() || s.username.isBlank() || s.password.isBlank()) {
            _state.value = _state.value.copy(connectionStatus = "Заполните все поля")
            return
        }
        viewModelScope.launch {
            saveSettings()
            _state.value = _state.value.copy(connectionStatus = "Проверка...", syncError = null)
            val client = NextCloudClient(s.serverUrl, s.username, s.password)
            client.testConnection().onSuccess {
                _state.value = _state.value.copy(connectionStatus = "Подключение успешно")
            }.onFailure { e ->
                _state.value = _state.value.copy(connectionStatus = "Ошибка: ${e.message}")
            }
        }
    }

    fun syncExport() {
        val s = _state.value
        viewModelScope.launch {
            _state.value = _state.value.copy(syncLoading = true, syncError = null, syncSuccess = null)
            try {
                val client = NextCloudClient(s.serverUrl, s.username, s.password)
                client.ensureDir("ReceiptExpenseTracker")
                val json = syncManager.exportAll()
                client.upload("ReceiptExpenseTracker/data.json", json)
                val now = System.currentTimeMillis()
                syncPreferences.lastSyncAt = now
                _state.value = _state.value.copy(
                    syncLoading = false,
                    syncSuccess = "Данные выгружены",
                    lastSyncAt = now
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(syncLoading = false, syncError = e.message)
            }
        }
    }

    fun syncImport() {
        val s = _state.value
        viewModelScope.launch {
            _state.value = _state.value.copy(syncLoading = true, syncError = null, syncSuccess = null)
            try {
                val client = NextCloudClient(s.serverUrl, s.username, s.password)
                val exists = client.exists("ReceiptExpenseTracker/data.json").getOrDefault(false)
                if (!exists) {
                    _state.value = _state.value.copy(syncLoading = false, syncError = "Нет сохранённых данных на сервере")
                    return@launch
                }
                val json = client.download("ReceiptExpenseTracker/data.json").getOrThrow()
                val count = syncManager.importAll(json).getOrThrow()
                val now = System.currentTimeMillis()
                syncPreferences.lastSyncAt = now
                _state.value = _state.value.copy(
                    syncLoading = false,
                    syncSuccess = "Импортировано $count записей",
                    lastSyncAt = now
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(syncLoading = false, syncError = e.message)
            }
        }
    }

    fun refreshCurrencies() {
        viewModelScope.launch {
            _state.value = _state.value.copy(currencyLoading = true, currencyError = null)
            try {
                currencyRepository.fetchAndSaveRates()
                _state.value = _state.value.copy(currencies = currencyRepository.getAll(), currencyLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(currencyLoading = false, currencyError = e.message)
            }
        }
    }
}
