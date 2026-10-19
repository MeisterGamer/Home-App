package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExpenseEntity
import com.example.data.model.AppLanguage
import com.example.localization.LanguageManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetScreen(
    expenses: List<ExpenseEntity>,
    language: AppLanguage,
    monthlyCap: Double = 1200.0,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onAddExpense: (String, String, Double, String) -> Unit,
    onUpdateBudgetCap: (Double) -> Unit = {}
) {
    var isCapDialogOpen by remember { mutableStateOf(false) }
    var selectedTimeFilter by remember { mutableStateOf("ALL") }

    // Quick inline input state
    var quickTitle by remember { mutableStateOf("") }
    var quickAmountText by remember { mutableStateOf("") }
    var quickCategory by remember { mutableStateOf("LEBENSMITTEL") }
    var quickNotes by remember { mutableStateOf("") }
    var isInputFormExpanded by remember { mutableStateOf(true) }

    // Date calculations
    val now = Calendar.getInstance()
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val weekStart = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }.timeInMillis

    // Dynamic calculations
    val totalSpent = expenses.sumOf { it.amount }
    val remaining = monthlyCap - totalSpent
    val isOverBudget = remaining < 0
    val spentRatio = if (monthlyCap > 0) ((totalSpent / monthlyCap).coerceIn(0.0, 1.0)).toFloat() else 0f

    val todaySpent = expenses.filter { it.date >= todayStart }.sumOf { it.amount }

    // Days remaining in month
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (daysInMonth - dayOfMonth + 1).coerceAtLeast(1)
    val dailyBudgetRemaining = if (remaining > 0) remaining / daysRemaining else 0.0

    // Live preview when typing in input
    val typedAmount = quickAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val previewRemaining = remaining - typedAmount
    val isPreviewOverBudget = previewRemaining < 0

    val dateFormat = SimpleDateFormat("dd. MMM, HH:mm", Locale.GERMANY)

    // Filtered expenses list
    val filteredExpenses = expenses.filter { exp ->
        when (selectedTimeFilter) {
            "TODAY" -> exp.date >= todayStart
            "WEEK" -> exp.date >= weekStart
            else -> true
        }
    }

    // Category distribution
    val categoryTotals = expenses.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amount } }

    val presetQuickItems = listOf(
        Pair("Supermarkt", "LEBENSMITTEL"),
        Pair("Bäckerei", "LEBENSMITTEL"),
        Pair("Drogerie", "HAUSHALT"),
        Pair("Tanken", "SONSTIGES"),
        Pair("Apotheke", "GESUNDHEIT"),
        Pair("Baumarkt", "REPARATUR")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Header with Cap Adjustment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = LanguageManager.getString("budget_overview", language),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Haushaltsausgaben & Monatslimit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Budget Cap Pill & Edit Action
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isCapDialogOpen = true }
                            .testTag("budget_cap_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Limit: ${formatCurrency(monthlyCap, language)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // DYNAMIC BUDGET METRICS CARD
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isOverBudget) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_progress_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Total Spent Column
                            Column {
                                Text(
                                    text = LanguageManager.getString("spent_so_far", language),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isOverBudget) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = formatCurrency(totalSpent, language),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOverBudget) MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Dynamic Remaining Column
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isOverBudget) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (isOverBudget) LanguageManager.getString("over_budget", language)
                                        else LanguageManager.getString("remaining_budget", language),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOverBudget) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = formatCurrency(remaining, language),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        isOverBudget -> MaterialTheme.colorScheme.error
                                        remaining < 150.0 -> Color(0xFFD97706) // Amber warning
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.testTag("remaining_budget_text")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Bar with Dynamic Coloring
                        LinearProgressIndicator(
                            progress = { spentRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = when {
                                isOverBudget -> MaterialTheme.colorScheme.error
                                spentRatio > 0.85f -> Color(0xFFD97706)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary Stats: Today Spent & Daily Available
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${LanguageManager.getString("today_expenses", language)}: ${formatCurrency(todaySpent, language)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            }

                            Text(
                                text = "Ø ${formatCurrency(dailyBudgetRemaining, language)} / Tag (${daysRemaining} Tage übrig)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // DEDICATED DAILY EXPENSE INPUT CARD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_expense_input_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header with expand/collapse
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isInputFormExpanded = !isInputFormExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = LanguageManager.getString("daily_expenses_title", language),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Direkt tägliche Ausgaben buchen & Restbudget berechnen",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isInputFormExpanded = !isInputFormExpanded },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isInputFormExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                        }

                        AnimatedVisibility(visible = isInputFormExpanded) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                // Quick Preset Chips
                                Text(
                                    text = "Häufige Vorlagen:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    presetQuickItems.forEach { (name, cat) ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    quickTitle = name
                                                    quickCategory = cat
                                                }
                                        ) {
                                            Text(
                                                text = "+ $name",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Expense Title Field
                                OutlinedTextField(
                                    value = quickTitle,
                                    onValueChange = { quickTitle = it },
                                    label = { Text("Ausgabe / Verwendungszweck *") },
                                    placeholder = { Text(LanguageManager.getString("expense_title_placeholder", language)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quick_expense_title_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Amount Input & Quick +€ Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = quickAmountText,
                                        onValueChange = { quickAmountText = it },
                                        label = { Text(LanguageManager.getString("enter_amount", language) + " *") },
                                        placeholder = { Text("z.B. 18.50") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.AttachMoney,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("quick_expense_amount_input")
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick Amount Increments (+5 €, +10 €, +20 €, +50 €)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(5.0, 10.0, 20.0, 50.0).forEach { addVal ->
                                        OutlinedButton(
                                            onClick = {
                                                val curr = quickAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                                val next = curr + addVal
                                                quickAmountText = String.format(Locale.US, "%.2f", next)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("+${addVal.toInt()}€", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Category Selection
                                Text(
                                    text = "Kategorie:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val categories = listOf(
                                    "LEBENSMITTEL",
                                    "HAUSHALT",
                                    "REPARATUR",
                                    "ENERGIE",
                                    "GESUNDHEIT",
                                    "SONSTIGES"
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    categories.forEach { cat ->
                                        FilterChip(
                                            selected = quickCategory == cat,
                                            onClick = { quickCategory = cat },
                                            label = { Text(cat, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Optional Notes
                                OutlinedTextField(
                                    value = quickNotes,
                                    onValueChange = { quickNotes = it },
                                    label = { Text("Notiz / Geschäft (optional)") },
                                    placeholder = { Text("z.B. Kassenbon REWE...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // DYNAMIC LIVE REMAINING CALCULATION BANNER
                                if (typedAmount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isPreviewOverBudget) MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = LanguageManager.getString("remaining_after_expense", language),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isPreviewOverBudget) MaterialTheme.colorScheme.error
                                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Text(
                                                    text = "${formatCurrency(previewRemaining, language)} (-${formatCurrency(typedAmount, language)})",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isPreviewOverBudget) MaterialTheme.colorScheme.error
                                                    else MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Icon(
                                                imageVector = if (isPreviewOverBudget) Icons.Default.Warning else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = if (isPreviewOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Submit Button
                                Button(
                                    onClick = {
                                        val validAmount = quickAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                        if (quickTitle.isNotBlank() && validAmount > 0) {
                                            onAddExpense(quickTitle.trim(), quickCategory, validAmount, quickNotes.trim())
                                            quickTitle = ""
                                            quickAmountText = ""
                                            quickNotes = ""
                                        }
                                    },
                                    enabled = quickTitle.isNotBlank() && typedAmount > 0,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("submit_daily_expense_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${LanguageManager.getString("log_expense", language)} (${formatCurrency(typedAmount.coerceAtLeast(0.0), language)})",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CATEGORY DISTRIBUTION
            if (categoryTotals.isNotEmpty()) {
                item {
                    Text(
                        text = "Ausgaben nach Kategorie",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoryTotals.forEach { (cat, sum) ->
                            val pct = if (totalSpent > 0) ((sum / totalSpent) * 100).toInt() else 0
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$cat: ${formatCurrency(sum, language)} ($pct%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // TRANSACTIONS LIST HEADER & TIME FILTERS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageManager.getString("recent_expenses", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Pair("ALL", "Alle"),
                            Pair("TODAY", "Heute"),
                            Pair("WEEK", "Woche")
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = selectedTimeFilter == key,
                                onClick = { selectedTimeFilter = key },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // EXPENSES LIST
            if (filteredExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Noch keine Ausgaben in dieser Auswahl gebucht.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredExpenses, key = { it.id }) { exp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_item_${exp.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exp.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${exp.category} • ${exp.loggedByName} • ${dateFormat.format(Date(exp.date))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (exp.notes.isNotBlank()) {
                                    Text(
                                        text = exp.notes,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            Text(
                                text = formatCurrency(exp.amount, language),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { onDeleteExpense(exp) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Löschen",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // MODAL DIALOG TO ADJUST MONTHLY BUDGET CAP
    if (isCapDialogOpen) {
        var capInput by remember { mutableStateOf(monthlyCap.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { isCapDialogOpen = false },
            title = {
                Text(
                    text = LanguageManager.getString("budget_cap_title", language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Geben Sie das maximale monatliche Budget für den Haushalt ein. Der verbleibende Betrag wird sofort neu berechnet.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = capInput,
                        onValueChange = { capInput = it },
                        label = { Text("Monatslimit (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("budget_cap_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(800.0, 1200.0, 1500.0, 2000.0).forEach { presetCap ->
                            OutlinedButton(
                                onClick = { capInput = presetCap.toInt().toString() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${presetCap.toInt()}€", fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newCap = capInput.toDoubleOrNull()
                        if (newCap != null && newCap > 0) {
                            onUpdateBudgetCap(newCap)
                            isCapDialogOpen = false
                        }
                    },
                    modifier = Modifier.testTag("save_budget_cap_button")
                ) {
                    Text(LanguageManager.getString("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { isCapDialogOpen = false }) {
                    Text(LanguageManager.getString("cancel", language))
                }
            }
        )
    }
}

private fun formatCurrency(amount: Double, language: AppLanguage): String {
    val symbol = if (language == AppLanguage.EN) "$" else "€"
    val isNegative = amount < 0
    val absAmount = Math.abs(amount)
    val formatted = String.format(Locale.GERMANY, "%.2f", absAmount)
    return if (language == AppLanguage.EN) {
        if (isNegative) "-$$formatted" else "$$formatted"
    } else {
        if (isNegative) "- $formatted $symbol" else "$formatted $symbol"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("LEBENSMITTEL") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("LEBENSMITTEL", "HAUSHALT", "REPARATUR", "ENERGIE", "GESUNDHEIT", "SONSTIGES")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ausgabe erfassen", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel / Zweck *") },
                    placeholder = { Text("z.B. Supermarkt Einkauf") },
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Betrag (€) *") },
                    placeholder = { Text("z.B. 24.50") },
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_amount_input"),
                    singleLine = true
                )

                Text("Kategorie:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notiz / Geschäft") },
                    placeholder = { Text("Details...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(title, category, amount, notes)
                    }
                },
                modifier = Modifier.testTag("confirm_log_expense_button")
            ) {
                Text("Ausgabe buchen")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

