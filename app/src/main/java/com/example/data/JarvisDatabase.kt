package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.SyncQueueDao
import com.example.data.dao.TransactionDao
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.SyncQueueEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Database(
  entities = [
    TransactionEntity::class,
    CategoryEntity::class,
    BudgetEntity::class,
    SyncQueueEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
  abstract fun transactionDao(): TransactionDao
  abstract fun categoryDao(): CategoryDao
  abstract fun budgetDao(): BudgetDao
  abstract fun syncQueueDao(): SyncQueueDao

  companion object {
    @Volatile
    private var INSTANCE: JarvisDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): JarvisDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          JarvisDatabase::class.java,
          "jarvis_expense_db"
        )
          .addCallback(DatabaseCallback(scope))
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }
    }

    suspend fun populateInitialData(database: JarvisDatabase) {
      val defaultCategories = listOf(
        CategoryEntity("cat_food", "Food & Dining", "Restaurant", "#00E5FF", 1500000L),
        CategoryEntity("cat_transport", "Transport & Fuel", "DirectionsCar", "#00B4D8", 800000L),
        CategoryEntity("cat_shopping", "Shopping & Gear", "ShoppingBag", "#FFB703", 1200000L),
        CategoryEntity("cat_entertainment", "Entertainment", "Movie", "#FF6B4A", 600000L),
        CategoryEntity("cat_bills", "Bills & Tech Utilities", "Bolt", "#00E676", 1000000L),
        CategoryEntity("cat_health", "Health & Armor", "Favorite", "#FF3366", 500000L),
        CategoryEntity("cat_invest", "Stark Investments", "TrendingUp", "#9D4EDD", 2000000L),
        CategoryEntity("cat_salary", "Salary / Influx", "AccountBalanceWallet", "#00E5FF", 0L, isIncome = true),
        CategoryEntity("cat_other", "Miscellaneous", "Category", "#90A4AE", 400000L)
      )
      database.categoryDao().insertAll(defaultCategories)

      // Set default monthly budget for current month
      val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
      database.budgetDao().insertOrUpdate(
        BudgetEntity(
          id = monthKey,
          monthKey = monthKey,
          totalLimitPaise = 6000000L, // ₹60,000 budget in paise
          alertThresholdPercent = 80
        )
      )

      // Seed initial transactions for an engaging initial experience
      val now = System.currentTimeMillis()
      val day = 24 * 60 * 60 * 1000L

      val initialTransactions = listOf(
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          title = "Stark Labs Cloud Server Cluster",
          amountPaise = 245000L, // ₹2,450.00
          category = "Bills & Tech Utilities",
          paymentMethod = "Credit Card",
          dateTime = now - (3 * 3600 * 1000L),
          note = "AWS / Jarvis Neural compute instance",
          syncStatus = "SYNCED"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          title = "Espresso & Reactor Fuel",
          amountPaise = 38000L, // ₹380.00
          category = "Food & Dining",
          paymentMethod = "UPI",
          dateTime = now - (8 * 3600 * 1000L),
          note = "Morning fuel at Starbucks",
          syncStatus = "SYNCED"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          title = "Autonomous Repulsor Fuel",
          amountPaise = 180000L, // ₹1,800.00
          category = "Transport & Fuel",
          paymentMethod = "UPI",
          dateTime = now - (1 * day),
          note = "Electric vehicle charging & toll",
          syncStatus = "SYNCED"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          title = "Consulting & Retainer Influx",
          amountPaise = 7500000L, // ₹75,000.00 income
          type = "INCOME",
          category = "Salary / Influx",
          paymentMethod = "Net Banking",
          dateTime = now - (2 * day),
          note = "Monthly R&D stipend received",
          syncStatus = "SYNCED"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          title = "Holographic Display Adapter",
          amountPaise = 429900L, // ₹4,299.00
          category = "Shopping & Gear",
          paymentMethod = "Credit Card",
          dateTime = now - (3 * day),
          note = "Ultra high refresh 4K projector hub",
          syncStatus = "SYNCED"
        ),
        TransactionEntity(
          id = UUID.randomUUID().toString(),
          title = "IMAX Avengers Screening",
          amountPaise = 120000L, // ₹1,200.00
          category = "Entertainment",
          paymentMethod = "UPI",
          dateTime = now - (4 * day),
          note = "Tickets & refreshments",
          syncStatus = "SYNCED"
        )
      )
      database.transactionDao().insertAll(initialTransactions)
    }
  }
}
