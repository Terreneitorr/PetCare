package com.tuapp.petcare.features.auth.presentation.screens

import com.tuapp.petcare.features.auth.domain.entities.UserRole

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: UserRole = UserRole.OWNER,
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)