package com.tuapp.petcare.features.profile.data.repositories

import com.tuapp.petcare.features.profile.data.datasources.local.ProfileDao
import com.tuapp.petcare.features.profile.data.datasources.local.toDomain
import com.tuapp.petcare.features.profile.data.datasources.local.toEntity
import com.tuapp.petcare.features.profile.domain.entities.Profile
import com.tuapp.petcare.features.profile.domain.repositories.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun getProfile(): Flow<Profile?> =
        profileDao.getProfile().map { it?.toDomain() }

    override suspend fun saveProfileLocally(profile: Profile) {
        profileDao.upsertProfile(profile.toEntity())
    }

    override suspend fun syncProfileWithBackend(profile: Profile): Result<Unit> {
        return try {
            profileDao.upsertProfile(
                profile.copy(updatedAt = System.currentTimeMillis()).toEntity()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}