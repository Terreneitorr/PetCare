package com.tuapp.petcare.features.pets.di

import com.tuapp.petcare.features.pets.data.repositories.PetsRepositoryImpl
import com.tuapp.petcare.features.pets.domain.repositories.PetsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PetsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPetsRepository(
        petsRepositoryImpl: PetsRepositoryImpl
    ): PetsRepository
}
