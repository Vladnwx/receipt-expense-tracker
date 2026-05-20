package com.qrcode.scanner.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.AccountEntity
import com.qrcode.scanner.BuildConfig
import com.qrcode.scanner.data.reporter.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.qrcode.scanner.data.reporter.GitHubIssueReporter
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
    val showGitHubTokenDialog: Boolean = false,
    val githubIssuesToken: String = "",
    val tokenSaved: Boolean = false,
    val githubTokenSaved: Boolean = false,
    val logCopied: Boolean = false,
    val logCleared: Boolean = false,
    val logSent: Boolean = false,
    val accounts: List<AccountEntity> = emptyList(),
    val defaultAccountId: Long? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateRepository: AppUpdateRepository,
    private val tokenRepository: TokenRepository,
    private val accountRepository: AccountRepository,
    private val preferencesRepository: PreferencesRepository,
    private val githubIssueReporter: GitHubIssueReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        val pkToken = tokenRepository.getToken()
        var ghToken = tokenRepository.getGitHubIssuesToken()
        if (ghToken.isNullOrBlank()) {
            val fromBuildConfig = BuildConfig.GITHUB_ISSUES_TOKEN
            if (fromBuildConfig.isNotBlank()) {
                tokenRepository.saveGitHubIssuesToken(fromBuildConfig)
                ghToken = fromBuildConfig
            }
        }
        _uiState.value = _uiState.value.copy(
            proverkachekaToken = pkToken ?: "",
            githubIssuesToken = ghToken ?: "",
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

    fun showGitHubTokenDialog() {
        _uiState.value = _uiState.value.copy(showGitHubTokenDialog = true)
    }

    fun dismissGitHubTokenDialog() {
        _uiState.value = _uiState.value.copy(showGitHubTokenDialog = false)
    }

    fun onGitHubTokenInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(githubIssuesToken = value)
    }

    fun saveGitHubToken() {
        viewModelScope.launch {
            val token = _uiState.value.githubIssuesToken.trim()
            if (token.isNotBlank()) {
                tokenRepository.saveGitHubIssuesToken(token)
            } else {
                tokenRepository.clearGitHubIssuesToken()
            }
            _uiState.value = _uiState.value.copy(
                showGitHubTokenDialog = false,
                githubTokenSaved = true
            )
        }
    }

    fun consumeGitHubTokenSaved() {
        _uiState.value = _uiState.value.copy(githubTokenSaved = false)
    }

    fun testCreateIssue() {
        AppLogger.i("SettingsVM", "testCreateIssue started")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = UpdateStatus.Checking, message = "Создание тестового issue…")
            try {
                val success = githubIssueReporter.reportIssue(
                    title = "Тестовый issue из настроек",
                    details = "Создан пользователем через кнопку в настройках приложения.\nВерсия: ${BuildConfig.VERSION_NAME}",
                    throwable = null
                )
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.UpToDate,
                        message = "Тестовый issue создан"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.Error,
                        message = "Ошибка: токен не настроен или запрос не удался"
                    )
                }
            } catch (e: Exception) {
                AppLogger.e("SettingsVM", "testCreateIssue failed", e)
                _uiState.value = _uiState.value.copy(
                    status = UpdateStatus.Error,
                    message = "Ошибка: ${e.localizedMessage ?: "неизвестная"}"
                )
            }
        }
    }

    fun checkUpdate() {
        AppLogger.i("SettingsVM", "checkUpdate started")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = UpdateStatus.Checking, message = "Проверка обновлений…")
            try {
                AppLogger.d("SettingsVM", "calling checkForUpdate")
                val info = withContext(Dispatchers.IO) {
                    updateRepository.checkForUpdate()
                }
                handleUpdateResult(info)
            } catch (e: ClassCastException) {
                AppLogger.w("SettingsVM", "checkUpdate: CameraX ClassCastException, retrying on IO")
                try {
                    val info = withContext(Dispatchers.IO) {
                        updateRepository.checkForUpdate()
                    }
                    handleUpdateResult(info)
                } catch (e2: Exception) {
                    AppLogger.e("SettingsVM", "checkUpdate retry failed", e2)
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.Error,
                        message = "Ошибка проверки: ${e2.localizedMessage ?: "неизвестная"}"
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

    fun sendErrorLogsToIssue() {
        AppLogger.i("SettingsVM", "sendErrorLogsToIssue started")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = UpdateStatus.Checking, message = "Отправка ошибок в GitHub Issues…")
            try {
                val logText = AppLogger.getErrorLogText()
                if (logText.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.Error,
                        message = "Нет ошибок для отправки"
                    )
                    return@launch
                }
                val success = githubIssueReporter.reportIssue(
                    title = "Ошибки из лога приложения",
                    details = "Автоматическая отправка логов ошибок.\n\nВерсия: ${BuildConfig.VERSION_NAME}\n\n## Лог ошибок\n\n```\n$logText\n```",
                    throwable = null
                )
                if (success) {
                    AppLogger.clearLog()
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.UpToDate,
                        message = "Ошибки отправлены в Issue, лог очищен",
                        logSent = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        status = UpdateStatus.Error,
                        message = "Ошибка: токен не настроен или запрос не удался"
                    )
                }
            } catch (e: Exception) {
                AppLogger.e("SettingsVM", "sendErrorLogsToIssue failed", e)
                _uiState.value = _uiState.value.copy(
                    status = UpdateStatus.Error,
                    message = "Ошибка: ${e.localizedMessage ?: "неизвестная"}"
                )
            }
        }
    }

    fun consumeLogSent() {
        _uiState.value = _uiState.value.copy(logSent = false)
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(status = UpdateStatus.Idle, message = null)
    }

    private fun handleUpdateResult(info: com.qrcode.scanner.domain.update.AppUpdateChecker.UpdateResult) {
        val apiUrl = "https://api.github.com/repos/Vladnwx/receipt-expense-tracker/releases/latest"
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
    }
}
