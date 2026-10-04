package com.example.util

import com.example.data.model.TransactionEntity
import java.util.Calendar
import java.util.Locale

data class ParsedExpense(
  val title: String,
  val amountPaise: Long,
  val category: String,
  val paymentMethod: String,
  val type: String = "EXPENSE"
)

data class JarvisHealthReport(
  val healthScore: Int, // 0 - 100
  val statusTitle: String,
  val statusDescription: String,
  val dailyBurnPaise: Long,
  val targetDailyBurnPaise: Long,
  val projectedMonthEndPaise: Long,
  val topCategory: String?,
  val aiRecommendation: String
)

object JarvisAiAdvisor {

  fun parseNaturalLanguage(prompt: String): ParsedExpense? {
    val clean = prompt.trim()
    if (clean.isEmpty()) return null

    val lower = clean.lowercase(Locale.ROOT)
    val isIncome = lower.contains("salary") || lower.contains("income") ||
      lower.contains("bonus") || lower.contains("received") || lower.contains("refund")

    // Extract amount: numbers with optional decimals (e.g. 450, 450.50, 1,200)
    val amountRegex = Regex("(?:rs\\.?|inr|\\$|€|£)?\\s*([0-9]+(?:,[0-9]+)*(?:\\.[0-9]{1,2})?)")
    val match = amountRegex.findAll(lower).firstOrNull { it.groupValues[1].replace(",", "").toDoubleOrNull() != null }
    val amountNumberStr = match?.groupValues?.get(1)?.replace(",", "") ?: return null
    val amountPaise = CurrencyHelper.parseToPaise(amountNumberStr)

    // Detect payment method
    val paymentMethod = when {
      lower.contains("upi") || lower.contains("gpay") || lower.contains("phonepe") || lower.contains("paytm") -> "UPI"
      lower.contains("credit") || lower.contains("cc") -> "Credit Card"
      lower.contains("debit") || lower.contains("dc") -> "Debit Card"
      lower.contains("cash") -> "Cash"
      lower.contains("net banking") || lower.contains("bank") || lower.contains("transfer") -> "Net Banking"
      lower.contains("crypto") || lower.contains("bitcoin") || lower.contains("eth") -> "Crypto"
      else -> "UPI"
    }

    // Detect category
    val category = when {
      isIncome -> "Salary / Influx"
      lower.contains("fuel") || lower.contains("petrol") || lower.contains("diesel") || lower.contains("uber") || lower.contains("ola") || lower.contains("taxi") || lower.contains("metro") || lower.contains("bus") || lower.contains("train") || lower.contains("flight") -> "Transport & Fuel"
      lower.contains("food") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("breakfast") || lower.contains("coffee") || lower.contains("tea") || lower.contains("starbucks") || lower.contains("pizza") || lower.contains("burger") || lower.contains("restaurant") || lower.contains("swiggy") || lower.contains("zomato") || lower.contains("groceries") || lower.contains("grocery") -> "Food & Dining"
      lower.contains("movie") || lower.contains("cinema") || lower.contains("netflix") || lower.contains("spotify") || lower.contains("game") || lower.contains("steam") || lower.contains("concert") -> "Entertainment"
      lower.contains("amazon") || lower.contains("flipkart") || lower.contains("clothes") || lower.contains("shoes") || lower.contains("shopping") || lower.contains("electronics") || lower.contains("gadget") -> "Shopping & Gear"
      lower.contains("electric") || lower.contains("water") || lower.contains("wifi") || lower.contains("internet") || lower.contains("recharge") || lower.contains("bill") || lower.contains("rent") || lower.contains("cloud") || lower.contains("server") -> "Bills & Tech Utilities"
      lower.contains("doctor") || lower.contains("medicine") || lower.contains("pharmacy") || lower.contains("gym") || lower.contains("health") || lower.contains("hospital") -> "Health & Fitness"
      lower.contains("stock") || lower.contains("mutual fund") || lower.contains("shares") || lower.contains("invest") -> "Stark Investments"
      else -> "Miscellaneous"
    }

    // Extract title: remove keywords like "spent", "paid", amount, "on", "with", "via"
    var title = clean
      .replace(Regex("(?i)\\b(spent|paid|bought|received|got|for|on|with|via|using|rs\\.?|inr|\\$|€|£)\\b"), " ")
      .replace(amountNumberStr, " ")
      .replace(Regex("(?i)\\b(upi|credit card|debit card|cash|net banking)\\b"), " ")
      .replace(Regex("\\s+"), " ")
      .trim()

    if (title.isEmpty()) {
      title = if (isIncome) "Income Influx" else category
    }

    // Capitalize first letter of title
    title = title.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

    return ParsedExpense(
      title = title,
      amountPaise = amountPaise,
      category = category,
      paymentMethod = paymentMethod,
      type = if (isIncome) "INCOME" else "EXPENSE"
    )
  }

