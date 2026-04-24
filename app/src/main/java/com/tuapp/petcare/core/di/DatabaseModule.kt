package com.tuapp.petcare.core.di

import android.content.Context
import androidx.room.Room
import com.tuapp.petcare.core.database.PetCareDatabase
import com.tuapp.petcare.features.appointments.data.datasources.local.AppointmentDao
import com.tuapp.petcare.features.medical.data.datasources.local.VaccineDao
import com.tuapp.petcare.features.pets.data.datasources.local.PetDao
import com.tuapp.petcare.features.profile.data.datasources.local.ProfileDao
import com.tuapp.petcare.features.reminders.data.datasources.local.ReminderDao
import com.tuapp.petcare.features.vet.data.datasources.local.MedicineDao
import com.tuapp.petcare.features.weight.data.datasources.local.WeightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePetCareDatabase(@ApplicationContext context: Context): PetCareDatabase {
        return Room.databaseBuilder(
            context,
            PetCareDatabase::class.java,
            "petcare_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePetDao(db: PetCareDatabase): PetDao = db.petDao()

    @Provides
    fun provideVaccineDao(db: PetCareDatabase): VaccineDao = db.vaccineDao()

    @Provides
    fun provideReminderDao(db: PetCareDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideProfileDao(db: PetCareDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideAppointmentDao(db: PetCareDatabase): AppointmentDao = db.appointmentDao()

    @Provides
    fun provideWeightDao(db: PetCareDatabase): WeightDao = db.weightDao()

    @Provides
    fun provideMedicineDao(db: PetCareDatabase): MedicineDao = db.medicineDao()
}