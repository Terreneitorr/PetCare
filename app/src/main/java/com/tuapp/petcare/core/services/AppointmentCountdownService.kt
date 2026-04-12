package com.tuapp.petcare.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tuapp.petcare.features.appointments.domain.repositories.AppointmentsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AppointmentCountdownService : Service() {

    @Inject
    lateinit var appointmentsRepository: AppointmentsRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var countdownJob: Job? = null

    companion object {
        const val CHANNEL_ID = "appointment_countdown_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        fun startService(context: Context) {
            val intent = Intent(context, AppointmentCountdownService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AppointmentCountdownService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startCountdown()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startCountdown() {
        // Inicia como foreground inmediatamente con notificación base
        startForeground(NOTIFICATION_ID, buildNotification("Buscando citas próximas..."))

        countdownJob = serviceScope.launch {
            while (true) {
                val upcomingAppointments = appointmentsRepository
                    .getUpcomingAppointments()
                    .firstOrNull()

                if (upcomingAppointments.isNullOrEmpty()) {
                    updateNotification("Sin citas en las próximas 24 horas")
                } else {
                    val next = upcomingAppointments.first()
                    val timeLeft = next.dateTimeMillis - System.currentTimeMillis()
                    val hours = timeLeft / (1000 * 60 * 60)
                    val minutes = (timeLeft % (1000 * 60 * 60)) / (1000 * 60)
                    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(Date(next.dateTimeMillis))

                    updateNotification(
                        "🐾 ${next.petName} — ${next.title}\n" +
                                "Dr. ${next.veterinarian} a las $timeStr\n" +
                                "Faltan ${hours}h ${minutes}min"
                    )
                }
                delay(60_000) // Actualiza cada minuto
            }
        }
    }

    private fun buildNotification(content: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Próximas citas veterinarias")
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun updateNotification(content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(content))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Citas Veterinarias",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Cuenta regresiva para próximas citas"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}