package com.tuapp.petcare.features.medical.data.datasources.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "vaccines")
data class VaccineEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val name: String,
    val date: String,
    val nextDoseDate: String,
    val veterinarian: String,
    val notes: String
)

@Dao
interface VaccineDao {

    // Consulta compleja 1: todas las vacunas de una mascota, orden descendente por fecha
    @Query("SELECT * FROM vaccines WHERE petId = :petId ORDER BY date DESC")
    fun getVaccinesByPet(petId: String): Flow<List<VaccineEntity>>

    // Consulta compleja 2: vacunas cuya próxima dosis vence en los próximos N días
    // Usa DATE de SQLite para calcular diferencia
    @Query("""
        SELECT * FROM vaccines 
        WHERE nextDoseDate BETWEEN date('now') AND date('now', '+' || :daysAhead || ' days')
        ORDER BY nextDoseDate ASC
    """)
    fun getUpcomingVaccines(daysAhead: Int): Flow<List<VaccineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccine(vaccine: VaccineEntity)

    @Query("DELETE FROM vaccines WHERE id = :id")
    suspend fun deleteVaccine(id: String)
}
