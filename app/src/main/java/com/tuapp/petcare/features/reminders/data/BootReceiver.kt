package com.tuapp.petcare.features.reminders.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tuapp.petcare.core.database.PetCareDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Re-programa todas las alarmas activas después del reinicio
        CoroutineScope(Dispatchers.IO).launch {
            val db = PetCareDatabase.getDatabase(context)
            val activeReminders = db.reminderDao().getActiveReminders()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            activeReminders
                .filter { it.triggerAtMillis > System.currentTimeMillis() }
                .forEach { reminder ->
                    val alarmIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                        putExtra("title", reminder.title)
                        putExtra("description", reminder.description)
                        putExtra("petName", reminder.petName)
                        putExtra("notifId", reminder.id.hashCode())
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        reminder.id.hashCode(),
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.triggerAtMillis,
                        pendingIntent
                    )
                }
        }
    }
}
