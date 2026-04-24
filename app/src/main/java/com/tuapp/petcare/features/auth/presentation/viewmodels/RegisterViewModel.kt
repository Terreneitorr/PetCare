package com.tuapp.petcare.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.petcare.features.auth.domain.entities.UserRole
import com.tuapp.petcare.features.auth.domain.repositories.AuthRepository
import com.tuapp.petcare.features.auth.domain.usecases.RegisterUseCase
import com.tuapp.petcare.features.auth.presentation.screens.RegisterUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(v: String)            = _uiState.update { it.copy(name = v, error = null) }
    fun onEmailChange(v: String)           = _uiState.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String)        = _uiState.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, error = null) }
    fun onTogglePasswordVisibility()       = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun onRoleChange(role: UserRole)       = _uiState.update { it.copy(role = role) }

    fun onRegister() {
        viewModelScope.launch {
            val s = _uiState.value
            if (s.password != s.confirmPassword) {
                _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = registerUseCase(s.name, s.email, s.password)
            result.fold(
                onSuccess = { user ->
                    authRepository.saveSession(user.token)
                    authRepository.saveRole(s.role)  // Guarda el rol elegido
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}