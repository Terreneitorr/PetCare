package com.tuapp.petcare.features.weight.domain.entities

data class WeightRecord(
    val id: String,
    val petId: String,
    val petName: String,
    val weightKg: Float,
    val recordedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)