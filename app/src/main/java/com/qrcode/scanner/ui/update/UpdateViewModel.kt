package com.qrcode.scanner.ui.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.repository.AppUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DownloadState {
    Idle, Downloading, Downloaded
}

data class UpdateUiState(
    val downloadState: DownloadState = DownloadState.Idle,
    val canInstall: Boolean = false
)

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: AppUpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    fun startDownload(context: Context, url: String, fileName: String): Long {
        _uiState.value = _uiState.value.copy(downloadState = DownloadState.Downloading)
        return updateRepository.downloadApk(url, fileName)
    }

    fun onDownloadComplete(context: Context) {
        _uiState.value = _uiState.value.copy(
            downloadState = DownloadState.Downloaded,
            canInstall = updateRepository.canRequestInstallPermission()
        )
    }

    fun installApk(context: Context, version: String) {
        val fileName = "receipt-expense-tracker-$version.apk"
        updateRepository.installApk(fileName)
    }

    fun checkInstallPermission(context: Context) {
        _uiState.value = _uiState.value.copy(
            canInstall = updateRepository.canRequestInstallPermission()
        )
    }

    fun openInstallSettings(context: Context) {
        updateRepository.openInstallSettings()
    }
}
