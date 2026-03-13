package com.tuapp.petcare.features.pets.domain.repositories

import com.tuapp.petcare.features.pets.domain.entities.Pet
import kotlinx.coroutines.flow.Flow

interface PetsRepository {
    fun getPets(): Flow<List<Pet>>
    suspend fun getPetById(id: String): Pet?
    suspend fun addPet(pet: Pet)
    suspend fun updatePet(pet: Pet)
    suspend fun deletePet(id: String)
}
