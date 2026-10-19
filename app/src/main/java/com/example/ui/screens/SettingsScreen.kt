package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CategoryEntity
import com.example.data.entity.FamilyMemberEntity
import com.example.data.entity.GroupEntity
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.localization.LanguageManager
import com.example.ui.viewmodel.SyncState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    members: List<FamilyMemberEntity>,
    groups: List<GroupEntity>,
    categories: List<CategoryEntity>,
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
    onAddMember: (String, String, String, String) -> Unit,
    onUpdateMember: (FamilyMemberEntity) -> Unit,
    onDeleteMember: (FamilyMemberEntity) -> Unit,
    onAddGroup: (String, String, Long) -> Unit,
    onUpdateGroup: (GroupEntity) -> Unit,
    onDeleteGroup: (GroupEntity) -> Unit,
    onAddCategory: (String, String, Long) -> Unit,
    onUpdateCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onUpdateBudgetCap: (Double) -> Unit,
    onStartSetupWizard: () -> Unit
) {
    var urgentNotifToggle by remember { mutableStateOf(true) }
    var medNotifToggle by remember { mutableStateOf(true) }
    var choreNotifToggle by remember { mutableStateOf(true) }

    // Dialog states
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<FamilyMemberEntity?>(null) }
    var deletingMember by remember { mutableStateOf<FamilyMemberEntity?>(null) }

    var showAddGroupDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<GroupEntity?>(null) }
    var deletingGroup by remember { mutableStateOf<GroupEntity?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var deletingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryFilterTab by remember { mutableStateOf("ALL") }

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
                text = "Einstellungen & Haushaltsverwaltung",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gruppen, Mitglieder, Rollen, Kategorien, Budget & Personalisierung",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Ersteinrichtungs-Assistent Tile
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth().testTag("setup_wizard_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ersteinrichtungs-Assistent",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Haushalt, Rollen & Kategorien Schritt für Schritt anpassen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onStartSetupWizard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("launch_setup_wizard_button")
                    ) {
                        Text("Assistent", fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. Gruppen Management (Erstellen, Bearbeiten, Löschen)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("groups_management_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Haushalte & Gruppen (${groups.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { showAddGroupDialog = true }, modifier = Modifier.testTag("add_group_button")) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Gruppe hinzufügen", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Verwalte verschiedene Haushalte, WGs oder Gruppen:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (groups.isEmpty()) {
                        Text("Keine Gruppen vorhanden. Erstelle eine neue Gruppe.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            groups.forEach { group ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(group.color)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = group.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                text = group.description.ifEmpty { "Gemeinsamer Haushalt" },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { editingGroup = group }, modifier = Modifier.size(32.dp)) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Gruppe bearbeiten", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                        if (groups.size > 1) {
                                            IconButton(onClick = { deletingGroup = group }, modifier = Modifier.size(32.dp)) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Gruppe löschen", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Familienmitglieder Management (Erstellen, Bearbeiten, Löschen)
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
                            Text("Familienmitglieder (${members.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { showAddMemberDialog = true }, modifier = Modifier.testTag("add_member_button")) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Mitglied hinzufügen", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aktuell aktiv: ${activeMember?.name ?: "Familie"} (${activeMember?.role ?: "ADMIN"})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Switcher Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(members) { m ->
                            FilterChip(
                                selected = m.id == activeMember?.id,
                                onClick = { onSelectMember(m) },
                                label = { Text("${m.name} (${m.role})", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Alle Profile verwalten:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        members.forEach { m ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(m.avatarColor)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (m.name.firstOrNull() ?: 'M').toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = m.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "${m.relationship.ifEmpty { "Mitglied" }} • Rolle: ${m.role}${if (m.emergencyPhone.isNotBlank()) " • Tel: ${m.emergencyPhone}" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { editingMember = m }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Mitglied bearbeiten", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                    if (members.size > 1) {
                                        IconButton(onClick = { deletingMember = m }, modifier = Modifier.size(32.dp)) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Mitglied löschen", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Kategorien Management (Erstellen, Bearbeiten, Löschen)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("categories_management_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kategorien (${categories.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { showAddCategoryDialog = true }, modifier = Modifier.testTag("add_category_button")) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Kategorie hinzufügen", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kategorien für Aufgaben, Einkäufe und monatliche Ausgaben:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter tabs
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            Pair("ALL", "Alle"),
                            Pair("CHORE", "Aufgaben"),
                            Pair("SHOPPING", "Einkauf"),
                            Pair("EXPENSE", "Finanzen")
                        ).forEach { tab ->
                            FilterChip(
                                selected = categoryFilterTab == tab.first,
                                onClick = { categoryFilterTab = tab.first },
                                label = { Text(tab.second, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredCategories = when (categoryFilterTab) {
                        "ALL" -> categories
                        "CHORE" -> categories.filter { it.type == "CHORE" || it.type == "ALL" }
                        "SHOPPING" -> categories.filter { it.type == "SHOPPING" || it.type == "ALL" }
                        "EXPENSE" -> categories.filter { it.type == "EXPENSE" || it.type == "ALL" }
                        else -> categories
                    }

                    if (filteredCategories.isEmpty()) {
                        Text("Keine Kategorien für diesen Filter gefunden.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            filteredCategories.forEach { cat ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(cat.color))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = cat.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                            Text(text = "Typ: ${cat.type}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { editingCategory = cat }, modifier = Modifier.size(30.dp)) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Kategorie bearbeiten", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { deletingCategory = cat }, modifier = Modifier.size(30.dp)) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Kategorie löschen", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Budget Cap Setting
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

        // 6. Role-Based Access Control (RBAC) Matrix
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
                        Pair("Jugendliche (Teen)", "Aufgaben abhaken, Einkaufsliste pflegen, Punkte sammeln"),
                        Pair("Kinder (Child)", "Eigene Aufgaben abhaken, Belohnungen & Sparziele sehen"),
                        Pair("Senior / Pflege", "Großschrift-Modus, Vitaldaten, Medikamente, Notruf-Auslöser")
                    )

                    rbacRoles.forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = role.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = role.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // 7. Cloud Sync & Backup Status
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("cloud_sync_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synchronisation & Daten", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        if (syncState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { onTriggerSync() }) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = "Jetzt synchronisieren", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = syncState.syncMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Status: ${syncState.lastSyncTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 8. Notifications Settings
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
                        Text("Benachrichtigungen", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dringende Reparaturen & Alarme", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Switch(checked = urgentNotifToggle, onCheckedChange = { urgentNotifToggle = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Medikamenten-Erinnerungen (Senioren)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Switch(checked = medNotifToggle, onCheckedChange = { medNotifToggle = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fällige Haushaltsaufgaben", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Switch(checked = choreNotifToggle, onCheckedChange = { choreNotifToggle = it })
                    }
                }
            }
        }

        // 9. Language & Display Settings
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sprache & Darstellung", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("App-Sprache:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = currentLanguage == AppLanguage.DE,
                            onClick = { onSetLanguage(AppLanguage.DE) },
                            label = { Text("Deutsch (DE)") }
                        )
                        FilterChip(
                            selected = currentLanguage == AppLanguage.EN,
                            onClick = { onSetLanguage(AppLanguage.EN) },
                            label = { Text("English (EN)") }
                        )
                    }

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

    // Modal: Add Family Member
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
                        modifier = Modifier.fillMaxWidth().testTag("add_member_name_input")
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
                    },
                    modifier = Modifier.testTag("confirm_add_member_button")
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

    // Modal: Edit Family Member
    editingMember?.let { m ->
        var editName by remember { mutableStateOf(m.name) }
        var editRole by remember { mutableStateOf(m.role) }
        var editRel by remember { mutableStateOf(m.relationship) }
        var editPhone by remember { mutableStateOf(m.emergencyPhone) }
        val roles = listOf("ADMIN", "TEEN", "CHILD", "SENIOR")

        AlertDialog(
            onDismissRequest = { editingMember = null },
            title = { Text("Mitglied bearbeiten", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editRel,
                        onValueChange = { editRel = it },
                        label = { Text("Rolle/Beziehung in der Familie") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Telefonnummer (Notfall)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Berechtigungsrolle:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        roles.forEach { r ->
                            FilterChip(
                                selected = editRole == r,
                                onClick = { editRole = r },
                                label = { Text(r, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            onUpdateMember(
                                m.copy(
                                    name = editName,
                                    role = editRole,
                                    relationship = editRel,
                                    emergencyPhone = editPhone
                                )
                            )
                            editingMember = null
                        }
                    }
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMember = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Modal: Delete Family Member confirmation
    deletingMember?.let { m ->
        AlertDialog(
            onDismissRequest = { deletingMember = null },
            title = { Text("Mitglied löschen?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Möchtest du das Profil von '${m.name}' wirklich entfernen? Zugeordnete Aufgaben bleiben erhalten.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMember(m)
                        deletingMember = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingMember = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Modal: Add Group
    if (showAddGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        var groupDesc by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(0xFF0D9488L) }
        val colorOptions = listOf(0xFF0D9488L, 0xFF2563EBL, 0xFF8B5CF6L, 0xFFF59E0BL, 0xFFEC4899L, 0xFF10B981L)

        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("Neue Haushaltsgruppe anlegen", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Gruppenname (z. B. Ferienhaus, WG)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Beschreibung (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Farbe wählen:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .clickable { selectedColor = c }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == c) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            onAddGroup(groupName, groupDesc, selectedColor)
                            showAddGroupDialog = false
                        }
                    }
                ) {
                    Text("Erstellen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Modal: Edit Group
    editingGroup?.let { g ->
        var editGroupName by remember { mutableStateOf(g.name) }
        var editGroupDesc by remember { mutableStateOf(g.description) }
        var editGroupColor by remember { mutableStateOf(g.color) }
        val colorOptions = listOf(0xFF0D9488L, 0xFF2563EBL, 0xFF8B5CF6L, 0xFFF59E0BL, 0xFFEC4899L, 0xFF10B981L)

        AlertDialog(
            onDismissRequest = { editingGroup = null },
            title = { Text("Gruppe bearbeiten", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editGroupName,
                        onValueChange = { editGroupName = it },
                        label = { Text("Gruppenname *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGroupDesc,
                        onValueChange = { editGroupDesc = it },
                        label = { Text("Beschreibung") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Farbe:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .clickable { editGroupColor = c }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (editGroupColor == c) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editGroupName.isNotBlank()) {
                            onUpdateGroup(g.copy(name = editGroupName, description = editGroupDesc, color = editGroupColor))
                            editingGroup = null
                        }
                    }
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingGroup = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Modal: Delete Group confirmation
    deletingGroup?.let { g ->
        AlertDialog(
            onDismissRequest = { deletingGroup = null },
            title = { Text("Gruppe löschen?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Möchtest du die Gruppe '${g.name}' wirklich entfernen?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGroup(g)
                        deletingGroup = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingGroup = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Modal: Add Category
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catType by remember { mutableStateOf("CHORE") }
        var catColor by remember { mutableStateOf(0xFF0D9488L) }
        val typeOptions = listOf(
            Pair("CHORE", "Aufgaben"),
            Pair("SHOPPING", "Einkauf"),
            Pair("EXPENSE", "Finanzen"),
            Pair("ALL", "Allgemein")
        )
        val colorOptions = listOf(0xFF0D9488L, 0xFFE11D48L, 0xFFD97706L, 0xFF16A34AL, 0xFF7C3AEDL, 0xFF2563EBL)

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Kategorie anlegen", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Kategoriename *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Typ:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        typeOptions.forEach { t ->
                            FilterChip(
                                selected = catType == t.first,
                                onClick = { catType = t.first },
                                label = { Text(t.second, fontSize = 11.sp) }
                            )
                        }
                    }
                    Text("Farbe:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .clickable { catColor = c },
                                contentAlignment = Alignment.Center
                            ) {
                                if (catColor == c) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            onAddCategory(catName, catType, catColor)
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text("Erstellen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Modal: Edit Category
    editingCategory?.let { c ->
        var editCatName by remember { mutableStateOf(c.name) }
        var editCatType by remember { mutableStateOf(c.type) }
        var editCatColor by remember { mutableStateOf(c.color) }
        val typeOptions = listOf(
            Pair("CHORE", "Aufgaben"),
            Pair("SHOPPING", "Einkauf"),
            Pair("EXPENSE", "Finanzen"),
            Pair("ALL", "Allgemein")
        )
        val colorOptions = listOf(0xFF0D9488L, 0xFFE11D48L, 0xFFD97706L, 0xFF16A34AL, 0xFF7C3AEDL, 0xFF2563EBL)

        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Kategorie bearbeiten", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editCatName,
                        onValueChange = { editCatName = it },
                        label = { Text("Kategoriename *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Typ:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        typeOptions.forEach { t ->
                            FilterChip(
                                selected = editCatType == t.first,
                                onClick = { editCatType = t.first },
                                label = { Text(t.second, fontSize = 11.sp) }
                            )
                        }
                    }
                    Text("Farbe:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .clickable { editCatColor = col },
                                contentAlignment = Alignment.Center
                            ) {
                                if (editCatColor == col) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editCatName.isNotBlank()) {
                            onUpdateCategory(c.copy(name = editCatName, type = editCatType, color = editCatColor))
                            editingCategory = null
                        }
                    }
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Modal: Delete Category confirmation
    deletingCategory?.let { c ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("Kategorie löschen?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Möchtest du die Kategorie '${c.name}' wirklich entfernen?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(c)
                        deletingCategory = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) {
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
