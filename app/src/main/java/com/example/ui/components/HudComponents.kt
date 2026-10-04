package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCard
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkNavy
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisHoloBlue
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisNeonGreen
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HudCard(
  modifier: Modifier = Modifier,
  glowColor: Color = JarvisCyan.copy(alpha = 0.35f),
  hasTopGlow: Boolean = true,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val shape = RoundedCornerShape(16.dp)
  val clickableModifier = if (onClick != null) {
    modifier
      .clip(shape)
      .clickable { onClick() }
  } else {
    modifier
  }

  Box(
    modifier = clickableModifier
      .clip(shape)
      .background(
        Brush.linearGradient(
          colors = listOf(
            JarvisNeonGreen.copy(alpha = 0.12f),
            Color(0xFF06110C).copy(alpha = 0.86f),
            JarvisNeonGreen.copy(alpha = 0.06f)
          )
        )
      )
      .border(1.dp, JarvisNeonGreen.copy(alpha = 0.42f), shape)
  ) {
    Column {
      if (hasTopGlow) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(
              Brush.horizontalGradient(
                listOf(
                  Color.Transparent,
                  glowColor,
                  Color.Transparent
                )
              )
            )
        )
      }
      Box(modifier = Modifier.padding(16.dp)) {
        content()
      }
    }
  }
}

@Composable
fun ArcReactorGauge(
  progressPercent: Float, // 0.0 to 1.0 (or > 1.0 for breach)
  modifier: Modifier = Modifier,
  size: Dp = 190.dp,
  title: String = "MONTHLY BUDGET",
  primaryLabel: String = "42%",
  subLabel: String = "REMAINING: ₹34,800"
) {
  val animatedProgress = remember { Animatable(0f) }

  LaunchedEffect(progressPercent) {
    animatedProgress.animateTo(
      targetValue = progressPercent.coerceIn(0f, 1.25f),
      animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
  }

  val arcColor = when {
    progressPercent >= 1.0f -> JarvisRed
    progressPercent >= 0.8f -> JarvisAmber
    else -> JarvisCyan
  }

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val center = Offset(this.size.width / 2, this.size.height / 2)
      val radius = (this.size.width / 2) - 16.dp.toPx()

      // Outer tech dashed circle
      val tickCount = 36
      for (i in 0 until tickCount) {
        val angle = (i * (360f / tickCount)) * (PI / 180f)
        val outerRadius = radius + 10.dp.toPx()
        val innerRadius = radius + 4.dp.toPx()
        val start = Offset(
          center.x + (cos(angle) * innerRadius).toFloat(),
          center.y + (sin(angle) * innerRadius).toFloat()
        )
        val end = Offset(
          center.x + (cos(angle) * outerRadius).toFloat(),
          center.y + (sin(angle) * outerRadius).toFloat()
        )
        drawLine(
          color = if (i % 6 == 0) JarvisCyan.copy(alpha = 0.7f) else JarvisCardBorder,
          start = start,
          end = end,
          strokeWidth = if (i % 6 == 0) 2.dp.toPx() else 1.dp.toPx()
        )
      }

      // Background Track Arc (260 degree arc)
      val startAngle = 140f
      val sweepAngleTotal = 260f
      drawArc(
        color = Color(0xFF16233B),
        startAngle = startAngle,
        sweepAngle = sweepAngleTotal,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
      )

      // Active progress arc
      val activeSweep = (sweepAngleTotal * animatedProgress.value).coerceAtMost(sweepAngleTotal)
      drawArc(
        brush = Brush.sweepGradient(
          0.0f to JarvisHoloBlue,
          0.5f to arcColor,
          1.0f to arcColor
        ),
        startAngle = startAngle,
        sweepAngle = activeSweep,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
      )

      // Inner subtle glow circle
      drawCircle(
        color = arcColor.copy(alpha = 0.06f),
        radius = radius - 16.dp.toPx(),
        center = center
      )
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 24.dp)
    ) {
      Text(
        text = title,
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = primaryLabel,
        color = arcColor,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subLabel,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Composable
fun OfflineVaultBadge(
  modifier: Modifier = Modifier,
  isStorageGranted: Boolean = true,
  onClick: () -> Unit
) {
  Surface(
    modifier = modifier
      .testTag("offline_vault_badge")
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
    color = JarvisCyan.copy(alpha = 0.12f)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(JarvisGreen)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Offline Secure",
        tint = JarvisCyan,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = "OFFLINE VAULT",
        color = JarvisCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.5.sp
      )
    }
  }
}

@Composable
fun SyncStatusBadge(
  syncStatus: Any? = null,
  pendingCount: Int = 0,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  OfflineVaultBadge(
    modifier = modifier,
    onClick = onClick
  )
}

/**
 * Futuristic HUD Storage Permission Dialog Pop-up
 */
