package com.tuapp.petcare.features.pets.presentation.screens

import com.tuapp.petcare.features.pets.domain.entities.Pet

enum class BackupStatus { IDLE, SYNCING, SUCCESS }

data class PetListUiState(
    val pets: List<Pet> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val backupStatus: BackupStatus = BackupStatus.IDLE
)

data class AddPetUiState(
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val birthDate: String = "",
    val photoUri: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val showSpeciesDropdown: Boolean = false
)