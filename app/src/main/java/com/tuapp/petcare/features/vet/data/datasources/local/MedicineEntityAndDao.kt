package com.tuapp.petcare.features.vet.data.datasources.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.tuapp.petcare.features.vet.domain.entities.Medicine
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Int,
    val unit: String,
    val expiryDate: String,
    val description: String,
    val addedAt: Long
)

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY addedAt DESC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicine(id: String)

    @Query("UPDATE medicines SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int)
}

fun MedicineEntity.toDomain() = Medicine(
    id = id, name = name, quantity = quantity,
    unit = unit, expiryDate = expiryDate,
    description = description, addedAt = addedAt
)

fun Medicine.toEntity() = MedicineEntity(
    id = id, name = name, quantity = quantity,
    unit = unit, expiryDate = expiryDate,
    description = description, addedAt = addedAt
)