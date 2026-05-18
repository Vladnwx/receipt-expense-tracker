package com.qrcode.scanner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.repository.AppUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateCheckResult(
    val isAvailable: Boolean,
    val latestVersion: String?,
    val downloadUrl: String?,
    val releaseNotes: String?,
    val isMandatory: Boolean
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateRepository: AppUpdateRepository
) : ViewModel() {

    fun checkUpdate(onResult: (UpdateCheckResult) -> Unit) {
        viewModelScope.launch {
            try {
                val info = updateRepository.checkForUpdate()
                onResult(
                    UpdateCheckResult(
                        isAvailable = info != null,
                        latestVersion = info?.latestVersion,
                        downloadUrl = info?.downloadUrl,
                        releaseNotes = info?.releaseNotes,
                        isMandatory = info?.isMandatory ?: false
                    )
                )
            } catch (_: Exception) {
                onResult(
                    UpdateCheckResult(false, null, null, null, false)
                )
            }
        }
    }
}
