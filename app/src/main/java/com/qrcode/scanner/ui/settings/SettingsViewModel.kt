package com.qrcode.scanner.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.AccountEntity
import com.qrcode.scanner.data.reporter.AppLogger
import com.qrcode.scanner.data.repository.AccountRepository
import com.qrcode.scanner.data.repository.AppUpdateRepository
import com.qrcode.scanner.data.repository.PreferencesRepository
import com.qrcode.scanner.data.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val isMandatory: Boolean = false,
    val proverkachekaToken: String = "",
    val showTokenDialog: Boolean = false,
    val tokenSaved: Boolean = false,
    val logCopied: Boolean = false,
    val logCleared: Boolean = false,
    val accounts: List<AccountEntity> = emptyList(),
    val defaultAccountId: Long? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateRepository: AppUpdateRepository,
    private val tokenRepository: TokenRepository,
    private val accountRepository: AccountRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            proverkachekaToken = tokenRepository.getToken() ?: "",
            defaultAccountId = preferencesRepository.getDefaultAccountId()
        )
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val accounts = accountRepository.getAll()
            _uiState.value = _uiState.value.copy(accounts = accounts)
        }
    }

    fun setDefaultAccount(accountId: Long?) {
        preferencesRepository.setDefaultAccountId(accountId)
        _uiState.value = _uiState.value.copy(defaultAccountId = accountId)
    }

    fun showTokenDialog() {
        _uiState.value = _uiState.value.copy(showTokenDialog = true)
    }

    fun dismissTokenDialog() {
        _uiState.value = _uiState.value.copy(showTokenDialog = false)
    }

    fun onTokenInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(proverkachekaToken = value)
    }

    fun saveToken() {
        viewModelScope.launch {
            val token = _uiState.value.proverkachekaToken.trim()
            if (token.isNotBlank()) {
                tokenRepository.saveToken(token)
            } else {
                tokenRepository.clearToken()
            }
            _uiState.value = _uiState.value.copy(
                showTokenDialog = false,
                tokenSaved = true
            )
        }
    }

    fun consumeTokenSaved() {
        _uiState.value = _uiState.value.copy(tokenSaved = false)
    }

    fun checkUpdate() {
        AppLogger.i("SettingsVM", "checkUpdate started")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = UpdateStatus.Checking, message = "Проверка обновлений…")
            try {
                val apiUrl = "https://api.github.com/repos/Vladnwx/receipt-expense-tracker/releases/latest"
                AppLogger.d("SettingsVM", "calling checkForUpdate")
                val info = updateRepository.checkForUpdate()
                AppLogger.i("SettingsVM", "checkForUpdate: available=${info.isAvailable} latest=${info.latestVersion} current=${info.currentVersion}")
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
                AppLogger.e("SettingsVM", "checkUpdate failed", e)
                _uiState.value = _uiState.value.copy(
                    status = UpdateStatus.Error,
                    message = "Ошибка проверки: ${e.localizedMessage ?: "неизвестная"}"
                )
            }
        }
    }

    fun copyLog() {
        val logText = AppLogger.getLogText()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("App Log", logText))
        _uiState.value = _uiState.value.copy(logCopied = true)
    }

    fun consumeLogCopied() {
        _uiState.value = _uiState.value.copy(logCopied = false)
    }

    fun clearLog() {
        AppLogger.clearLog()
        _uiState.value = _uiState.value.copy(logCleared = true)
    }

    fun consumeLogCleared() {
        _uiState.value = _uiState.value.copy(logCleared = false)
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(status = UpdateStatus.Idle, message = null)
    }
}
