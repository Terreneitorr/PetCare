package com.tuapp.petcare.features.pets.di

import com.tuapp.petcare.core.di.PetCareRetrofit
import com.tuapp.petcare.features.pets.data.datasources.remote.api.PetsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PetsNetworkModule {

    @Provides
    @Singleton
    fun providePetsApi(@PetCareRetrofit retrofit: Retrofit): PetsApi =
        retrofit.create(PetsApi::class.java)
}
