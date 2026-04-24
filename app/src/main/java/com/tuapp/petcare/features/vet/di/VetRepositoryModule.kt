package com.tuapp.petcare.features.vet.di

import com.tuapp.petcare.features.vet.data.repositories.VetRepositoryImpl
import com.tuapp.petcare.features.vet.domain.repositories.VetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VetRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVetRepository(
        vetRepositoryImpl: VetRepositoryImpl
    ): VetRepository
}