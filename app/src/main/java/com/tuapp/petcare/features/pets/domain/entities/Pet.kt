package com.tuapp.petcare.features.pets.domain.entities

data class Pet(
    val id: String,
    val name: String,
    val species: String,   // Perro, Gato, Ave...
    val breed: String,
    val birthDate: String, // "2022-05-10"
    val photoUri: String,  // URI local o URL remota
    val ownerId: String
)
