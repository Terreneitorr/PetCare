package com.tuapp.petcare.features.appointments.domain.usecases

import com.tuapp.petcare.features.appointments.domain.entities.Appointment
import com.tuapp.petcare.features.appointments.domain.repositories.AppointmentsRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetAppointmentsByPetUseCase @Inject constructor(
    private val repository: AppointmentsRepository
) {
    operator fun invoke(petId: String): Flow<List<Appointment>> =
        repository.getAppointmentsByPet(petId)
}

class GetUpcomingAppointmentsUseCase @Inject constructor(
    private val repository: AppointmentsRepository
) {
    operator fun invoke(): Flow<List<Appointment>> =
        repository.getUpcomingAppointments()
}

class AddAppointmentUseCase @Inject constructor(
    private val repository: AppointmentsRepository
) {
    suspend operator fun invoke(appointment: Appointment): Result<Unit> {
        return try {
            if (appointment.title.isBlank())
                return Result.failure(Exception("El título es obligatorio"))
            if (appointment.veterinarian.isBlank())
                return Result.failure(Exception("El veterinario es obligatorio"))
            val withId = appointment.copy(id = UUID.randomUUID().toString())
            repository.addAppointment(withId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteAppointmentUseCase @Inject constructor(
    private val repository: AppointmentsRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            repository.deleteAppointment(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CompleteAppointmentUseCase @Inject constructor(
    private val repository: AppointmentsRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            repository.completeAppointment(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}