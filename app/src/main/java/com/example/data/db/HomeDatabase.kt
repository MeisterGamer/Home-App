package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ChoreDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.FamilyMemberDao
import com.example.data.dao.MedicationDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.ShoppingDao
import com.example.data.dao.SmartDeviceDao
import com.example.data.dao.WearableDao
import com.example.data.entity.ChoreEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.FamilyMemberEntity
import com.example.data.entity.MedicationEntity
import com.example.data.entity.NotificationAlertEntity
import com.example.data.entity.ShoppingItemEntity
import com.example.data.entity.SmartDeviceEntity
import com.example.data.entity.WearableVitalEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChoreEntity::class,
        ShoppingItemEntity::class,
        ExpenseEntity::class,
        FamilyMemberEntity::class,
        SmartDeviceEntity::class,
        MedicationEntity::class,
        WearableVitalEntity::class,
        NotificationAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HomeDatabase : RoomDatabase() {
    abstract fun choreDao(): ChoreDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun smartDeviceDao(): SmartDeviceDao
    abstract fun medicationDao(): MedicationDao
    abstract fun wearableDao(): WearableDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: HomeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HomeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomeDatabase::class.java,
                    "homenest_master_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(HomeDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class HomeDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(db: HomeDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 86_400_000L

            // 1. Family Members
            val members = listOf(
                FamilyMemberEntity(1, "Sarah (Mom)", "ADMIN", 0xFF0D9488, 140, "555-0101", "Mom"),
                FamilyMemberEntity(2, "David (Dad)", "ADMIN", 0xFF2563EB, 115, "555-0102", "Dad"),
                FamilyMemberEntity(3, "Alex", "TEEN", 0xFF8B5CF6, 85, "555-0103", "Son (15)"),
                FamilyMemberEntity(4, "Maya", "CHILD", 0xFFF59E0B, 120, "555-0104", "Daughter (8)"),
                FamilyMemberEntity(5, "Grandma Rose", "SENIOR", 0xFFEC4899, 50, "555-0199", "Grandmother")
            )
            db.familyMemberDao().insertMembers(members)

            // 2. Chores & Urgent Repairs
            val chores = listOf(
                ChoreEntity(
                    id = 1,
                    title = "Fix Leaking Kitchen Sink Pipe",
                    description = "Under-sink P-trap has slight drip. Tighten slip-nut or replace washer.",
                    category = "MAINTENANCE",
                    assignedMemberId = 2,
                    assignedMemberName = "David (Dad)",
                    dueDate = now + (3 * 3600_000L),
                    priority = "URGENT",
                    isCompleted = false,
                    pointsValue = 35,
                    isUrgentRepair = true,
                    recurrence = "Once"
                ),
                ChoreEntity(
                    id = 2,
                    title = "Replace HVAC Air Filter (12% Left)",
                    description = "Carrier smart monitor alerted high dust resistance. Size: 16x25x1 Merv 11.",
                    category = "MAINTENANCE",
                    assignedMemberId = 2,
                    assignedMemberName = "David (Dad)",
                    dueDate = now + (6 * 3600_000L),
                    priority = "URGENT",
                    isCompleted = false,
                    pointsValue = 25,
                    isUrgentRepair = true,
                    recurrence = "Monthly"
                ),
                ChoreEntity(
                    id = 3,
                    title = "Unload Clean Dishes from Bosch Dishwasher",
                    description = "Cycle finished 2 hours ago. Put away glassware and cutlery.",
                    category = "KITCHEN",
                    assignedMemberId = 3,
                    assignedMemberName = "Alex",
                    dueDate = now + (2 * 3600_000L),
                    priority = "MEDIUM",
                    isCompleted = false,
                    pointsValue = 15,
                    isUrgentRepair = false,
                    recurrence = "Daily"
                ),
                ChoreEntity(
                    id = 4,
                    title = "Feed Bella & Refresh Water Bowl",
                    description = "Scoop 1.5 cups of dry kibble and add allergy chewable treat.",
                    category = "PETS",
                    assignedMemberId = 4,
                    assignedMemberName = "Maya",
                    dueDate = now + (1 * 3600_000L),
                    priority = "LOW",
                    isCompleted = false,
                    pointsValue = 10,
                    isUrgentRepair = false,
                    recurrence = "Daily"
                ),
                ChoreEntity(
                    id = 5,
                    title = "Empty RoboVac Dustbin & Sensor Wipe",
                    description = "Docked after completing downstairs living room route.",
                    category = "CLEANING",
                    assignedMemberId = 1,
                    assignedMemberName = "Sarah (Mom)",
                    dueDate = now + (8 * 3600_000L),
                    priority = "LOW",
                    isCompleted = true,
                    completedAt = now - 3600_000L,
                    pointsValue = 10,
                    isUrgentRepair = false,
                    recurrence = "Daily"
                ),
                ChoreEntity(
                    id = 6,
                    title = "Mow Front Lawn & Edge Walkway",
                    description = "Keep grass at 2.5 inches. Sweep clippings into yard waste bin.",
                    category = "YARD",
                    assignedMemberId = 3,
                    assignedMemberName = "Alex",
                    dueDate = now + dayMs,
                    priority = "MEDIUM",
                    isCompleted = false,
                    pointsValue = 30,
                    isUrgentRepair = false,
                    recurrence = "Weekly"
                ),
                ChoreEntity(
                    id = 7,
                    title = "Test Smoke & Carbon Monoxide Detectors",
                    description = "Press test button on 2nd-floor detector unit until 3 beeps sound.",
                    category = "SAFETY",
                    assignedMemberId = 2,
                    assignedMemberName = "David (Dad)",
                    dueDate = now + (2 * dayMs),
                    priority = "HIGH",
                    isCompleted = false,
                    pointsValue = 20,
                    isUrgentRepair = false,
                    recurrence = "Monthly"
                ),
                ChoreEntity(
                    id = 8,
                    title = "Gentle Afternoon Garden Walk & Herb Watering",
                    description = "Water basil and cherry tomatoes on the sunny patio.",
                    category = "YARD",
                    assignedMemberId = 5,
                    assignedMemberName = "Grandma Rose",
                    dueDate = now + (5 * 3600_000L),
                    priority = "LOW",
                    isCompleted = false,
                    pointsValue = 15,
                    isUrgentRepair = false,
                    recurrence = "Daily"
                )
            )
            db.choreDao().insertChores(chores)

            // 3. Shopping Items
            val shopping = listOf(
                ShoppingItemEntity(1, "Organic Whole Milk (2 Gallons)", "Groceries", "2 gal", 7.50, false, "Smart Fridge", "High"),
                ShoppingItemEntity(2, "Merv-11 HVAC Air Filter (16x25x1)", "Hardware", "2 units", 24.99, false, "HVAC Sensor", "High"),
                ShoppingItemEntity(3, "Plumber's Thread Seal Tape & O-Rings", "Hardware", "1 kit", 6.80, false, "David (Dad)", "High"),
                ShoppingItemEntity(4, "Artisan Sourdough Loaf", "Groceries", "1 loaf", 4.50, false, "Sarah (Mom)", "Normal"),
                ShoppingItemEntity(5, "Cascade Platinum Dishwasher Pods", "Household", "62 count", 19.99, false, "Smart Dishwasher", "Normal"),
                ShoppingItemEntity(6, "Bella Dog Crunchy Dental Treats", "Groceries", "1 box", 12.49, true, "Maya", "Normal"),
                ShoppingItemEntity(7, "Senior CoQ10 & Heart Health Vitamins", "Pharmacy", "1 bottle", 18.25, false, "Grandma Rose", "High")
            )
            db.shoppingDao().insertItems(shopping)

            // 4. Expenses (Automated Budget Tracking)
            val expenses = listOf(
                ExpenseEntity(1, "Plumbing Repair Fittings & Teflon Sealant", "MAINTENANCE", 32.50, now - (dayMs), "David (Dad)", "Fixed kitchen sink connection"),
                ExpenseEntity(2, "Weekly Household Groceries (Fresh Market)", "GROCERIES", 186.40, now - (2 * dayMs), "Sarah (Mom)", "Fresh produce, meat, dairy"),
                ExpenseEntity(3, "City Municipal Water & Sewer Utility", "UTILITIES", 78.90, now - (4 * dayMs), "Sarah (Mom)", "Monthly statement"),
                ExpenseEntity(4, "Cascade Dishwasher & Eco Cleaning Supplies", "SUPPLIES", 38.20, now - (6 * dayMs), "David (Dad)", "Target store trip"),
                ExpenseEntity(5, "Grandma Rose Monthly Prescriptions Copay", "HEALTH", 25.00, now - (8 * dayMs), "Grandma Rose", "Pharmacy pickup")
            )
            db.expenseDao().insertExpenses(expenses)

            // 5. Smart Devices
            val devices = listOf(
                SmartDeviceEntity(
                    id = "robovac_01",
                    name = "RoboVac Pro Series",
                    type = "ROBOT_VACUUM",
                    location = "Living Room",
                    status = "NORMAL",
                    batteryLevel = 90,
                    healthPercent = 94,
                    metricLabel = "Status",
                    metricValue = "Docked & Ready",
                    lastAction = "Cleaned 480 sq ft"
                ),
                SmartDeviceEntity(
                    id = "dishwasher_01",
                    name = "Bosch Series 8 Dishwasher",
                    type = "DISHWASHER",
                    location = "Kitchen",
                    status = "NORMAL",
                    batteryLevel = 100,
                    healthPercent = 98,
                    metricLabel = "Cycle",
                    metricValue = "Clean - Ready to Unload",
                    lastAction = "Sanitized Eco Cycle"
                ),
                SmartDeviceEntity(
                    id = "hvac_01",
                    name = "Carrier Air Quality & HVAC Monitor",
                    type = "HVAC_FILTER",
                    location = "Basement Main Hall",
                    status = "WARNING",
                    batteryLevel = 100,
                    healthPercent = 12,
                    metricLabel = "Filter Life",
                    metricValue = "12% - Urgent Replace!",
                    lastAction = "High particulate resistance"
                ),
                SmartDeviceEntity(
                    id = "smoke_01",
                    name = "Nest Protect Smoke + CO",
                    type = "SMOKE_DETECTOR",
                    location = "Upstairs Bedroom Hall",
                    status = "NORMAL",
                    batteryLevel = 96,
                    healthPercent = 100,
                    metricLabel = "Sensors",
                    metricValue = "All Safe (No smoke detected)",
                    lastAction = "Self-test passed"
                ),
                SmartDeviceEntity(
                    id = "fridge_01",
                    name = "Samsung Family Hub Refrigerator",
                    type = "SMART_FRIDGE",
                    location = "Kitchen",
                    status = "NORMAL",
                    batteryLevel = 100,
                    healthPercent = 88,
                    metricLabel = "Water Filter",
                    metricValue = "78% (Good for 60 days)",
                    lastAction = "Milk level low - auto added"
                )
            )
            db.smartDeviceDao().insertDevices(devices)

            // 6. Senior Medications
            val meds = listOf(
                MedicationEntity(1, "Lisinopril (Blood Pressure)", "10 mg", "Morning", "08:00 AM", "Take with water after breakfast", true, 5),
                MedicationEntity(2, "Metformin (Blood Sugar)", "500 mg", "Afternoon", "01:00 PM", "Take with warm lunch", false, 5),
                MedicationEntity(3, "Atorvastatin (Cholesterol)", "20 mg", "Evening", "07:30 PM", "Take at dinner time", false, 5),
                MedicationEntity(4, "Vitamin D3 + Calcium", "1000 IU", "Morning", "08:00 AM", "Bone and immunity support", true, 5)
            )
            db.medicationDao().insertMedications(meds)

            // 7. Wearable Health Vitals (Elder & Family Health)
            val vitals = WearableVitalEntity(
                memberId = 5,
                heartRate = 72,
                steps = 3840,
                sleepHours = 7.6f,
                deepSleepPercent = 24,
                spo2Percent = 98,
                fallDetected = false,
                lastSyncTimestamp = now
            )
            db.wearableDao().insertVital(vitals)

            // 8. Notifications
            val alerts = listOf(
                NotificationAlertEntity(
                    id = 1,
                    title = "🚨 Urgent Repair: Kitchen Sink Pipe Leak",
                    message = "Water drip detected under kitchen sink. P-trap needs seal tightening.",
                    type = "URGENT_REPAIR",
                    timestamp = now - (30 * 60_000L),
                    isRead = false,
                    isUrgent = true
                ),
                NotificationAlertEntity(
                    id = 2,
                    title = "⚠️ Smart Home: HVAC Air Filter at 12%",
                    message = "Air resistance high. Replacement filter (16x25x1) recommended this week.",
                    type = "SMART_HOME",
                    timestamp = now - (2 * 3600_000L),
                    isRead = false,
                    isUrgent = true
                ),
                NotificationAlertEntity(
                    id = 3,
                    title = "💊 Medication Due: Afternoon Metformin",
                    message = "Grandma Rose - 1:00 PM dose (500mg) scheduled with lunch.",
                    type = "MEDICATION",
                    timestamp = now - (15 * 60_000L),
                    isRead = false,
                    isUrgent = false
                )
            )
            db.notificationDao().insertAlerts(alerts)
        }
    }
}
