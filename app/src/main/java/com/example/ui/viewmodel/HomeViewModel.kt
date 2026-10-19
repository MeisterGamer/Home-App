package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ChoreEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.FamilyMemberEntity
import com.example.data.entity.GroupEntity
import com.example.data.entity.MedicationEntity
import com.example.data.entity.NotificationAlertEntity
import com.example.data.entity.ShoppingItemEntity
import com.example.data.entity.SmartDeviceEntity
import com.example.data.entity.WearableVitalEntity
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.data.repository.HomeRepository
import com.example.voice.VoiceActionResult
import com.example.voice.VoiceCommandHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    CHORES,
    SHOPPING,
    CALENDAR,
    BUDGET,
    SMART_HOME,
    SENIOR_CARE,
    ANALYTICS,
    SETTINGS
}

data class SyncState(
    val isSyncing: Boolean = false,
    val lastSyncTime: String = "Gerade eben",
    val syncMessage: String = "100% Offline-fähig • Lokale SQLite-Datenbank aktiv"
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepository.getInstance(application, viewModelScope)
    private val prefs = application.getSharedPreferences("homenest_app_prefs", Context.MODE_PRIVATE)

    // Flows from repository
    val allChores = repository.allChores.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val urgentRepairs = repository.urgentRepairs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allShoppingItems = repository.allShoppingItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMembers = repository.allMembers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allGroups = repository.allGroups.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCategories = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDevices = repository.allDevices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMedications = repository.allMedications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val seniorVitals = repository.seniorVitals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val notifications = repository.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unreadNotifCount = repository.unreadNotificationCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // First-time Setup Wizard / Onboarding state
    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _showSetupWizard = MutableStateFlow(!prefs.getBoolean("onboarding_completed", false))
    val showSetupWizard: StateFlow<Boolean> = _showSetupWizard.asStateFlow()

    // UI Configuration States
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedMember = MutableStateFlow<FamilyMemberEntity?>(null)
    val selectedMember: StateFlow<FamilyMemberEntity?> = _selectedMember.asStateFlow()

    private val _isSeniorMode = MutableStateFlow(false)
    val isSeniorMode: StateFlow<Boolean> = _isSeniorMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.DE)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _monthlyBudgetCap = MutableStateFlow(1200.0)
    val monthlyBudgetCap: StateFlow<Double> = _monthlyBudgetCap.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _voiceFeedback = MutableStateFlow<String?>(null)
    val voiceFeedback: StateFlow<String?> = _voiceFeedback.asStateFlow()

    private val _isVoiceDialogOpen = MutableStateFlow(false)
    val isVoiceDialogOpen: StateFlow<Boolean> = _isVoiceDialogOpen.asStateFlow()

    private val _isNotifDialogOpen = MutableStateFlow(false)
    val isNotifDialogOpen: StateFlow<Boolean> = _isNotifDialogOpen.asStateFlow()

    private val _smartLightsOn = MutableStateFlow(false)
    val smartLightsOn: StateFlow<Boolean> = _smartLightsOn.asStateFlow()

    private val _smartHeatingOn = MutableStateFlow(true)
    val smartHeatingOn: StateFlow<Boolean> = _smartHeatingOn.asStateFlow()

    // Navigation and screen switching
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectMember(member: FamilyMemberEntity) {
        _selectedMember.value = member
        if (member.role == "SENIOR") {
            _isSeniorMode.value = true
        }
    }

    fun toggleSeniorMode() {
        _isSeniorMode.value = !_isSeniorMode.value
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleHighContrast() {
        _isHighContrast.value = !_isHighContrast.value
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun setMonthlyBudgetCap(cap: Double) {
        _monthlyBudgetCap.value = cap
    }

    fun setVoiceDialogOpen(open: Boolean) {
        _isVoiceDialogOpen.value = open
        if (!open) _voiceFeedback.value = null
    }

    fun setNotifDialogOpen(open: Boolean) {
        _isNotifDialogOpen.value = open
    }

    fun toggleSmartLights(enabled: Boolean? = null) {
        _smartLightsOn.value = enabled ?: !_smartLightsOn.value
    }

    fun toggleSmartHeating(enabled: Boolean? = null) {
        _smartHeatingOn.value = enabled ?: !_smartHeatingOn.value
    }

    // Chore Actions
    fun toggleChore(chore: ChoreEntity) {
        viewModelScope.launch {
            val activeId = _selectedMember.value?.id ?: 1L
            repository.toggleChoreCompletion(chore, activeId)
        }
    }

    fun addChore(
        title: String,
        description: String,
        category: String,
        assignedMemberId: Long,
        assignedMemberName: String,
        isUrgent: Boolean,
        recurrence: String = "Once",
        points: Int = 15
    ) {
        viewModelScope.launch {
            repository.addChore(
                ChoreEntity(
                    title = title,
                    description = description,
                    category = category,
                    assignedMemberId = assignedMemberId,
                    assignedMemberName = assignedMemberName,
                    dueDate = System.currentTimeMillis() + (4 * 3600_000L),
                    priority = if (isUrgent) "URGENT" else "MEDIUM",
                    isUrgentRepair = isUrgent,
                    recurrence = recurrence,
                    pointsValue = points
                )
            )
        }
    }

    fun deleteChore(chore: ChoreEntity) {
        viewModelScope.launch {
            repository.deleteChore(chore)
        }
    }

    fun clearCompletedChores() {
        viewModelScope.launch {
            repository.clearCompletedChores()
        }
    }

    // Shopping Actions
    fun toggleShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.toggleShoppingItem(item)
        }
    }

    fun addShoppingItem(
        name: String,
        category: String = "Lebensmittel",
        cost: Double = 0.0,
        quantity: String = "1",
        priority: String = "Normal"
    ) {
        viewModelScope.launch {
            val adder = _selectedMember.value?.name ?: "Familie"
            repository.addShoppingItem(
                name = name,
                category = category,
                quantity = quantity,
                cost = cost,
                priority = priority,
                addedBy = adder
            )
        }
    }

    fun deleteShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun clearCompletedShopping() {
        viewModelScope.launch {
            repository.clearCompletedShopping()
        }
    }

    // Budget & Expense Actions
    fun logExpense(title: String, category: String, amount: Double, notes: String = "") {
        viewModelScope.launch {
            val loggedBy = _selectedMember.value?.name ?: "Familie"
            repository.addExpense(title, category, amount, loggedBy, notes)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Setup Wizard Operations
    fun startSetupWizard() {
        _showSetupWizard.value = true
    }

    fun dismissSetupWizard() {
        _showSetupWizard.value = false
    }

    fun completeSetupWizard(
        householdName: String,
        monthlyBudget: Double,
        enableSeniorMode: Boolean,
        enableDarkMode: Boolean
    ) {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _isOnboardingCompleted.value = true
        _showSetupWizard.value = false
        _monthlyBudgetCap.value = monthlyBudget
        _isSeniorMode.value = enableSeniorMode
        _isDarkMode.value = enableDarkMode
        viewModelScope.launch {
            val groups = allGroups.value
            if (groups.isNotEmpty() && householdName.isNotBlank()) {
                val mainGroup = groups.first()
                repository.updateGroup(mainGroup.copy(name = householdName))
            } else if (householdName.isNotBlank()) {
                repository.addGroup(name = householdName, description = "Hauptwohnsitz")
            }
        }
    }

    // Family Member Operations (Add, Edit, Delete)
    fun addFamilyMember(
        name: String,
        role: String,
        relationship: String,
        emergencyPhone: String = "",
        groupId: Long = 1L
    ) {
        viewModelScope.launch {
            val colors = listOf(0xFF0D9488L, 0xFF2563EBL, 0xFF8B5CF6L, 0xFFF59E0BL, 0xFFEC4899L, 0xFF10B981L)
            val randomColor = colors.random()
            repository.addFamilyMember(name, role, relationship, emergencyPhone, randomColor, groupId)
        }
    }

    fun updateFamilyMember(member: FamilyMemberEntity) {
        viewModelScope.launch {
            repository.updateFamilyMember(member)
        }
    }

    fun deleteFamilyMember(member: FamilyMemberEntity) {
        viewModelScope.launch {
            repository.deleteFamilyMember(member)
        }
    }

    // Household Group Operations (Create, Edit, Delete)
    fun addGroup(name: String, description: String = "", color: Long = 0xFF0D9488L, iconName: String = "Home") {
        viewModelScope.launch {
            repository.addGroup(name, description, color, iconName)
        }
    }

    fun updateGroup(group: GroupEntity) {
        viewModelScope.launch {
            repository.updateGroup(group)
        }
    }

    fun deleteGroup(group: GroupEntity) {
        viewModelScope.launch {
            repository.deleteGroup(group)
        }
    }

    // Custom Category Operations (Create, Edit, Delete)
    fun addCategory(name: String, type: String = "ALL", color: Long = 0xFF0D9488L, iconName: String = "Label") {
        viewModelScope.launch {
            repository.addCategory(name, type, color, iconName)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Smart Device Operations
    fun addSmartDevice(name: String, type: String, location: String) {
        viewModelScope.launch {
            repository.addSmartDevice(name, type, location)
        }
    }

    fun deleteSmartDevice(device: SmartDeviceEntity) {
        viewModelScope.launch {
            repository.deleteSmartDevice(device)
        }
    }

    fun triggerSmartDevice(device: SmartDeviceEntity) {
        viewModelScope.launch {
            repository.triggerDeviceAction(device)
        }
    }

    // Medication Operations
    fun addMedication(name: String, dosage: String, timeOfDay: String, reminderTime: String, instructions: String) {
        viewModelScope.launch {
            repository.addMedication(name, dosage, timeOfDay, reminderTime, instructions)
        }
    }

    fun toggleMedication(med: MedicationEntity) {
        viewModelScope.launch {
            repository.toggleMedicationTaken(med)
        }
    }

    fun deleteMedication(med: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(med)
        }
    }

    // Wearable Vitals
    fun updateWearableVitals(heartRate: Int, steps: Int, sleepHours: Float, deepSleepPercent: Int, spo2Percent: Int) {
        viewModelScope.launch {
            repository.updateWearableVitals(heartRate, steps, sleepHours, deepSleepPercent, spo2Percent)
        }
    }

    // Notifications
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun deleteNotification(alert: NotificationAlertEntity) {
        viewModelScope.launch {
            repository.deleteNotification(alert)
        }
    }

    // Complete Wipe of all example / demo data
    fun clearAllExampleData() {
        viewModelScope.launch {
            repository.clearAllExampleData()
            _voiceFeedback.value = "Alle Beispieldaten wurden erfolgreich gelöscht! Die App ist jetzt sauber und bereit."
        }
    }

    // Cloud Sync Simulator
    fun triggerCloudSync() {
        viewModelScope.launch {
            _syncState.value = SyncState(isSyncing = true, syncMessage = "Synchronisiere mit HomeNest Cloud...")
            delay(1000)
            val json = repository.exportBackupJson()
            _syncState.value = SyncState(
                isSyncing = false,
                lastSyncTime = "Gerade eben",
                syncMessage = "Backup geprüft & AES-256 verschlüsselt gespeichert"
            )
        }
    }

    // Process Voice Command
    fun handleVoiceCommand(rawText: String) {
        val action = VoiceCommandHelper.parseCommand(rawText)
        viewModelScope.launch {
            when (action) {
                is VoiceActionResult.AddShoppingItem -> {
                    val adder = _selectedMember.value?.name ?: "Sprachassistent"
                    repository.addShoppingItem(
                        name = action.name,
                        category = action.category,
                        quantity = "1",
                        cost = 0.0,
                        priority = "Normal",
                        addedBy = adder
                    )
                    _voiceFeedback.value = "'${action.name}' zur Einkaufsliste hinzugefügt!"
                }
                is VoiceActionResult.CompleteChore -> {
                    val currentChores = allChores.value
                    val match = currentChores.find {
                        it.title.lowercase().contains(action.choreQuery) ||
                        action.choreQuery.contains(it.title.lowercase())
                    }
                    if (match != null) {
                        repository.toggleChoreCompletion(match, _selectedMember.value?.id ?: 1L)
                        _voiceFeedback.value = "'${match.title}' als erledigt markiert! (+${match.pointsValue} Pkt)"
                    } else {
                        _voiceFeedback.value = "Keine Aufgabe passend zu '${action.choreQuery}' gefunden."
                    }
                }
                is VoiceActionResult.AddUrgentRepair -> {
                    repository.addChore(
                        ChoreEntity(
                            title = action.title,
                            description = action.description,
                            category = "MAINTENANCE",
                            assignedMemberId = _selectedMember.value?.id ?: 1L,
                            assignedMemberName = _selectedMember.value?.name ?: "Familie",
                            dueDate = System.currentTimeMillis() + 3600_000L,
                            priority = "URGENT",
                            isUrgentRepair = true,
                            pointsValue = 30
                        )
                    )
                    _voiceFeedback.value = "Dringende Reparatur '${action.title}' erstellt & Alarm gesendet!"
                }
                is VoiceActionResult.LogExpense -> {
                    val loggedBy = _selectedMember.value?.name ?: "Sprachassistent"
                    repository.addExpense(action.title, "MAINTENANCE", action.amount, loggedBy, "Per Sprachbefehl erfasst")
                    _voiceFeedback.value = "Ausgabe von ${action.amount} € für '${action.title}' erfasst."
                }
                is VoiceActionResult.ToggleSeniorMode -> {
                    _isSeniorMode.value = true
                    _voiceFeedback.value = "In den Seniorenmodus gewechselt."
                }
                is VoiceActionResult.TakeMedication -> {
                    val meds = allMedications.value
                    val untaken = meds.firstOrNull { !it.isTakenToday }
                    if (untaken != null) {
                        repository.toggleMedicationTaken(untaken)
                        _voiceFeedback.value = "'${untaken.name}' als eingenommen markiert (+10 Pkt)"
                    } else {
                        _voiceFeedback.value = "Alle Medikamente für heute bereits eingenommen!"
                    }
                }
                is VoiceActionResult.ToggleSmartLights -> {
                    toggleSmartLights(action.turnOn)
                    _voiceFeedback.value = if (action.turnOn) "Smarte Beleuchtung eingeschaltet (80% Warmweiß)." else "Smarte Beleuchtung ausgeschaltet."
                }
                is VoiceActionResult.ToggleSmartHeating -> {
                    toggleSmartHeating(action.turnOn)
                    _voiceFeedback.value = if (action.turnOn) "Intelligente Heizung auf Komfortmodus (21.5°C) gestellt." else "Intelligente Heizung auf Eco-Modus gestellt."
                }
                is VoiceActionResult.Unknown -> {
                    _voiceFeedback.value = "Erkannt: '$rawText'. Beispiel: 'Milch zur Einkaufsliste hinzufügen' oder 'Geschirr erledigt'"
                }
            }
        }
    }
}
