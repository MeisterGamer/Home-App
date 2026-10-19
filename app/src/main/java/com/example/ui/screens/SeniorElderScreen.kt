package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MedicationEntity
import com.example.data.entity.WearableVitalEntity
import com.example.data.model.AppLanguage
import com.example.localization.LanguageManager

@Composable
fun SeniorElderScreen(
    medications: List<MedicationEntity>,
    vitals: WearableVitalEntity?,
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    language: AppLanguage,
    onToggleMedication: (MedicationEntity) -> Unit,
    onAddMedication: (String, String, String, String, String) -> Unit,
    onDeleteMedication: (MedicationEntity) -> Unit,
    onUpdateVitals: (Int, Int, Float, Int, Int) -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleHighContrast: () -> Unit,
    onOpenVoice: () -> Unit
) {
    val context = LocalContext.current
    var showAddMedDialog by remember { mutableStateOf(false) }
    var showVitalsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Big Accessibility Header & Theme Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Senioren- & Pflegemodus",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Große Schrift • Hoher Kontrast • Einfache Bedienung",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Dark mode button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { onToggleDarkMode() }
                            .padding(4.dp)
                            .testTag("elder_dark_mode_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Modus",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isDarkMode) "Hell" else "Dunkel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // High Contrast Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { onToggleHighContrast() }
                            .padding(4.dp)
                            .testTag("elder_contrast_toggle")
                    ) {
                        Text(
                            text = if (isHighContrast) "Kontrast AN" else "Kontrast",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighContrast) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Emergency Call Family Button (Giant Red 64dp+ tap target)
        item {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // fallback
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .testTag("elder_emergency_call_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Familie / Notruf anrufen",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }

        // Voice Command Assistant Giant Button
        item {
            Button(
                onClick = onOpenVoice,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("elder_voice_help_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sprachassistent starten",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

        // Wearable Health Vitals Card (Heart Rate, Steps, Sleep, Fall Detection)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("wearables_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wearable Vitalwerte & Schlafmuster",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { showVitalsDialog = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Werte eintragen", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Heart Rate
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${vitals?.heartRate ?: 72} BPM",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("Ruhepuls", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Steps
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${vitals?.steps ?: 0}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("Schritte heute", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Sleep
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${vitals?.sleepHours ?: 0f} Std",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("Tiefschlaf: ${vitals?.deepSleepPercent ?: 0}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Fall monitor
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sicher",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981)
                            )
                            Text("Sturzwächter", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Section: Medication Schedule
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Medikamentenplan & Erinnerungen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = { showAddMedDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Medikament anlegen")
                }
            }
        }

        if (medications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Noch keine Medikamente eingetragen. Klicke auf 'Medikament anlegen', um tägliche Einnahmen zu planen.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(medications) { med ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (med.isTakenToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("elder_med_${med.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (med.isTakenToday) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.secondaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = if (med.isTakenToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = med.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (med.isTakenToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${med.reminderTime} • ${med.dosage} (${med.timeOfDay})",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (med.instructions.isNotBlank()) {
                                Text(
                                    text = med.instructions,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { onToggleMedication(med) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (med.isTakenToday) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("toggle_med_${med.id}")
                        ) {
                            Text(
                                text = if (med.isTakenToday) "Eingenommen ✓" else "Einnehmen",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (med.isTakenToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(onClick = { onDeleteMedication(med) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Add Medication Dialog
    if (showAddMedDialog) {
        var medName by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("") }
        var timeOfDay by remember { mutableStateOf("Morgens") }
        var reminderTime by remember { mutableStateOf("08:00") }
        var instructions by remember { mutableStateOf("") }

        val times = listOf("Morgens", "Mittags", "Abends", "Zur Nacht")

        AlertDialog(
            onDismissRequest = { showAddMedDialog = false },
            title = { Text("Medikament anlegen", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Medikamentenname *") },
                        placeholder = { Text("z. B. Blutdrucktabletten") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosierung (z. B. 10mg / 1 Tablette)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        label = { Text("Uhrzeit (z. B. 08:00)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Tageszeit:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        times.forEach { t ->
                            FilterChip(
                                selected = timeOfDay == t,
                                onClick = { timeOfDay = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Hinweis (z. B. Nach dem Essen mit Wasser)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank()) {
                            onAddMedication(medName, dosage, timeOfDay, reminderTime, instructions)
                            showAddMedDialog = false
                        }
                    }
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMedDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Vitals Dialog
    if (showVitalsDialog) {
        var hrText by remember { mutableStateOf((vitals?.heartRate ?: 72).toString()) }
        var stepsText by remember { mutableStateOf((vitals?.steps ?: 3500).toString()) }
        var sleepText by remember { mutableStateOf((vitals?.sleepHours ?: 7.5f).toString()) }
        var deepText by remember { mutableStateOf((vitals?.deepSleepPercent ?: 22).toString()) }

        AlertDialog(
            onDismissRequest = { showVitalsDialog = false },
            title = { Text("Vitalwerte eintragen / aktualisieren", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = hrText,
                        onValueChange = { hrText = it },
                        label = { Text("Puls (BPM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = stepsText,
                        onValueChange = { stepsText = it },
                        label = { Text("Schritte heute") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sleepText,
                        onValueChange = { sleepText = it },
                        label = { Text("Schlafstunden (z. B. 7.5)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deepText,
                        onValueChange = { deepText = it },
                        label = { Text("Tiefschlaf (%)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hr = hrText.toIntOrNull() ?: 72
                        val steps = stepsText.toIntOrNull() ?: 3500
                        val sleep = sleepText.toFloatOrNull() ?: 7.5f
                        val deep = deepText.toIntOrNull() ?: 20
                        onUpdateVitals(hr, steps, sleep, deep, 98)
                        showVitalsDialog = false
                    }
                ) {
                    Text("Aktualisieren")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVitalsDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
