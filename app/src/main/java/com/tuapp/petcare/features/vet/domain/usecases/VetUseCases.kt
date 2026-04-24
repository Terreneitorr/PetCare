package com.tuapp.petcare.features.vet.domain.usecases

import com.tuapp.petcare.features.vet.domain.entities.Medicine
import com.tuapp.petcare.features.vet.domain.repositories.VetRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetMedicinesUseCase @Inject constructor(
    private val repository: VetRepository
) {
    operator fun invoke(): Flow<List<Medicine>> = repository.getMedicines()
}

class AddMedicineUseCase @Inject constructor(
    private val repository: VetRepository
) {
    suspend operator fun invoke(medicine: Medicine): Result<Unit> {
        return try {
            if (medicine.name.isBlank())
                return Result.failure(Exception("El nombre es obligatorio"))
            if (medicine.quantity <= 0)
                return Result.failure(Exception("La cantidad debe ser mayor a 0"))
            val withId = medicine.copy(id = UUID.randomUUID().toString())
            repository.addMedicine(withId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteMedicineUseCase @Inject constructor(
    private val repository: VetRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            repository.deleteMedicine(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UpdateMedicineQuantityUseCase @Inject constructor(
    private val repository: VetRepository
) {
    suspend operator fun invoke(id: String, quantity: Int): Result<Unit> {
        return try {
            repository.updateQuantity(id, quantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}