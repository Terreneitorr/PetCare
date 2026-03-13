package com.tuapp.petcare.features.reminders.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuapp.petcare.features.reminders.presentation.components.ReminderCard
import com.tuapp.petcare.features.reminders.presentation.viewmodels.RemindersViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios 🔔", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onShowDialog) {
                Icon(Icons.Default.Add, "Nuevo recordatorio")
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
                uiState.reminders.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔔", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sin recordatorios activos", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Toca + para programar uno",
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
                        items(uiState.reminders, key = { it.id }) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                onCancel = { viewModel.onCancelReminder(reminder.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Dialog para crear recordatorio ────────────────────────────────────────
    if (uiState.showAddDialog) {
        AddReminderDialog(
            uiState    = uiState,
            onTitle    = viewModel::onTitleChange,
            onDesc     = viewModel::onDescriptionChange,
            onPetName  = viewModel::onPetNameChange,
            onMillis   = viewModel::onTriggerMillisChange,
            onSave     = viewModel::onScheduleReminder,
            onDismiss  = viewModel::onDismissDialog
        )
    }
}

@Composable
private fun AddReminderDialog(
    uiState: RemindersUiState,
    onTitle: (String) -> Unit,
    onDesc: (String) -> Unit,
    onPetName: (String) -> Unit,
    onMillis: (Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    // Fecha/hora para la alarma — usamos estado local para los pickers
    var selectedHour   by remember { mutableIntStateOf(9) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var selectedDay    by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var selectedMonth  by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear   by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    // Calcula el timestamp cada vez que cambia hora/fecha
    LaunchedEffect(selectedHour, selectedMinute, selectedDay, selectedMonth, selectedYear) {
        val cal = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)
            set(Calendar.MILLISECOND, 0)
        }
        onMillis(cal.timeInMillis)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo recordatorio", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.newTitle,
                    onValueChange = onTitle,
                    label = { Text("Título *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.newPetName,
                    onValueChange = onPetName,
                    label = { Text("Nombre de la mascota") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.newDescription,
                    onValueChange = onDesc,
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text(
                    "Fecha y hora:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Fila de fecha
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = selectedDay.toString(),
                        onValueChange = { selectedDay = it.toIntOrNull()?.coerceIn(1, 31) ?: selectedDay },
                        label = { Text("Día") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = (selectedMonth + 1).toString(),
                        onValueChange = { selectedMonth = ((it.toIntOrNull() ?: 1) - 1).coerceIn(0, 11) },
                        label = { Text("Mes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = selectedYear.toString(),
                        onValueChange = { selectedYear = it.toIntOrNull() ?: selectedYear },
                        label = { Text("Año") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                }

                // Fila de hora
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = selectedHour.toString(),
                        onValueChange = { selectedHour = it.toIntOrNull()?.coerceIn(0, 23) ?: selectedHour },
                        label = { Text("Hora") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = selectedMinute.toString(),
                        onValueChange = { selectedMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: selectedMinute },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (uiState.saveError != null) {
                    Text(
                        text = uiState.saveError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Programar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
