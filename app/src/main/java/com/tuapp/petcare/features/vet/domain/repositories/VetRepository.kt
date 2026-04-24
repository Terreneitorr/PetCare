package com.tuapp.petcare.features.vet.domain.repositories

import com.tuapp.petcare.features.vet.domain.entities.Medicine
import kotlinx.coroutines.flow.Flow

interface VetRepository {
    fun getMedicines(): Flow<List<Medicine>>
    suspend fun addMedicine(medicine: Medicine)
    suspend fun deleteMedicine(id: String)
    suspend fun updateQuantity(id: String, quantity: Int)
}