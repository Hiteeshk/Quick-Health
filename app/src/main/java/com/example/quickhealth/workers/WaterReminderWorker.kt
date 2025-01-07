package com.example.quickhealth.workers

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.quickhealth.R
import com.example.quickhealth.MainActivity
import com.example.quickhealth.ui.navigation.Destinations
import com.example.quickhealth.utils.SettingsManager
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue


class WaterReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("WaterReminder", "Worker started")
        
        if (!SettingsManager.areNotificationsEnabled(context)) {
            Log.d("WaterReminder", "Notifications are disabled")
            return Result.success()
        }

        val waterAmount = inputData.getInt("waterAmount", 0)
        val scheduledTime = inputData.getString("scheduledTime")
        Log.d("WaterReminder", "Processing reminder for $scheduledTime with amount $waterAmount")

        // Check if this notification should still be shown
        if (shouldShowNotification(scheduledTime)) {
            showNotification(waterAmount)
            Log.d("WaterReminder", "Notification shown")
        } else {
            Log.d("WaterReminder", "Notification skipped - outside time window")
        }

        return Result.success()
    }

    @SuppressLint("NewApi")
    private fun shouldShowNotification(scheduledTime: String?): Boolean {
        if (scheduledTime == null) return false

        val scheduled = LocalTime.parse(scheduledTime)
        val now = LocalTime.now()
        
        // Only show notification if it's within 1 minute of scheduled time
        val minutesDifference = ChronoUnit.MINUTES.between(scheduled, now).absoluteValue
        return minutesDifference <= 1
    }

    @SuppressLint("NewApi")
    private fun showNotification(waterAmount: Int) {
        // Use the notification ID from input data, or generate one if not available
        val notificationId = inputData.getInt("notificationId", LocalTime.now().toSecondOfDay())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if a notification with this ID already exists
        if (notificationManager.activeNotifications.any { it.id == notificationId }) {
            Log.d("WaterReminder", "Notification with ID $notificationId already exists, skipping")
            return
        }

        createNotificationChannel(notificationManager)
        val intent = createNotificationIntent(waterAmount)

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Time to Hydrate!")
            .setContentText(String.format("Drink %.1fL of water now", waterAmount / 1000f))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true) // Prevent multiple alerts for the same notification
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d("WaterReminder", "Showing notification with ID $notificationId")
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to drink water"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotificationIntent(waterAmount: Int): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "add_water_intake/${waterAmount.toFloat()}")
        }
    }

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val TAG = "water_reminder"
    }
}