package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChoreEntity
import com.example.data.entity.FamilyMemberEntity
import com.example.data.entity.MedicationEntity
import com.example.data.model.AppLanguage
import com.example.localization.LanguageManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    chores: List<ChoreEntity>,
    members: List<FamilyMemberEntity>,
    medications: List<MedicationEntity>,
    language: AppLanguage,
    onToggleChore: (ChoreEntity) -> Unit,
    onAddChore: ((String, String, String, Long, String, Boolean, String, Int) -> Unit)? = null
) {
    var selectedDayOffset by remember { mutableStateOf(0) }
    var selectedMemberFilter by remember { mutableStateOf<Long?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, selectedDayOffset)
    val selectedDate = calendar.time

    val locale = if (language == AppLanguage.DE) Locale.GERMAN else Locale.getDefault()
    val dayFormat = SimpleDateFormat("EEE", locale)
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", locale)
    val fullDateFormat = SimpleDateFormat("EEEE, d. MMMM yyyy", locale)
    val ymdFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val selectedYmd = ymdFormat.format(selectedDate)
    val selectedDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    // Generate days for horizontal strip (-3 to +10 days)
    val daysList = (-3..10).map { offset ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offset)
        offset to cal.time
    }

    // Helper to check if a chore matches a specific date
    fun choreMatchesDate(chore: ChoreEntity, dateYmd: String, dayOfWeek: Int): Boolean {
        val choreDueDate = Date(chore.dueDate)
        val choreYmd = ymdFormat.format(choreDueDate)
        if (choreYmd == dateYmd) return true
        if (chore.recurrence.equals("Daily", ignoreCase = true) || chore.recurrence.equals("Täglich", ignoreCase = true)) return true
        if ((chore.recurrence.equals("Weekly", ignoreCase = true) || chore.recurrence.equals("Wöchentlich", ignoreCase = true))) {
            val choreCal = Calendar.getInstance().apply { time = choreDueDate }
            if (choreCal.get(Calendar.DAY_OF_WEEK) == dayOfWeek) return true
        }
        if (chore.isUrgentRepair && !chore.isCompleted) return true
        return false
    }

    // Filter chores for selected day
    val dayChores = chores.filter { chore ->
        val matchesMember = selectedMemberFilter == null || chore.assignedMemberId == selectedMemberFilter
        val matchesDate = choreMatchesDate(chore, selectedYmd, selectedDayOfWeek)
        matchesMember && matchesDate
    }

    val dayAppointments = dayChores.filter { it.category.equals("APPOINTMENT", ignoreCase = true) }
    val dayTasks = dayChores.filter { !it.category.equals("APPOINTMENT", ignoreCase = true) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Month & Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthYearFormat.format(selectedDate),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = { selectedDayOffset-- }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Day")
                    }
                    IconButton(onClick = { selectedDayOffset++ }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Day")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Date Strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(daysList) { (offset, date) ->
                    val isSelected = offset == selectedDayOffset
                    val isToday = offset == 0
                    val cal = Calendar.getInstance().apply { time = date }
                    val dayNum = cal.get(Calendar.DAY_OF_MONTH)
                    val dayName = dayFormat.format(date)
                    val dateYmd = ymdFormat.format(date)
                    val dow = cal.get(Calendar.DAY_OF_WEEK)

                    // Check if tasks exist on this day
                    val hasTasks = chores.any { choreMatchesDate(it, dateYmd, dow) } ||
                            (medications.isNotEmpty() && offset >= 0)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else if (isToday) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .width(54.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedDayOffset = offset }
                            .testTag("calendar_day_$offset")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$dayNum",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Dot for scheduled tasks
                            if (hasTasks) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.primary
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.size(4.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Family Member Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedMemberFilter == null,
                        onClick = { selectedMemberFilter = null },
                        label = { Text(LanguageManager.getString("all_family", language)) }
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

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Day Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fullDateFormat.format(selectedDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Agenda Items
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Medication Routine for today
                if (medications.isNotEmpty() && selectedDayOffset >= 0 && (selectedMemberFilter == null || members.any { it.role == "SENIOR" && it.id == selectedMemberFilter })) {
                    item {
                        Text(
                            text = LanguageManager.getString("medication_routine", language),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    items(medications) { med ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${med.reminderTime} • ${med.name}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "${med.dosage} (${med.timeOfDay})" + if (med.instructions.isNotBlank()) " • ${med.instructions}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (med.isTakenToday && selectedDayOffset == 0) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = LanguageManager.getString("take_dose", language),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Appointments & Calendar Events for selected day
                if (dayAppointments.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Termine & Familienkalender",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    items(dayAppointments) { appointment ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (appointment.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleChore(appointment) }
                                .testTag("appointment_card_${appointment.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (appointment.isCompleted) Icons.Default.CheckCircle else Icons.Default.Event,
                                    contentDescription = null,
                                    tint = if (appointment.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = appointment.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "TERMIN",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (appointment.description.isNotBlank()) {
                                        Text(
                                            text = appointment.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${appointment.assignedMemberName} • ${appointment.recurrence}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }

                // Tasks & Chores section
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageManager.getString("chores_routine", language),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (dayTasks.isEmpty() && dayAppointments.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().testTag("empty_calendar_card")
                        ) {
                            Box(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Keine Aufgaben oder Termine für diesen Tag geplant. Tippe auf '+', um einen Termin oder eine Aufgabe einzutragen!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else if (dayTasks.isEmpty()) {
                    item {
                        Text(
                            text = "Keine zusätzlichen Haushaltsaufgaben für diesen Tag.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(dayTasks) { chore ->
                        ChoreItemCard(chore = chore, onToggle = { onToggleChore(chore) })
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // FAB to add chore / event for this day
        if (onAddChore != null) {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("calendar_add_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Calendar Event")
            }
        }
    }

    if (isAddDialogOpen && onAddChore != null) {
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
