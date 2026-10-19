package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SmartDeviceEntity
import com.example.data.model.AppLanguage
import com.example.localization.LanguageManager

@Composable
fun SmartHomeHubScreen(
    devices: List<SmartDeviceEntity>,
    language: AppLanguage,
    onTriggerDevice: (SmartDeviceEntity) -> Unit,
    onAddDevice: (String, String, String) -> Unit,
    onDeleteDevice: (SmartDeviceEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.getString("smart_hub_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${devices.size} Verbunden",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Smart-Home-Geräte überwachen und automatisierte Aufgaben verknüpfen (z. B. Spülmaschine, Saugroboter, HVAC-Filter).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (devices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Noch keine Smart-Home-Geräte verbunden.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Erstes Gerät verbinden")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(devices) { device ->
                        SmartDeviceCard(
                            device = device,
                            onTrigger = { onTriggerDevice(device) },
                            onDelete = { onDeleteDevice(device) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_smart_device_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Gerät verbinden")
        }
    }

    if (showAddDialog) {
        AddSmartDeviceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, location ->
                onAddDevice(name, type, location)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SmartDeviceCard(
    device: SmartDeviceEntity,
    onTrigger: () -> Unit,
    onDelete: () -> Unit
) {
    val isWarning = device.status == "WARNING" || device.healthPercent < 20
    val isRunning = device.status == "RUNNING"

    val icon: ImageVector = when (device.type) {
        "ROBOT_VACUUM" -> Icons.Default.CleaningServices
        "DISHWASHER" -> Icons.Default.LocalLaundryService
        "HVAC_FILTER" -> Icons.Default.Air
        "SMOKE_DETECTOR" -> Icons.Default.Security
        "SMART_FRIDGE" -> Icons.Default.Kitchen
        else -> Icons.Default.Router
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("device_card_${device.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${device.location} • ${device.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${device.metricLabel}: ${device.metricValue}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Zustand: ${device.healthPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { device.healthPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (device.healthPercent < 25) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Letztes: ${device.lastAction}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onTrigger,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isRunning) "Stoppen" else "Aktivieren / Testen", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSmartDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Wohnzimmer") }
    var selectedType by remember { mutableStateOf("ROBOT_VACUUM") }

    val types = listOf(
        Pair("ROBOT_VACUUM", "Saugroboter"),
        Pair("DISHWASHER", "Geschirrspüler"),
        Pair("HVAC_FILTER", "Luftfilter / Heizung"),
        Pair("SMOKE_DETECTOR", "Rauchmelder"),
        Pair("SMART_FRIDGE", "Kühlschrank"),
        Pair("WASHING_MACHINE", "Waschmaschine")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Smart-Home-Gerät verbinden", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Gerätename *") },
                    placeholder = { Text("z. B. Saugroboter Erdgeschoss") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Raum / Standort *") },
                    placeholder = { Text("z. B. Küche, Keller, Flur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Gerätetyp:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { (typeKey, typeLabel) ->
                        FilterChip(
                            selected = selectedType == typeKey,
                            onClick = { selectedType = typeKey },
                            label = { Text(typeLabel, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedType, location)
                    }
                }
            ) {
                Text("Verbinden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
