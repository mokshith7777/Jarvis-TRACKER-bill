package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisExpenseViewModel
import com.example.ui.components.HudCard
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCard
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkNavy
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisHoloBlue
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyHelper
import java.util.Calendar

@Composable
fun AnalyticsScreen(
  viewModel: JarvisExpenseViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
  val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val currency by viewModel.currency.collectAsStateWithLifecycle()

  // Calculate category aggregates for current month
  val expenses = transactions.filter { it.type == "EXPENSE" }
  val totalExpensesPaise = expenses.sumOf { it.amountPaise }.coerceAtLeast(1L)

  val categoryTotals = expenses
    .groupBy { it.category }
    .mapValues { entry -> entry.value.sumOf { it.amountPaise } }
    .toList()
    .sortedByDescending { it.second }

  // Calculate daily spending for last 7 days for the velocity bar chart
  val cal = Calendar.getInstance()
  val daysList = (6 downTo 0).map { dayOffset ->
    val targetCal = Calendar.getInstance().apply {
      add(Calendar.DAY_OF_YEAR, -dayOffset)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val startMs = targetCal.timeInMillis
    val endMs = startMs + (24 * 60 * 60 * 1000L)
    val dayName = when (targetCal.get(Calendar.DAY_OF_WEEK)) {
      Calendar.SUNDAY -> "Sun"
      Calendar.MONDAY -> "Mon"
      Calendar.TUESDAY -> "Tue"
      Calendar.WEDNESDAY -> "Wed"
      Calendar.THURSDAY -> "Thu"
      Calendar.FRIDAY -> "Fri"
      Calendar.SATURDAY -> "Sat"
      else -> ""
    }
    val daySpent = expenses.filter { it.dateTime in startMs until endMs }.sumOf { it.amountPaise }
    Pair(dayName, daySpent)
  }

  val maxDaySpent = daysList.maxOfOrNull { it.second }?.coerceAtLeast(10000L) ?: 10000L

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(JarvisVoid)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Top Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("analytics_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = JarvisCyan
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "JARVIS ANALYTICS HUD",
            color = JarvisCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "TELEMETRIC BREAKDOWN & VELOCITY VECTORS",
            color = TextMuted,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Donut Chart Card (Category Distribution)
      HudCard(glowColor = JarvisCyan) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.PieChart, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "EXPENDITURE SEGMENTATION",
                color = JarvisCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
              )
            }
            Text(
              text = "TOTAL: ${CurrencyHelper.formatPaise(totalExpensesPaise, currency)}",
              color = TextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Canvas Donut Chart
          Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.size(180.dp)) {
              val center = Offset(size.width / 2, size.height / 2)
              val radius = (size.width / 2) - 16.dp.toPx()
              val strokeWidth = 22.dp.toPx()

              if (categoryTotals.isEmpty()) {
                drawCircle(
                  color = Color(0xFF1E2D4A),
                  radius = radius,
                  center = center,
                  style = Stroke(width = strokeWidth)
                )
              } else {
                var currentAngle = -90f
                categoryTotals.forEach { (catName, amount) ->
                  val sweep = (amount.toFloat() / totalExpensesPaise.toFloat()) * 360f
                  val color = getCategoryColor(catName)
                  drawArc(
                    color = color,
                    startAngle = currentAngle,
                    sweepAngle = sweep.coerceAtLeast(3f),
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                  )
                  currentAngle += sweep
                }
              }
            }

            // Center Telemetry
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "ALLOCATED",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
              Text(
                text = "${categoryTotals.size} CATS",
                color = JarvisCyan,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 7-Day Spending Velocity Bar Chart
      HudCard(glowColor = JarvisHoloBlue) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Speed, contentDescription = null, tint = JarvisHoloBlue, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "7-DAY OUTFLOW VELOCITY",
                color = JarvisHoloBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
              )
            }
            Text(
              text = "DAILY TREND",
              color = TextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Bar chart canvas
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp)
          ) {
            val barCount = daysList.size
            val availableWidth = size.width
            val barWidth = 24.dp.toPx()
            val spacing = (availableWidth - (barWidth * barCount)) / (barCount + 1)
            val chartHeight = size.height - 24.dp.toPx()

            daysList.forEachIndexed { index, pair ->
              val amount = pair.second
              val barHeight = ((amount.toFloat() / maxDaySpent.toFloat()) * chartHeight).coerceAtLeast(4.dp.toPx())
              val x = spacing + (index * (barWidth + spacing))
              val y = chartHeight - barHeight

              // Bar background track
              drawRoundRect(
                color = Color(0xFF13223E),
                topLeft = Offset(x, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
              )

              // Active bar
              drawRoundRect(
                color = if (index == barCount - 1) JarvisCyan else JarvisHoloBlue,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
              )
            }
          }

          // Bar Labels
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            daysList.forEachIndexed { index, pair ->
              Text(
                text = pair.first,
                color = if (index == daysList.size - 1) JarvisCyan else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Tactical Health & Burn Metrics
      if (summary.healthReport != null) {
        val report = summary.healthReport!!
        HudCard(glowColor = JarvisGold) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = JarvisGold, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "JARVIS TACTICAL DIAGNOSTICS",
                color = JarvisGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = "CURRENT DAILY BURN", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = CurrencyHelper.formatPaise(report.dailyBurnPaise, currency),
                  color = TextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(text = "TARGET DAILY CEILING", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = CurrencyHelper.formatPaise(report.targetDailyBurnPaise, currency),
                  color = JarvisGreen,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = "PROJECTED MONTH END", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = CurrencyHelper.formatPaise(report.projectedMonthEndPaise, currency),
                  color = if (report.projectedMonthEndPaise > summary.budgetLimitPaise) JarvisRed else JarvisCyan,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(text = "DOMINANT OUTFLOW", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = report.topCategory ?: "None",
                  color = JarvisAmber,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Category Leaderboard
      Text(
        text = "CATEGORY LEADERBOARD",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categoryTotals.forEach { (catName, amount) ->
          val fraction = (amount.toFloat() / totalExpensesPaise.toFloat()).coerceIn(0f, 1f)
          val percentInt = (fraction * 100).toInt()
          val catColor = getCategoryColor(catName)
          val catIcon = getCategoryIcon(catName)

          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp)),
            color = JarvisCard
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(catIcon, contentDescription = null, tint = catColor, modifier = Modifier.size(16.dp))
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = catName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = CurrencyHelper.formatPaise(amount, currency),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "($percentInt%)",
                    color = catColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp)),
                color = catColor,
                trackColor = Color(0xFF16233B)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
