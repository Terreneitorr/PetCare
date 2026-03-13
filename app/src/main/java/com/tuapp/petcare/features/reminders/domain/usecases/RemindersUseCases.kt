package com.tuapp.petcare.features.reminders.domain.usecases

import com.tuapp.petcare.features.reminders.domain.entities.Reminder
import com.tuapp.petcare.features.reminders.domain.repositories.RemindersRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val repository: RemindersRepository
) {
    operator fun invoke(): Flow<List<Reminder>> = repository.getAllReminders()
}

class ScheduleReminderUseCase @Inject constructor(
    private val repository: RemindersRepository
) {
    suspend operator fun invoke(reminder: Reminder): Result<Unit> {
        return try {
            if (reminder.title.isBlank())
                return Result.failure(Exception("El título es obligatorio"))
            if (reminder.triggerAtMillis <= System.currentTimeMillis())
                return Result.failure(Exception("La fecha debe ser en el futuro"))
            val reminderWithId = reminder.copy(id = UUID.randomUUID().toString())
            repository.scheduleReminder(reminderWithId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
