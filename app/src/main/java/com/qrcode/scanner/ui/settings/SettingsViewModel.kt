package com.qrcode.scanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.repository.AppUpdateRepository
import com.qrcode.scanner.data.repository.FnsAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UpdateStatus {
    Idle, Checking, Available, UpToDate, Error
}

sealed class FnsAuthState {
    data object Loading : FnsAuthState()
    data object NotLoggedIn : FnsAuthState()
    data class LoggedIn(val phone: String) : FnsAuthState()
}

data class SettingsUiState(
    val status: UpdateStatus = UpdateStatus.Idle,
    val message: String? = null,
    val latestVersion: String? = null,
    val downloadUrl: String? = null,
    val releaseNotes: String? = null,
    val isMandatory: Boolean = false,
    val fnsAuthState: FnsAuthState = FnsAuthState.Loading,
    val showPhoneDialog: Boolean = false,
    val showCodeDialog: Boolean = false,
    val authErrorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateRepository: AppUpdateRepository,
    private val fnsAuthRepository: FnsAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val session = fnsAuthRepository.getActiveSession()
            _uiState.value = _uiState.value.copy(
                fnsAuthState = if (session != null) {
                    FnsAuthState.LoggedIn(phone = session.phone ?: session.sessionId)
                } else {
                    FnsAuthState.NotLoggedIn
                }
            )
        }
    }

    fun showPhoneDialog() {
        _uiState.value = _uiState.value.copy(showPhoneDialog = true, authErrorMessage = null)
    }

    fun dismissPhoneDialog() {
        _uiState.value = _uiState.value.copy(showPhoneDialog = false)
    }

    fun submitPhone(phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showPhoneDialog = false)
            // FnsAuthService.requestCode would be called here
            // For now, show code dialog directly
            _uiState.value = _uiState.value.copy(showCodeDialog = true, authErrorMessage = null)
        }
    }

    fun dismissCodeDialog() {
        _uiState.value = _uiState.value.copy(showCodeDialog = false)
    }

    fun submitCode(code: String) {
        viewModelScope.launch {
            try {
                // FnsAuthService.confirmCode would be called here
                // Mock: save a session to mark as logged in
                fnsAuthRepository.saveSession(
                    com.qrcode.scanner.data.local.entity.FnsSessionEntity(
                        sessionId = "manual",
                        phone = _uiState.value.let {
                            (it.fnsAuthState as? FnsAuthState.LoggedIn)?.phone ?: "+7"
                        }
                    )
                )
                _uiState.value = _uiState.value.copy(showCodeDialog = false)
                checkAuthStatus()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    authErrorMessage = "Ошибка авторизации: ${e.localizedMessage ?: "неизвестная"}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            fnsAuthRepository.deactivateAll()
            _uiState.value = _uiState.value.copy(fnsAuthState = FnsAuthState.NotLoggedIn)
        }
    }

    fun checkUpdate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = UpdateStatus.Checking, message = "Проверка обновлений…")
            try {
                val info = updateRepository.checkForUpdate()
                if (info.isAvailable && !info.downloadUrl.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.Available,
                        message = "Доступна версия ${info.latestVersion}",
                        latestVersion = info.latestVersion,
                        downloadUrl = info.downloadUrl,
                        releaseNotes = info.releaseNotes,
                        isMandatory = info.isMandatory
                    )
                } else {
                    val current = info.currentVersion ?: "неизвестно"
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.UpToDate,
                        message = "У вас актуальная версия $current"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = UpdateStatus.Error,
                    message = "Ошибка проверки: ${e.localizedMessage ?: "неизвестная"}"
                )
            }
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(status = UpdateStatus.Idle, message = null)
    }
}
