package com.tuapp.petcare.features.weight.data.datasources.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.tuapp.petcare.features.weight.domain.entities.WeightRecord
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "weight_records")
data class WeightEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val petName: String,
    val weightKg: Float,
    val recordedAt: Long,
    val notes: String
)

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_records WHERE petId = :petId ORDER BY recordedAt ASC")
    fun getWeightRecordsByPet(petId: String): Flow<List<WeightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightRecord(record: WeightEntity)

    @Query("DELETE FROM weight_records WHERE id = :id")
    suspend fun deleteWeightRecord(id: String)
}

fun WeightEntity.toDomain() = WeightRecord(
    id = id, petId = petId, petName = petName,
    weightKg = weightKg, recordedAt = recordedAt, notes = notes
)

fun WeightRecord.toEntity() = WeightEntity(
    id = id, petId = petId, petName = petName,
    weightKg = weightKg, recordedAt = recordedAt, notes = notes
)