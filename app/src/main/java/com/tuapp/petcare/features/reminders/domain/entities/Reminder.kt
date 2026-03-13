package com.tuapp.petcare.features.reminders.domain.entities

data class Reminder(
    val id: String,
    val petId: String,
    val petName: String,
    val title: String,
    val description: String,
    val triggerAtMillis: Long,  // timestamp Unix cuando debe disparar
    val isActive: Boolean = true
)
