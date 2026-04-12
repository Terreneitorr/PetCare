package com.tuapp.petcare.features.appointments.presentation.screens

import com.tuapp.petcare.features.appointments.domain.entities.Appointment

data class AppointmentsUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AddAppointmentUiState(
    val title: String = "",
    val description: String = "",
    val veterinarian: String = "",
    val dateText: String = "",
    val hourText: String = "",
    val minuteText: String = "",
    val amPm: String = "AM",
    val dateTimeMillis: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)