package com.tuapp.petcare.features.vet.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuapp.petcare.features.vet.domain.entities.Medicine

@Composable
fun MedicineCard(
    medicine: Medicine,
    onDelete: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuantityDialog by remember { mutableStateOf(false) }
    var newQuantityText    by remember { mutableStateOf(medicine.quantity.toString()) }

    if (showQuantityDialog) {
        AlertDialog(
            onDismissRequest = { showQuantityDialog = false },
            title = { Text("Actualizar cantidad") },
            text = {
                OutlinedTextField(
                    value = newQuantityText,
                    onValueChange = { newQuantityText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nueva cantidad") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    newQuantityText.toIntOrNull()?.let { onUpdateQuantity(it) }
                    showQuantityDialog = false
                }) { Text("Actualizar") }
            },
            dismissButton = {
                TextButton(onClick = { showQuantityDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    medicine.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Stock: ${medicine.quantity} ${medicine.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (medicine.quantity > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                if (medicine.expiryDate.isNotBlank()) {
                    Text(
                        "Vence: ${medicine.expiryDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                if (medicine.description.isNotBlank()) {
                    Text(
                        medicine.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            TextButton(onClick = { showQuantityDialog = true }) {
                Text("Editar")
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}