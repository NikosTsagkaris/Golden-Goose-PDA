package com.ntvelop.goldengoosepda.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun saveRole(role: String) {
        prefs.edit().putString("user_role", role).apply()
    }

    fun getRole(): String? {
        return prefs.getString("user_role", "WAITER")
    }

    fun saveUserId(id: String) {
        prefs.edit().putString("user_id", id).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
