package com.tuapp.petcare.features.vet.data.repositories

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.tuapp.petcare.core.workers.MedicineNotifyWorker
import com.tuapp.petcare.features.vet.data.datasources.local.MedicineDao
import com.tuapp.petcare.features.vet.data.datasources.local.toDomain
import com.tuapp.petcare.features.vet.data.datasources.local.toEntity
import com.tuapp.petcare.features.vet.domain.entities.Medicine
import com.tuapp.petcare.features.vet.domain.repositories.VetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VetRepositoryImpl @Inject constructor(
    private val medicineDao: MedicineDao,
    @ApplicationContext private val context: Context
) : VetRepository {

    override fun getMedicines(): Flow<List<Medicine>> =
        medicineDao.getAllMedicines().map { list -> list.map { it.toDomain() } }

    override suspend fun addMedicine(medicine: Medicine) {
        // 1. Guarda en Room
        medicineDao.insertMedicine(medicine.toEntity())
        // 2. WorkManager notifica a usuarios via FCM
        scheduleMedicineNotification(medicine)
    }

    override suspend fun deleteMedicine(id: String) =
        medicineDao.deleteMedicine(id)

    override suspend fun updateQuantity(id: String, quantity: Int) =
        medicineDao.updateQuantity(id, quantity)

    private fun scheduleMedicineNotification(medicine: Medicine) {
        val request = OneTimeWorkRequestBuilder<MedicineNotifyWorker>()
            .setInputData(
                workDataOf(
                    MedicineNotifyWorker.KEY_MEDICINE_NAME to medicine.name,
                    MedicineNotifyWorker.KEY_QUANTITY     to medicine.quantity,
                    MedicineNotifyWorker.KEY_UNIT         to medicine.unit
                )
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "${MedicineNotifyWorker.WORK_NAME}_${medicine.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}