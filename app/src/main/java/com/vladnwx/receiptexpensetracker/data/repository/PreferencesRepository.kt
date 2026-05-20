package com.vladnwx.receiptexpensetracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secure_app_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getDefaultAccountId(): Long? {
        val id = prefs.getLong(KEY_DEFAULT_ACCOUNT_ID, -1L)
        return if (id == -1L) null else id
    }

    fun setDefaultAccountId(accountId: Long?) {
        prefs.edit().apply {
            if (accountId != null) {
                putLong(KEY_DEFAULT_ACCOUNT_ID, accountId)
            } else {
                remove(KEY_DEFAULT_ACCOUNT_ID)
            }
            apply()
        }
    }

    companion object {
        private const val KEY_DEFAULT_ACCOUNT_ID = "default_account_id"
    }
}
