package com.tuapp.petcare.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tuapp.petcare.features.appointments.data.datasources.local.AppointmentDao
import com.tuapp.petcare.features.appointments.data.datasources.local.AppointmentEntity
import com.tuapp.petcare.features.medical.data.datasources.local.VaccineDao
import com.tuapp.petcare.features.medical.data.datasources.local.VaccineEntity
import com.tuapp.petcare.features.pets.data.datasources.local.PetDao
import com.tuapp.petcare.features.pets.data.datasources.local.PetEntity
import com.tuapp.petcare.features.profile.data.datasources.local.ProfileDao
import com.tuapp.petcare.features.profile.data.datasources.local.ProfileEntity
import com.tuapp.petcare.features.reminders.data.datasources.local.ReminderDao
import com.tuapp.petcare.features.reminders.data.datasources.local.ReminderEntity

@Database(
    entities = [
        PetEntity::class,
        VaccineEntity::class,
        ReminderEntity::class,
        ProfileEntity::class,
        AppointmentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PetCareDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun vaccineDao(): VaccineDao
    abstract fun reminderDao(): ReminderDao
    abstract fun profileDao(): ProfileDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile private var INSTANCE: PetCareDatabase? = null

        fun getDatabase(context: Context): PetCareDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PetCareDatabase::class.java,
                    "petcare_database"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}