package com.tuapp.petcare.features.auth.domain.repositories

import com.tuapp.petcare.features.auth.domain.entities.User
import com.tuapp.petcare.features.auth.domain.entities.UserRole

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun saveSession(token: String)
    suspend fun clearSession()
    suspend fun getToken(): String?
    suspend fun saveRole(role: UserRole)
    suspend fun getRole(): UserRole
}