@Composable
fun HudStoragePermissionDialog(
  onGrantPermission: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(1.5.dp, JarvisCyan, RoundedCornerShape(16.dp))
        .testTag("hud_storage_permission_dialog"),
      color = JarvisDarkNavy
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Glowing Security / Storage Icon
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(JarvisCyan.copy(alpha = 0.15f))
            .border(1.5.dp, JarvisCyan.copy(alpha = 0.6f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.FolderShared,
            contentDescription = "Storage Clearance",
            tint = JarvisCyan,
            modifier = Modifier.size(30.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "CLEARANCE REQUIRED",
          color = JarvisCyan,
          fontSize = 15.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )

        Text(
          text = "LOCAL STORAGE ACCESS PERMISSION",
          color = TextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
          color = Color(0xFF070C18),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "• 100% Offline Architecture: Zero cloud servers, zero internet tracking.",
              color = TextSecondary,
              fontSize = 11.sp,
              lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "• Local Storage Protocol: Jarvis requests storage clearance to store receipt captures, attach local audit documents, and export encrypted ledger backups (.csv / .json) directly on your device.",
              color = TextSecondary,
              fontSize = 11.sp,
              lineHeight = 16.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .testTag("dismiss_storage_perm_btn"),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
          ) {
            Text("LATER", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              onGrantPermission()
              onDismiss()
            },
            modifier = Modifier
              .weight(1.3f)
              .testTag("grant_storage_perm_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = JarvisCyan,
              contentColor = JarvisVoid
            )
          ) {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("AUTHORIZE", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

/**
 * Futuristic HUD Chronometer & Date Picker Dialog
 */
@Composable
fun HudDatePickerDialog(
  initialTimestamp: Long,
  onDateSelected: (Long) -> Unit,
  onDismiss: () -> Unit
) {
  val cal = remember {
    Calendar.getInstance().apply { timeInMillis = initialTimestamp }
  }

  var selectedYear by remember { mutableIntStateOf(cal.get(Calendar.YEAR)) }
  var selectedMonth by remember { mutableIntStateOf(cal.get(Calendar.MONTH)) } // 0-11
  var selectedDay by remember { mutableIntStateOf(cal.get(Calendar.DAY_OF_MONTH)) }

  val monthNames = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

  // Days in selected month
  val daysInMonth = remember(selectedYear, selectedMonth) {
    val tempCal = Calendar.getInstance()
    tempCal.set(Calendar.YEAR, selectedYear)
    tempCal.set(Calendar.MONTH, selectedMonth)
    tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(1.5.dp, JarvisCyan, RoundedCornerShape(16.dp))
        .testTag("hud_date_picker_dialog"),
      color = JarvisDarkNavy
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "CHRONO TIMESTAMP PICKER",
              color = JarvisCyan,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Date Display
        Surface(
          color = Color(0xFF070C18),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          val formatted = String.format(Locale.US, "%04d-%02d-%02d // %s", selectedYear, selectedMonth + 1, selectedDay, monthNames[selectedMonth])
          Text(
            text = "LOCKED CHRONO: $formatted",
            color = JarvisCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Month & Year Navigator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              if (selectedMonth == 0) {
                selectedMonth = 11
                selectedYear -= 1
              } else {
                selectedMonth -= 1
              }
              if (selectedDay > 28) selectedDay = 1
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = JarvisCyan)
          }

          Text(
            text = "${monthNames[selectedMonth]} $selectedYear",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          IconButton(
            onClick = {
              if (selectedMonth == 11) {
                selectedMonth = 0
                selectedYear += 1
              } else {
                selectedMonth += 1
              }
              if (selectedDay > 28) selectedDay = 1
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = JarvisCyan)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Preset Chips (Today, Yesterday, -3 Days)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val presets = listOf(
            "TODAY" to 0,
            "YESTERDAY" to -1,
            "-3 DAYS" to -3,
            "-7 DAYS" to -7
          )
          presets.forEach { (label, dayOffset) ->
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                  val target = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, dayOffset)
                  }
                  selectedYear = target.get(Calendar.YEAR)
                  selectedMonth = target.get(Calendar.MONTH)
                  selectedDay = target.get(Calendar.DAY_OF_MONTH)
                }
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(6.dp)),
              color = Color(0xFF0F1A2D)
            ) {
              Text(
                text = label,
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Day Matrix Grid (1 to daysInMonth)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          val allDays = (1..daysInMonth).toList()
          val dayRows = allDays.chunked(7)
          dayRows.forEach { rowDays ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              rowDays.forEach { dayNum ->
                val isSelected = selectedDay == dayNum
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) JarvisCyan else Color(0xFF0E182A))
                    .border(
                      1.dp,
                      if (isSelected) JarvisCyan else JarvisCardBorder.copy(alpha = 0.5f),
                      RoundedCornerShape(6.dp)
                    )
                    .clickable { selectedDay = dayNum },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = dayNum.toString(),
                    color = if (isSelected) JarvisVoid else TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
              // Fill remaining spaces in row
              for (k in 0 until (7 - rowDays.size)) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lock Date Button
        Button(
          onClick = {
            val confirmedCal = Calendar.getInstance().apply {
              set(Calendar.YEAR, selectedYear)
              set(Calendar.MONTH, selectedMonth)
              set(Calendar.DAY_OF_MONTH, selectedDay)
              set(Calendar.HOUR_OF_DAY, 12)
              set(Calendar.MINUTE, 0)
              set(Calendar.SECOND, 0)
            }
            onDateSelected(confirmedCal.timeInMillis)
            onDismiss()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("confirm_date_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = JarvisCyan,
            contentColor = JarvisVoid
          ),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "LOCK CHRONO TIMESTAMP",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp
          )
        }
      }
    }
  }
}

@Composable
fun JarvisStatusBanner(
  message: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, JarvisCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF0F1A2E)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(JarvisCyan.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Psychology,
          contentDescription = "JARVIS AI",
          tint = JarvisCyan,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "JARVIS INTEL",
          color = JarvisCyan,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Text(
          text = message,
          color = TextPrimary,
          fontSize = 12.sp,
          lineHeight = 16.sp
        )
      }
      IconButton(
        onClick = onDismiss,
        modifier = Modifier.size(24.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Dismiss",
          tint = TextMuted,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
