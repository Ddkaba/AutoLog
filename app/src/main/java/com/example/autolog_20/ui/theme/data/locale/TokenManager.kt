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
    private const val KEY_CURRENT_TIRES_PREFIX = "current_tires_"
    private const val KEY_TIRES_LAST_CHECK_PREFIX = "tires_last_check_timestamp_"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"

    private const val PREF_FIRST_TIME_MAINTENANCE_PREFIX = "first_time_maintenance_"

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

    fun isFirstTimeOnMaintenance(numberPlate: String): Boolean {
        return prefs.getBoolean(PREF_FIRST_TIME_MAINTENANCE_PREFIX + numberPlate, true)
    }

    fun setMaintenanceInfoShown(numberPlate: String) {
        prefs.edit().putBoolean(PREF_FIRST_TIME_MAINTENANCE_PREFIX + numberPlate, false).apply()
    }

    fun clearMaintenanceData(numberPlate: String) {
        prefs.edit().remove(PREF_FIRST_TIME_MAINTENANCE_PREFIX + numberPlate).apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank() && getUserId() != -1

    fun setCurrentTires(numberPlate: String, tireType: String) {
        prefs.edit {
            putString(KEY_CURRENT_TIRES_PREFIX + numberPlate, tireType)
            putLong(KEY_TIRES_LAST_CHECK_PREFIX + numberPlate, System.currentTimeMillis())
        }
        Log.d("TokenManager", "Сохранён тип резины для авто $numberPlate: $tireType")
    }

    fun getCurrentTires(numberPlate: String): String? {
        val type = prefs.getString(KEY_CURRENT_TIRES_PREFIX + numberPlate, null)
        return type
    }


    fun shouldShowTireDialog(numberPlate: String): Boolean {
        val hasTireType = !getCurrentTires(numberPlate).isNullOrBlank()
        return !hasTireType
    }

    fun clearTireData(numberPlate: String) {
        prefs.edit {
            remove(KEY_CURRENT_TIRES_PREFIX + numberPlate)
            remove(KEY_TIRES_LAST_CHECK_PREFIX + numberPlate)
        }
    }

    fun clearTokens() {
        prefs.edit()
            .clear()
            .apply()
        Log.d("TokenManager", "Все токены и данные о резине очищены при выходе")
    }
}