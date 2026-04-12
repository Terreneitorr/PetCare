package com.tuapp.petcare.features.appointments.data.datasources.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.tuapp.petcare.features.appointments.domain.entities.Appointment
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val petName: String,
    val title: String,
    val description: String,
    val veterinarian: String,
    val dateTimeMillis: Long,
    val isCompleted: Boolean
)

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments WHERE petId = :petId ORDER BY dateTimeMillis ASC")
    fun getAppointmentsByPet(petId: String): Flow<List<AppointmentEntity>>

    // Consulta compleja: citas próximas no completadas en las siguientes 24 horas
    @Query("""
        SELECT * FROM appointments 
        WHERE isCompleted = 0 
        AND dateTimeMillis BETWEEN :now AND :next24h
        ORDER BY dateTimeMillis ASC
    """)
    fun getUpcomingAppointments(now: Long, next24h: Long): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Query("UPDATE appointments SET isCompleted = 1 WHERE id = :id")
    suspend fun completeAppointment(id: String)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointment(id: String)
}

fun AppointmentEntity.toDomain() = Appointment(
    id = id, petId = petId, petName = petName,
    title = title, description = description,
    veterinarian = veterinarian,
    dateTimeMillis = dateTimeMillis,
    isCompleted = isCompleted
)

fun Appointment.toEntity() = AppointmentEntity(
    id = id, petId = petId, petName = petName,
    title = title, description = description,
    veterinarian = veterinarian,
    dateTimeMillis = dateTimeMillis,
    isCompleted = isCompleted
)