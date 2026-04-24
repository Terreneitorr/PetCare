package com.tuapp.petcare.features.pets.data.repositories

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.tuapp.petcare.core.workers.DataBackupWorker
import com.tuapp.petcare.features.pets.data.datasources.local.PetDao
import com.tuapp.petcare.features.pets.data.datasources.remote.mapper.toDomain
import com.tuapp.petcare.features.pets.data.datasources.remote.mapper.toEntity
import com.tuapp.petcare.features.pets.domain.entities.Pet
import com.tuapp.petcare.features.pets.domain.repositories.PetsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PetsRepositoryImpl @Inject constructor(
    private val petDao: PetDao,
    @ApplicationContext private val context: Context
) : PetsRepository {

    override fun getPets(): Flow<List<Pet>> =
        petDao.getAllPets().map { list -> list.map { it.toDomain() } }

    override suspend fun getPetById(id: String): Pet? =
        petDao.getPetById(id)?.toDomain()

    override suspend fun addPet(pet: Pet) {
        // 1. Guarda en Room inmediatamente
        petDao.insertPet(pet.toEntity())
        // 2. Encola respaldo en background con WorkManager
        scheduleBackup()
    }

    override suspend fun updatePet(pet: Pet) {
        petDao.updatePet(pet.toEntity())
        scheduleBackup()
    }

    override suspend fun deletePet(id: String) =
        petDao.deletePet(id)

    private fun scheduleBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val backupRequest = OneTimeWorkRequestBuilder<DataBackupWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(DataBackupWorker.KEY_TYPE to DataBackupWorker.TYPE_PETS))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            DataBackupWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            backupRequest
        )
    }
}