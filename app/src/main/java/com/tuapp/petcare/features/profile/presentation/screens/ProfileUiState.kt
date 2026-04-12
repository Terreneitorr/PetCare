package com.tuapp.petcare.features.profile.presentation.screens

import com.tuapp.petcare.features.profile.domain.entities.Profile

data class ProfileUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val city: String = "",
    val photoUri: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)