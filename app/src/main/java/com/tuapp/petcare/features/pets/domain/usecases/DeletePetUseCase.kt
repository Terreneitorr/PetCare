package com.tuapp.petcare.features.pets.domain.usecases

import com.tuapp.petcare.features.pets.domain.repositories.PetsRepository
import javax.inject.Inject

class DeletePetUseCase @Inject constructor(
    private val repository: PetsRepository
) {
    suspend operator fun invoke(petId: String): Result<Unit> {
        return try {
            repository.deletePet(petId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}