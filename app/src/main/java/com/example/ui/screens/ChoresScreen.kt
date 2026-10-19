package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChoreEntity
import com.example.data.entity.FamilyMemberEntity
import com.example.data.model.AppLanguage
import com.example.localization.LanguageManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChoresScreen(
    chores: List<ChoreEntity>,
    members: List<FamilyMemberEntity>,
    language: AppLanguage,
    onToggleChore: (ChoreEntity) -> Unit,
    onDeleteChore: (ChoreEntity) -> Unit,
    onAddChore: (String, String, String, Long, String, Boolean, String, Int) -> Unit,
    onClearCompleted: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "OPEN", "COMPLETED"
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedMemberFilter by remember { mutableStateOf<Long?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }

    val categories = listOf(
        Pair("ALL", if (language == AppLanguage.DE) "Alle" else "All"),
        Pair("URGENT", if (language == AppLanguage.DE) "Dringend" else "Urgent"),
        Pair("KITCHEN", if (language == AppLanguage.DE) "Küche" else "Kitchen"),
        Pair("CLEANING", if (language == AppLanguage.DE) "Reinigung" else "Cleaning"),
        Pair("MAINTENANCE", if (language == AppLanguage.DE) "Wartung" else "Maintenance"),
        Pair("YARD", if (language == AppLanguage.DE) "Garten" else "Yard"),
        Pair("SAFETY", if (language == AppLanguage.DE) "Sicherheit" else "Safety"),
        Pair("PETS", if (language == AppLanguage.DE) "Haustiere" else "Pets"),
        Pair("LAUNDRY", if (language == AppLanguage.DE) "Wäsche" else "Laundry")
    )

    val completedCount = chores.count { it.isCompleted }

    val filteredChores = chores.filter { chore ->
        val matchesCategory = when (selectedCategory) {
            "ALL" -> true
            "URGENT" -> chore.isUrgentRepair
            else -> chore.category.equals(selectedCategory, ignoreCase = true)
        }
        val matchesMember = selectedMemberFilter == null || chore.assignedMemberId == selectedMemberFilter
        val matchesStatus = when (statusFilter) {
            "OPEN" -> !chore.isCompleted
            "COMPLETED" -> chore.isCompleted
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                chore.title.contains(searchQuery, ignoreCase = true) ||
                chore.description.contains(searchQuery, ignoreCase = true) ||
                chore.assignedMemberName.contains(searchQuery, ignoreCase = true) ||
                chore.category.contains(searchQuery, ignoreCase = true)

        matchesCategory && matchesMember && matchesStatus && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Title & Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.getString("chores_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${filteredChores.size} ${LanguageManager.getString("tasks_count", language)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chores_search_input"),
                placeholder = { Text(LanguageManager.getString("search_chores", language), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Tabs (All, Open, Completed) + Clear Completed Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = statusFilter == "ALL",
                        onClick = { statusFilter = "ALL" },
                        label = { Text(LanguageManager.getString("filter_all", language), fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = statusFilter == "OPEN",
                        onClick = { statusFilter = "OPEN" },
                        label = { Text(LanguageManager.getString("filter_open", language), fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = statusFilter == "COMPLETED",
                        onClick = { statusFilter = "COMPLETED" },
                        label = { Text(LanguageManager.getString("filter_completed", language), fontSize = 12.sp) }
                    )
                }

                if (completedCount > 0 && onClearCompleted != null) {
                    TextButton(
                        onClick = onClearCompleted,
                        modifier = Modifier.testTag("clear_completed_chores_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LanguageManager.getString("clear_completed_chores", language), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { (catKey, catLabel) ->
                    FilterChip(
                        selected = selectedCategory == catKey,
                        onClick = { selectedCategory = catKey },
                        label = { Text(catLabel, fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_$catKey")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Family Member filter pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedMemberFilter == null,
                        onClick = { selectedMemberFilter = null },
                        label = { Text(LanguageManager.getString("all_family", language), fontSize = 12.sp) }
                    )
                }
                items(members) { member ->
                    FilterChip(
                        selected = selectedMemberFilter == member.id,
                        onClick = {
                            selectedMemberFilter = if (selectedMemberFilter == member.id) null else member.id
                        },
                        label = { Text(member.name, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chore List
            if (filteredChores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = LanguageManager.getString("no_chores_found", language),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { isAddDialogOpen = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(LanguageManager.getString("add_chore", language))
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredChores) { chore ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chore_detail_${chore.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (chore.isUrgentRepair && !chore.isCompleted) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                else if (chore.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (chore.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Toggle",
                                    tint = if (chore.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { onToggleChore(chore) }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = chore.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (chore.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (chore.description.isNotBlank()) {
                                        Text(
                                            text = chore.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = chore.assignedMemberName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = chore.recurrence,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (chore.isUrgentRepair) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.error,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "URGENT",
                                                    color = MaterialTheme.colorScheme.onError,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else {
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "+${chore.pointsValue} ${LanguageManager.getString("points", language)}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                IconButton(onClick = { onDeleteChore(chore) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { isAddDialogOpen = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("chores_screen_add_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    if (isAddDialogOpen) {
        AddChoreDialog(
            members = members,
            onDismiss = { isAddDialogOpen = false },
            onConfirm = { title, desc, cat, memberId, memberName, isUrgent, recurrence, pts ->
                onAddChore(title, desc, cat, memberId, memberName, isUrgent, recurrence, pts)
                isAddDialogOpen = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddChoreDialog(
    members: List<FamilyMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long, String, Boolean, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("MAINTENANCE") }
    var selectedMember by remember { mutableStateOf(members.firstOrNull() ?: FamilyMemberEntity(1, "Familie", "ADMIN", 0L)) }
    var isUrgentRepair by remember { mutableStateOf(false) }
    var recurrence by remember { mutableStateOf("Einmalig") }

    val categories = listOf(
        Pair("APPOINTMENT", "Termin & Event"),
        Pair("MAINTENANCE", "Wartung & Reparatur"),
        Pair("CLEANING", "Reinigung"),
        Pair("KITCHEN", "Küche"),
        Pair("YARD", "Garten"),
        Pair("SAFETY", "Sicherheit"),
        Pair("PETS", "Haustiere"),
        Pair("LAUNDRY", "Wäsche")
    )

    val recurrences = listOf("Einmalig", "Täglich", "Wöchentlich", "Monatlich")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isUrgentRepair) "Dringende Haushaltsreparatur melden" else "Haushaltsaufgabe anlegen",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel der Aufgabe / Reparatur *") },
                    placeholder = { Text("z. B. Tropfenden Wasserhahn reparieren") },
                    modifier = Modifier.fillMaxWidth().testTag("add_chore_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung / Werkzeuge / Notizen") },
                    placeholder = { Text("Benötigte Werkzeuge, Raum, Anleitung...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isUrgentRepair = !isUrgentRepair }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isUrgentRepair,
                        onCheckedChange = { isUrgentRepair = it },
                        modifier = Modifier.testTag("urgent_repair_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Dringende Reparatur / Alarm",
                            fontWeight = FontWeight.Bold,
                            color = if (isUrgentRepair) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sendet sofortigen Push-Alarm & markiert rot",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text("Kategorie:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { (catKey, catLabel) ->
                        FilterChip(
                            selected = category == catKey,
                            onClick = { category = catKey },
                            label = { Text(catLabel, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Wiederholung:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    recurrences.forEach { rec ->
                        FilterChip(
                            selected = recurrence == rec,
                            onClick = { recurrence = rec },
                            label = { Text(rec, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Zuweisen an:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(members) { m ->
                        FilterChip(
                            selected = selectedMember.id == m.id,
                            onClick = { selectedMember = m },
                            label = { Text(m.name, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title,
                            description,
                            category,
                            selectedMember.id,
                            selectedMember.name,
                            isUrgentRepair,
                            recurrence,
                            if (isUrgentRepair) 30 else 15
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUrgentRepair) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("confirm_add_chore_button")
            ) {
                Text(if (isUrgentRepair) "Dringenden Alarm auslösen" else "Aufgabe speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
