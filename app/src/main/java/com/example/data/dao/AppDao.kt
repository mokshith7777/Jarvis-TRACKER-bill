package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.SyncQueueEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY dateTime DESC")
  fun getAllActive(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY dateTime DESC")
  suspend fun getAllActiveList(): List<TransactionEntity>

  @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND title = :title AND amountPaise = :amountPaise AND category = :category AND type = :type AND dateTime >= :minTime LIMIT 5")
  suspend fun findRecentDuplicate(title: String, amountPaise: Long, category: String, type: String, minTime: Long): List<TransactionEntity>

  @Query("SELECT COUNT(*) FROM transactions WHERE isDeleted = 0")
  fun getActiveCount(): Flow<Int>

  @Query("DELETE FROM transactions WHERE id IN (:ids)")
  suspend fun hardDeleteBatch(ids: List<String>)

  @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
  fun getById(id: String): Flow<TransactionEntity?>

  @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND dateTime BETWEEN :startTime AND :endTime ORDER BY dateTime DESC")
  fun getBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND category = :category ORDER BY dateTime DESC")
  fun getByCategory(category: String): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(transaction: TransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(transactions: List<TransactionEntity>)

  @Update
  suspend fun update(transaction: TransactionEntity)

  @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :id")
  suspend fun softDelete(id: String, updatedAt: Long = System.currentTimeMillis())

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun hardDelete(id: String)

  @Query("UPDATE transactions SET syncStatus = :status WHERE id = :id")
  suspend fun updateSyncStatus(id: String, status: String)

  @Query("SELECT COUNT(*) FROM transactions WHERE syncStatus != 'SYNCED' AND isDeleted = 0")
  fun getUnsyncedCount(): Flow<Int>
}

@Dao
interface CategoryDao {
  @Query("SELECT * FROM categories ORDER BY name ASC")
  fun getAll(): Flow<List<CategoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(categories: List<CategoryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(category: CategoryEntity)
}

@Dao
interface BudgetDao {
  @Query("SELECT * FROM budgets WHERE monthKey = :monthKey LIMIT 1")
  fun getBudgetForMonth(monthKey: String): Flow<BudgetEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(budget: BudgetEntity)
}

@Dao
interface SyncQueueDao {
  @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
  fun getAll(): Flow<List<SyncQueueEntity>>

  @Query("SELECT COUNT(*) FROM sync_queue WHERE status != 'COMPLETED'")
  fun getPendingCount(): Flow<Int>

  @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR (status = 'FAILED' AND nextRetryAt <= :now) ORDER BY createdAt ASC LIMIT :limit")
  suspend fun getNextBatch(now: Long = System.currentTimeMillis(), limit: Int = 20): List<SyncQueueEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(operation: SyncQueueEntity)

  @Update
  suspend fun update(operation: SyncQueueEntity)

  @Query("DELETE FROM sync_queue WHERE operationId = :operationId")
  suspend fun delete(operationId: String)

  @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
  suspend fun clearCompleted()

  @Query("UPDATE sync_queue SET status = 'PENDING', nextRetryAt = 0 WHERE status = 'FAILED'")
  suspend fun resetFailed()
}
