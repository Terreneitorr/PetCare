package com.tuapp.petcare.core.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object NotificationHelper {

    const val CHANNEL_VACCINES  = "channel_vaccines_reminder"
    const val CHANNEL_APPTS     = "channel_appointments_reminder"
    const val CHANNEL_REMINDERS = "channel_reminders_alert"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        listOf(
            NotificationChannel(CHANNEL_VACCINES,  "Vacunas próximas",     NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_APPTS,     "Citas próximas",       NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel(CHANNEL_REMINDERS, "Recordatorios",        NotificationManager.IMPORTANCE_HIGH)
        ).forEach { channel ->
            channel.description = "Alertas 24h antes"
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
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(notifId, notification)
    }
}