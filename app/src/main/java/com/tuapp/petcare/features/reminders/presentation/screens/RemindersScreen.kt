package com.tuapp.petcare.features.reminders.presentation.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Banner de alertas próximas 24h ────────────────────────────
            val now = System.currentTimeMillis()
            val limit = now + 24 * 60 * 60 * 1000L
            val proximosCount = uiState.reminders.count {
                it.isActive && it.triggerAtMillis in now..limit
            }
            if (proximosCount > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "⚠️ $proximosCount recordatorio(s) en las próximas 24 horas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Contenido principal ───────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var minuteText     by remember {
        mutableStateOf(now.get(Calendar.MINUTE).toString().padStart(2, '0'))
    }

    // ── DatePicker ────────────────────────────────────────────────────────────
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        selectedDay   = cal.get(Calendar.DAY_OF_MONTH)
                        selectedMonth = cal.get(Calendar.MONTH)
                        selectedYear  = cal.get(Calendar.YEAR)
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

    val displayHour = when {
        selectedHour == 0 -> 12
        selectedHour > 12 -> selectedHour - 12
        else              -> selectedHour
    }
    val amPm = if (selectedHour < 12) "AM" else "PM"

    // Texto de fecha para mostrar
    val dateDisplay = "%02d/%02d/%d".format(selectedDay, selectedMonth + 1, selectedYear)

    // Actualiza el timestamp cuando cambia cualquier valor
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

                // ── Campo fecha con DatePicker ─────────────────────────────
                OutlinedTextField(
                    value = dateDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    placeholder = { Text("Selecciona una fecha") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, "Seleccionar fecha")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
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
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }.take(2)
                            minuteText = clean
                            clean.toIntOrNull()?.coerceIn(0, 59)?.let { selectedMinute = it }
                        },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
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