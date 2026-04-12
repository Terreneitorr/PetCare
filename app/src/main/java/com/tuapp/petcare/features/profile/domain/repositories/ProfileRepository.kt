package com.tuapp.petcare.features.profile.domain.repositories

import com.tuapp.petcare.features.profile.domain.entities.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(): Flow<Profile?>
    suspend fun saveProfileLocally(profile: Profile)
    suspend fun syncProfileWithBackend(profile: Profile): Result<Unit>
}