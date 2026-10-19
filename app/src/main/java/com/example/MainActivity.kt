package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HomeBottomNavBar
import com.example.ui.components.HomeTopBar
import com.example.ui.dialogs.FamilyMemberSelectDialog
import com.example.ui.dialogs.NotificationCenterDialog
import com.example.ui.dialogs.VoiceAssistantDialog
import com.example.ui.screens.AddChoreDialog
import com.example.ui.screens.AddExpenseDialog
import com.example.ui.screens.AddShoppingDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ChoresScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SeniorElderScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupWizardDialog
import com.example.ui.screens.ShoppingScreen
import com.example.ui.screens.SmartHomeHubScreen
import com.example.ui.theme.HomeNestTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeApp()
        }
    }
}

@Composable
fun HomeApp(viewModel: HomeViewModel = viewModel()) {
    // Collect StateFlows
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allChores by viewModel.allChores.collectAsStateWithLifecycle()
    val urgentRepairs by viewModel.urgentRepairs.collectAsStateWithLifecycle()
    val allShoppingItems by viewModel.allShoppingItems.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val allMembers by viewModel.allMembers.collectAsStateWithLifecycle()
    val allGroups by viewModel.allGroups.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allDevices by viewModel.allDevices.collectAsStateWithLifecycle()
    val allMedications by viewModel.allMedications.collectAsStateWithLifecycle()
    val seniorVitals by viewModel.seniorVitals.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifs by viewModel.unreadNotifCount.collectAsStateWithLifecycle()

    val selectedMember by viewModel.selectedMember.collectAsStateWithLifecycle()
    val isSeniorMode by viewModel.isSeniorMode.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isHighContrast by viewModel.isHighContrast.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val monthlyBudgetCap by viewModel.monthlyBudgetCap.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    val voiceFeedback by viewModel.voiceFeedback.collectAsStateWithLifecycle()
    val isVoiceDialogOpen by viewModel.isVoiceDialogOpen.collectAsStateWithLifecycle()
    val isNotifDialogOpen by viewModel.isNotifDialogOpen.collectAsStateWithLifecycle()
    val smartLightsOn by viewModel.smartLightsOn.collectAsStateWithLifecycle()
    val smartHeatingOn by viewModel.smartHeatingOn.collectAsStateWithLifecycle()
    val showSetupWizard by viewModel.showSetupWizard.collectAsStateWithLifecycle()

    var isMemberSelectOpen by remember { mutableStateOf(false) }
    var isAddChoreDialogOpen by remember { mutableStateOf(false) }

    // Set default active member on first load if null
    LaunchedEffect(allMembers) {
        if (selectedMember == null && allMembers.isNotEmpty()) {
            viewModel.selectMember(allMembers.first())
        }
    }

    // Permission launcher for Notifications on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Back handling for sub-screens & elder mode
    BackHandler(enabled = currentScreen != AppScreen.HOME || isSeniorMode) {
        if (isSeniorMode && selectedMember?.role != "SENIOR") {
            viewModel.toggleSeniorMode()
        } else {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    HomeNestTheme(
        darkTheme = isDarkMode,
        highContrast = isHighContrast
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    currentScreen = currentScreen,
                    activeMember = selectedMember ?: allMembers.firstOrNull(),
                    unreadNotifs = unreadNotifs,
                    isSeniorMode = isSeniorMode,
                    language = currentLanguage,
                    onNotifClick = { viewModel.setNotifDialogOpen(true) },
                    onSeniorToggle = { viewModel.toggleSeniorMode() },
                    onMemberClick = { isMemberSelectOpen = true }
                )
            },
            bottomBar = {
                Box(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                    HomeBottomNavBar(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) },
                        language = currentLanguage
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isSeniorMode) {
                    SeniorElderScreen(
                        medications = allMedications,
                        vitals = seniorVitals,
                        isDarkMode = isDarkMode,
                        isHighContrast = isHighContrast,
                        language = currentLanguage,
                        onToggleMedication = { viewModel.toggleMedication(it) },
                        onAddMedication = { name, dosage, time, rem, inst ->
                            viewModel.addMedication(name, dosage, time, rem, inst)
                        },
                        onDeleteMedication = { viewModel.deleteMedication(it) },
                        onUpdateVitals = { hr, steps, sleep, deep, spo2 ->
                            viewModel.updateWearableVitals(hr, steps, sleep, deep, spo2)
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onToggleHighContrast = { viewModel.toggleHighContrast() },
                        onOpenVoice = { viewModel.setVoiceDialogOpen(true) }
                    )
                } else {
                    when (currentScreen) {
                        AppScreen.HOME -> HomeScreen(
                            chores = allChores,
                            urgentRepairs = urgentRepairs,
                            devices = allDevices,
                            activeMember = selectedMember,
                            language = currentLanguage,
                            onToggleChore = { viewModel.toggleChore(it) },
                            onNavigate = { viewModel.navigateTo(it) },
                            onOpenVoice = { viewModel.setVoiceDialogOpen(true) },
                            onAddChoreClick = { isAddChoreDialogOpen = true },
                            smartLightsOn = smartLightsOn,
                            smartHeatingOn = smartHeatingOn,
                            onToggleSmartLights = { viewModel.toggleSmartLights(it) },
                            onToggleSmartHeating = { viewModel.toggleSmartHeating(it) }
                        )
                        AppScreen.CHORES -> ChoresScreen(
                            chores = allChores,
                            members = allMembers,
                            language = currentLanguage,
                            onToggleChore = { viewModel.toggleChore(it) },
                            onDeleteChore = { viewModel.deleteChore(it) },
                            onAddChore = { title, desc, cat, memberId, memberName, isUrgent, recurrence, pts ->
                                viewModel.addChore(title, desc, cat, memberId, memberName, isUrgent, recurrence, pts)
                            },
                            onClearCompleted = { viewModel.clearCompletedChores() }
                        )
                        AppScreen.SHOPPING -> ShoppingScreen(
                            items = allShoppingItems,
                            language = currentLanguage,
                            onToggleItem = { viewModel.toggleShoppingItem(it) },
                            onDeleteItem = { viewModel.deleteShoppingItem(it) },
                            onAddItem = { name, cat, cost -> viewModel.addShoppingItem(name, cat, cost) },
                            onAddItemDetailed = { name, cat, qty, cost, prio ->
                                viewModel.addShoppingItem(name, cat, cost, qty, prio)
                            },
                            onClearCompleted = { viewModel.clearCompletedShopping() }
                        )
                        AppScreen.CALENDAR -> CalendarScreen(
                            chores = allChores,
                            members = allMembers,
                            medications = allMedications,
                            language = currentLanguage,
                            onToggleChore = { viewModel.toggleChore(it) },
                            onAddChore = { title, desc, cat, memberId, memberName, isUrgent, recurrence, pts ->
                                viewModel.addChore(title, desc, cat, memberId, memberName, isUrgent, recurrence, pts)
                            }
                        )
                        AppScreen.BUDGET -> BudgetScreen(
                            expenses = allExpenses,
                            language = currentLanguage,
                            monthlyCap = monthlyBudgetCap,
                            onDeleteExpense = { viewModel.deleteExpense(it) },
                            onAddExpense = { title, cat, amount, notes ->
                                viewModel.logExpense(title, cat, amount, notes)
                            },
                            onUpdateBudgetCap = { viewModel.setMonthlyBudgetCap(it) }
                        )
                        AppScreen.SMART_HOME -> SmartHomeHubScreen(
                            devices = allDevices,
                            language = currentLanguage,
                            onTriggerDevice = { viewModel.triggerSmartDevice(it) },
                            onAddDevice = { name, type, loc -> viewModel.addSmartDevice(name, type, loc) },
                            onDeleteDevice = { viewModel.deleteSmartDevice(it) }
                        )
                        AppScreen.SENIOR_CARE -> SeniorElderScreen(
                            medications = allMedications,
                            vitals = seniorVitals,
                            isDarkMode = isDarkMode,
                            isHighContrast = isHighContrast,
                            language = currentLanguage,
                            onToggleMedication = { viewModel.toggleMedication(it) },
                            onAddMedication = { name, dosage, time, rem, inst ->
                                viewModel.addMedication(name, dosage, time, rem, inst)
                            },
                            onDeleteMedication = { viewModel.deleteMedication(it) },
                            onUpdateVitals = { hr, steps, sleep, deep, spo2 ->
                                viewModel.updateWearableVitals(hr, steps, sleep, deep, spo2)
                            },
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onToggleHighContrast = { viewModel.toggleHighContrast() },
                            onOpenVoice = { viewModel.setVoiceDialogOpen(true) }
                        )
                        AppScreen.ANALYTICS -> AnalyticsScreen(
                            chores = allChores,
                            members = allMembers,
                            language = currentLanguage,
                            expenses = allExpenses,
                            monthlyBudgetCap = monthlyBudgetCap
                        )
                        AppScreen.SETTINGS -> SettingsScreen(
                            members = allMembers,
                            groups = allGroups,
                            categories = allCategories,
                            activeMember = selectedMember,
                            isDarkMode = isDarkMode,
                            isHighContrast = isHighContrast,
                            currentLanguage = currentLanguage,
                            syncState = syncState,
                            monthlyBudgetCap = monthlyBudgetCap,
                            onSelectMember = { viewModel.selectMember(it) },
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onToggleHighContrast = { viewModel.toggleHighContrast() },
                            onSetLanguage = { viewModel.setLanguage(it) },
                            onTriggerSync = { viewModel.triggerCloudSync() },
                            onAddMember = { name, role, rel, phone ->
                                viewModel.addFamilyMember(name, role, rel, phone)
                            },
                            onUpdateMember = { viewModel.updateFamilyMember(it) },
                            onDeleteMember = { viewModel.deleteFamilyMember(it) },
                            onAddGroup = { name, desc, color ->
                                viewModel.addGroup(name, desc, color)
                            },
                            onUpdateGroup = { viewModel.updateGroup(it) },
                            onDeleteGroup = { viewModel.deleteGroup(it) },
                            onAddCategory = { name, type, color ->
                                viewModel.addCategory(name, type, color)
                            },
                            onUpdateCategory = { viewModel.updateCategory(it) },
                            onDeleteCategory = { viewModel.deleteCategory(it) },
                            onUpdateBudgetCap = { viewModel.setMonthlyBudgetCap(it) },
                            onStartSetupWizard = { viewModel.startSetupWizard() }
                        )
                    }
                }
            }
        }

        // Voice Assistant Dialog
        VoiceAssistantDialog(
            isOpen = isVoiceDialogOpen,
            feedback = voiceFeedback,
            language = currentLanguage,
            onDismiss = { viewModel.setVoiceDialogOpen(false) },
            onSubmitCommand = { viewModel.handleVoiceCommand(it) }
        )

        // Notification Center Dialog
        NotificationCenterDialog(
            isOpen = isNotifDialogOpen,
            notifications = notifications,
            language = currentLanguage,
            onDismiss = { viewModel.setNotifDialogOpen(false) },
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onDelete = { viewModel.deleteNotification(it) }
        )

        // Family Member Switcher Dialog
        FamilyMemberSelectDialog(
            isOpen = isMemberSelectOpen,
            members = allMembers,
            selectedId = selectedMember?.id ?: 1L,
            language = currentLanguage,
            onDismiss = { isMemberSelectOpen = false },
            onSelect = { viewModel.selectMember(it) }
        )

        // Add Chore / Urgent Repair Dialog (when triggered from Home FAB)
        if (isAddChoreDialogOpen) {
            AddChoreDialog(
                members = allMembers,
                onDismiss = { isAddChoreDialogOpen = false },
                onConfirm = { title, desc, cat, memberId, memberName, isUrgent, recurrence, pts ->
                    viewModel.addChore(title, desc, cat, memberId, memberName, isUrgent, recurrence, pts)
                    isAddChoreDialogOpen = false
                }
            )
        }

        // First-Time Setup Wizard Dialog (100% finished onboarding assistant)
        SetupWizardDialog(
            isOpen = showSetupWizard,
            currentHouseholdName = allGroups.firstOrNull()?.name ?: "Unsere Familie",
            members = allMembers,
            groups = allGroups,
            categories = allCategories,
            initialBudgetCap = monthlyBudgetCap,
            isDarkMode = isDarkMode,
            isSeniorMode = isSeniorMode,
            onComplete = { householdName, budget, enableSenior, enableDark ->
                viewModel.completeSetupWizard(householdName, budget, enableSenior, enableDark)
            },
            onDismiss = { viewModel.dismissSetupWizard() },
            onAddMember = { name, role, rel, phone ->
                viewModel.addFamilyMember(name, role, rel, phone)
            },
            onUpdateMember = { viewModel.updateFamilyMember(it) },
            onDeleteMember = { viewModel.deleteFamilyMember(it) },
            onAddCategory = { name, type, color ->
                viewModel.addCategory(name, type, color)
            },
            onDeleteCategory = { viewModel.deleteCategory(it) }
        )
    }
}
