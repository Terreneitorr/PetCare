package com.tuapp.petcare.features.auth.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuapp.petcare.features.auth.domain.entities.UserRole
import com.tuapp.petcare.features.auth.presentation.components.AuthTextField
import com.tuapp.petcare.features.auth.presentation.viewmodels.RegisterViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: (UserRole) -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess(uiState.role)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🐾", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = "Nombre completo",
                leadingIcon = Icons.Default.Person,
                isError = uiState.error != null
            )
            Spacer(modifier = Modifier.height(12.dp))
            AuthTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Correo electrónico",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                isError = uiState.error != null
            )
            Spacer(modifier = Modifier.height(12.dp))
            AuthTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = uiState.passwordVisible,
                onTogglePassword = viewModel::onTogglePasswordVisibility,
                isError = uiState.error != null
            )
            Spacer(modifier = Modifier.height(12.dp))
            AuthTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirmar contraseña",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = uiState.passwordVisible,
                isError = uiState.error != null
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Selección de rol ──────────────────────────────────────────
            Text(
                "¿Cómo usarás PetCare?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tarjeta dueño
                Card(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onRoleChange(UserRole.OWNER) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.role == UserRole.OWNER)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (uiState.role == UserRole.OWNER)
                        androidx.compose.foundation.BorderStroke(
                            2.dp, MaterialTheme.colorScheme.primary
                        )
                    else null
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            tint = if (uiState.role == UserRole.OWNER)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Dueño",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Gestiona tus mascotas",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Tarjeta veterinario
                Card(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onRoleChange(UserRole.VET) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.role == UserRole.VET)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (uiState.role == UserRole.VET)
                        androidx.compose.foundation.BorderStroke(
                            2.dp, MaterialTheme.colorScheme.secondary
                        )
                    else null
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = if (uiState.role == UserRole.VET)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Veterinario",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Gestiona pacientes",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Registrarse", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onGoToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}