package com.tuapp.petcare.features.medical.domain.entities

data class Vaccine(
    val id: String,
    val petId: String,
    val name: String,        // "Rabia", "Moquillo"...
    val date: String,        // "2024-03-15"
    val nextDoseDate: String,// "2025-03-15"
    val veterinarian: String,
    val notes: String
)
