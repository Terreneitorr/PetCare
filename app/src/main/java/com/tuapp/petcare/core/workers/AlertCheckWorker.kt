package com.tuapp.petcare.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tuapp.petcare.features.appointments.domain.repositories.AppointmentsRepository
import com.tuapp.petcare.features.medical.domain.repositories.MedicalRepository
import com.tuapp.petcare.features.reminders.domain.repositories.RemindersRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class AlertCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val medicalRepository: MedicalRepository,
    private val appointmentsRepository: AppointmentsRepository,
    private val remindersRepository: RemindersRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "alert_check_work"
        private const val H24 = 24 * 60 * 60 * 1000L
        private val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
        private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    override suspend fun doWork(): Result {
        return try {
            val now   = System.currentTimeMillis()
            val limit = now + H24
            checkVaccines()
            checkAppointments(now, limit)
            checkReminders(now, limit)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    // ── Vacunas próximas a vencer en 1 día ───────────────────────────────
    private suspend fun checkVaccines() {
        val vaccines = medicalRepository.getUpcomingVaccines(1).firstOrNull() ?: return
        vaccines.forEachIndexed { i, vaccine ->
            NotificationHelper.showNotification(
                context   = applicationContext,
                channelId = NotificationHelper.CHANNEL_VACCINES,
                notifId   = 5000 + i,
                title     = "💉 Vacuna próxima — ${vaccine.name}",
                body      = "La vacuna ${vaccine.name} vence el ${vaccine.nextDoseDate}. " +
                        "Veterinario: ${vaccine.veterinarian}. ¡Agenda tu cita!"
            )
        }
    }

    // ── Citas en las próximas 24 horas ────────────────────────────────────
    private suspend fun checkAppointments(now: Long, limit: Long) {
        val appointments = appointmentsRepository
            .getUpcomingAppointments().firstOrNull() ?: return

        appointments
            .filter { !it.isCompleted && it.dateTimeMillis in now..limit }
            .forEachIndexed { i, appt ->
                val timeStr = timeFmt.format(Date(appt.dateTimeMillis))
                NotificationHelper.showNotification(
                    context   = applicationContext,
                    channelId = NotificationHelper.CHANNEL_APPTS,
                    notifId   = 6000 + i,
                    title     = "📅 Cita mañana — ${appt.petName}",
                    body      = "${appt.title} con Dr. ${appt.veterinarian} " +
                            "a las $timeStr. ¡No olvides llevar a ${appt.petName}!"
                )
            }
    }

    // ── Recordatorios en las próximas 24 horas ───────────────────────────
    private suspend fun checkReminders(now: Long, limit: Long) {
        val reminders = remindersRepository.getActiveReminders()

        reminders
            .filter { it.isActive && it.triggerAtMillis in now..limit }
            .forEachIndexed { i, reminder ->
                val timeStr = timeFmt.format(Date(reminder.triggerAtMillis))
                NotificationHelper.showNotification(
                    context   = applicationContext,
                    channelId = NotificationHelper.CHANNEL_REMINDERS,
                    notifId   = 7000 + i,
                    title     = "🔔 Recordatorio mañana — ${reminder.petName}",
                    body      = "${reminder.title} programado para las $timeStr"
                )
            }
    }
}