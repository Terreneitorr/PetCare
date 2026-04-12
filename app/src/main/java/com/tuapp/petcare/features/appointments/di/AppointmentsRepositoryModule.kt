package com.tuapp.petcare.features.appointments.di

import com.tuapp.petcare.features.appointments.data.repositories.AppointmentsRepositoryImpl
import com.tuapp.petcare.features.appointments.domain.repositories.AppointmentsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppointmentsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppointmentsRepository(
        appointmentsRepositoryImpl: AppointmentsRepositoryImpl
    ): AppointmentsRepository
}