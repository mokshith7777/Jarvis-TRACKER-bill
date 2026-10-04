package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyHelper {
  val SUPPORTED_CURRENCIES = listOf("INR", "USD", "EUR", "GBP", "JPY")

  fun getSymbol(currency: String): String {
    return when (currency.uppercase(Locale.ROOT)) {
      "INR" -> "₹"
      "USD" -> "$"
      "EUR" -> "€"
      "GBP" -> "£"
      "JPY" -> "¥"
      else -> "₹"
    }
  }

  fun formatPaise(amountPaise: Long, currency: String = "INR"): String {
    val symbol = getSymbol(currency)
    val isNegative = amountPaise < 0
    val absPaise = Math.abs(amountPaise)

    if (currency.equals("JPY", ignoreCase = true)) {
      // JPY does not have minor decimal units
      val formatter = DecimalFormat("#,##0")
      val formatted = formatter.format(absPaise)
      return if (isNegative) "-$symbol$formatted" else "$symbol$formatted"
    }

    val major = absPaise / 100
    val minor = absPaise % 100

    val symbols = DecimalFormatSymbols(Locale.US).apply {
      groupingSeparator = ','
    }
    val formatter = DecimalFormat("#,##0", symbols)
    val majorFormatted = formatter.format(major)
    val result = "$symbol$majorFormatted.${minor.toString().padStart(2, '0')}"
    return if (isNegative) "-$result" else result
  }

  fun parseToPaise(input: String, currency: String = "INR"): Long {
    val clean = input.replace("[^0-9.]".toRegex(), "").trim()
    if (clean.isEmpty()) return 0L

    return try {
      val parts = clean.split(".")
      val major = parts[0].toLongOrNull() ?: 0L
      val minor = if (parts.size > 1) {
        val minorStr = parts[1].take(2).padEnd(2, '0')
        minorStr.toLongOrNull() ?: 0L
      } else {
        0L
      }
      (major * 100L) + minor
    } catch (e: Exception) {
      0L
    }
  }
}
