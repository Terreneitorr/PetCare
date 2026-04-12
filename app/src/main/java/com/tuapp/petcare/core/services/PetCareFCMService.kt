package com.tuapp.petcare.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PetCareFCMService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "petcare_fcm_channel"
        const val NOTIFICATION_ID = 3001
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Obtener título y cuerpo del mensaje
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "PetCare"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Tienes una notificación nueva"

        createNotificationChannel()
        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Aquí se enviaría el token al backend para guardar
        // En producción: apiService.updateFcmToken(token)
    }

    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Notificaciones PetCare",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones push de PetCare"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}