package com.tuapp.petcare.features.reminders.presentation.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
                                onCancel = { viewModel.onCancelReminder(reminder.id) },
                                onDelete = { viewModel.onDeleteReminder(reminder.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddReminderDialog(
            uiState   = uiState,
            onTitle   = viewModel::onTitleChange,
            onDesc    = viewModel::onDescriptionChange,
            onPetName = viewModel::onPetNameChange,
            onMillis  = viewModel::onTriggerMillisChange,
            onSave    = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!am.canScheduleExactAlarms()) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        return@AddReminderDialog
                    }
                }
                viewModel.onScheduleReminder()
            },
            onDismiss = viewModel::onDismissDialog
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
    val now = Calendar.getInstance()
    var selectedHour   by remember { mutableIntStateOf(now.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(now.get(Calendar.MINUTE)) }
    var selectedDay    by remember { mutableIntStateOf(now.get(Calendar.DAY_OF_MONTH)) }
    var selectedMonth  by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }
    var selectedYear   by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }

    // Estado local de texto para minutos — evita el cuatrapeo
    var minuteText by remember {
        mutableStateOf(now.get(Calendar.MINUTE).toString().padStart(2, '0'))
    }

    // Estado local de texto para fecha con separador automático
    var dateText by remember { mutableStateOf("") }

    val displayHour = when {
        selectedHour == 0 -> 12
        selectedHour > 12 -> selectedHour - 12
        else              -> selectedHour
    }
    val amPm = if (selectedHour < 12) "AM" else "PM"

    // Actualiza el timestamp cuando cambia cualquier valor
    LaunchedEffect(selectedHour, selectedMinute, selectedDay, selectedMonth, selectedYear) {
        val cal = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)
            set(Calendar.MILLISECOND, 0)
        }
        onMillis(cal.timeInMillis)
    }

    // Parsea la fecha cuando cambia dateText
    LaunchedEffect(dateText) {
        val digits = dateText.filter { it.isDigit() }
        if (digits.length >= 2)
            selectedDay = digits.substring(0, 2).toIntOrNull()?.coerceIn(1, 31) ?: selectedDay
        if (digits.length >= 4)
            selectedMonth = (digits.substring(2, 4).toIntOrNull()?.coerceIn(1, 12) ?: (selectedMonth + 1)) - 1
        if (digits.length >= 8)
            selectedYear = digits.substring(4, 8).toIntOrNull() ?: selectedYear
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo recordatorio", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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

                // Fecha con separador automático DD/MM/YYYY
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(8)
                        dateText = buildString {
                            digits.forEachIndexed { i, c ->
                                if (i == 2 || i == 4) append('/')
                                append(c)
                            }
                        }
                    },
                    label = { Text("Fecha") },
                    placeholder = { Text("DD/MM/YYYY") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    "Hora (formato 12h):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Campo hora
                    OutlinedTextField(
                        value = displayHour.toString(),
                        onValueChange = { input ->
                            val h = input.toIntOrNull()?.coerceIn(1, 12) ?: return@OutlinedTextField
                            selectedHour = when {
                                amPm == "AM" && h == 12 -> 0
                                amPm == "PM" && h != 12 -> h + 12
                                else                    -> h
                            }
                        },
                        label = { Text("Hora") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    // Campo minutos — estado local para evitar cuatrapeo
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }.take(2)
                            minuteText = clean
                            clean.toIntOrNull()?.coerceIn(0, 59)?.let {
                                selectedMinute = it
                            }
                        },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    // Botón AM/PM
                    FilledTonalButton(
                        onClick = {
                            selectedHour = if (selectedHour < 12) selectedHour + 12
                            else selectedHour - 12
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(amPm, fontWeight = FontWeight.Bold)
                    }
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