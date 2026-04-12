package com.tuapp.petcare.features.appointments.domain.entities

data class Appointment(
    val id: String,
    val petId: String,
    val petName: String,
    val title: String,
    val description: String,
    val veterinarian: String,
    val dateTimeMillis: Long,
    val isCompleted: Boolean = false
)