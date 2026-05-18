package com.qrcode.scanner.ui.update

import android.content.Context
import androidx.lifecycle.ViewModel
import com.qrcode.scanner.data.repository.AppUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: AppUpdateRepository
) : ViewModel() {

    fun startDownload(context: Context, url: String, fileName: String): Long {
        return updateRepository.downloadApk(url, fileName)
    }

    fun onDownloadComplete(context: Context, version: String) {
        val fileName = "receipt-expense-tracker-$version.apk"
        updateRepository.installApk(fileName)
    }

    fun canInstall(context: Context): Boolean {
        return updateRepository.canRequestInstallPermission()
    }

    fun openInstallSettings(context: Context) {
        updateRepository.openInstallSettings()
    }
}
