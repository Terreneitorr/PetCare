package com.tuapp.petcare.core.services

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor() {

    // Obtiene el token FCM del dispositivo actual y lo guarda en Realtime Database
    suspend fun registerToken(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            FirebaseDatabase.getInstance()
                .getReference("fcm_tokens")
                .child(userId)
                .setValue(token)
                .await()
        } catch (e: Exception) {
            // Silencioso — no crítico
        }
    }

    // Obtiene todos los tokens registrados en Realtime Database
    suspend fun getAllTokens(): List<String> {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("fcm_tokens")
                .get()
                .await()
            snapshot.children.mapNotNull { it.getValue(String::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}