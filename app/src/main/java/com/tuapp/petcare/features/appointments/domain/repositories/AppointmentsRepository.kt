package com.tuapp.petcare.features.appointments.domain.repositories

import com.tuapp.petcare.features.appointments.domain.entities.Appointment
import kotlinx.coroutines.flow.Flow

interface AppointmentsRepository {
    fun getAppointmentsByPet(petId: String): Flow<List<Appointment>>
    fun getUpcomingAppointments(): Flow<List<Appointment>>
    suspend fun addAppointment(appointment: Appointment)
    suspend fun completeAppointment(id: String)
    suspend fun deleteAppointment(id: String)
}