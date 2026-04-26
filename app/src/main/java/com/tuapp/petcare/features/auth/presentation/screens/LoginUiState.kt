package com.tuapp.petcare.features.auth.presentation.screens

import com.tuapp.petcare.features.auth.domain.entities.UserRole

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val passwordVisible: Boolean = false,
    val role: UserRole = UserRole.OWNER
)