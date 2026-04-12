package com.tuapp.petcare.features.appointments.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuapp.petcare.features.appointments.presentation.viewmodels.AppointmentsViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    petId: String,
    petName: String,
    onBack: () -> Unit,
    viewModel: AppointmentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.addState.collectAsStateWithLifecycle()

    var selectedHour   by remember { mutableIntStateOf(9) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var minuteText     by remember { mutableStateOf("00") }
    var dateText       by remember { mutableStateOf("") }

    val displayHour = when {
        selectedHour == 0  -> 12
        selectedHour > 12  -> selectedHour - 12
        else               -> selectedHour
    }
    val amPm = if (selectedHour < 12) "AM" else "PM"

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.resetAddSuccess()
            onBack()
        }
    }

    // Recalcula el timestamp cuando cambia fecha u hora
    LaunchedEffect(dateText, selectedHour, selectedMinute) {
        val digits = dateText.filter { it.isDigit() }
        if (digits.length >= 8) {
            val day   = digits.substring(0, 2).toIntOrNull() ?: return@LaunchedEffect
            val month = (digits.substring(2, 4).toIntOrNull() ?: return@LaunchedEffect) - 1
            val year  = digits.substring(4, 8).toIntOrNull() ?: return@LaunchedEffect
            val cal = Calendar.getInstance().apply {
                set(year, month, day, selectedHour, selectedMinute, 0)
                set(Calendar.MILLISECOND, 0)
            }
            viewModel.onDateTimeMillisChange(cal.timeInMillis)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva cita", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Título de la cita *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Ej: Vacuna antirrábica, Revisión anual") }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.veterinarian,
                onValueChange = viewModel::onVetChange,
                label = { Text("Veterinario *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Fecha con separador automático
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
            Spacer(modifier = Modifier.height(12.dp))

            // Hora con AM/PM
            Text(
                "Hora (formato 12h):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
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
                            else -> h
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
                onClick = { viewModel.onSaveAppointment(petId, petName) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar cita")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}