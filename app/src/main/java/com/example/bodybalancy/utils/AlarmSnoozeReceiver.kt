package com.example.bodybalancy.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmSnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, -1)
        Log.e("AlarmSnoozeReceiver", "Snoozed: scheduleId=$scheduleId")

        // Cancel the notification and sound

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(scheduleId)
        // Stop the sound
        AlarmReceiver.stopSound()

        // Handle any additional logic here after snoozing the alarm
    }
}