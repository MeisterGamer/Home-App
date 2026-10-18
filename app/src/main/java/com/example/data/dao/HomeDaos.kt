package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ChoreEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.FamilyMemberEntity
import com.example.data.entity.MedicationEntity
import com.example.data.entity.NotificationAlertEntity
import com.example.data.entity.ShoppingItemEntity
import com.example.data.entity.SmartDeviceEntity
import com.example.data.entity.WearableVitalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChoreDao {
    @Query("SELECT * FROM chores ORDER BY isCompleted ASC, isUrgentRepair DESC, dueDate ASC")
    fun getAllChores(): Flow<List<ChoreEntity>>

    @Query("SELECT * FROM chores WHERE isUrgentRepair = 1 AND isCompleted = 0")
    fun getUrgentRepairs(): Flow<List<ChoreEntity>>

    @Query("SELECT * FROM chores WHERE assignedMemberId = :memberId ORDER BY isCompleted ASC, dueDate ASC")
    fun getChoresForMember(memberId: Long): Flow<List<ChoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChore(chore: ChoreEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChores(chores: List<ChoreEntity>)

    @Update
    suspend fun updateChore(chore: ChoreEntity)

    @Delete
    suspend fun deleteChore(chore: ChoreEntity)
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC, id DESC")
    fun getAllItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE isPurchased = 1")
    suspend fun clearCompleted()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Long): FamilyMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<FamilyMemberEntity>)

    @Update
    suspend fun updateMember(member: FamilyMemberEntity)
}

@Dao
interface SmartDeviceDao {
    @Query("SELECT * FROM smart_devices ORDER BY name ASC")
    fun getAllDevices(): Flow<List<SmartDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<SmartDeviceEntity>)

    @Update
    suspend fun updateDevice(device: SmartDeviceEntity)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY id ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET isTakenToday = :taken")
    suspend fun resetAllTakenStatus(taken: Boolean)
}

@Dao
interface WearableDao {
    @Query("SELECT * FROM wearable_vitals WHERE memberId = :memberId LIMIT 1")
    fun getVitalsForMember(memberId: Long): Flow<WearableVitalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVital(vital: WearableVitalEntity)

    @Update
    suspend fun updateVital(vital: WearableVitalEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<NotificationAlertEntity>>

    @Query("SELECT COUNT(*) FROM notification_alerts WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: NotificationAlertEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<NotificationAlertEntity>)

    @Update
    suspend fun updateAlert(alert: NotificationAlertEntity)

    @Query("UPDATE notification_alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Delete
    suspend fun deleteAlert(alert: NotificationAlertEntity)
}
