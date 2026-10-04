package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ExpenseRepository
import com.example.data.JarvisDatabase
import com.example.data.NetworkMode
import com.example.data.SyncEngine
import com.example.data.SyncEngineState
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.util.CurrencyHelper
import com.example.util.JarvisAiAdvisor
import com.example.util.JarvisHealthReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardSummary(
  val totalExpensePaise: Long = 0L,
  val totalIncomePaise: Long = 0L,
  val netBalancePaise: Long = 0L,
  val budgetLimitPaise: Long = 6000000L,
  val budgetPercent: Float = 0f,
  val remainingBudgetPaise: Long = 6000000L,
  val transactionsCount: Int = 0,
  val healthReport: JarvisHealthReport? = null,
  val offlineVaultStatus: String = "AIR-GAPPED OFFLINE VAULT ACTIVE"
)

data class UiFilterState(
  val searchQuery: String = "",
  val selectedCategory: String? = null,
  val selectedType: String? = null, // "ALL", "EXPENSE", "INCOME"
  val dateRange: String = "This Month" // "Today", "This Week", "This Month", "All"
)

class JarvisExpenseViewModel(application: Application) : AndroidViewModel(application) {
  private val database = JarvisDatabase.getDatabase(application, viewModelScope)
  val repository = ExpenseRepository(
    database.transactionDao(),
    database.categoryDao(),
    database.budgetDao(),
    database.syncQueueDao()
  )
  val syncEngine = SyncEngine(
    database.syncQueueDao(),
    database.transactionDao(),
    viewModelScope
  )

