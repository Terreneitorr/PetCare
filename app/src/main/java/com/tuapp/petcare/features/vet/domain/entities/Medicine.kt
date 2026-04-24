package com.tuapp.petcare.features.vet.domain.entities

data class Medicine(
    val id: String,
    val name: String,
    val quantity: Int,
    val unit: String,           // "dosis", "frascos", "cajas"
    val expiryDate: String,     // "DD/MM/YYYY"
    val description: String,
    val addedAt: Long = System.currentTimeMillis()
)