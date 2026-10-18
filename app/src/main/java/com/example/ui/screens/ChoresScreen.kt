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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

@Composable
fun ChoresScreen(
    chores: List<ChoreEntity>,
    members: List<FamilyMemberEntity>,
    language: AppLanguage,
    onToggleChore: (ChoreEntity) -> Unit,
    onDeleteChore: (ChoreEntity) -> Unit,
    onAddChore: (String, String, String, Long, String, Boolean, String, Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedMemberFilter by remember { mutableStateOf<Long?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "URGENT", "KITCHEN", "CLEANING", "MAINTENANCE", "YARD", "SAFETY", "PETS")

    val filteredChores = chores.filter { chore ->
        val matchesCategory = when (selectedCategory) {
            "ALL" -> true
            "URGENT" -> chore.isUrgentRepair
            else -> chore.category.equals(selectedCategory, ignoreCase = true)
        }
        val matchesMember = selectedMemberFilter == null || chore.assignedMemberId == selectedMemberFilter
        matchesCategory && matchesMember
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
                    text = "Household Tasks & Chores",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${filteredChores.size} Tasks",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("filter_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Family Member filter pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedMemberFilter == null,
                        onClick = { selectedMemberFilter = null },
                        label = { Text("All Family") }
                    )
                }
                items(members) { member ->
                    FilterChip(
                        selected = selectedMemberFilter == member.id,
                        onClick = {
                            selectedMemberFilter = if (selectedMemberFilter == member.id) null else member.id
                        },
                        label = { Text(member.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chore List
            if (filteredChores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks found for this filter. Tap + to add a chore or repair!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    var selectedMember by remember { mutableStateOf(members.firstOrNull() ?: FamilyMemberEntity(1, "David (Dad)", "ADMIN", 0L)) }
    var isUrgentRepair by remember { mutableStateOf(false) }
    var recurrence by remember { mutableStateOf("Once") }

    val categories = listOf("MAINTENANCE", "CLEANING", "KITCHEN", "YARD", "SAFETY", "PETS", "LAUNDRY")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUrgentRepair) "Report Urgent Home Repair" else "Add Family Chore", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task / Repair Title *") },
                    placeholder = { Text("e.g., Fix Leaking Sink Pipe") },
                    modifier = Modifier.fillMaxWidth().testTag("add_chore_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    placeholder = { Text("Tools needed, location, instructions...") },
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
                            text = "Urgent Household Repair",
                            fontWeight = FontWeight.Bold,
                            color = if (isUrgentRepair) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sends instant priority push alert to family",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text("Category:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Assign to:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
                Text(if (isUrgentRepair) "Submit Urgent Alert" else "Save Task")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
