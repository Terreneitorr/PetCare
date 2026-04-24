package com.tuapp.petcare.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tuapp.petcare.core.services.FcmTokenManager
import com.tuapp.petcare.core.services.OAuthTokenHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@HiltWorker
class MedicineNotifyWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fcmTokenManager: FcmTokenManager,
    private val oAuthTokenHelper: OAuthTokenHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME         = "medicine_notify_work"
        const val KEY_MEDICINE_NAME = "medicine_name"
        const val KEY_QUANTITY      = "quantity"
        const val KEY_UNIT          = "unit"
        private const val PROJECT_ID = "petcare-3522c"
    }

    override suspend fun doWork(): Result {
        return try {
            val name     = inputData.getString(KEY_MEDICINE_NAME) ?: return Result.success()
            val quantity = inputData.getInt(KEY_QUANTITY, 0)
            val unit     = inputData.getString(KEY_UNIT) ?: "dosis"

            // Obtiene access token OAuth2
            val accessToken = oAuthTokenHelper.getAccessToken(context)
                ?: return Result.retry()

            // Obtiene todos los tokens FCM de usuarios registrados
            val tokens = fcmTokenManager.getAllTokens()

            if (tokens.isEmpty()) {
                // Si no hay tokens registrados muestra notificación local
                NotificationHelper.showNotification(
                    context   = context,
                    channelId = NotificationHelper.CHANNEL_VACCINES,
                    notifId   = (System.currentTimeMillis() % 10000).toInt(),
                    title     = "💊 Medicamento disponible — $name",
                    body      = "Se agregaron $quantity $unit de $name al inventario"
                )
                return Result.success()
            }

            // Manda push a cada token registrado
            tokens.forEach { token ->
                sendFcmNotification(
                    accessToken = accessToken,
                    token       = token,
                    title       = "💊 Medicamento disponible — $name",
                    body        = "Tu veterinario agregó $quantity $unit de $name. ¡Ya disponible!"
                )
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()
            else Result.success()
        }
    }

    private suspend fun sendFcmNotification(
        accessToken: String,
        token: String,
        title: String,
        body: String
    ) = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val message = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                    })
                })
            }

            OutputStreamWriter(conn.outputStream).use {
                it.write(message.toString())
            }

            BufferedReader(InputStreamReader(conn.inputStream)).readText()
        } catch (e: Exception) {
            // Log silencioso
        }
    }
}