  fun analyzeFinances(
    transactions: List<TransactionEntity>,
    budgetLimitPaise: Long,
    currency: String = "INR"
  ): JarvisHealthReport {
    val cal = Calendar.getInstance()
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysRemaining = (maxDaysInMonth - dayOfMonth).coerceAtLeast(1)

    val currentMonthExpenses = transactions.filter { it.type == "EXPENSE" }
    val totalSpentPaise = currentMonthExpenses.sumOf { it.amountPaise }

    val dailyBurn = if (dayOfMonth > 0) totalSpentPaise / dayOfMonth else totalSpentPaise
    val budget = if (budgetLimitPaise > 0) budgetLimitPaise else 5000000L // 50,000 default if not set
    val remainingBudget = (budget - totalSpentPaise).coerceAtLeast(0L)
    val targetDailyBurn = remainingBudget / daysRemaining
    val projectedMonthEnd = totalSpentPaise + (dailyBurn * daysRemaining)

    val categoryBreakdown = currentMonthExpenses
      .groupBy { it.category }
      .mapValues { entry -> entry.value.sumOf { it.amountPaise } }
    val topCategory = categoryBreakdown.maxByOrNull { it.value }?.key

    val budgetUsagePercent = if (budget > 0) (totalSpentPaise.toDouble() / budget.toDouble()) * 100.0 else 0.0
    val expectedUsagePercent = (dayOfMonth.toDouble() / maxDaysInMonth.toDouble()) * 100.0

    val (score, status, desc, rec) = when {
      budgetUsagePercent > 100.0 -> {
        val overrun = CurrencyHelper.formatPaise(totalSpentPaise - budget, currency)
        Tuple4(
          35,
          "DEFCON 1: BUDGET BREACH",
          "Expenditures exceed authorized monthly threshold by $overrun.",
          "Emergency protocol engaged: Restrict discretionary spending immediately. Divert surplus income to cover deficit."
        )
      }
      budgetUsagePercent > expectedUsagePercent + 15.0 -> {
        Tuple4(
          65,
          "ELEVATED BURN VELOCITY",
          "Current velocity is running ${String.format(Locale.US, "%.1f", budgetUsagePercent - expectedUsagePercent)}% higher than scheduled calendar progression.",
          "Sir, I suggest limiting spending in $topCategory over the next $daysRemaining days to avoid monthly overrun."
        )
      }
      budgetUsagePercent < expectedUsagePercent - 10.0 -> {
        Tuple4(
          98,
          "EXEMPLARY EFFICIENCY",
          "Capital conservation is outstanding. Operating comfortably beneath allocation thresholds.",
          "Expenditure metrics are pristine, sir. Consider deploying surplus reserves into Stark Investments."
        )
      }
      else -> {
        Tuple4(
          88,
          "STARK PROTOCOL: OPTIMAL",
          "Financial telemetry within standard operating tolerance. Balance and burn rates are synchronized.",
          "All systems nominal. Projected month-end position aligns with target reserves."
        )
      }
    }

    return JarvisHealthReport(
      healthScore = score,
      statusTitle = status,
      statusDescription = desc,
      dailyBurnPaise = dailyBurn,
      targetDailyBurnPaise = targetDailyBurn,
      projectedMonthEndPaise = projectedMonthEnd,
      topCategory = topCategory,
      aiRecommendation = rec
    )
  }

  private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
