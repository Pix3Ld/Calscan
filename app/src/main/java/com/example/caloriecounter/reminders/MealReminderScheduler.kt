package com.example.caloriecounter.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.caloriecounter.MainActivity
import com.example.caloriecounter.R

object MealReminderScheduler {
    private const val ACTION_REMIND = "com.example.caloriecounter.action.MEAL_REMINDER"
    private const val EXTRA_MEAL_INDEX = "meal_index"
    private const val REQUEST_CODE_BASE = 4100

    fun hasExactAlarmAccess(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    fun scheduleAll(context: Context) {
        cancelAll(context)
        val settings = MealReminderSettingsStore(context).load()
        if (!settings.enabled || !hasExactAlarmAccess(context)) return

        settings.mealTimesMinutes.indices.forEach { mealIndex ->
            scheduleNext(context, mealIndex, settings)
        }
    }

    fun scheduleNext(context: Context, mealIndex: Int) {
        scheduleNext(context, mealIndex, MealReminderSettingsStore(context).load())
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        repeat(ReminderTimeCalculator.MAX_MEALS) { mealIndex ->
            alarmManager.cancel(reminderPendingIntent(context, mealIndex))
        }
    }

    internal fun isReminderAction(action: String?): Boolean = action == ACTION_REMIND

    internal fun mealIndex(intent: Intent): Int = intent.getIntExtra(EXTRA_MEAL_INDEX, -1)

    private fun scheduleNext(context: Context, mealIndex: Int, settings: MealReminderSettings) {
        if (!settings.enabled || mealIndex !in settings.mealTimesMinutes.indices) return
        if (!hasExactAlarmAccess(context)) return

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAtMillis = ReminderTimeCalculator.nextOccurrenceMillis(
            settings.mealTimesMinutes[mealIndex],
            System.currentTimeMillis()
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                reminderPendingIntent(context, mealIndex)
            )
        } catch (_: SecurityException) {
            cancelAll(context)
        }
    }

    private fun reminderPendingIntent(context: Context, mealIndex: Int): PendingIntent {
        val intent = Intent(context, MealReminderReceiver::class.java)
            .setAction(ACTION_REMIND)
            .setData(Uri.parse("caloriecounter://meal-reminder/$mealIndex"))
            .putExtra(EXTRA_MEAL_INDEX, mealIndex)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + mealIndex,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class MealReminderReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!MealReminderScheduler.isReminderAction(intent.action)) return

        val settings = MealReminderSettingsStore(context).load()
        val mealIndex = MealReminderScheduler.mealIndex(intent)
        if (!settings.enabled || mealIndex !in settings.mealTimesMinutes.indices) return

        MealReminderNotification.show(context, mealIndex)
        MealReminderScheduler.scheduleNext(context, mealIndex)
    }
}

class ReminderRestoreReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> MealReminderScheduler.scheduleAll(context)
        }
    }
}

private object MealReminderNotification {
    private const val CHANNEL_ID = "meal_reminders"
    private const val NOTIFICATION_ID_BASE = 5200

    fun show(context: Context, mealIndex: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return

        val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                android.app.NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.meal_reminder_channel),
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        val openAppIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_BASE + mealIndex,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.meal_reminder_title, mealIndex + 1))
            .setContentText(context.getString(R.string.meal_reminder_message))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_BASE + mealIndex, notification)
    }
}