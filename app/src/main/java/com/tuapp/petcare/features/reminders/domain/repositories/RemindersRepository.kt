package com.tuapp.petcare.features.reminders.domain.repositories

import com.tuapp.petcare.features.reminders.domain.entities.Reminder
import kotlinx.coroutines.flow.Flow

interface RemindersRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    suspend fun getActiveReminders(): List<Reminder>
    suspend fun scheduleReminder(reminder: Reminder)
    suspend fun cancelReminder(id: String)
    suspend fun deleteReminder(id: String)
}