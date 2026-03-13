package com.tuapp.petcare.features.pets.data.datasources.remote.mapper

import com.tuapp.petcare.features.pets.data.datasources.local.PetEntity
import com.tuapp.petcare.features.pets.data.datasources.remote.models.PetDto
import com.tuapp.petcare.features.pets.domain.entities.Pet

// DTO de la API → entidad de dominio
fun PetDto.toDomain(): Pet = Pet(
    id        = this.id,
    name      = this.name,
    species   = this.species,
    breed     = this.breed,
    birthDate = this.birthDate,
    photoUri  = this.photoUri,
    ownerId   = this.ownerId
)

// Entidad Room → entidad de dominio
fun PetEntity.toDomain(): Pet = Pet(
    id        = this.id,
    name      = this.name,
    species   = this.species,
    breed     = this.breed,
    birthDate = this.birthDate,
    photoUri  = this.photoUri,
    ownerId   = this.ownerId
)

// Entidad de dominio → entidad Room
fun Pet.toEntity(): PetEntity = PetEntity(
    id        = this.id,
    name      = this.name,
    species   = this.species,
    breed     = this.breed,
    birthDate = this.birthDate,
    photoUri  = this.photoUri,
    ownerId   = this.ownerId
)
