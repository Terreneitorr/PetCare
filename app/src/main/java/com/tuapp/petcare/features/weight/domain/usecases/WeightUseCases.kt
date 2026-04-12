package com.tuapp.petcare.features.weight.domain.usecases

import com.tuapp.petcare.features.weight.domain.entities.WeightRecord
import com.tuapp.petcare.features.weight.domain.repositories.WeightRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetWeightRecordsUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(petId: String): Flow<List<WeightRecord>> =
        repository.getWeightRecordsByPet(petId)
}

class AddWeightRecordUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(record: WeightRecord): Result<Unit> {
        return try {
            if (record.weightKg <= 0)
                return Result.failure(Exception("El peso debe ser mayor a 0"))
            val withId = record.copy(id = UUID.randomUUID().toString())
            repository.addWeightRecord(withId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteWeightRecordUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            repository.deleteWeightRecord(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}