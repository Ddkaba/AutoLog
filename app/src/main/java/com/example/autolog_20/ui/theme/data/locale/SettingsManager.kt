package com.example.autolog_20.ui.theme.data.locale

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SettingsManager {
    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_THEME = "theme"
    private const val KEY_GPS_MILEAGE = "gps_mileage"

    private const val KEY_GPS_TRACKING_ENABLED = "gps_tracking_enabled"
    private const val KEY_SHOW_ASSIGN_TRIPS = "show_assign_trips"

    private lateinit var prefs: SharedPreferences
    private var languageChangeListener: (() -> Unit)? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setLanguageChangeListener(listener: (() -> Unit)?) {
        languageChangeListener = listener
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "ru") ?: "ru"

    fun setLanguage(language: String) {
        prefs.edit { putString(KEY_LANGUAGE, language) }
        languageChangeListener?.invoke()
    }

    fun getTheme(): String = prefs.getString(KEY_THEME, "dark") ?: "dark"

    fun setTheme(theme: String) {
        prefs.edit { putString(KEY_THEME, theme) }
    }

    fun isGpsMileageEnabled(): Boolean = prefs.getBoolean(KEY_GPS_MILEAGE, false)

    fun setGpsMileageEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_GPS_MILEAGE, enabled) }
    }

    fun isGpsTrackingEnabled(): Boolean = prefs.getBoolean(KEY_GPS_TRACKING_ENABLED, false)

    fun setGpsTrackingEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_GPS_TRACKING_ENABLED, enabled) }
    }

    fun shouldShowAssignTrips(): Boolean = prefs.getBoolean(KEY_SHOW_ASSIGN_TRIPS, true)

    fun setShowAssignTrips(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_ASSIGN_TRIPS, show) }
    }
}