package com.tuapp.petcare.features.auth.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tuapp.petcare.features.auth.data.datasources.remote.api.AuthApi
import com.tuapp.petcare.features.auth.data.datasources.remote.mapper.toDomain
import com.tuapp.petcare.features.auth.data.datasources.remote.models.LoginRequestDto
import com.tuapp.petcare.features.auth.data.datasources.remote.models.RegisterRequestDto
import com.tuapp.petcare.features.auth.domain.entities.User
import com.tuapp.petcare.features.auth.domain.entities.UserRole
import com.tuapp.petcare.features.auth.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    companion object {
        private val TOKEN_KEY        = stringPreferencesKey("auth_token")
        private val CURRENT_EMAIL_KEY = stringPreferencesKey("current_email")
        // El rol se guarda por email: "role_email@ejemplo.com"
        private fun roleKey(email: String) = stringPreferencesKey("role_$email")
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequestDto(email, password))
            // Guarda el email actual para saber qué rol leer después
            dataStore.edit { prefs -> prefs[CURRENT_EMAIL_KEY] = email }
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val response = api.register(RegisterRequestDto(name, email, password))
            // Guarda el email actual
            dataStore.edit { prefs -> prefs[CURRENT_EMAIL_KEY] = email }
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSession(token: String) {
        dataStore.edit { prefs -> prefs[TOKEN_KEY] = token }
    }

    override suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(CURRENT_EMAIL_KEY)
            // NO borramos los roles por email — así cada cuenta mantiene su rol
        }
    }

    override suspend fun getToken(): String? =
        dataStore.data.map { it[TOKEN_KEY] }.firstOrNull()

    override suspend fun saveRole(role: UserRole) {
        // Guarda el rol asociado al email actual
        val email = dataStore.data.map { it[CURRENT_EMAIL_KEY] }.firstOrNull() ?: return
        dataStore.edit { prefs -> prefs[roleKey(email)] = role.name }
    }

    override suspend fun getRole(): UserRole {
        // Lee el rol del email que está actualmente logueado
        val email = dataStore.data.map { it[CURRENT_EMAIL_KEY] }.firstOrNull()
            ?: return UserRole.OWNER
        val roleName = dataStore.data.map { it[roleKey(email)] }.firstOrNull()
        return try {
            UserRole.valueOf(roleName ?: UserRole.OWNER.name)
        } catch (e: Exception) {
            UserRole.OWNER
        }
    }
}