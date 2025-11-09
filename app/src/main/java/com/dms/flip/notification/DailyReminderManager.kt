package com.dms.flip.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import java.util.Calendar
import androidx.core.content.edit

class DailyReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun schedule(time: String) {
        val (hour, minute) = parseTime(time)
        val normalizedTime = "%02d:%02d".format(hour, minute)
        preferences.edit { putString(KEY_SCHEDULED_TIME, normalizedTime) }

        val pendingIntent = createPendingIntent()
        alarmManager.cancel(pendingIntent)

        val triggerAtMillis = calculateTriggerTime(hour, minute)

        AlarmManagerCompat.setAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun rescheduleSavedReminder() {
        val savedTime = preferences.getString(KEY_SCHEDULED_TIME, null) ?: return
        schedule(savedTime)
    }

    fun cancel() {
        val pendingIntent = createPendingIntent()
        alarmManager.cancel(pendingIntent)
        preferences.edit { remove(KEY_SCHEDULED_TIME) }
    }

    companion object {
        private const val REQUEST_CODE = 123
        private const val PREFERENCES_NAME = "daily_reminder_preferences"
        private const val KEY_SCHEDULED_TIME = "scheduled_time"
        private const val DEFAULT_HOUR = 8
    }

    private fun parseTime(time: String): Pair<Int, Int> {
        val timeParts = time.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: DEFAULT_HOUR
        val minute = timeParts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return hour to minute
    }

    private fun calculateTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    private fun createPendingIntent(extraFlags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val flags = extraFlags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            flags
        )
    }
}
