package com.tuapp.petcare.features.reminders.di

import com.tuapp.petcare.features.reminders.data.repositories.RemindersRepositoryImpl
import com.tuapp.petcare.features.reminders.domain.repositories.RemindersRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemindersRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRemindersRepository(
        remindersRepositoryImpl: RemindersRepositoryImpl
    ): RemindersRepository
}
