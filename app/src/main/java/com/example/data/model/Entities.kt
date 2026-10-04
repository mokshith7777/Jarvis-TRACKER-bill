package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val userId: String = "stark_user_001",
  val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
  val title: String,
  val amountPaise: Long, // Minor currency units (e.g. paise / cents)
  val currency: String = "INR",
  val category: String,
  val paymentMethod: String = "UPI", // UPI, Credit Card, Debit Card, Cash, Net Banking, Crypto
  val dateTime: Long = System.currentTimeMillis(),
  val note: String? = null,
  val receiptUri: String? = null,
  val isDeleted: Boolean = false, // Soft delete tombstone for resilient sync
  val syncStatus: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
  val version: Long = 1L,
  val deviceId: String = "jarvis_mk_device",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey val id: String,
  val name: String,
  val iconName: String,
  val colorHex: String,
  val budgetPaise: Long = 0L,
  val isIncome: Boolean = false
)

@Entity(tableName = "budgets")
data class BudgetEntity(
  @PrimaryKey val id: String, // e.g. "2026-10"
  val monthKey: String,
  val totalLimitPaise: Long,
  val alertThresholdPercent: Int = 80 // alert at 80% utilization
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
  @PrimaryKey val operationId: String = UUID.randomUUID().toString(),
  val entityType: String = "TRANSACTION", // TRANSACTION, BUDGET, CATEGORY
  val entityId: String,
  val operationType: String, // CREATE, UPDATE, DELETE
  val payloadJson: String,
  val status: String = "PENDING", // PENDING, SYNCING, COMPLETED, FAILED
  val retryCount: Int = 0,
  val nextRetryAt: Long = 0L,
  val createdAt: Long = System.currentTimeMillis(),
  val lastError: String? = null
)
