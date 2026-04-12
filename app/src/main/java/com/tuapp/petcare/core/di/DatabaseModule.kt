package com.tuapp.petcare.core.di

import com.tuapp.petcare.features.profile.data.datasources.local.ProfileDao
import android.content.Context
import androidx.room.Room
import com.tuapp.petcare.features.medical.data.datasources.local.VaccineDao
import com.tuapp.petcare.features.pets.data.datasources.local.PetDao
import com.tuapp.petcare.features.reminders.data.datasources.local.ReminderDao
import com.tuapp.petcare.core.database.PetCareDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// contiene instrucciones para crear dependencias de librerías externas
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideProfileDao(db: PetCareDatabase): ProfileDao = db.profileDao()

    @Provides
    @Singleton
    fun providePetCareDatabase(@ApplicationContext context: Context): PetCareDatabase {
        return Room.databaseBuilder(
            context,
            PetCareDatabase::class.java,
            "petcare_database"
        ).build()
    }

    @Provides
    @Singleton
    fun providePetDao(database: PetCareDatabase): PetDao = database.petDao()

    @Provides
    @Singleton
    fun provideVaccineDao(database: PetCareDatabase): VaccineDao = database.vaccineDao()

    @Provides
    @Singleton
    fun provideReminderDao(database: PetCareDatabase): ReminderDao = database.reminderDao()
}
