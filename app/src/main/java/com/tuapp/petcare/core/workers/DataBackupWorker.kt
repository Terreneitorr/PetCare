package com.tuapp.petcare.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.tuapp.petcare.features.pets.domain.repositories.PetsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class DataBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val petsRepository: PetsRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME   = "data_backup_work"
        const val KEY_TYPE    = "backup_type"
        const val TYPE_PETS   = "pets"
        const val TYPE_VACCINE = "vaccine"
    }

    override suspend fun doWork(): Result {
        return try {
            val type = inputData.getString(KEY_TYPE) ?: TYPE_PETS

            when (type) {
                TYPE_PETS -> backupPets()
                else      -> Result.success()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()
            else Result.success()
        }
    }

    private suspend fun backupPets(): Result {
        // Obtiene todas las mascotas de Room
        val pets = petsRepository.getPets().firstOrNull() ?: return Result.success()
        // Aquí iría la llamada al backend — por ahora simula el backup
        // En producción: apiService.backupPets(pets)
        // Simula trabajo de red
        kotlinx.coroutines.delay(1500)
        return Result.success(
            workDataOf("backed_up_count" to pets.size)
        )
    }
}