package com.qrcode.scanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.repository.AppUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UpdateStatus {
    Idle, Checking, Available, UpToDate, Error
}

data class SettingsUiState(
    val status: UpdateStatus = UpdateStatus.Idle,
    val message: String? = null,
    val latestVersion: String? = null,
    val downloadUrl: String? = null,
    val releaseNotes: String? = null,
    val isMandatory: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateRepository: AppUpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun checkUpdate() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(status = UpdateStatus.Checking, message = "Проверка обновлений…")
            try {
                val info = updateRepository.checkForUpdate()
                if (info.isAvailable && !info.downloadUrl.isNullOrBlank()) {
                    _uiState.value = SettingsUiState(
                        status = UpdateStatus.Available,
                        message = "Доступна версия ${info.latestVersion}",
                        latestVersion = info.latestVersion,
                        downloadUrl = info.downloadUrl,
                        releaseNotes = info.releaseNotes,
                        isMandatory = info.isMandatory
                    )
                } else {
                    val current = info.currentVersion ?: "неизвестно"
                    _uiState.value = SettingsUiState(
                        status = UpdateStatus.UpToDate,
                        message = "У вас актуальная версия $current"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState(
                    status = UpdateStatus.Error,
                    message = "Ошибка проверки: ${e.localizedMessage ?: "неизвестная"}"
                )
            }
        }
    }

    fun consumeMessage() {
        _uiState.value = SettingsUiState()
    }
}
