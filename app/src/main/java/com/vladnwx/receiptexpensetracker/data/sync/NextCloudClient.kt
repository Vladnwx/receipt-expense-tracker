package com.vladnwx.receiptexpensetracker.data.sync

import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class NextCloudClient(
    private val baseUrl: String,
    username: String,
    password: String
) {
    private val auth = Credentials.basic(username, password)
    private val jsonMediaType = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun url(path: String): String {
        val clean = baseUrl.trimEnd('/')
        return "$clean/remote.php/dav/files/$username/$path"
    }

    private val username: String

    init {
        val parts = username.split("/")
        this.username = parts.last()
    }

    suspend fun ensureDir(path: String): Result<Unit> = runCatching {
        val req = Request.Builder()
            .url(url(path))
            .method("MKCOL", null)
            .header("Authorization", auth)
            .build()
        val resp = client.newCall(req).execute()
        if (resp.code != 201 && resp.code != 405) {
            throw Exception("Failed to create dir: ${resp.code}")
        }
    }

    suspend fun upload(path: String, json: String): Result<Unit> = runCatching {
        val body = json.toRequestBody(jsonMediaType)
        val req = Request.Builder()
            .url(url(path))
            .put(body)
            .header("Authorization", auth)
            .build()
        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) {
            throw Exception("Upload failed: ${resp.code}")
        }
    }

    suspend fun download(path: String): Result<String> = runCatching {
        val req = Request.Builder()
            .url(url(path))
            .get()
            .header("Authorization", auth)
            .build()
        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) {
            throw Exception("Download failed: ${resp.code}")
        }
        resp.body!!.string()
    }

    suspend fun exists(path: String): Result<Boolean> = runCatching {
        val req = Request.Builder()
            .url(url(path))
            .method("PROPFIND", null)
            .header("Authorization", auth)
            .header("Depth", "0")
            .build()
        val resp = client.newCall(req).execute()
        resp.code in listOf(200, 207)
    }

    suspend fun testConnection(): Result<Unit> = runCatching {
        val req = Request.Builder()
            .url(url(""))
            .method("PROPFIND", null)
            .header("Authorization", auth)
            .header("Depth", "0")
            .build()
        val resp = client.newCall(req).execute()
        if (resp.code !in listOf(200, 207, 301, 302, 401)) {
            throw Exception("Connection failed: ${resp.code}")
        }
        if (resp.code == 401) {
            throw Exception("Неверный логин или пароль")
        }
    }
}
