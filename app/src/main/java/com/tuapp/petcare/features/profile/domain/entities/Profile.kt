package com.tuapp.petcare.features.profile.domain.entities

data class Profile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val city: String,
    val photoUri: String,
    val updatedAt: Long = System.currentTimeMillis()
)