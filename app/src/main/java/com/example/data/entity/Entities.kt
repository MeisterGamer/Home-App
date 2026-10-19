package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chores")
data class ChoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String, // CLEANING, KITCHEN, MAINTENANCE, etc.
    val assignedMemberId: Long,
    val assignedMemberName: String,
    val dueDate: Long, // timestamp ms
    val priority: String, // LOW, MEDIUM, HIGH, URGENT
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val pointsValue: Int = 10,
    val isUrgentRepair: Boolean = false,
    val recurrence: String = "Once" // Once, Daily, Weekly, Monthly
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Groceries", // Groceries, Household, Hardware, Pharmacy
    val quantity: String = "1",
    val estimatedCost: Double = 0.0,
    val isPurchased: Boolean = false,
    val addedByName: String = "Family",
    val priority: String = "Normal"
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // MAINTENANCE, GROCERIES, UTILITIES, SUPPLIES, etc.
    val amount: Double,
    val date: Long,
    val loggedByName: String,
    val notes: String = ""
)

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String, // ADMIN, TEEN, CHILD, SENIOR
    val avatarColor: Long,
    val points: Int = 0,
    val emergencyPhone: String = "",
    val relationship: String = "",
    val groupId: Long = 1L
)

@Entity(tableName = "household_groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: Long = 0xFF0D9488,
    val iconName: String = "Home"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "ALL", // ALL, CHORE, SHOPPING, EXPENSE
    val color: Long = 0xFF0D9488,
    val iconName: String = "Label"
)

@Entity(tableName = "smart_devices")
data class SmartDeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // ROBOT_VACUUM, DISHWASHER, HVAC_FILTER, etc.
    val location: String,
    val status: String, // NORMAL, RUNNING, NEEDS_ATTENTION, WARNING
    val batteryLevel: Int = 100,
    val healthPercent: Int = 100,
    val metricLabel: String = "",
    val metricValue: String = "",
    val lastAction: String = ""
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val timeOfDay: String, // Morning, Afternoon, Evening, Bedtime
    val reminderTime: String, // "08:00 AM"
    val instructions: String = "",
    val isTakenToday: Boolean = false,
    val seniorMemberId: Long = 0
)

@Entity(tableName = "wearable_vitals")
data class WearableVitalEntity(
    @PrimaryKey val memberId: Long,
    val heartRate: Int,
    val steps: Int,
    val sleepHours: Float,
    val deepSleepPercent: Int,
    val spo2Percent: Int,
    val fallDetected: Boolean = false,
    val lastSyncTimestamp: Long
)

@Entity(tableName = "notification_alerts")
data class NotificationAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // URGENT_REPAIR, MEDICATION, CHORE_DUE, SMART_HOME
    val timestamp: Long,
    val isRead: Boolean = false,
    val isUrgent: Boolean = false
)
