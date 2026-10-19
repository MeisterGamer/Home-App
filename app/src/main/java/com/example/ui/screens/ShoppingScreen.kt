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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ShoppingItemEntity
import com.example.data.model.AppLanguage
import com.example.localization.LanguageManager
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShoppingScreen(
    items: List<ShoppingItemEntity>,
    language: AppLanguage,
    onToggleItem: (ShoppingItemEntity) -> Unit,
    onDeleteItem: (ShoppingItemEntity) -> Unit,
    onAddItem: (String, String, Double) -> Unit,
    onAddItemDetailed: (String, String, String, Double, String) -> Unit = { name, cat, qty, cost, prio ->
        onAddItem(name, cat, cost)
    },
    onClearCompleted: () -> Unit
) {
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, OPEN, COMPLETED
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    // Quick add inputs on screen
    var quickName by remember { mutableStateOf("") }
    var quickQuantity by remember { mutableStateOf("1x") }
    var quickCategory by remember { mutableStateOf("Lebensmittel") }
    var quickCostText by remember { mutableStateOf("") }
    var isQuickUrgent by remember { mutableStateOf(false) }
    var isQuickAddExpanded by remember { mutableStateOf(true) }

    // Stats
    val totalItems = items.size
    val purchasedItems = items.count { it.isPurchased }
    val openItems = totalItems - purchasedItems
    val progressRatio = if (totalItems > 0) (purchasedItems.toFloat() / totalItems.toFloat()) else 0f
    val totalOpenCost = items.filter { !it.isPurchased }.sumOf { it.estimatedCost }
    val totalCartCost = items.sumOf { it.estimatedCost }

    val categories = listOf("ALL", "Lebensmittel", "Haushalt", "Baumarkt", "Drogerie", "Haustiere")

    // Quick Staples
    val quickStaples = listOf(
        Triple("Frische Vollmilch", "Lebensmittel", 1.89),
        Triple("Bauernbrot", "Lebensmittel", 2.49),
        Triple("Freilandeier (10er)", "Lebensmittel", 3.29),
        Triple("Kaffee Bohnen", "Lebensmittel", 6.99),
        Triple("Butter", "Lebensmittel", 2.29),
        Triple("Spülmaschinentabs", "Haushalt", 4.99),
        Triple("Toilettenpapier", "Haushalt", 3.79),
        Triple("Müllbeutel 35L", "Haushalt", 2.19),
        Triple("AA Batterien", "Baumarkt", 4.49),
        Triple("Hundefutter / Leckerlis", "Haustiere", 5.90)
    )

    // Filtered items
    val filteredItems = items.filter { item ->
        val matchesStatus = when (selectedStatusFilter) {
            "OPEN" -> !item.isPurchased
            "COMPLETED" -> item.isPurchased
            else -> true
        }
        val matchesCategory = if (selectedCategoryFilter == "ALL") true
        else item.category.equals(selectedCategoryFilter, ignoreCase = true)

        val matchesSearch = if (searchQuery.isBlank()) true
        else item.name.contains(searchQuery, ignoreCase = true) || item.category.contains(searchQuery, ignoreCase = true)

        matchesStatus && matchesCategory && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Header & Clear Completed Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = LanguageManager.getString("shopping_title", language),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$openItems zu kaufen • $purchasedItems im Korb",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (purchasedItems > 0) {
                        OutlinedButton(
                            onClick = onClearCompleted,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("clear_completed_shopping")
                        ) {
                            Icon(imageVector = Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(LanguageManager.getString("clear_purchased", language), fontSize = 11.sp)
                        }
                    }
                }
            }

            // SUMMARY PROGRESS CARD
            item {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_summary_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = LanguageManager.getString("cart_total", language),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = formatCurrency(totalOpenCost, language),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (totalCartCost > totalOpenCost) {
                                    Text(
                                        text = "Gesamt (inkl. gekauft): ${formatCurrency(totalCartCost, language)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$purchasedItems / $totalItems ${LanguageManager.getString("in_cart_badge", language)}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // FUNCTIONAL DIRECT INPUT AREA (ÄHNLICH WIE AUFGABEN-VERWALTUNG)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_direct_input_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isQuickAddExpanded = !isQuickAddExpanded },
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
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = LanguageManager.getString("add_item", language),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Produkt schnell zur Liste hinzufügen",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isQuickAddExpanded = !isQuickAddExpanded },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isQuickAddExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                        }

                        AnimatedVisibility(visible = isQuickAddExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                // Product name text field
                                OutlinedTextField(
                                    value = quickName,
                                    onValueChange = { quickName = it },
                                    label = { Text("Produktname *") },
                                    placeholder = { Text(LanguageManager.getString("item_name_placeholder", language)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("direct_shopping_name_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quantity & Estimated Cost Row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = quickQuantity,
                                        onValueChange = { quickQuantity = it },
                                        label = { Text(LanguageManager.getString("quantity_label", language)) },
                                        placeholder = { Text("z.B. 1x, 500g") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("direct_shopping_qty_input")
                                    )

                                    OutlinedTextField(
                                        value = quickCostText,
                                        onValueChange = { quickCostText = it },
                                        label = { Text("Preis (€)") },
                                        placeholder = { Text("z.B. 2.49") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("direct_shopping_cost_input")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Quantity Pills
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf("1x", "2x", "3x", "500g", "1 kg", "1 Pck").forEach { qtyPill ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (quickQuantity == qtyPill) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { quickQuantity = qtyPill }
                                        ) {
                                            Text(
                                                text = qtyPill,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Category & Urgent Priority Chips
                                Text("Kategorie:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Lebensmittel", "Haushalt", "Baumarkt", "Drogerie", "Haustiere").forEach { cat ->
                                        FilterChip(
                                            selected = quickCategory == cat,
                                            onClick = { quickCategory = cat },
                                            label = { Text(cat, fontSize = 11.sp) }
                                        )
                                    }

                                    // Urgent Priority Toggle
                                    FilterChip(
                                        selected = isQuickUrgent,
                                        onClick = { isQuickUrgent = !isQuickUrgent },
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isQuickUrgent) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    LanguageManager.getString("priority_urgent_shopping", language),
                                                    fontSize = 11.sp,
                                                    color = if (isQuickUrgent) MaterialTheme.colorScheme.error else Color.Unspecified
                                                )
                                            }
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Add Button
                                Button(
                                    onClick = {
                                        if (quickName.isNotBlank()) {
                                            val cost = quickCostText.replace(",", ".").toDoubleOrNull() ?: 0.0
                                            val prio = if (isQuickUrgent) "Urgent" else "Normal"
                                            val nameWithQty = if (quickQuantity.isNotBlank() && !quickName.contains(quickQuantity)) {
                                                "${quickName.trim()} (${quickQuantity.trim()})"
                                            } else {
                                                quickName.trim()
                                            }

                                            onAddItemDetailed(nameWithQty, quickCategory, quickQuantity, cost, prio)
                                            quickName = ""
                                            quickCostText = ""
                                            isQuickUrgent = false
                                        }
                                    },
                                    enabled = quickName.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("submit_direct_shopping_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(LanguageManager.getString("add_item", language), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // QUICK ADD STAPLES
            item {
                Text(
                    text = LanguageManager.getString("quick_add", language),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickStaples) { (name, cat, cost) ->
                        val alreadyAdded = items.any { it.name.contains(name, ignoreCase = true) && !it.isPurchased }
                        FilterChip(
                            selected = alreadyAdded,
                            onClick = {
                                if (!alreadyAdded) {
                                    onAddItemDetailed(name, cat, "1x", cost, "Normal")
                                }
                            },
                            label = { Text("+ $name (${formatCurrency(cost, language)})", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // STATUS & CATEGORY FILTERS (ÄHNLICH WIE CHORESSCREEN)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status row (Alle, Offen, Erledigt)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Triple("ALL", LanguageManager.getString("filter_all", language), totalItems),
                            Triple("OPEN", LanguageManager.getString("filter_open", language), openItems),
                            Triple("COMPLETED", LanguageManager.getString("filter_completed", language), purchasedItems)
                        ).forEach { (statusKey, label, count) ->
                            FilterChip(
                                selected = selectedStatusFilter == statusKey,
                                onClick = { selectedStatusFilter = statusKey },
                                label = { Text("$label ($count)", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                modifier = Modifier.testTag("filter_status_$statusKey")
                            )
                        }
                    }

                    // Category row
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(if (cat == "ALL") "Alle Kategorien" else cat, fontSize = 11.sp) },
                                modifier = Modifier.testTag("filter_cat_$cat")
                            )
                        }
                    }

                    // Live Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(LanguageManager.getString("search_shopping", language)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("shopping_search_input")
                    )
                }
            }

            // SHOPPING LIST ITEMS (ÄHNLICH WIE BEI DER AUFGABEN-VERWALTUNG)
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = LanguageManager.getString("no_items_found", language),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shopping_item_${item.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isPurchased) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox to check off (abhaken) with clear state
                            Icon(
                                imageVector = if (item.isPurchased) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (item.isPurchased) "Als offen markieren" else "Abhaken",
                                tint = if (item.isPurchased) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { onToggleItem(item) }
                                    .testTag("toggle_item_${item.id}")
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Item details (Title, quantity, category, added by)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (item.isPurchased) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                        else MaterialTheme.colorScheme.onSurface
                                    )

                                    if (item.priority.equals("Urgent", ignoreCase = true) || item.priority.equals("Dringend", ignoreCase = true)) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Dringend",
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = item.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Von ${item.addedByName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Estimated Price
                            if (item.estimatedCost > 0) {
                                Text(
                                    text = formatCurrency(item.estimatedCost, language),
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.isPurchased) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }

                            // Delete button (Löschen)
                            IconButton(
                                onClick = { onDeleteItem(item) },
                                modifier = Modifier.testTag("delete_item_${item.id}")
                            ) {
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

        // Floating Action Button for detailed modal adding
        FloatingActionButton(
            onClick = { isAddDialogOpen = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("shopping_add_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Artikel hinzufügen")
        }
    }

    if (isAddDialogOpen) {
        AddShoppingDialog(
            language = language,
            onDismiss = { isAddDialogOpen = false },
            onConfirm = { name, cat, qty, cost, prio ->
                val fullName = if (qty.isNotBlank() && !name.contains(qty)) "$name ($qty)" else name
                onAddItemDetailed(fullName, cat, qty, cost, prio)
                isAddDialogOpen = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddShoppingDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Lebensmittel") }
    var quantity by remember { mutableStateOf("1x") }
    var costText by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    val categories = listOf("Lebensmittel", "Haushalt", "Baumarkt", "Drogerie", "Haustiere", "Sonstiges")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(LanguageManager.getString("add_custom_item", language), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Produktname *") },
                    placeholder = { Text("z.B. Bio-Milch, Schrauben...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_shopping_name_input"),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Menge") },
                        placeholder = { Text("z.B. 2x, 500g") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Preis (€)") },
                        placeholder = { Text("z.B. 3.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Kategorie:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }

                    FilterChip(
                        selected = isUrgent,
                        onClick = { isUrgent = !isUrgent },
                        label = { Text("Dringend", fontSize = 11.sp, color = if (isUrgent) MaterialTheme.colorScheme.error else Color.Unspecified) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val cost = costText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val prio = if (isUrgent) "Urgent" else "Normal"
                        onConfirm(name.trim(), category, quantity.trim(), cost, prio)
                    }
                },
                modifier = Modifier.testTag("confirm_add_shopping_button")
            ) {
                Text(LanguageManager.getString("add_item", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageManager.getString("cancel", language))
            }
        }
    )
}

@Composable
fun AddShoppingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double) -> Unit
) {
    AddShoppingDialog(
        language = AppLanguage.DE,
        onDismiss = onDismiss,
        onConfirm = { name, cat, _, cost, _ -> onConfirm(name, cat, cost) }
    )
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
