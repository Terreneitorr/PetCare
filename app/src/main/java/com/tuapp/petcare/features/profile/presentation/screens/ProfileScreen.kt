package com.tuapp.petcare.features.profile.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tuapp.petcare.features.profile.presentation.viewmodels.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.profileState.collectAsStateWithLifecycle()
    val logoutState by viewModel.logoutState.collectAsStateWithLifecycle()

    // Navega al login cuando se cierra sesión
    LaunchedEffect(logoutState) {
        if (logoutState) {
            viewModel.resetLogout()
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, "Editar perfil")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.profile == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👤", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sin perfil configurado", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onEditProfile) { Text("Crear perfil") }
                    }
                }
                else -> {
                    val profile = uiState.profile!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Foto de perfil ────────────────────────────────
                        if (profile.photoUri.isNotBlank()) {
                            AsyncImage(
                                model = profile.photoUri,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.size(110.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(110.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            profile.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            profile.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Banner WorkManager ────────────────────────────
                        WorkManagerStatusBanner(status = uiState.syncStatus)

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Datos del perfil ──────────────────────────────
                        ProfileInfoCard(
                            label = "Teléfono",
                            value = profile.phone.ifBlank { "No registrado" }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileInfoCard(
                            label = "Ciudad",
                            value = profile.city.ifBlank { "No registrada" }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Última sincronización WorkManager ─────────────
                        ProfileInfoCard(
                            label = "⚙️ Última sincronización (WorkManager)",
                            value = if (profile.updatedAt > 0L) {
                                SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                                    .format(Date(profile.updatedAt))
                            } else {
                                "Pendiente de sincronizar"
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // ── Botón editar perfil ───────────────────────────
                        Button(
                            onClick = onEditProfile,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar perfil")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Botón cerrar sesión ───────────────────────────
                        OutlinedButton(
                            onClick = { viewModel.onLogout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Cerrar sesión",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// ── Banner de estado WorkManager ──────────────────────────────────────────────
@Composable
private fun WorkManagerStatusBanner(status: SyncStatus) {
    val (icon, text, color) = when (status) {
        SyncStatus.SYNCING -> Triple(
            Icons.Default.Sync,
            "Sincronizando perfil con el servidor...",
            MaterialTheme.colorScheme.primary
        )
        SyncStatus.SUCCESS -> Triple(
            Icons.Default.CheckCircle,
            "Perfil sincronizado correctamente ✓",
            MaterialTheme.colorScheme.tertiary
        )
        SyncStatus.ERROR -> Triple(
            Icons.Default.SyncProblem,
            "Error al sincronizar — reintentando...",
            MaterialTheme.colorScheme.error
        )
        SyncStatus.IDLE -> return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}