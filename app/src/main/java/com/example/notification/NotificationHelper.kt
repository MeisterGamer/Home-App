package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT_REPAIRS,
                "Urgent Household Repairs",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alerts for leaks, electrical faults, and emergency repairs"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }

            val medChannel = NotificationChannel(
                CHANNEL_MEDICATION,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled daily reminders for senior medications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }

            val routineChannel = NotificationChannel(
                CHANNEL_DAILY_CHORES,
                "Daily Chores & Smart Home",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates on daily chore schedules and connected smart devices"
            }

            notificationManager.createNotificationChannel(urgentChannel)
            notificationManager.createNotificationChannel(medChannel)
            notificationManager.createNotificationChannel(routineChannel)
        }
    }

    fun showUrgentRepairNotification(title: String, message: String, notificationId: Int = 101) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT_REPAIRS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Android 13+ permission not yet granted; in-app alert will handle it
        }
    }

    fun showMedicationNotification(title: String, message: String, notificationId: Int = 201) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MEDICATION)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("💊 $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission handling
        }
    }

    companion object {
        const val CHANNEL_URGENT_REPAIRS = "channel_urgent_repairs"
        const val CHANNEL_MEDICATION = "channel_medication"
        const val CHANNEL_DAILY_CHORES = "channel_daily_chores"
    }
}
