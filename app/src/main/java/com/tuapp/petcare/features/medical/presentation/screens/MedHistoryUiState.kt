package com.tuapp.petcare.features.medical.presentation.screens

import com.tuapp.petcare.features.medical.domain.entities.Vaccine

data class MedHistoryUiState(
    val vaccines: List<Vaccine> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AddVaccineUiState(
    val name: String = "",
    val date: String = "",
    val nextDoseDate: String = "",
    val veterinarian: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
