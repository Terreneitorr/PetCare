package com.tuapp.petcare.features.pets.data.repositories

import com.tuapp.petcare.features.pets.data.datasources.local.PetDao
import com.tuapp.petcare.features.pets.data.datasources.remote.mapper.toDomain
import com.tuapp.petcare.features.pets.data.datasources.remote.mapper.toEntity
import com.tuapp.petcare.features.pets.domain.entities.Pet
import com.tuapp.petcare.features.pets.domain.repositories.PetsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PetsRepositoryImpl @Inject constructor(
    private val petDao: PetDao
) : PetsRepository {

    // Room como fuente de verdad — devuelve Flow reactivo
    override fun getPets(): Flow<List<Pet>> =
        petDao.getAllPets().map { list -> list.map { it.toDomain() } }

    override suspend fun getPetById(id: String): Pet? =
        petDao.getPetById(id)?.toDomain()

    override suspend fun addPet(pet: Pet) =
        petDao.insertPet(pet.toEntity())

    override suspend fun updatePet(pet: Pet) =
        petDao.updatePet(pet.toEntity())

    override suspend fun deletePet(id: String) =
        petDao.deletePet(id)
}
