package com.qrcode.scanner.domain.update

interface AppUpdateChecker {

    suspend fun checkForUpdate(): UpdateResult

    data class UpdateResult(
        val isAvailable: Boolean,
        val latestVersion: String?,
        val currentVersion: String?,
        val downloadUrl: String?,
        val releaseNotes: String?,
        val isMandatory: Boolean
    )
}
