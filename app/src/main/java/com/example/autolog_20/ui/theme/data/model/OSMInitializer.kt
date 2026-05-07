package com.example.autolog_20.ui.theme.data.model

import android.content.Context

object OSMInitializer {
    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            org.osmdroid.config.Configuration.getInstance().load(
                context,
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            )
            isInitialized = true
        }
    }
}