package com.tuapp.petcare.features.pets.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.tuapp.petcare.features.pets.presentation.viewmodels.AddPetViewModel
import java.io.File

val SPECIES_OPTIONS = listOf("Perro", "Gato", "Ave", "Conejo", "Pez", "Reptil", "Otro")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddPetScreen(
    onBack: () -> Unit,
    viewModel: AddPetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let { viewModel.onPhotoSelected(it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    var showPhotoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva mascota", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { showPhotoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.photoUri.isNotBlank()) {
                    AsyncImage(
                        model = uiState.photoUri,
                        contentDescription = "Foto seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Foto", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null && uiState.name.isBlank()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = uiState.showSpeciesDropdown,
                onExpandedChange = { viewModel.onToggleDropdown() }
            ) {
                OutlinedTextField(
                    value = uiState.species.ifBlank { "Selecciona especie *" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Especie") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = uiState.showSpeciesDropdown,
                    onDismissRequest = { viewModel.onToggleDropdown() }
                ) {
                    SPECIES_OPTIONS.forEach { species ->
                        DropdownMenuItem(
                            text = { Text(species) },
                            onClick = { viewModel.onSpeciesChange(species) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.breed,
                onValueChange = viewModel::onBreedChange,
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Campo de fecha corregido ──────────────────────────────────
            OutlinedTextField(
                value = uiState.birthDate,
                onValueChange = { input ->
                    // Filtra solo dígitos para reconstruir desde cero
                    val digits = input.filter { it.isDigit() }.take(8)
                    viewModel.onBirthDateChange(digits)
                },
                label = { Text("Fecha de nacimiento") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("DD/MM/YYYY") }
            )

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.onSavePet(ownerId = "local_user") },
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
                    Text("Guardar mascota")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Foto de tu mascota") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoDialog = false
                            if (cameraPermission.status.isGranted) {
                                val file = File(context.cacheDir, "pet_photo_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                cameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermission.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("📷  Tomar foto") }

                    TextButton(
                        onClick = {
                            showPhotoDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🖼  Elegir de galería") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) { Text("Cancelar") }
            }
        )
    }
}