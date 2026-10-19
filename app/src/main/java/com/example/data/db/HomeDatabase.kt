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
    version = 2,
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
         * Initialize only standard user profiles so roles (Admin, Teen, Child, Senior)
         * can be used immediately, but with ZERO example chores, ZERO fake expenses,
         * ZERO fake shopping items, and ZERO fake notifications.
         */
        suspend fun seedInitialProfiles(db: HomeDatabase) {
            val members = listOf(
                FamilyMemberEntity(1, "Eltern (Admin)", "ADMIN", 0xFF0D9488, 0, "", "Hauptverwalter"),
                FamilyMemberEntity(2, "Partner / Eltern", "ADMIN", 0xFF2563EB, 0, "", "Co-Verwalter"),
                FamilyMemberEntity(3, "Jugendliche/r", "TEEN", 0xFF8B5CF6, 0, "", "Familienmitglied"),
                FamilyMemberEntity(4, "Kind", "CHILD", 0xFFF59E0B, 0, "", "Familienmitglied"),
                FamilyMemberEntity(5, "Senior / Großeltern", "SENIOR", 0xFFEC4899, 0, "", "Senioren-Betreuung")
            )
            db.familyMemberDao().insertMembers(members)
        }
    }
}