  // Strictly deduplicated transactions flow (guarantees zero duplicate copies displayed)
  val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
    .map { list ->
      list.distinctBy { it.id }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val currentBudget: StateFlow<BudgetEntity?> = repository.getCurrentMonthBudget()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val syncState: StateFlow<SyncEngineState> = syncEngine.state

  private val _currency = MutableStateFlow("INR")
  val currency: StateFlow<String> = _currency.asStateFlow()

  private val _filterState = MutableStateFlow(UiFilterState())
  val filterState: StateFlow<UiFilterState> = _filterState.asStateFlow()

  private val _jarvisFeedback = MutableStateFlow<String?>("JARVIS Core v4.2 Online • 100% Offline Enclave Nominal.")
  val jarvisFeedback: StateFlow<String?> = _jarvisFeedback.asStateFlow()

  // Local Storage Permission State & Dialog trigger
  private val _isStoragePermissionGranted = MutableStateFlow(false)
  val isStoragePermissionGranted: StateFlow<Boolean> = _isStoragePermissionGranted.asStateFlow()

  private val _showStoragePermissionDialog = MutableStateFlow(false)
  val showStoragePermissionDialog: StateFlow<Boolean> = _showStoragePermissionDialog.asStateFlow()

  fun setStoragePermissionGranted(granted: Boolean) {
    _isStoragePermissionGranted.value = granted
    if (granted) {
      _jarvisFeedback.value = "Storage clearance confirmed. Offline audit and receipt attachments active."
    }
  }

  fun openStoragePermissionDialog() {
    _showStoragePermissionDialog.value = true
  }

  fun dismissStoragePermissionDialog() {
    _showStoragePermissionDialog.value = false
  }

  // Filtered transactions for History / Ledger screen
  val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
    allTransactions,
    _filterState
  ) { list, filter ->
    val now = Calendar.getInstance()
    val startOfToday = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val startOfWeek = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val startOfMonth = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    list.filter { tx ->
      val matchesSearch = filter.searchQuery.isBlank() ||
        tx.title.contains(filter.searchQuery, ignoreCase = true) ||
        tx.category.contains(filter.searchQuery, ignoreCase = true) ||
        (tx.note?.contains(filter.searchQuery, ignoreCase = true) == true) ||
        tx.paymentMethod.contains(filter.searchQuery, ignoreCase = true)

      val matchesCategory = filter.selectedCategory == null || tx.category == filter.selectedCategory
      val matchesType = filter.selectedType == null || filter.selectedType == "ALL" || tx.type == filter.selectedType

      val matchesDate = when (filter.dateRange) {
        "Today" -> tx.dateTime >= startOfToday
        "This Week" -> tx.dateTime >= startOfWeek
        "This Month" -> tx.dateTime >= startOfMonth
        else -> true
      }

      matchesSearch && matchesCategory && matchesType && matchesDate
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dashboard summary
  val dashboardSummary: StateFlow<DashboardSummary> = combine(
    allTransactions,
    currentBudget,
    _currency
  ) { transactions, budget, curr ->
    val startOfMonth = Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val monthTransactions = transactions.filter { it.dateTime >= startOfMonth }
    val totalExpense = monthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amountPaise }
    val totalIncome = monthTransactions.filter { it.type == "INCOME" }.sumOf { it.amountPaise }
    val netBalance = totalIncome - totalExpense
    val limit = budget?.totalLimitPaise ?: 6000000L
    val percent = if (limit > 0) (totalExpense.toFloat() / limit.toFloat()).coerceIn(0f, 1.5f) else 0f
    val remaining = (limit - totalExpense).coerceAtLeast(0L)

    val health = JarvisAiAdvisor.analyzeFinances(monthTransactions, limit, curr)

    DashboardSummary(
      totalExpensePaise = totalExpense,
      totalIncomePaise = totalIncome,
      netBalancePaise = netBalance,
      budgetLimitPaise = limit,
      budgetPercent = percent,
      remainingBudgetPaise = remaining,
      transactionsCount = monthTransactions.size,
      healthReport = health,
      offlineVaultStatus = "AIR-GAPPED OFFLINE VAULT ACTIVE"
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

  fun setCurrency(newCurrency: String) {
    _currency.value = newCurrency
  }

  fun updateSearchQuery(query: String) {
    _filterState.update { it.copy(searchQuery = query) }
  }

  fun setCategoryFilter(category: String?) {
    _filterState.update { it.copy(selectedCategory = category) }
  }

  fun setTypeFilter(type: String?) {
    _filterState.update { it.copy(selectedType = type) }
  }

  fun setDateRangeFilter(range: String) {
    _filterState.update { it.copy(dateRange = range) }
  }

  fun dismissJarvisFeedback() {
    _jarvisFeedback.value = null
  }

  fun purgeDuplicates() {
    viewModelScope.launch {
      val purgedCount = repository.purgeDuplicates()
      if (purgedCount > 0) {
        _jarvisFeedback.value = "Duplicate Purge Complete: Removed $purgedCount duplicate copies from database."
      } else {
        _jarvisFeedback.value = "Duplicate Scan Complete: Database verified pristine with zero duplicate copies."
      }
    }
  }

  fun addTransaction(
    title: String,
    amountPaise: Long,
    category: String,
    type: String = "EXPENSE",
    paymentMethod: String = "UPI",
    note: String? = null,
    dateTime: Long = System.currentTimeMillis(),
    receiptUri: String? = null,
    onResult: ((Boolean) -> Unit)? = null
  ) {
    viewModelScope.launch {
      val tx = TransactionEntity(
        title = title,
        amountPaise = amountPaise,
        currency = _currency.value,
        type = type,
        category = category,
        paymentMethod = paymentMethod,
        note = note,
        receiptUri = receiptUri,
        dateTime = dateTime
      )
      val inserted = repository.addTransaction(tx)
      if (inserted) {
        val formatted = CurrencyHelper.formatPaise(amountPaise, _currency.value)
        _jarvisFeedback.value = "Committed $formatted for '$title' to offline vault. Zero duplicates guaranteed."
        onResult?.invoke(true)
      } else {
        _jarvisFeedback.value = "Duplicate prevented: Identical record was just logged."
        onResult?.invoke(false)
      }
    }
  }

  fun updateTransaction(tx: TransactionEntity) {
    viewModelScope.launch {
      repository.updateTransaction(tx)
      _jarvisFeedback.value = "Record '${tx.title}' updated in local database."
    }
  }

  fun deleteTransaction(id: String) {
    viewModelScope.launch {
      repository.deleteTransaction(id)
      _jarvisFeedback.value = "Transaction hard deleted from local database."
    }
  }

  fun updateBudget(limitPaise: Long, alertThresholdPercent: Int = 80) {
    viewModelScope.launch {
      repository.setBudget(limitPaise, alertThresholdPercent)
      val formatted = CurrencyHelper.formatPaise(limitPaise, _currency.value)
      _jarvisFeedback.value = "Monthly budget ceiling calibrated to $formatted (Threshold: $alertThresholdPercent%)."
    }
  }

  fun parseAndAddNaturalLanguage(prompt: String): Boolean {
    val parsed = JarvisAiAdvisor.parseNaturalLanguage(prompt) ?: return false
    addTransaction(
      title = parsed.title,
      amountPaise = parsed.amountPaise,
      category = parsed.category,
      type = parsed.type,
      paymentMethod = parsed.paymentMethod,
      note = "Parsed via Jarvis Neural Command"
    )
    return true
  }
}
