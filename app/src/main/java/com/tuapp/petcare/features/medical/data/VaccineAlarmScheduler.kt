package com.tuapp.petcare.features.medical.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tuapp.petcare.features.reminders.data.ReminderBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccineAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun scheduleVaccineAlert(
        vaccineId: String,
        vaccineName: String,
        veterinarian: String,
        nextDoseDate: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        val doseDate = try {
            dateFmt.parse(nextDoseDate)?.time ?: return
        } catch (e: Exception) { return }

        // Alarma 24h antes de la próxima dosis
        val alertTime = doseDate - 24 * 60 * 60 * 1000L
        if (alertTime <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("title", "💉 Vacuna mañana — $vaccineName")
            putExtra("description", "Próxima dosis de $vaccineName. Dr. $veterinarian")
            putExtra("petName", "")
            putExtra("notifId", vaccineId.hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            vaccineId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alertTime,
            pendingIntent
        )
    }

    fun cancelVaccineAlert(vaccineId: String) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            vaccineId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}