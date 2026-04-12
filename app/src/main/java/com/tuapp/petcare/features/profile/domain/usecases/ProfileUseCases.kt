package com.tuapp.petcare.features.profile.domain.usecases

import com.tuapp.petcare.features.profile.domain.entities.Profile
import com.tuapp.petcare.features.profile.domain.repositories.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<Profile?> = repository.getProfile()
}

class SaveProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: Profile): Result<Unit> {
        return try {
            if (profile.name.isBlank())
                return Result.failure(Exception("El nombre es obligatorio"))
            repository.saveProfileLocally(profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}