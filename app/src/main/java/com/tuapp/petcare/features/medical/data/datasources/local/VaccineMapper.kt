package com.tuapp.petcare.features.medical.data.datasources.local

import com.tuapp.petcare.features.medical.domain.entities.Vaccine

fun VaccineEntity.toDomain(): Vaccine = Vaccine(
    id           = this.id,
    petId        = this.petId,
    name         = this.name,
    date         = this.date,
    nextDoseDate = this.nextDoseDate,
    veterinarian = this.veterinarian,
    notes        = this.notes
)

fun Vaccine.toEntity(): VaccineEntity = VaccineEntity(
    id           = this.id,
    petId        = this.petId,
    name         = this.name,
    date         = this.date,
    nextDoseDate = this.nextDoseDate,
    veterinarian = this.veterinarian,
    notes        = this.notes
)
