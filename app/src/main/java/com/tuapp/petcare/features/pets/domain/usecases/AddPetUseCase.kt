package com.tuapp.petcare.features.pets.domain.usecases

import com.tuapp.petcare.features.pets.domain.entities.Pet
import com.tuapp.petcare.features.pets.domain.repositories.PetsRepository
import java.util.UUID
import javax.inject.Inject

class AddPetUseCase @Inject constructor(
    private val repository: PetsRepository
) {
    suspend operator fun invoke(pet: Pet): Result<Unit> {
        return try {
            if (pet.name.isBlank())
                return Result.failure(Exception("El nombre de la mascota es obligatorio"))
            if (pet.species.isBlank())
                return Result.failure(Exception("La especie es obligatoria"))
            val petWithId = pet.copy(id = UUID.randomUUID().toString())
            repository.addPet(petWithId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
