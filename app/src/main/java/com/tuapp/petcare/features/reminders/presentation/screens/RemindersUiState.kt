package com.tuapp.petcare.features.reminders.presentation.screens

import com.tuapp.petcare.features.reminders.domain.entities.Reminder

data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // Campos para agregar un nuevo recordatorio
    val newTitle: String = "",
    val newDescription: String = "",
    val newPetName: String = "",
    val newTriggerMillis: Long = 0L,
    val showAddDialog: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)
