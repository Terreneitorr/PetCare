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
import com.tuapp.petcare.features.auth.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequestDto(email, password))
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            val response = api.register(RegisterRequestDto(name, email, password))
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSession(token: String) {
        dataStore.edit { prefs -> prefs[TOKEN_KEY] = token }
    }

    override suspend fun clearSession() {
        dataStore.edit { prefs -> prefs.remove(TOKEN_KEY) }
    }

    override suspend fun getToken(): String? {
        return dataStore.data.map { it[TOKEN_KEY] }.firstOrNull()
    }
}
