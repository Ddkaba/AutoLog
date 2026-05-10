package com.example.autolog_20.ui.theme.data.tracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import kotlinx.coroutines.runBlocking

class TrackingActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "FINISH_TRIP") {
            val finishIntent = Intent(context, SimpleTrackingService::class.java)
            finishIntent.action = "FINISH_TRIP_NOW"
            context.startService(finishIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(100)
        }
    }
}