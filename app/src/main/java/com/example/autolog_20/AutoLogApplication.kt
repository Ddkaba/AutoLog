package com.example.autolog_20

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.autolog_20.ui.theme.data.locale.LocaleManager
import com.example.autolog_20.ui.theme.data.locale.SettingsManager
import com.example.autolog_20.ui.theme.data.tracking.SyncWorker
import java.util.concurrent.TimeUnit

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
        setupSyncWorker()
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        currentActivity?.finishAffinity()
    }

    private fun setupSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_trips",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }


    companion object {
        var currentActivity: Activity? = null
    }
}