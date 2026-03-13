package com.tuapp.petcare.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tuapp.petcare.features.medical.data.datasources.local.VaccineDao
import com.tuapp.petcare.features.medical.data.datasources.local.VaccineEntity
import com.tuapp.petcare.features.pets.data.datasources.local.PetDao
import com.tuapp.petcare.features.pets.data.datasources.local.PetEntity
import com.tuapp.petcare.features.reminders.data.datasources.local.ReminderDao
import com.tuapp.petcare.features.reminders.data.datasources.local.ReminderEntity

@Database(
    entities = [PetEntity::class, VaccineEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PetCareDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun vaccineDao(): VaccineDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var INSTANCE: PetCareDatabase? = null

        // Usado por BootReceiver (fuera del grafo de Hilt)
        fun getDatabase(context: Context): PetCareDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PetCareDatabase::class.java,
                    "petcare_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
