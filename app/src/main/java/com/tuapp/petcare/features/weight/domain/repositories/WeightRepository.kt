package com.tuapp.petcare.features.weight.domain.repositories

import com.tuapp.petcare.features.weight.domain.entities.WeightRecord
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun getWeightRecordsByPet(petId: String): Flow<List<WeightRecord>>
    suspend fun addWeightRecord(record: WeightRecord)
    suspend fun deleteWeightRecord(id: String)
}