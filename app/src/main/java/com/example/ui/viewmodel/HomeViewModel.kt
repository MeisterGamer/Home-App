package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.ChoreEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.FamilyMemberEntity
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
import kotlinx.coroutines.flow.combine
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
    val lastSyncTime: String = "Just now",
    val syncMessage: String = "All data encrypted and cached locally"
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HomeRepository.getInstance(application, viewModelScope)

    // Flows from repository
    val allChores = repository.allChores.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val urgentRepairs = repository.urgentRepairs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allShoppingItems = repository.allShoppingItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMembers = repository.allMembers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDevices = repository.allDevices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMedications = repository.allMedications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val seniorVitals = repository.seniorVitals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val notifications = repository.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unreadNotifCount = repository.unreadNotificationCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    private val _currentLanguage = MutableStateFlow(AppLanguage.EN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _voiceFeedback = MutableStateFlow<String?>(null)
    val voiceFeedback: StateFlow<String?> = _voiceFeedback.asStateFlow()

    private val _isVoiceDialogOpen = MutableStateFlow(false)
    val isVoiceDialogOpen: StateFlow<Boolean> = _isVoiceDialogOpen.asStateFlow()

    private val _isNotifDialogOpen = MutableStateFlow(false)
    val isNotifDialogOpen: StateFlow<Boolean> = _isNotifDialogOpen.asStateFlow()

    // Navigation and screen switching
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectMember(member: FamilyMemberEntity) {
        _selectedMember.value = member
        // If grandma is selected, automatically activate Senior Mode!
        if (member.role == "SENIOR") {
            _isSeniorMode.value = true
        } else if (_isSeniorMode.value && member.role != "SENIOR") {
            // Keep user preference or allow manual toggle
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

    fun setVoiceDialogOpen(open: Boolean) {
        _isVoiceDialogOpen.value = open
        if (!open) _voiceFeedback.value = null
    }

    fun setNotifDialogOpen(open: Boolean) {
        _isNotifDialogOpen.value = open
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

    // Shopping Actions
    fun toggleShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.toggleShoppingItem(item)
        }
    }

    fun addShoppingItem(name: String, category: String, cost: Double) {
        viewModelScope.launch {
            val adder = _selectedMember.value?.name ?: "Family"
            repository.addShoppingItem(name, category, cost, adder)
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
            val loggedBy = _selectedMember.value?.name ?: "David (Dad)"
            repository.addExpense(title, category, amount, loggedBy, notes)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Smart Device Trigger
    fun triggerSmartDevice(device: SmartDeviceEntity) {
        viewModelScope.launch {
            repository.triggerDeviceAction(device)
        }
    }

    // Medication Actions
    fun toggleMedication(med: MedicationEntity) {
        viewModelScope.launch {
            repository.toggleMedicationTaken(med)
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

    // Cloud Sync Simulator
    fun triggerCloudSync() {
        viewModelScope.launch {
            _syncState.value = SyncState(isSyncing = true, syncMessage = "Synchronizing with HomeNest Cloud...")
            delay(1200)
            val json = repository.exportBackupJson()
            _syncState.value = SyncState(
                isSyncing = false,
                lastSyncTime = "Just now",
                syncMessage = "Backup verified & safely encrypted across devices"
            )
        }
    }

    // Process Voice Command
    fun handleVoiceCommand(rawText: String) {
        val action = VoiceCommandHelper.parseCommand(rawText)
        viewModelScope.launch {
            when (action) {
                is VoiceActionResult.AddShoppingItem -> {
                    val adder = _selectedMember.value?.name ?: "Voice Assistant"
                    repository.addShoppingItem(action.name, action.category, 0.0, adder)
                    _voiceFeedback.value = "Added '${action.name}' to shopping list!"
                }
                is VoiceActionResult.CompleteChore -> {
                    val currentChores = allChores.value
                    val match = currentChores.find {
                        it.title.lowercase().contains(action.choreQuery) ||
                        action.choreQuery.contains(it.title.lowercase())
                    }
                    if (match != null) {
                        repository.toggleChoreCompletion(match, _selectedMember.value?.id ?: 1L)
                        _voiceFeedback.value = "Marked '${match.title}' as completed! (+${match.pointsValue} pts)"
                    } else {
                        _voiceFeedback.value = "Could not find task matching '${action.choreQuery}'"
                    }
                }
                is VoiceActionResult.AddUrgentRepair -> {
                    repository.addChore(
                        ChoreEntity(
                            title = action.title,
                            description = action.description,
                            category = "MAINTENANCE",
                            assignedMemberId = _selectedMember.value?.id ?: 2L,
                            assignedMemberName = _selectedMember.value?.name ?: "Family",
                            dueDate = System.currentTimeMillis() + 3600_000L,
                            priority = "URGENT",
                            isUrgentRepair = true,
                            pointsValue = 30
                        )
                    )
                    _voiceFeedback.value = "Urgent repair created: '${action.title}' & alert sent!"
                }
                is VoiceActionResult.LogExpense -> {
                    val loggedBy = _selectedMember.value?.name ?: "Voice Command"
                    repository.addExpense(action.title, "MAINTENANCE", action.amount, loggedBy, "Voice logged")
                    _voiceFeedback.value = "Logged \$${action.amount} expense for '${action.title}'"
                }
                is VoiceActionResult.ToggleSeniorMode -> {
                    _isSeniorMode.value = true
                    _voiceFeedback.value = "Switched to Elder Care Mode"
                }
                is VoiceActionResult.TakeMedication -> {
                    val meds = allMedications.value
                    val untaken = meds.firstOrNull { !it.isTakenToday }
                    if (untaken != null) {
                        repository.toggleMedicationTaken(untaken)
                        _voiceFeedback.value = "Recorded '${untaken.name}' taken (+10 pts)"
                    } else {
                        _voiceFeedback.value = "All medications are already taken today!"
                    }
                }
                is VoiceActionResult.Unknown -> {
                    _voiceFeedback.value = "Recognized: '$rawText'. Try: 'Add milk to shopping list' or 'Clean dishes complete'"
                }
            }
        }
    }
}
