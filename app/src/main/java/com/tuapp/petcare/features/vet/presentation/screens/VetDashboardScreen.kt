package com.tuapp.petcare.features.vet.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuapp.petcare.features.vet.presentation.components.MedicineCard
import com.tuapp.petcare.features.vet.presentation.viewmodels.VetViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val UNIT_OPTIONS = listOf("dosis", "frascos", "cajas", "tabletas", "sobres")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetDashboardScreen(
    onLogout: () -> Unit,
    viewModel: VetViewModel = hiltViewModel()
) {
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val addState by viewModel.addState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    // DatePicker para fecha de vencimiento
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(millis))
                        viewModel.onExpiryDateChange(formatted)
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario 💊", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Agregar medicamento")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Banner WorkManager notificación ───────────────────────────
            when (uiState.notifyStatus) {
                NotifyStatus.NOTIFYING -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "⚙️ Notificando a usuarios via WorkManager...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                NotifyStatus.SENT -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "✓ Notificación enviada a usuarios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {}
            }

            // ── Lista de medicamentos ─────────────────────────────────────
            if (uiState.medicines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💊", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sin medicamentos en inventario",
                            style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Toca + para agregar uno",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.medicines, key = { it.id }) { medicine ->
                        MedicineCard(
                            medicine = medicine,
                            onDelete = { viewModel.onDeleteMedicine(medicine.id) },
                            onUpdateQuantity = { qty ->
                                viewModel.onUpdateQuantity(medicine.id, qty)
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Dialog agregar medicamento ────────────────────────────────────────────
    if (showAddDialog) {
        LaunchedEffect(addState.isSuccess) {
            if (addState.isSuccess) {
                viewModel.resetAddSuccess()
                showAddDialog = false
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nuevo medicamento", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = addState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Nombre del medicamento *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = addState.quantity,
                            onValueChange = viewModel::onQuantityChange,
                            label = { Text("Cantidad *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        ExposedDropdownMenuBox(
                            expanded = addState.showUnitDropdown,
                            onExpandedChange = { viewModel.onToggleUnitDropdown() },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = addState.unit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unidad") },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = addState.showUnitDropdown,
                                onDismissRequest = { viewModel.onToggleUnitDropdown() }
                            ) {
                                UNIT_OPTIONS.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = { viewModel.onUnitChange(unit) }
                                    )
                                }
                            }
                        }
                    }

                    // Fecha de vencimiento con DatePicker
                    OutlinedTextField(
                        value = addState.expiryDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de vencimiento") },
                        placeholder = { Text("Selecciona una fecha") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    if (addState.error != null) {
                        Text(
                            addState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onSaveMedicine() },
                    enabled = !addState.isLoading
                ) {
                    if (addState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }
}