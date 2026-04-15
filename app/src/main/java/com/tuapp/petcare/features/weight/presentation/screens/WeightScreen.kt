package com.tuapp.petcare.features.weight.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuapp.petcare.features.weight.domain.entities.WeightRecord
import com.tuapp.petcare.features.weight.presentation.viewmodels.WeightViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    petId: String,
    petName: String,
    petSpecies: String,
    petBirthDate: String,
    onBack: () -> Unit,
    onAddWeight: () -> Unit,
    viewModel: WeightViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(petId) { viewModel.loadRecords(petId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peso y crecimiento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddWeight) {
                Icon(Icons.Default.Add, "Registrar peso")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gráfica de peso
            if (uiState.records.size >= 2) {
                item {
                    WeightChart(records = uiState.records)
                }
            }

            // Calculadora de edad
            item {
                AgeCalculatorCard(
                    species = petSpecies,
                    birthDate = petBirthDate,
                    petName = petName
                )
            }

            // Lista de registros
            if (uiState.records.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚖️", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sin registros de peso", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Toca + para registrar el peso",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(uiState.records.reversed(), key = { it.id }) { record ->
                    WeightRecordCard(
                        record = record,
                        onDelete = { viewModel.onDeleteRecord(record.id) }
                    )
                }
            }
        }
    }
}

// ── Gráfica de peso ───────────────────────────────────────────────────────────
@Composable
private fun WeightChart(records: List<WeightRecord>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Evolución del peso",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val minW = records.minOf { it.weightKg }
                val maxW = records.maxOf { it.weightKg }
                val range = if (maxW - minW < 0.1f) 1f else maxW - minW
                val w = size.width
                val h = size.height
                val pad = 20f

                drawRect(color = surfaceColor, size = size)

                val path = Path()
                records.forEachIndexed { i, record ->
                    val x = pad + (i.toFloat() / (records.size - 1)) * (w - 2 * pad)
                    val y = h - pad - ((record.weightKg - minW) / range) * (h - 2 * pad)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = primaryColor, style = Stroke(width = 3f))

                records.forEachIndexed { i, record ->
                    val x = pad + (i.toFloat() / (records.size - 1)) * (w - 2 * pad)
                    val y = h - pad - ((record.weightKg - minW) / range) * (h - 2 * pad)
                    drawCircle(color = primaryColor, radius = 6f, center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 3f, center = Offset(x, y))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${records.first().weightKg} kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "${records.last().weightKg} kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Calculadora de edad ───────────────────────────────────────────────────────
@Composable
private fun AgeCalculatorCard(species: String, birthDate: String, petName: String) {
    val humanAge = calculateHumanAge(birthDate)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🎂 Edad de $petName",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Edad humana: $humanAge años",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))

            when (species.lowercase()) {
                "perro" -> {
                    val smallAge = dogAgeSmallMedium(humanAge)
                    val largeAge = dogAgeLarge(humanAge)
                    val giantAge = dogAgeGiant(humanAge)
                    Text(
                        "🐕 Perro pequeño/mediano: $smallAge años",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "🐕 Perro grande: $largeAge años",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "🐕 Perro gigante: $giantAge años",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                "gato" -> {
                    val catAge = catAge(humanAge)
                    Text(
                        "🐱 Edad equivalente en gato: $catAge años",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                "conejo" -> {
                    val rabbitAge = rabbitAge(humanAge)
                    Text(
                        "🐰 Edad equivalente en conejo: $rabbitAge años",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                else -> {
                    Text(
                        "No hay tabla de conversión disponible para ${species.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ── Tarjeta de registro ───────────────────────────────────────────────────────
@Composable
private fun WeightRecordCard(record: WeightRecord, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(record.recordedAt))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${record.weightKg} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (record.notes.isNotBlank()) {
                    Text(
                        record.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar registro",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ── Funciones de cálculo de edad ──────────────────────────────────────────────
private fun calculateHumanAge(birthDate: String): Int {
    return try {
        if (birthDate.isBlank()) return 0
        val parts = when {
            birthDate.contains("/") -> {
                // Formato DD/MM/YYYY
                val p = birthDate.split("/")
                if (p.size < 3) return 0
                Triple(p[2].toInt(), p[1].toInt(), p[0].toInt())
            }
            birthDate.contains("-") -> {
                // Formato YYYY-MM-DD
                val p = birthDate.split("-")
                if (p.size < 3) return 0
                Triple(p[0].toInt(), p[1].toInt(), p[2].toInt())
            }
            else -> return 0
        }
        val birthYear  = parts.first
        val birthMonth = parts.second
        val cal = java.util.Calendar.getInstance()
        val currentYear  = cal.get(java.util.Calendar.YEAR)
        val currentMonth = cal.get(java.util.Calendar.MONTH) + 1
        var age = currentYear - birthYear
        if (currentMonth < birthMonth) age--
        if (age < 0) 0 else age
    } catch (e: Exception) { 0 }
}

private fun dogAgeSmallMedium(humanYears: Int): Int {
    return when (humanYears) {
        0 -> 0; 1 -> 15; 2 -> 24; 3 -> 28; 4 -> 32
        5 -> 36; 6 -> 40; 7 -> 44; 8 -> 48; 9 -> 52
        10 -> 56; 11 -> 60; 12 -> 64; 13 -> 68; 14 -> 72
        15 -> 76; 16 -> 80; else -> 80 + (humanYears - 16) * 4
    }
}

private fun dogAgeLarge(humanYears: Int): Int {
    return when (humanYears) {
        0 -> 0; 1 -> 15; 2 -> 24; 3 -> 28; 4 -> 32
        5 -> 36; 6 -> 42; 7 -> 47; 8 -> 51; 9 -> 56
        10 -> 60; 11 -> 65; 12 -> 69; 13 -> 74; 14 -> 78
        else -> 78 + (humanYears - 14) * 5
    }
}

private fun dogAgeGiant(humanYears: Int): Int {
    return when (humanYears) {
        0 -> 0; 1 -> 15; 2 -> 24; 3 -> 28; 4 -> 32
        5 -> 36; 6 -> 45; 7 -> 50; 8 -> 55; 9 -> 61
        10 -> 66; 11 -> 72; 12 -> 77; else -> 77 + (humanYears - 12) * 6
    }
}

private fun catAge(humanYears: Int): Int {
    return when (humanYears) {
        0 -> 0; 1 -> 15; 2 -> 24; 3 -> 28; 4 -> 32
        5 -> 36; 6 -> 40; 7 -> 44; 8 -> 48; 9 -> 52
        10 -> 56; 11 -> 60; 12 -> 64; 13 -> 68; 14 -> 72
        15 -> 76; else -> 76 + (humanYears - 15) * 4
    }
}

private fun rabbitAge(humanYears: Int): Int {
    return when (humanYears) {
        0 -> 0; 1 -> 12; 2 -> 20; 3 -> 28; 4 -> 36
        5 -> 40; 6 -> 46; 7 -> 52; else -> 52 + (humanYears - 7) * 6
    }
}