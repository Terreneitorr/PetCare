package com.tuapp.petcare.features.pets.data.datasources.remote.api

import com.tuapp.petcare.features.pets.data.datasources.remote.models.CreatePetDto
import com.tuapp.petcare.features.pets.data.datasources.remote.models.PetDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PetsApi {
    @GET("pets")
    suspend fun getPets(): List<PetDto>

    @GET("pets/{id}")
    suspend fun getPetById(@Path("id") id: String): PetDto

    @POST("pets")
    suspend fun createPet(@Body pet: CreatePetDto): PetDto

    @PUT("pets/{id}")
    suspend fun updatePet(@Path("id") id: String, @Body pet: CreatePetDto): PetDto

    @DELETE("pets/{id}")
    suspend fun deletePet(@Path("id") id: String)
}
