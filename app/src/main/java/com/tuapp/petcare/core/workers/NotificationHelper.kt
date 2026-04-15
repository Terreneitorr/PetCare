package com.tuapp.petcare.core.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    const val CHANNEL_VACCINES  = "channel_vaccines_reminder"
    const val CHANNEL_APPTS     = "channel_appointments_reminder"
    const val CHANNEL_REMINDERS = "channel_reminders_alert"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        listOf(
            Triple(CHANNEL_VACCINES,  "💉 Vacunas próximas",   "Alertas cuando una vacuna está por vencer"),
            Triple(CHANNEL_APPTS,     "📅 Citas próximas",     "Alertas 24h antes de una cita veterinaria"),
            Triple(CHANNEL_REMINDERS, "🔔 Recordatorios",      "Alertas de recordatorios programados")
        ).forEach { (id, name, desc) ->
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = desc
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        notifId: Int,
        title: String,
        body: String
    ) {
        createChannels(context)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            // Permiso POST_NOTIFICATIONS no concedido
        }
    }
}