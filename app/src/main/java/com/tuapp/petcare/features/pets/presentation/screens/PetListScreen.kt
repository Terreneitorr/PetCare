package com.tuapp.petcare.features.pets.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuapp.petcare.features.pets.presentation.components.PetCard
import com.tuapp.petcare.features.pets.presentation.viewmodels.PetListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListScreen(
    onAddPet: () -> Unit,
    onPetClick: (String) -> Unit,
    onReminders: () -> Unit,
    onProfile: () -> Unit,
    viewModel: PetListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis mascotas 🐾", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onReminders) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Recordatorios"
                        )
                    }
                    IconButton(onClick = onProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Mi perfil"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPet) {
                Icon(Icons.Default.Add, contentDescription = "Agregar mascota")
            }
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
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                uiState.pets.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🐶", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Aún no tienes mascotas",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Toca + para agregar tu primera",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.pets, key = { it.id }) { pet ->
                            PetCard(
                                pet = pet,
                                onClick = { onPetClick(pet.id) },
                                onDelete = { viewModel.onDeletePet(pet.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}