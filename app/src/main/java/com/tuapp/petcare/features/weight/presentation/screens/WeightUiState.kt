package com.tuapp.petcare.features.weight.presentation.screens

import com.tuapp.petcare.features.weight.domain.entities.WeightRecord

data class WeightUiState(
    val records: List<WeightRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AddWeightUiState(
    val weightText: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)