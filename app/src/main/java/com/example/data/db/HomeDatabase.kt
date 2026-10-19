package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CategoryDao
import com.example.data.dao.ChoreDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.FamilyMemberDao
import com.example.data.dao.GroupDao
import com.example.data.dao.MedicationDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.ShoppingDao
import com.example.data.dao.SmartDeviceDao
import com.example.data.dao.WearableDao
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
        NotificationAlertEntity::class,
        GroupEntity::class,
        CategoryEntity::class
    ],
    version = 3,
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
    abstract fun groupDao(): GroupDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: HomeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HomeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomeDatabase::class.java,
                    "homenest_clean_db"
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
                        seedInitialProfiles(database)
                    }
                }
            }
        }

        /**
         * Initialize standard groups, profiles, and customizable categories.
         */
        suspend fun seedInitialProfiles(db: HomeDatabase) {
            // Seed Household Groups
            val defaultGroups = listOf(
                GroupEntity(1, "Hauptfamilie", "Zuhause & gemeinsamer Alltag", 0xFF0D9488, "Home"),
                GroupEntity(2, "Wohngemeinschaft", "Gemeinsame Aufgaben & Einkäufe", 0xFF2563EB, "Group")
            )
            db.groupDao().insertGroups(defaultGroups)

            // Seed Family Members
            val members = listOf(
                FamilyMemberEntity(1, "Eltern (Admin)", "ADMIN", 0xFF0D9488, 0, "", "Hauptverwalter", 1L),
                FamilyMemberEntity(2, "Partner / Eltern", "ADMIN", 0xFF2563EB, 0, "", "Co-Verwalter", 1L),
                FamilyMemberEntity(3, "Jugendliche/r", "TEEN", 0xFF8B5CF6, 0, "", "Familienmitglied", 1L),
                FamilyMemberEntity(4, "Kind", "CHILD", 0xFFF59E0B, 0, "", "Familienmitglied", 1L),
                FamilyMemberEntity(5, "Senior / Großeltern", "SENIOR", 0xFFEC4899, 0, "", "Senioren-Betreuung", 1L)
            )
            db.familyMemberDao().insertMembers(members)

            // Seed Default Categories
            val defaultCategories = listOf(
                CategoryEntity(1, "Reinigung", "CHORE", 0xFF0D9488, "CleaningServices"),
                CategoryEntity(2, "Wartung & Reparatur", "CHORE", 0xFFE11D48, "Build"),
                CategoryEntity(3, "Küche", "CHORE", 0xFFD97706, "Kitchen"),
                CategoryEntity(4, "Garten", "CHORE", 0xFF16A34A, "Yard"),
                CategoryEntity(5, "Termin & Event", "CHORE", 0xFF7C3AED, "CalendarToday"),
                CategoryEntity(6, "Lebensmittel", "SHOPPING", 0xFF059669, "ShoppingCart"),
                CategoryEntity(7, "Haushalt & Drogerie", "SHOPPING", 0xFF2563EB, "House"),
                CategoryEntity(8, "Baumarkt & Garten", "SHOPPING", 0xFFD97706, "Hardware"),
                CategoryEntity(9, "Apotheke & Gesundheit", "SHOPPING", 0xFFDC2626, "MedicalServices"),
                CategoryEntity(10, "Miete & Wohnen", "EXPENSE", 0xFF4F46E5, "Home"),
                CategoryEntity(11, "Lebensmittel & Kochen", "EXPENSE", 0xFF059669, "Restaurant"),
                CategoryEntity(12, "Haushalt & Reparatur", "EXPENSE", 0xFFD97706, "Handyman"),
                CategoryEntity(13, "Freizeit & Familie", "EXPENSE", 0xFFDB2777, "Attractions")
            )
            db.categoryDao().insertCategories(defaultCategories)
        }
    }
}
