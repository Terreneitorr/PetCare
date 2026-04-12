package com.tuapp.petcare.features.weight.data.repositories

import com.tuapp.petcare.features.weight.data.datasources.local.WeightDao
import com.tuapp.petcare.features.weight.data.datasources.local.toDomain
import com.tuapp.petcare.features.weight.data.datasources.local.toEntity
import com.tuapp.petcare.features.weight.domain.entities.WeightRecord
import com.tuapp.petcare.features.weight.domain.repositories.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WeightRepositoryImpl @Inject constructor(
    private val weightDao: WeightDao
) : WeightRepository {

    override fun getWeightRecordsByPet(petId: String): Flow<List<WeightRecord>> =
        weightDao.getWeightRecordsByPet(petId).map { list -> list.map { it.toDomain() } }

    override suspend fun addWeightRecord(record: WeightRecord) =
        weightDao.insertWeightRecord(record.toEntity())

    override suspend fun deleteWeightRecord(id: String) =
        weightDao.deleteWeightRecord(id)
}