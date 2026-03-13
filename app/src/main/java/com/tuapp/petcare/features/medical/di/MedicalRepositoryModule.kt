package com.tuapp.petcare.features.medical.di

import com.tuapp.petcare.features.medical.data.repositories.MedicalRepositoryImpl
import com.tuapp.petcare.features.medical.domain.repositories.MedicalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MedicalRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicalRepository(
        medicalRepositoryImpl: MedicalRepositoryImpl
    ): MedicalRepository
}
