package com.qrcode.scanner.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secure_token_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getToken(): String? = prefs.getString(KEY_PROVERKACHEKA_TOKEN, null)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_PROVERKACHEKA_TOKEN, token).apply()
    }

    fun clearToken() {
        prefs.edit().remove(KEY_PROVERKACHEKA_TOKEN).apply()
    }

    fun hasToken(): Boolean = !getToken().isNullOrBlank()

    companion object {
        private const val KEY_PROVERKACHEKA_TOKEN = "proverkacheka_token"
    }
}
