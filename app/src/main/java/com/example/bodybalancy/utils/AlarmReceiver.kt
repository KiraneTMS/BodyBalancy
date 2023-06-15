package com.example.bodybalancy.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bodybalancy.R

class AlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("scheduleId", -1)
        Log.e("AlarmReceiver", "Triggered")

        // Check if the alarm is enabled in shared preferences
        val settingsManager = SettingsManager(context)
        val isAlarmEnabled = settingsManager.isAlarmEnabled()
        if (isAlarmEnabled) {
            // Play a sound
            playSound(context)

            // Show the alarm notification
            showAlarmNotification(context, scheduleId)
        }
    }

    private fun playSound(context: Context) {
        // Release any previously playing media player
        mediaPlayer?.release()

        // Create an AudioAttributes object to set the sound usage
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Create a new MediaPlayer and set the audio attributes
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(audioAttributes)

            // Set the alarm sound URI
            val alarmSoundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_sound}")
            setDataSource(context, alarmSoundUri)

            // Set the audio stream type to alarm
            setAudioStreamType(AudioManager.STREAM_ALARM)

            prepare()
            start()
        }
    }


    private fun showAlarmNotification(context: Context, scheduleId: Int) {
        // Create a PendingIntent for the dismiss action
        val dismissIntent = createDismissIntent(context, scheduleId)
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create a PendingIntent for the snooze action
        val snoozeIntent = createSnoozeIntent(context, scheduleId)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification channel
        createNotificationChannel(context)

        // Set the custom alarm sound
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Alarm")
            .setContentText("It's time for your schedule!")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent)
            .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(scheduleId, notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Channel for alarm notifications"

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun createDismissIntent(context: Context, scheduleId: Int): Intent {

        val intent = Intent(context, AlarmDismissReceiver::class.java)
        intent.action = ACTION_DISMISS_ALARM
        intent.putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        return intent
    }

    private fun createSnoozeIntent(context: Context, scheduleId: Int): Intent {

        val intent = Intent(context, AlarmSnoozeReceiver::class.java)
        intent.action = ACTION_SNOOZE_ALARM
        intent.putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        return intent
    }


    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val ACTION_DISMISS_ALARM = "com.example.bodybalancy.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.example.bodybalancy.ACTION_SNOOZE_ALARM"
        const val EXTRA_SCHEDULE_ID = "com.example.bodybalancy.EXTRA_SCHEDULE_ID"

        @JvmStatic
        private var mediaPlayer: MediaPlayer? = null
        fun stopSound() {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        }
    }
}

