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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
    onToggleChore: (ChoreEntity) -> Unit
) {
    var selectedDayOffset by remember { mutableStateOf(0) }
    var selectedMemberFilter by remember { mutableStateOf<Long?>(null) }

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, selectedDayOffset)
    val selectedDate = calendar.time

    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    // Generate days for horizontal strip (-3 to +10 days)
    val daysList = (-3..10).map { offset ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offset)
        offset to cal.time
    }

    // Filter chores for selected day
    val dayChores = chores.filter { chore ->
        val matchesMember = selectedMemberFilter == null || chore.assignedMemberId == selectedMemberFilter
        // Simple day matching for demo
        matchesMember
    }

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
                        // Dot for scheduled tasks
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary
                                )
                        )
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

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Day Title
        Text(
            text = fullDateFormat.format(selectedDate),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Agenda Items
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Medication Times for today
            if (medications.isNotEmpty() && (selectedMemberFilter == null || selectedMemberFilter == 5L)) {
                item {
                    Text(
                        text = "Medication Routine",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                items(medications) { med ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
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
                                    text = "${med.dosage} (${med.timeOfDay}) - Grandma Rose",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (med.isTakenToday) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Taken ✓",
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

            item {
                Text(
                    text = "Scheduled Chores & Maintenance",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(dayChores) { chore ->
                ChoreItemCard(chore = chore, onToggle = { onToggleChore(chore) })
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
