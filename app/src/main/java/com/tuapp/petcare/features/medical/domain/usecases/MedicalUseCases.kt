package com.tuapp.petcare.features.medical.domain.usecases

import com.tuapp.petcare.features.medical.domain.entities.Vaccine
import com.tuapp.petcare.features.medical.domain.repositories.MedicalRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetVaccinesUseCase @Inject constructor(
    private val repository: MedicalRepository
) {
    operator fun invoke(petId: String): Flow<List<Vaccine>> =
        repository.getVaccinesByPet(petId)
}

class AddVaccineUseCase @Inject constructor(
    private val repository: MedicalRepository
) {
    suspend operator fun invoke(vaccine: Vaccine): Result<Unit> {
        return try {
            if (vaccine.name.isBlank())
                return Result.failure(Exception("El nombre de la vacuna es obligatorio"))
            if (vaccine.date.isBlank())
                return Result.failure(Exception("La fecha es obligatoria"))
            val vaccineWithId = vaccine.copy(id = UUID.randomUUID().toString())
            repository.addVaccine(vaccineWithId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ── NUEVO ──────────────────────────────────────────────────────────────────
class DeleteVaccineUseCase @Inject constructor(
    private val repository: MedicalRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            repository.deleteVaccine(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}