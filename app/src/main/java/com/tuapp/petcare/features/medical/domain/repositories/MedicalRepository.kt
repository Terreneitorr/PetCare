package com.tuapp.petcare.features.medical.domain.repositories

import com.tuapp.petcare.features.medical.domain.entities.Vaccine
import kotlinx.coroutines.flow.Flow

interface MedicalRepository {
    // Consulta compleja: todas las vacunas de una mascota ordenadas por fecha
    fun getVaccinesByPet(petId: String): Flow<List<Vaccine>>
    // Consulta compleja: vacunas próximas a vencer en los siguientes N días
    fun getUpcomingVaccines(daysAhead: Int): Flow<List<Vaccine>>
    suspend fun addVaccine(vaccine: Vaccine)
    suspend fun deleteVaccine(id: String)
}
