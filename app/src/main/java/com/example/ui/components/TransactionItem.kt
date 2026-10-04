package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCard
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisHoloBlue
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
  transaction: TransactionEntity,
  currency: String = "INR",
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isIncome = transaction.type == "INCOME"
  val categoryIcon = getCategoryIcon(transaction.category)
  val categoryColor = getCategoryColor(transaction.category)

  val dateFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
  val dateStr = dateFormatter.format(Date(transaction.dateTime))

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("transaction_item_${transaction.id}")
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .border(1.dp, JarvisCardBorder, RoundedCornerShape(14.dp)),
    color = JarvisCard
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Category Avatar with high-tech glow
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(categoryColor.copy(alpha = 0.15f))
          .border(1.dp, categoryColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = categoryIcon,
          contentDescription = transaction.category,
          tint = categoryColor,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Title & Metadata
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = transaction.title,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Payment Method Pill
          Surface(
            color = Color(0xFF1E2D4A),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = transaction.paymentMethod,
              color = JarvisHoloBlue,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
          }

          Text(
            text = dateStr,
            color = TextMuted,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Amount & Sync Status
      Column(horizontalAlignment = Alignment.End) {
        val formattedAmount = CurrencyHelper.formatPaise(transaction.amountPaise, currency)
        val displayAmount = if (isIncome) "+$formattedAmount" else "-$formattedAmount"
        val amountColor = if (isIncome) JarvisGreen else TextPrimary

        Text(
          text = displayAmount,
          color = amountColor,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Tiny sync indicator icon
        Row(verticalAlignment = Alignment.CenterVertically) {
          val (syncColor, syncText) = when (transaction.syncStatus) {
            "SYNCED" -> Pair(JarvisGreen, "Synced")
            "SYNCING" -> Pair(JarvisCyan, "Syncing")
            "FAILED" -> Pair(JarvisRed, "Failed")
            else -> Pair(JarvisAmber, "Queued")
          }
          Box(
            modifier = Modifier
              .size(5.dp)
              .clip(CircleShape)
              .background(syncColor)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = syncText,
            color = syncColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}

fun getCategoryIcon(category: String): ImageVector {
  val cat = category.lowercase(Locale.ROOT)
  return when {
    cat.contains("food") || cat.contains("dining") -> Icons.Default.Restaurant
    cat.contains("transport") || cat.contains("fuel") -> Icons.Default.DirectionsCar
    cat.contains("shopping") || cat.contains("gear") -> Icons.Default.ShoppingBag
    cat.contains("entertainment") -> Icons.Default.Movie
    cat.contains("bill") || cat.contains("utility") -> Icons.Default.Bolt
    cat.contains("health") || cat.contains("armor") -> Icons.Default.Favorite
    cat.contains("invest") -> Icons.Default.TrendingUp
    cat.contains("salary") || cat.contains("influx") -> Icons.Default.AccountBalanceWallet
    else -> Icons.Default.Category
  }
}

fun getCategoryColor(category: String): Color {
  val cat = category.lowercase(Locale.ROOT)
  return when {
    cat.contains("food") || cat.contains("dining") -> JarvisCyan
    cat.contains("transport") || cat.contains("fuel") -> JarvisHoloBlue
    cat.contains("shopping") || cat.contains("gear") -> Color(0xFFFFB703)
    cat.contains("entertainment") -> Color(0xFFFF6B4A)
    cat.contains("bill") || cat.contains("utility") -> JarvisGreen
    cat.contains("health") || cat.contains("armor") -> JarvisRed
    cat.contains("invest") -> Color(0xFFB388FF)
    cat.contains("salary") || cat.contains("influx") -> JarvisCyan
    else -> Color(0xFF90A4AE)
  }
}
