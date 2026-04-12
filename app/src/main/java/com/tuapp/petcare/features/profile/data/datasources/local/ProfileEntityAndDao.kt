package com.tuapp.petcare.features.profile.data.datasources.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.tuapp.petcare.features.profile.domain.entities.Profile
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val city: String,
    val photoUri: String,
    val updatedAt: Long
)

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile LIMIT 1")
    fun getProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    @Query("DELETE FROM profile")
    suspend fun clearProfile()
}

fun ProfileEntity.toDomain() = Profile(
    id = id, name = name, email = email,
    phone = phone, city = city,
    photoUri = photoUri, updatedAt = updatedAt
)

fun Profile.toEntity() = ProfileEntity(
    id = id, name = name, email = email,
    phone = phone, city = city,
    photoUri = photoUri, updatedAt = updatedAt
)