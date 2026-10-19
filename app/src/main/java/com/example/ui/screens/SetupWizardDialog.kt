package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.CategoryEntity
import com.example.data.entity.FamilyMemberEntity
import com.example.data.entity.GroupEntity

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun SetupWizardDialog(
    isOpen: Boolean,
    currentHouseholdName: String,
    members: List<FamilyMemberEntity>,
    groups: List<GroupEntity>,
    categories: List<CategoryEntity>,
    initialBudgetCap: Double,
    isDarkMode: Boolean,
    isSeniorMode: Boolean,
    onComplete: (householdName: String, monthlyBudget: Double, enableSeniorMode: Boolean, enableDarkMode: Boolean) -> Unit,
    onDismiss: () -> Unit,
    onAddMember: (String, String, String, String) -> Unit,
    onUpdateMember: (FamilyMemberEntity) -> Unit,
    onDeleteMember: (FamilyMemberEntity) -> Unit,
    onAddCategory: (String, String, Long) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    if (!isOpen) return

    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 5

    var householdNameInput by remember { mutableStateOf(currentHouseholdName.ifBlank { "Unsere Familie" }) }
    var budgetInput by remember { mutableStateOf(initialBudgetCap.toInt().toString()) }
    var enableSenior by remember { mutableStateOf(isSeniorMode) }
    var enableDark by remember { mutableStateOf(isDarkMode) }

    // Dialog state for adding/editing member in wizard
    var showMemberModal by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<FamilyMemberEntity?>(null) }
    var memberNameField by remember { mutableStateOf("") }
    var memberRoleField by remember { mutableStateOf("ADMIN") }
    var memberRelField by remember { mutableStateOf("Eltern") }
    var memberPhoneField by remember { mutableStateOf("") }

    // Dialog state for adding category in wizard
    var showCategoryModal by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var newCatType by remember { mutableStateOf("CHORE") }

    Dialog(
        onDismissRequest = { /* Force explicit completion or skip */ },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("setup_wizard_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header: Wizard Title & Step Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FamilyRestroom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ersteinrichtungs-Assistent",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Schritt $currentStep von $totalSteps: ${getStepTitle(currentStep)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TextButton(onClick = {
                        onComplete(householdNameInput, budgetInput.toDoubleOrNull() ?: 1200.0, enableSenior, enableDark)
                    }) {
                        Text("Überspringen", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i <= currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step Content
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(targetState = currentStep, label = "wizard_step_anim") { step ->
                        when (step) {
                            1 -> Step1Household(
                                householdName = householdNameInput,
                                onNameChange = { householdNameInput = it }
                            )
                            2 -> Step2Members(
                                members = members,
                                onAddClick = {
                                    editingMember = null
                                    memberNameField = ""
                                    memberRoleField = "TEEN"
                                    memberRelField = "Familienmitglied"
                                    memberPhoneField = ""
                                    showMemberModal = true
                                },
                                onEditClick = { m ->
                                    editingMember = m
                                    memberNameField = m.name
                                    memberRoleField = m.role
                                    memberRelField = m.relationship
                                    memberPhoneField = m.emergencyPhone
                                    showMemberModal = true
                                },
                                onDeleteClick = onDeleteMember
                            )
                            3 -> Step3Categories(
                                categories = categories,
                                onAddClick = {
                                    newCatName = ""
                                    newCatType = "CHORE"
                                    showCategoryModal = true
                                },
                                onDeleteClick = onDeleteCategory
                            )
                            4 -> Step4Preferences(
                                budget = budgetInput,
                                onBudgetChange = { budgetInput = it },
                                enableSenior = enableSenior,
                                onSeniorChange = { enableSenior = it },
                                enableDark = enableDark,
                                onDarkChange = { enableDark = it }
                            )
                            5 -> Step5Summary(
                                householdName = householdNameInput,
                                membersCount = members.size,
                                categoriesCount = categories.size,
                                budget = budgetInput.toDoubleOrNull() ?: 1200.0,
                                enableSenior = enableSenior,
                                enableDark = enableDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Controls (Back / Forward / Finish)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Zurück")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStep < totalSteps) {
                        Button(
                            onClick = { currentStep++ },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("wizard_next_button")
                        ) {
                            Text("Weiter")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                onComplete(
                                    householdNameInput,
                                    budgetInput.toDoubleOrNull() ?: 1200.0,
                                    enableSenior,
                                    enableDark
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("wizard_finish_button")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("100% Bereit • Jetzt starten", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal: Add or Edit Member inside Wizard
    if (showMemberModal) {
        val roles = listOf("ADMIN", "TEEN", "CHILD", "SENIOR")
        AlertDialog(
            onDismissRequest = { showMemberModal = false },
            title = { Text(if (editingMember == null) "Familienmitglied hinzufügen" else "Mitglied bearbeiten", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = memberNameField,
                        onValueChange = { memberNameField = it },
                        label = { Text("Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = memberRelField,
                        onValueChange = { memberRelField = it },
                        label = { Text("Beziehung (z. B. Mutter, Sohn, Oma)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = memberPhoneField,
                        onValueChange = { memberPhoneField = it },
                        label = { Text("Notfall-Telefon (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Rolle & Berechtigung:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        roles.forEach { r ->
                            FilterChip(
                                selected = memberRoleField == r,
                                onClick = { memberRoleField = r },
                                label = { Text(r, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memberNameField.isNotBlank()) {
                            if (editingMember != null) {
                                onUpdateMember(
                                    editingMember!!.copy(
                                        name = memberNameField,
                                        role = memberRoleField,
                                        relationship = memberRelField,
                                        emergencyPhone = memberPhoneField
                                    )
                                )
                            } else {
                                onAddMember(memberNameField, memberRoleField, memberRelField, memberPhoneField)
                            }
                            showMemberModal = false
                        }
                    }
                ) {
                    Text(if (editingMember == null) "Hinzufügen" else "Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMemberModal = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Modal: Add Category inside Wizard
    if (showCategoryModal) {
        val types = listOf(
            Pair("CHORE", "Aufgaben"),
            Pair("SHOPPING", "Einkauf"),
            Pair("EXPENSE", "Finanzen"),
            Pair("ALL", "Allgemein")
        )
        AlertDialog(
            onDismissRequest = { showCategoryModal = false },
            title = { Text("Neue Kategorie anlegen", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Kategoriename *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Verwendung:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        types.forEach { t ->
                            FilterChip(
                                selected = newCatType == t.first,
                                onClick = { newCatType = t.first },
                                label = { Text(t.second, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            onAddCategory(newCatName, newCatType, 0xFF0D9488L)
                            showCategoryModal = false
                        }
                    }
                ) {
                    Text("Erstellen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryModal = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

private fun getStepTitle(step: Int): String {
    return when (step) {
        1 -> "Haushalt & Gruppe anlegen"
        2 -> "Familienmitglieder & Rollen"
        3 -> "Kategorien anpassen"
        4 -> "Budget & Modi konfigurieren"
        5 -> "Zusammenfassung & Start"
        else -> ""
    }
}

@Composable
fun Step1Household(
    householdName: String,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Willkommen bei HomeNest!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Richte deinen Haushalt, deine Familie oder Wohngemeinschaft in wenigen Schritten ein.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Text("Wie heißt dein Haushalt oder deine Gruppe?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = householdName,
            onValueChange = onNameChange,
            label = { Text("Haushaltsname (z. B. Familie Müller, WG Sonnenallee)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("wizard_household_name_input")
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Vorteile der Gruppeneinrichtung:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Aufgaben und Einkaufslisten werden automatisch allen geteilt\n• Punkte & Belohnungen für Kinder und Jugendliche\n• Integrierter Seniorenmodus für ältere Familienmitglieder\n• 100% Offline-fähig mit lokaler SQLite-Datenbank", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun Step2Members(
    members: List<FamilyMemberEntity>,
    onAddClick: () -> Unit,
    onEditClick: (FamilyMemberEntity) -> Unit,
    onDeleteClick: (FamilyMemberEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mitglieder im Haushalt (${members.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(10.dp),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hinzufügen", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Passe die Namen an oder erstelle neue Profile für deine Familienmitglieder.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(members) { m ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                            Text(text = "${m.relationship.ifEmpty { "Mitglied" }} • Rolle: ${m.role}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onEditClick(m) }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Bearbeiten", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        if (members.size > 1) {
                            IconButton(onClick = { onDeleteClick(m) }, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Löschen", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step3Categories(
    categories: List<CategoryEntity>,
    onAddClick: () -> Unit,
    onDeleteClick: (CategoryEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf("ALL") }

    val filtered = when (selectedTab) {
        "ALL" -> categories
        "CHORE" -> categories.filter { it.type == "CHORE" || it.type == "ALL" }
        "SHOPPING" -> categories.filter { it.type == "SHOPPING" || it.type == "ALL" }
        "EXPENSE" -> categories.filter { it.type == "EXPENSE" || it.type == "ALL" }
        else -> categories
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kategorien (${categories.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Neue Kategorie", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Kategorien für Aufgaben, Einkäufe und Ausgaben. Du kannst Kategorien bearbeiten oder entfernen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips for type
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Pair("ALL", "Alle"),
                Pair("CHORE", "Aufgaben"),
                Pair("SHOPPING", "Einkauf"),
                Pair("EXPENSE", "Finanzen")
            ).forEach { tab ->
                FilterChip(
                    selected = selectedTab == tab.first,
                    onClick = { selectedTab = tab.first },
                    label = { Text(tab.second, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filtered) { cat ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(cat.color))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = cat.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = cat.type,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteClick(cat) }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Löschen", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Step4Preferences(
    budget: String,
    onBudgetChange: (String) -> Unit,
    enableSenior: Boolean,
    onSeniorChange: (Boolean) -> Unit,
    enableDark: Boolean,
    onDarkChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Budget & Grundeinstellungen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Monatliches Budgetlimit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Hilft deiner Familie, monatliche Ausgaben und Reparaturen im Blick zu behalten:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = budget,
                    onValueChange = onBudgetChange,
                    label = { Text("Monatslimit in Euro (€)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Senioren-Betreuungsmodus", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Große Schrift, kontrastreiche Farben und Medikamenten-Übersicht für ältere Angehörige.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = enableSenior, onCheckedChange = onSeniorChange)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dunkler Modus (Dark Theme)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Schont die Augen bei Dunkelheit und spart Akkuleistung.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = enableDark, onCheckedChange = onDarkChange)
                }
            }
        }
    }
}

@Composable
fun Step5Summary(
    householdName: String,
    membersCount: Int,
    categoriesCount: Int,
    budget: Double,
    enableSenior: Boolean,
    enableDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ersteinrichtung 100% fertig!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dein Haushalt ist vollständig eingerichtet und bereit für den Alltag.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Übersicht deiner Konfiguration:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Haushaltsname:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(householdName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Familienmitglieder:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$membersCount Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Kategorien:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$categoriesCount aktive Kategorien", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Monatsbudget:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${budget.toInt()} €", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Senioren-Modus:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (enableSenior) "Aktiviert" else "Deaktiviert", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Erscheinungsbild:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (enableDark) "Dunkles Design" else "Helles Design", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
