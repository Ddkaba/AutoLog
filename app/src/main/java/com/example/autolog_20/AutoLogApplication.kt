package com.example.autolog_20

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.example.autolog_20.ui.theme.data.locale.LocaleManager
import com.example.autolog_20.ui.theme.data.locale.SettingsManager

class AutoLogApplication : Application() {

    override fun attachBaseContext(base: Context) {
        SettingsManager.init(base)
        val language = SettingsManager.getLanguage()
        super.attachBaseContext(LocaleManager.setLocale(base, language))
    }

    override fun onCreate() {
        super.onCreate()
        SettingsManager.init(this)

        SettingsManager.setLanguageChangeListener {
            restartApp()
        }
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        currentActivity?.finishAffinity()
    }

    companion object {
        var currentActivity: Activity? = null
    }
}