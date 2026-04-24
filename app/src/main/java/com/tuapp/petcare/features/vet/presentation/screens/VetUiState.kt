package com.tuapp.petcare.features.vet.presentation.screens

import com.tuapp.petcare.features.vet.domain.entities.Medicine

data class VetUiState(
    val medicines: List<Medicine> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val notifyStatus: NotifyStatus = NotifyStatus.IDLE
)

data class AddMedicineUiState(
    val name: String = "",
    val quantity: String = "",
    val unit: String = "dosis",
    val expiryDate: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val showUnitDropdown: Boolean = false
)

enum class NotifyStatus { IDLE, NOTIFYING, SENT }