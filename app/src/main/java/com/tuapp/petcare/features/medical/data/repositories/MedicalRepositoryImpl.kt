package com.tuapp.petcare.features.medical.data.repositories

import com.tuapp.petcare.features.medical.data.VaccineAlarmScheduler
import com.tuapp.petcare.features.medical.data.datasources.local.VaccineDao
import com.tuapp.petcare.features.medical.data.datasources.local.toDomain
import com.tuapp.petcare.features.medical.data.datasources.local.toEntity
import com.tuapp.petcare.features.medical.domain.entities.Vaccine
import com.tuapp.petcare.features.medical.domain.repositories.MedicalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicalRepositoryImpl @Inject constructor(
    private val vaccineDao: VaccineDao,
    private val alarmScheduler: VaccineAlarmScheduler
) : MedicalRepository {

    override fun getVaccinesByPet(petId: String): Flow<List<Vaccine>> =
        vaccineDao.getVaccinesByPet(petId).map { list -> list.map { it.toDomain() } }

    override fun getUpcomingVaccines(daysAhead: Int): Flow<List<Vaccine>> =
        vaccineDao.getUpcomingVaccines(daysAhead).map { list -> list.map { it.toDomain() } }

    override suspend fun addVaccine(vaccine: Vaccine) {
        vaccineDao.insertVaccine(vaccine.toEntity())
        // Programa alarma 24h antes de la próxima dosis
        alarmScheduler.scheduleVaccineAlert(
            vaccineId    = vaccine.id,
            vaccineName  = vaccine.name,
            veterinarian = vaccine.veterinarian,
            nextDoseDate = vaccine.nextDoseDate
        )
    }

    override suspend fun deleteVaccine(id: String) {
        alarmScheduler.cancelVaccineAlert(id)
        vaccineDao.deleteVaccine(id)
    }
}