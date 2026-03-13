package com.tuapp.petcare.features.auth.data.datasources.remote.api

import com.tuapp.petcare.features.auth.data.datasources.remote.models.AuthResponseDto
import com.tuapp.petcare.features.auth.data.datasources.remote.models.LoginRequestDto
import com.tuapp.petcare.features.auth.data.datasources.remote.models.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("users/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("users")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto
}
