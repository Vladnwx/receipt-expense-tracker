package com.qrcode.scanner.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET

interface GitHubReleaseApi {

    @GET("repos/Vladnwx/receipt-expense-tracker/releases/latest")
    suspend fun getLatestRelease(): Response<GitHubRelease>
}

data class GitHubRelease(
    @SerializedName("tag_name")
    val tagName: String = "",
    val body: String? = null,
    val assets: List<GitHubAsset> = emptyList()
) {
    data class GitHubAsset(
        val name: String = "",
        @SerializedName("browser_download_url")
        val browserDownloadUrl: String = "",
        @SerializedName("content_type")
        val contentType: String = ""
    )
}
