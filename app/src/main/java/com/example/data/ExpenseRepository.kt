package com.example.data

import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.SyncQueueDao
import com.example.data.dao.TransactionDao
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.SyncQueueEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ExpenseRepository(
  private val transactionDao: TransactionDao,
  private val categoryDao: CategoryDao,
  private val budgetDao: BudgetDao,
  private val syncQueueDao: SyncQueueDao
) {
  val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllActive()
  val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAll()
  val activeTransactionCount: Flow<Int> = transactionDao.getActiveCount()

  fun getTransactionById(id: String): Flow<TransactionEntity?> = transactionDao.getById(id)

  fun getCurrentMonthBudget(): Flow<BudgetEntity?> {
    val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    return budgetDao.getBudgetForMonth(monthKey)
  }

  suspend fun addTransaction(transaction: TransactionEntity): Boolean = withContext(Dispatchers.IO) {
    // 1. Duplicate Shield Guard: Check if an identical record was added in the last 6 seconds
    val duplicateWindow = 6000L
    val minTime = transaction.dateTime - duplicateWindow
    val recentDuplicates = transactionDao.findRecentDuplicate(
      title = transaction.title,
      amountPaise = transaction.amountPaise,
      category = transaction.category,
      type = transaction.type,
      minTime = minTime
    )

    if (recentDuplicates.isNotEmpty()) {
      // Duplicate copy detected! Abort to prevent duplicate copies
      return@withContext false
    }

    val newTx = transaction.copy(
      syncStatus = "OFFLINE_COMMITTED",
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )
    // Write directly to local Room database (pure offline persistence)
    transactionDao.insert(newTx)
    true
  }

  suspend fun purgeDuplicates(): Int = withContext(Dispatchers.IO) {
    val allList = transactionDao.getAllActiveList()
    val duplicatesToRemove = mutableListOf<String>()
    val seenSignatures = HashSet<String>()

    // Sort by dateTime ASC to keep the oldest original copy and discard duplicates
    val sorted = allList.sortedBy { it.dateTime }
    for (tx in sorted) {
      // Signature based on title, amount, category, type, and rounded timestamp (within 60s)
      val timeWindowBucket = tx.dateTime / 60000L
      val signature = "${tx.title.trim().lowercase()}_${tx.amountPaise}_${tx.category}_${tx.type}_$timeWindowBucket"
      if (!seenSignatures.add(signature)) {
        duplicatesToRemove.add(tx.id)
      }
    }

    if (duplicatesToRemove.isNotEmpty()) {
      transactionDao.hardDeleteBatch(duplicatesToRemove)
    }
    duplicatesToRemove.size
  }

  suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
    val updatedTx = transaction.copy(
      syncStatus = "OFFLINE_COMMITTED",
      version = transaction.version + 1,
      updatedAt = System.currentTimeMillis()
    )
    transactionDao.update(updatedTx)
  }

  suspend fun deleteTransaction(id: String) = withContext(Dispatchers.IO) {
    transactionDao.hardDelete(id)
  }

  suspend fun setBudget(totalLimitPaise: Long, alertThresholdPercent: Int = 80) = withContext(Dispatchers.IO) {
    val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    val budget = BudgetEntity(
      id = monthKey,
      monthKey = monthKey,
      totalLimitPaise = totalLimitPaise,
      alertThresholdPercent = alertThresholdPercent
    )
    budgetDao.insertOrUpdate(budget)
  }

  suspend fun addCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
    categoryDao.insert(category)
  }
}
