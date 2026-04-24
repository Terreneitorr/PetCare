package com.tuapp.petcare.features.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.petcare.core.services.FcmTokenManager
import com.tuapp.petcare.features.auth.domain.repositories.AuthRepository
import com.tuapp.petcare.features.auth.domain.usecases.LoginUseCase
import com.tuapp.petcare.features.auth.presentation.screens.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository,
    private val fcmTokenManager: FcmTokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, error = null) }

    fun onTogglePasswordVisibility() =
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun onLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase(
                email    = _uiState.value.email,
                password = _uiState.value.password
            )
            result.fold(
                onSuccess = { user ->
                    authRepository.saveSession(user.token)
                    // Registra el token FCM en Realtime Database
                    // para que el médico pueda mandar notificaciones push
                    fcmTokenManager.registerToken(user.id)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }
}