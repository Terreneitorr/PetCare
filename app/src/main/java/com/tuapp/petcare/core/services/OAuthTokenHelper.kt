package com.tuapp.petcare.core.services

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthTokenHelper @Inject constructor() {

    companion object {
        private const val SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
        private const val TOKEN_URI = "https://oauth2.googleapis.com/token"
        private const val CLIENT_EMAIL = "firebase-adminsdk-fbsvc@petcare-3522c.iam.gserviceaccount.com"
    }

    suspend fun getAccessToken(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Lee la clave privada del archivo JSON en assets
                val json = context.assets.open("service_account.json")
                    .bufferedReader().readText()
                val privateKeyPem = JSONObject(json)
                    .getString("private_key")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\\n", "")
                    .replace("\n", "")
                    .trim()

                val privateKeyBytes = Base64.getDecoder().decode(privateKeyPem)
                val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
                val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec)

                // Crea el JWT
                val now = System.currentTimeMillis() / 1000
                val header = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("""{"alg":"RS256","typ":"JWT"}""".toByteArray())
                val payload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("""
                        {"iss":"$CLIENT_EMAIL",
                         "scope":"$SCOPE",
                         "aud":"$TOKEN_URI",
                         "exp":${now + 3600},
                         "iat":$now}
                    """.trimIndent().toByteArray())

                val signingInput = "$header.$payload"
                val signature = Signature.getInstance("SHA256withRSA").apply {
                    initSign(privateKey)
                    update(signingInput.toByteArray())
                }.sign()

                val jwt = "$signingInput.${Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(signature)}"

                // Intercambia JWT por access token
                val url = URL(TOKEN_URI)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                conn.doOutput = true

                val body = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=$jwt"
                OutputStreamWriter(conn.outputStream).use { it.write(body) }

                val response = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                JSONObject(response).getString("access_token")
            } catch (e: Exception) {
                null
            }
        }
    }
}