package com.tuapp.petcare.features.appointments.data.repositories

import com.tuapp.petcare.features.appointments.data.datasources.local.AppointmentDao
import com.tuapp.petcare.features.appointments.data.datasources.local.toDomain
import com.tuapp.petcare.features.appointments.data.datasources.local.toEntity
import com.tuapp.petcare.features.appointments.domain.entities.Appointment
import com.tuapp.petcare.features.appointments.domain.repositories.AppointmentsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppointmentsRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentDao
) : AppointmentsRepository {

    override fun getAppointmentsByPet(petId: String): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsByPet(petId).map { list -> list.map { it.toDomain() } }

    override fun getUpcomingAppointments(): Flow<List<Appointment>> {
        val now = System.currentTimeMillis()
        val next24h = now + 24 * 60 * 60 * 1000L
        return appointmentDao.getUpcomingAppointments(now, next24h)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addAppointment(appointment: Appointment) =
        appointmentDao.insertAppointment(appointment.toEntity())

    override suspend fun completeAppointment(id: String) =
        appointmentDao.completeAppointment(id)

    override suspend fun deleteAppointment(id: String) =
        appointmentDao.deleteAppointment(id)
}