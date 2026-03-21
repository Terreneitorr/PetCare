package com.tuapp.petcare.features.reminders.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.petcare.features.reminders.domain.entities.Reminder
import com.tuapp.petcare.features.reminders.domain.usecases.GetRemindersUseCase
import com.tuapp.petcare.features.reminders.domain.usecases.ScheduleReminderUseCase
import com.tuapp.petcare.features.reminders.domain.repositories.RemindersRepository
import com.tuapp.petcare.features.reminders.presentation.screens.RemindersUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val remindersRepository: RemindersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getRemindersUseCase()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list -> _uiState.update { it.copy(isLoading = false, reminders = list) } }
        }
    }

    fun onTitleChange(v: String)       = _uiState.update { it.copy(newTitle = v, saveError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(newDescription = v) }
    fun onPetNameChange(v: String)     = _uiState.update { it.copy(newPetName = v) }
    fun onTriggerMillisChange(v: Long) = _uiState.update { it.copy(newTriggerMillis = v) }
    fun onShowDialog()                 = _uiState.update { it.copy(showAddDialog = true, saveSuccess = false) }
    fun onDismissDialog()              = _uiState.update { it.copy(showAddDialog = false) }

    fun onScheduleReminder() {
        viewModelScope.launch {
            val s = _uiState.value
            val result = scheduleReminderUseCase(
                Reminder(
                    id              = "",
                    petId           = "local",
                    petName         = s.newPetName,
                    title           = s.newTitle,
                    description     = s.newDescription,
                    triggerAtMillis = s.newTriggerMillis
                )
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            showAddDialog    = false,
                            saveSuccess      = true,
                            newTitle         = "",
                            newDescription   = "",
                            newPetName       = "",
                            newTriggerMillis = 0L
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(saveError = e.message) }
                }
            )
        }
    }

    fun onCancelReminder(id: String) {
        viewModelScope.launch {
            remindersRepository.cancelReminder(id)
        }
    }

    // ── NUEVO: elimina el recordatorio completamente ───────────────────────
    fun onDeleteReminder(id: String) {
        viewModelScope.launch {
            remindersRepository.deleteReminder(id)
        }
    }
}