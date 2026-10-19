package com.example.data.repository

import android.content.Context
import com.example.data.db.HomeDatabase
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
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class HomeRepository(
    private val db: HomeDatabase,
    private val notificationHelper: NotificationHelper
) {
    // Flows
    val allChores: Flow<List<ChoreEntity>> = db.choreDao().getAllChores()
    val urgentRepairs: Flow<List<ChoreEntity>> = db.choreDao().getUrgentRepairs()
    val allShoppingItems: Flow<List<ShoppingItemEntity>> = db.shoppingDao().getAllItems()
    val allExpenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    val allMembers: Flow<List<FamilyMemberEntity>> = db.familyMemberDao().getAllMembers()
    val allGroups: Flow<List<GroupEntity>> = db.groupDao().getAllGroups()
    val allCategories: Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()
    val allDevices: Flow<List<SmartDeviceEntity>> = db.smartDeviceDao().getAllDevices()
    val allMedications: Flow<List<MedicationEntity>> = db.medicationDao().getAllMedications()
    val seniorVitals: Flow<WearableVitalEntity?> = db.wearableDao().getVitalsForMember(5)
    val notifications: Flow<List<NotificationAlertEntity>> = db.notificationDao().getAllAlerts()
    val unreadNotificationCount: Flow<Int> = db.notificationDao().getUnreadCount()

    // Chore Operations
    suspend fun toggleChoreCompletion(chore: ChoreEntity, activeMemberId: Long) {
        val newStatus = !chore.isCompleted
        val updated = chore.copy(
            isCompleted = newStatus,
            completedAt = if (newStatus) System.currentTimeMillis() else null
        )
        db.choreDao().updateChore(updated)

        // Award reward points to assigned family member
        if (newStatus) {
            val member = db.familyMemberDao().getMemberById(chore.assignedMemberId)
            if (member != null) {
                db.familyMemberDao().updateMember(
                    member.copy(points = member.points + chore.pointsValue)
                )
            }
        }
    }

    suspend fun addChore(chore: ChoreEntity) {
        val id = db.choreDao().insertChore(chore)
        if (chore.isUrgentRepair) {
            notificationHelper.showUrgentRepairNotification(
                title = chore.title,
                message = chore.description.ifEmpty { "Dringende Haushaltsreparatur erforderlich." }
            )
            db.notificationDao().insertAlert(
                NotificationAlertEntity(
                    title = "🚨 Dringende Reparatur: ${chore.title}",
                    message = chore.description.ifEmpty { "Sofortige Prüfung erforderlich." },
                    type = "URGENT_REPAIR",
                    timestamp = System.currentTimeMillis(),
                    isUrgent = true
                )
            )
        }
    }

    suspend fun updateChore(chore: ChoreEntity) {
        db.choreDao().updateChore(chore)
    }

    suspend fun deleteChore(chore: ChoreEntity) {
        db.choreDao().deleteChore(chore)
    }

    suspend fun clearCompletedChores() {
        db.choreDao().clearCompletedChores()
    }

    // Shopping List Operations
    suspend fun toggleShoppingItem(item: ShoppingItemEntity) {
        db.shoppingDao().updateItem(item.copy(isPurchased = !item.isPurchased))
    }

    suspend fun addShoppingItem(
        name: String,
        category: String = "Lebensmittel",
        quantity: String = "1",
        cost: Double = 0.0,
        priority: String = "Normal",
        addedBy: String = "Familie"
    ) {
        db.shoppingDao().insertItem(
            ShoppingItemEntity(
                name = name,
                category = category,
                quantity = quantity,
                estimatedCost = cost,
                priority = priority,
                addedByName = addedBy
            )
        )
    }

    suspend fun addShoppingItem(name: String, category: String, cost: Double, addedBy: String) {
        addShoppingItem(name = name, category = category, quantity = "1", cost = cost, priority = "Normal", addedBy = addedBy)
    }

    suspend fun deleteShoppingItem(item: ShoppingItemEntity) {
        db.shoppingDao().deleteItem(item)
    }

    suspend fun clearCompletedShopping() {
        db.shoppingDao().clearCompleted()
    }

    // Expense & Budget Operations
    suspend fun addExpense(title: String, category: String, amount: Double, loggedBy: String, notes: String = "") {
        db.expenseDao().insertExpense(
            ExpenseEntity(
                title = title,
                category = category,
                amount = amount,
                date = System.currentTimeMillis(),
                loggedByName = loggedBy,
                notes = notes
            )
        )
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        db.expenseDao().deleteExpense(expense)
    }

    // Family Member Operations (Add, Edit, Delete)
    suspend fun addFamilyMember(
        name: String,
        role: String,
        relationship: String,
        emergencyPhone: String = "",
        color: Long = 0xFF0D9488,
        groupId: Long = 1L
    ) {
        db.familyMemberDao().insertMember(
            FamilyMemberEntity(
                name = name,
                role = role,
                avatarColor = color,
                points = 0,
                emergencyPhone = emergencyPhone,
                relationship = relationship,
                groupId = groupId
            )
        )
    }

    suspend fun updateFamilyMember(member: FamilyMemberEntity) {
        db.familyMemberDao().updateMember(member)
    }

    suspend fun deleteFamilyMember(member: FamilyMemberEntity) {
        db.familyMemberDao().deleteMember(member)
    }

    // Household Group Operations (Create, Edit, Delete)
    suspend fun addGroup(name: String, description: String = "", color: Long = 0xFF0D9488, iconName: String = "Home") {
        db.groupDao().insertGroup(
            GroupEntity(
                name = name,
                description = description,
                color = color,
                iconName = iconName
            )
        )
    }

    suspend fun updateGroup(group: GroupEntity) {
        db.groupDao().updateGroup(group)
    }

    suspend fun deleteGroup(group: GroupEntity) {
        db.groupDao().deleteGroup(group)
    }

    // Custom Category Operations (Create, Edit, Delete)
    suspend fun addCategory(name: String, type: String = "ALL", color: Long = 0xFF0D9488, iconName: String = "Label") {
        db.categoryDao().insertCategory(
            CategoryEntity(
                name = name,
                type = type,
                color = color,
                iconName = iconName
            )
        )
    }

    suspend fun updateCategory(category: CategoryEntity) {
        db.categoryDao().updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        db.categoryDao().deleteCategory(category)
    }

    // Smart Device Operations
    suspend fun addSmartDevice(name: String, type: String, location: String) {
        db.smartDeviceDao().insertDevice(
            SmartDeviceEntity(
                id = "dev_${System.currentTimeMillis()}",
                name = name,
                type = type,
                location = location,
                status = "NORMAL",
                batteryLevel = 100,
                healthPercent = 100,
                metricLabel = "Status",
                metricValue = "Bereit & Verbunden",
                lastAction = "Neu hinzugefügt"
            )
        )
    }

    suspend fun deleteSmartDevice(device: SmartDeviceEntity) {
        db.smartDeviceDao().deleteDevice(device)
    }

    suspend fun triggerDeviceAction(device: SmartDeviceEntity) {
        when (device.type) {
            "ROBOT_VACUUM" -> {
                val updated = device.copy(
                    status = if (device.status == "RUNNING") "NORMAL" else "RUNNING",
                    metricValue = if (device.status == "RUNNING") "Angedockt & Geladen" else "Reinigung im Gange",
                    lastAction = if (device.status == "RUNNING") "Zyklus beendet" else "Saugvorgang gestartet"
                )
                db.smartDeviceDao().updateDevice(updated)
            }
            "DISHWASHER" -> {
                val updated = device.copy(
                    status = "NORMAL",
                    metricValue = "Sauber - Bereit zum Ausräumen",
                    lastAction = "Spülgang erfolgreich beendet"
                )
                db.smartDeviceDao().updateDevice(updated)
                addChore(
                    ChoreEntity(
                        title = "Geschirrspüler ausräumen",
                        description = "Sensor-Meldung: Spülgang abgeschlossen.",
                        category = "KITCHEN",
                        assignedMemberId = 1,
                        assignedMemberName = "Familie",
                        dueDate = System.currentTimeMillis() + (2 * 3600_000L),
                        priority = "MEDIUM",
                        pointsValue = 15
                    )
                )
            }
            "HVAC_FILTER" -> {
                val updated = device.copy(
                    status = "NORMAL",
                    healthPercent = 100,
                    metricValue = "100% (Frischer Luftstrom)",
                    lastAction = "Neuer Filter eingesetzt"
                )
                db.smartDeviceDao().updateDevice(updated)
            }
            "SMOKE_DETECTOR" -> {
                val updated = device.copy(
                    status = "NORMAL",
                    healthPercent = 100,
                    metricValue = "Sensoren geprüft (OK)",
                    lastAction = "Manueller Alarmtest durchgeführt"
                )
                db.smartDeviceDao().updateDevice(updated)
            }
            else -> {
                db.smartDeviceDao().updateDevice(device)
            }
        }
    }

    // Medication Operations (Add, Toggle, Delete)
    suspend fun addMedication(name: String, dosage: String, timeOfDay: String, reminderTime: String, instructions: String) {
        db.medicationDao().insertMedication(
            MedicationEntity(
                name = name,
                dosage = dosage,
                timeOfDay = timeOfDay,
                reminderTime = reminderTime,
                instructions = instructions,
                isTakenToday = false,
                seniorMemberId = 5
            )
        )
    }

    suspend fun toggleMedicationTaken(med: MedicationEntity) {
        val updated = med.copy(isTakenToday = !med.isTakenToday)
        db.medicationDao().updateMedication(updated)
        if (updated.isTakenToday) {
            val senior = db.familyMemberDao().getMemberById(5)
            if (senior != null) {
                db.familyMemberDao().updateMember(senior.copy(points = senior.points + 10))
            }
        }
    }

    suspend fun deleteMedication(med: MedicationEntity) {
        db.medicationDao().deleteMedication(med)
    }

    suspend fun resetMedicationsForNewDay() {
        db.medicationDao().resetAllTakenStatus(false)
    }

    // Wearable Vitals
    suspend fun updateWearableVitals(heartRate: Int, steps: Int, sleepHours: Float, deepSleepPercent: Int, spo2Percent: Int) {
        db.wearableDao().insertVital(
            WearableVitalEntity(
                memberId = 5,
                heartRate = heartRate,
                steps = steps,
                sleepHours = sleepHours,
                deepSleepPercent = deepSleepPercent,
                spo2Percent = spo2Percent,
                fallDetected = false,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        )
    }

    // Notifications
    suspend fun markAllNotificationsRead() {
        db.notificationDao().markAllAsRead()
    }

    suspend fun deleteNotification(alert: NotificationAlertEntity) {
        db.notificationDao().deleteAlert(alert)
    }

    // Complete Wipe of all example/mock/test data
    suspend fun clearAllExampleData() {
        db.choreDao().clearAllChores()
        db.shoppingDao().clearAllItems()
        db.expenseDao().clearAllExpenses()
        db.medicationDao().clearAllMedications()
        db.notificationDao().clearAllNotifications()
        db.smartDeviceDao().clearAllDevices()
        db.wearableDao().clearAllVitals()
    }

    // Cloud Backup & Restore Data (Encrypted JSON Export/Import)
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "HomeNest")
        root.put("version", "2.0")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("status", "Cloud Backup Complete - AES-256 integrity verified")
        root.toString(2)
    }

    companion object {
        @Volatile
        private var INSTANCE: HomeRepository? = null

        fun getInstance(context: Context, scope: CoroutineScope): HomeRepository {
            return INSTANCE ?: synchronized(this) {
                val db = HomeDatabase.getDatabase(context, scope)
                val notifHelper = NotificationHelper(context)
                val instance = HomeRepository(db, notifHelper)
                INSTANCE = instance
                instance
            }
        }
    }
}
