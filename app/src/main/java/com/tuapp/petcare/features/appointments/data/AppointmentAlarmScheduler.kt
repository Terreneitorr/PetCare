package com.tuapp.petcare.features.appointments.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tuapp.petcare.features.reminders.data.ReminderBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Programa alarma 24h ANTES de la cita
    fun scheduleAppointmentAlert(
        appointmentId: String,
        petName: String,
        title: String,
        veterinarian: String,
        dateTimeMillis: Long
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        val alertTime = dateTimeMillis - 24 * 60 * 60 * 1000L
        if (alertTime <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("title", "📅 Cita mañana — $petName")
            putExtra("description", "$title con Dr. $veterinarian")
            putExtra("petName", petName)
            putExtra("notifId", appointmentId.hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alertTime,
            pendingIntent
        )
    }

    fun cancelAppointmentAlert(appointmentId: String) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}