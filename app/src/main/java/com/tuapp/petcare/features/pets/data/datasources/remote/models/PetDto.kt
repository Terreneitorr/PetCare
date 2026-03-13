package com.tuapp.petcare.features.pets.data.datasources.remote.models

import com.google.gson.annotations.SerializedName

data class PetDto(
    @SerializedName("id")        val id: String,
    @SerializedName("name")      val name: String,
    @SerializedName("species")   val species: String,
    @SerializedName("breed")     val breed: String,
    @SerializedName("birthDate") val birthDate: String,
    @SerializedName("photoUri")  val photoUri: String,
    @SerializedName("ownerId")   val ownerId: String
)

data class CreatePetDto(
    @SerializedName("name")      val name: String,
    @SerializedName("species")   val species: String,
    @SerializedName("breed")     val breed: String,
    @SerializedName("birthDate") val birthDate: String,
    @SerializedName("photoUri")  val photoUri: String,
    @SerializedName("ownerId")   val ownerId: String
)
