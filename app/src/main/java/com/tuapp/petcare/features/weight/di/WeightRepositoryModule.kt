package com.tuapp.petcare.features.weight.di

import com.tuapp.petcare.features.weight.data.repositories.WeightRepositoryImpl
import com.tuapp.petcare.features.weight.domain.repositories.WeightRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeightRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeightRepository(
        weightRepositoryImpl: WeightRepositoryImpl
    ): WeightRepository
}