package com.qrcode.scanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.repository.AppUpdateRepository
import com.qrcode.scanner.domain.fns.FnsAuthService
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
    val showSending: Boolean = false,
    val showCodeDialog: Boolean = false,
    val authErrorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateRepository: AppUpdateRepository,
    private val fnsAuthService: FnsAuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var pendingSessionId: String? = null

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val session = fnsAuthService.getActiveSession()
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
            _uiState.value = _uiState.value.copy(showPhoneDialog = false, showSending = true, authErrorMessage = null)
            try {
                val result = fnsAuthService.requestCode(phone)
                pendingSessionId = result.sessionId
                _uiState.value = _uiState.value.copy(showSending = false, showCodeDialog = true)
            } catch (e: FnsAuthService.AuthError) {
                val msg = when (e) {
                    FnsAuthService.AuthError.NetworkError -> "Ошибка сети, проверьте подключение"
                    else -> "Ошибка: ${e}"
                }
                _uiState.value = _uiState.value.copy(showSending = false, authErrorMessage = msg)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showSending = false,
                    authErrorMessage = "Ошибка: ${e.localizedMessage ?: "неизвестная"}"
                )
            }
        }
    }

    fun dismissCodeDialog() {
        _uiState.value = _uiState.value.copy(showCodeDialog = false)
    }

    fun submitCode(code: String) {
        val sessionId = pendingSessionId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showCodeDialog = false, showSending = true, authErrorMessage = null)
            try {
                fnsAuthService.confirmCode(sessionId, code)
                pendingSessionId = null
                _uiState.value = _uiState.value.copy(showSending = false)
                checkAuthStatus()
            } catch (e: FnsAuthService.AuthError) {
                val msg = when (e) {
                    FnsAuthService.AuthError.InvalidCredentials -> "Неверный код"
                    FnsAuthService.AuthError.ExpiredCode -> "Код истёк, запросите новый"
                    FnsAuthService.AuthError.NetworkError -> "Ошибка сети, проверьте подключение"
                    is FnsAuthService.AuthError.ServiceError -> e.description
                }
                _uiState.value = _uiState.value.copy(showSending = false, authErrorMessage = msg)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showSending = false,
                    authErrorMessage = "Ошибка: ${e.localizedMessage ?: "неизвестная"}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val session = fnsAuthService.getActiveSession()
            fnsAuthService.logout(session?.sessionId ?: "")
            _uiState.value = _uiState.value.copy(fnsAuthState = FnsAuthState.NotLoggedIn)
        }
    }

    fun checkUpdate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = UpdateStatus.Checking, message = "Проверка обновлений…")
            try {
                val apiUrl = "https://api.github.com/repos/Vladnwx/receipt-expense-tracker/releases/latest"
                val info = updateRepository.checkForUpdate()
                val current = info.currentVersion ?: "неизвестно"
                val latest = info.latestVersion ?: "неизвестно"
                val detailMsg = buildString {
                    append("Текущая: $current, последняя: $latest")
                    if (info.isAvailable && !info.downloadUrl.isNullOrBlank()) {
                        append(" — ДА, обновление доступно!")
                    } else if (info.isAvailable) {
                        append(" — версия новее, но APK не найден в релизе")
                    } else {
                        append(" — версия актуальна")
                    }
                    append("\nРелиз: $apiUrl")
                }
                if (info.isAvailable && !info.downloadUrl.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.Available,
                        message = detailMsg,
                        latestVersion = info.latestVersion,
                        downloadUrl = info.downloadUrl,
                        releaseNotes = info.releaseNotes,
                        isMandatory = info.isMandatory
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.UpToDate,
                        message = detailMsg
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
