package com.tuapp.petcare.features.pets.data.datasources.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val photoUri: String,
    val ownerId: String
)
