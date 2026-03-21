package com.tuapp.petcare.features.reminders.data.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tuapp.petcare.features.reminders.data.ReminderBroadcastReceiver
import com.tuapp.petcare.features.reminders.data.datasources.local.ReminderDao
import com.tuapp.petcare.features.reminders.data.datasources.local.toDomain
import com.tuapp.petcare.features.reminders.data.datasources.local.toEntity
import com.tuapp.petcare.features.reminders.domain.entities.Reminder
import com.tuapp.petcare.features.reminders.domain.repositories.RemindersRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemindersRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    @ApplicationContext private val context: Context
) : RemindersRepository {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun getAllReminders(): Flow<List<Reminder>> =
        reminderDao.getAllReminders().map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveReminders(): List<Reminder> =
        reminderDao.getActiveReminders().map { it.toDomain() }

    override suspend fun scheduleReminder(reminder: Reminder) {
        // 1. Guarda en Room
        reminderDao.insertReminder(reminder.toEntity())

        // 2. Verifica permiso en Android 12+ antes de programar alarma exacta
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        // 3. Programa alarma con AlarmManager (hardware #3)
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("description", reminder.description)
            putExtra("petName", reminder.petName)
            putExtra("notifId", reminder.id.hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.triggerAtMillis,
            pendingIntent
        )
    }

    override suspend fun cancelReminder(id: String) {
        reminderDao.cancelReminder(id)

        // Cancela la alarma del sistema también
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    // ── elimina la alarma del sistema y el registro de Room ─────────
    override suspend fun deleteReminder(id: String) {
        // Primero cancela la alarma del sistema
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }

        // Luego elimina de Room
        reminderDao.deleteReminder(id)
    }
}