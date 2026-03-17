@file:Suppress("DEPRECATION")

package com.example.autolog_20.ui.theme.data.locale

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

object TokenManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_CURRENT_TIRES = "current_tires"
    private const val KEY_TIRES_LAST_CHECK = "tires_last_check_timestamp"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveTokens(access: String, refresh: String?, userId: Int, username: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, access)
                .putString(KEY_REFRESH_TOKEN, refresh)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank() && getUserId() != -1

    fun setCurrentTires(tireType: String) {
        prefs.edit {
            putString(KEY_CURRENT_TIRES, tireType)
                .putLong(KEY_TIRES_LAST_CHECK, System.currentTimeMillis())
        }
        Log.d("TokenManager", "Сохранён тип резины: $tireType")
    }

    fun getCurrentTires(): String? {
        val type = prefs.getString(KEY_CURRENT_TIRES, null)
        Log.d("TokenManager", "Получен тип резины: $type")
        return type
    }

    fun shouldShowTireDialog(): Boolean {
        val hasTireType = !getCurrentTires().isNullOrBlank()
        Log.d("TokenManager", "Нужно показать диалог выбора резины? $!hasTireType")
        return !hasTireType
    }

    fun clearTokens() {
        prefs.edit()
            .clear()
            .remove(KEY_CURRENT_TIRES)
            .remove(KEY_TIRES_LAST_CHECK)
            .apply()
        Log.d("TokenManager", "Все токены и данные о резине очищены при выходе")
    }
}