package com.qrcode.scanner.data.repository

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.qrcode.scanner.BuildConfig
import com.qrcode.scanner.data.remote.GitHubReleaseApi
import com.qrcode.scanner.domain.update.AppUpdateChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRepository @Inject constructor(
    private val api: GitHubReleaseApi,
    @ApplicationContext private val context: Context
) : AppUpdateChecker {

    override suspend fun checkForUpdate(): UpdateResult {
        return try {
            val release = api.getLatestRelease()
            val latestTag = release.tagName.removePrefix("v")
            val currentVersion = BuildConfig.VERSION_NAME

            val isNewer = compareVersions(latestTag, currentVersion) > 0

            val releaseApk = release.assets.firstOrNull { asset ->
                asset.name.endsWith(".apk") && asset.name.contains("release")
            }

            UpdateResult(
                isAvailable = isNewer,
                latestVersion = latestTag,
                currentVersion = currentVersion,
                downloadUrl = releaseApk?.browserDownloadUrl,
                releaseNotes = release.body,
                isMandatory = shouldBeMandatory(currentVersion, latestTag)
            )
        } catch (e: Exception) {
            UpdateResult(
                isAvailable = false,
                latestVersion = null,
                currentVersion = BuildConfig.VERSION_NAME,
                downloadUrl = null,
                releaseNotes = null,
                isMandatory = false
            )
        }
    }

    fun downloadApk(downloadUrl: String, fileName: String): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("ReceiptExpenseTracker")
            .setDescription("Скачивание обновления…")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setMimeType("application/vnd.android.package-archive")

        return downloadManager.enqueue(request)
    }

    fun installApk(fileName: String) {
        val file = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            java.io.File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    private fun shouldBeMandatory(current: String, latest: String): Boolean {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }

        if (latestParts.size < 2 || currentParts.size < 2) return false

        val currentMajor = currentParts.getOrElse(0) { 0 }
        val latestMajor = latestParts.getOrElse(0) { 0 }

        return latestMajor > currentMajor
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }
}
