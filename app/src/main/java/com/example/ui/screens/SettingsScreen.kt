package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FamilyMemberEntity
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.localization.LanguageManager
import com.example.ui.viewmodel.SyncState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    members: List<FamilyMemberEntity>,
    activeMember: FamilyMemberEntity?,
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    currentLanguage: AppLanguage,
    syncState: SyncState,
    monthlyBudgetCap: Double,
    onSelectMember: (FamilyMemberEntity) -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleHighContrast: () -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onTriggerSync: () -> Unit,
    onClearExampleData: () -> Unit,
    onAddMember: (String, String, String, String) -> Unit,
    onDeleteMember: (FamilyMemberEntity) -> Unit,
    onUpdateBudgetCap: (Double) -> Unit
) {
    var urgentNotifToggle by remember { mutableStateOf(true) }
    var medNotifToggle by remember { mutableStateOf(true) }
    var choreNotifToggle by remember { mutableStateOf(true) }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showBudgetCapDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Einstellungen & Steuerung",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rollen, Berechtigungen, Datenverwaltung, Sprache & Benachrichtigungen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action Banner: Example Data Management
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("clear_data_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Beispieldaten bereinigen",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Entfernt alle Test-Aufgaben, Dummy-Ausgaben, Demo-Einkaufslisten und Test-Geräte, damit du mit einer sauberen App startest.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showClearDataDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("clear_all_example_data_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Alle Beispieldaten löschen", fontSize = 13.sp)
                    }
                }
            }
        }

        // Active Profile Selector & Member Management
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("profile_selector_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Familienmitglieder & Rollen", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { showAddMemberDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Mitglied hinzufügen", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aktuell aktiv: ${activeMember?.name ?: "Familie"} (${activeMember?.role ?: "ADMIN"})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(members) { m ->
                            FilterChip(
                                selected = m.id == activeMember?.id,
                                onClick = { onSelectMember(m) },
                                label = { Text("${m.name} (${m.role})", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Budget Cap Setting
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Monatliches Budgetlimit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Aktuelles Limit: ${monthlyBudgetCap.toInt()} € pro Monat", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(
                        onClick = { showBudgetCapDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ändern")
                    }
                }
            }
        }

        // Role-Based Access Control (RBAC) Matrix
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("rbac_permissions_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rollenberechtigungen (RBAC)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Schützt sensible Haushaltsdaten wie Finanzen und Einstellungen:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val rbacRoles = listOf(
                        Pair("Eltern / Admin", "Voller Zugriff auf Budget, Reparaturen, Rollen & Smart Home"),
                        Pair("Jugendliche/r", "Aufgaben, Kalender, Einkaufsliste (Budget ausgeblendet)"),
                        Pair("Kind", "Spielerische Ansicht, Aufgaben mit Sternen & Belohnungen"),
                        Pair("Senior / Großeltern", "Einfache Großschrift, Medikamentenplan & Notruf")
                    )

                    rbacRoles.forEach { (role, perm) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(role, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Text(perm, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Multi-Language Switcher
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("language_selector_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sprache / Language", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AppLanguage.values()) { lang ->
                            FilterChip(
                                selected = currentLanguage == lang,
                                onClick = { onSetLanguage(lang) },
                                label = { Text("${lang.flag} ${lang.label}", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Cloud Synchronization & Backup Recovery
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("cloud_sync_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cloud-Synchronisation & Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = syncState.syncMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onTriggerSync,
                        enabled = !syncState.isSyncing,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("sync_now_button")
                    ) {
                        if (syncState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Synchronisiere...", fontSize = 12.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Jetzt Sichern / Sync", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Notification Settings
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Benachrichtigungsregeln", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dringende Reparatur-Alarme", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Vibration & Ton bei Rohrbruch oder Filteralarm", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = urgentNotifToggle, onCheckedChange = { urgentNotifToggle = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Medikamenten-Erinnerungen", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Tägliche Einnahmeerinnerungen für Senioren", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = medNotifToggle, onCheckedChange = { medNotifToggle = it })
                    }
                }
            }
        }

        // Appearance
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Darstellung & Barrierefreiheit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dunkler Modus (Dark Mode)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hoher Kontrast (Seniorenmodus)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Switch(checked = isHighContrast, onCheckedChange = { onToggleHighContrast() })
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Confirmation Dialog: Clear Example Data
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Beispieldaten löschen?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Alle vorhandenen Test-Aufgaben, Dummy-Ausgaben, Test-Einkäufe und Test-Geräte werden unwiderruflich gelöscht. Deine App ist danach sauber und bereit für deine echten Familiendaten.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearExampleData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ja, alles bereinigen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Add Family Member Dialog
    if (showAddMemberDialog) {
        var memberName by remember { mutableStateOf("") }
        var memberRole by remember { mutableStateOf("TEEN") }
        var relationship by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }

        val roles = listOf("ADMIN", "TEEN", "CHILD", "SENIOR")

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Familienmitglied anlegen", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = memberName,
                        onValueChange = { memberName = it },
                        label = { Text("Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = relationship,
                        onValueChange = { relationship = it },
                        label = { Text("Rolle in der Familie (z. B. Sohn, Tochter, Oma)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefonnummer (optional für Notruf)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Berechtigungsebene:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        roles.forEach { r ->
                            FilterChip(
                                selected = memberRole == r,
                                onClick = { memberRole = r },
                                label = { Text(r, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memberName.isNotBlank()) {
                            onAddMember(memberName, memberRole, relationship.ifEmpty { memberRole }, phone)
                            showAddMemberDialog = false
                        }
                    }
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Budget Cap Dialog
    if (showBudgetCapDialog) {
        var capInput by remember { mutableStateOf(monthlyBudgetCap.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showBudgetCapDialog = false },
            title = { Text("Monatslimit anpassen", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = capInput,
                    onValueChange = { capInput = it },
                    label = { Text("Limit in Euro (€) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cap = capInput.toDoubleOrNull()
                        if (cap != null && cap > 0) {
                            onUpdateBudgetCap(cap)
                            showBudgetCapDialog = false
                        }
                    }
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetCapDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
