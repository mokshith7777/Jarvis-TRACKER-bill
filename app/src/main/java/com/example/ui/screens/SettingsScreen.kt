package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisExpenseViewModel
import com.example.ui.components.HudCard
import com.example.ui.theme.JarvisCard
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisDarkNavy
import com.example.ui.theme.JarvisNeonGreen
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyHelper

@Composable
fun SettingsScreen(
  viewModel: JarvisExpenseViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currency by viewModel.currency.collectAsStateWithLifecycle()
  val budget by viewModel.currentBudget.collectAsStateWithLifecycle()

  var budgetInput by remember(budget) {
    val initialAmount = if (budget != null) (budget!!.totalLimitPaise / 100).toString() else "60000"
    mutableStateOf(initialAmount)
  }
  var thresholdSlider by remember(budget) {
    mutableFloatStateOf(budget?.alertThresholdPercent?.toFloat() ?: 80f)
  }
  var biometricEnabled by remember { mutableStateOf(true) }

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
          modifier = Modifier.testTag("settings_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = JarvisNeonRed
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "JARVIS CONFIGURATION",
            color = JarvisCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "SYSTEM PARAMETERS & FINANCIAL PROTOCOLS",
            color = TextMuted,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Currency Selection
      HudCard(glowColor = JarvisCyan) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "CURRENCY TELEMETRY",
              color = JarvisCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            CurrencyHelper.SUPPORTED_CURRENCIES.forEach { curr ->
              val isSelected = currency == curr
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .border(1.dp, if (isSelected) JarvisCyan else JarvisCardBorder, RoundedCornerShape(8.dp))
                  .clickable { viewModel.setCurrency(curr) },
                color = if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisDarkNavy
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = CurrencyHelper.getSymbol(curr),
                    color = if (isSelected) JarvisCyan else TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = curr,
                    color = if (isSelected) JarvisCyan else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Monthly Budget Allocation Card
      HudCard(glowColor = JarvisGreen) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = JarvisGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "MONTHLY BUDGET CEILING",
              color = JarvisGreen,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = budgetInput,
            onValueChange = { budgetInput = it.filter { ch -> ch.isDigit() } },
            label = { Text("Budget Limit (${CurrencyHelper.getSymbol(currency)})", color = TextMuted) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("budget_limit_input"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = JarvisCyan,
              unfocusedBorderColor = JarvisCardBorder,
              focusedContainerColor = JarvisDarkNavy,
              unfocusedContainerColor = JarvisDarkNavy,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "ALERT THRESHOLD", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
              text = "${thresholdSlider.toInt()}%",
              color = JarvisCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Slider(
            value = thresholdSlider,
            onValueChange = { thresholdSlider = it },
            valueRange = 50f..100f,
            colors = SliderDefaults.colors(
              thumbColor = JarvisCyan,
              activeTrackColor = JarvisCyan,
              inactiveTrackColor = Color(0xFF1E304E)
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              val major = budgetInput.toLongOrNull() ?: 60000L
              val paise = major * 100L
              viewModel.updateBudget(paise, thresholdSlider.toInt())
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("save_budget_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = JarvisCyan,
              contentColor = JarvisVoid
            ),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("CALIBRATE BUDGET", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Stark Biometric Gate
      HudCard(glowColor = JarvisCyan) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = "STARK ENCLAVE SECURITY", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Text(
                text = "Biometric gate & hardware keystore verification.",
                color = TextMuted,
                fontSize = 11.sp
              )
            }
          }
          Switch(
            checked = biometricEnabled,
            onCheckedChange = { biometricEnabled = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = JarvisCyan,
              checkedTrackColor = Color(0xFF003740)
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Anti-Duplicate & Local Storage Tools
      HudCard(glowColor = JarvisCyan) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "OFFLINE STORAGE & DUPLICATE PROTOCOLS",
              color = JarvisCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { viewModel.openStoragePermissionDialog() },
              modifier = Modifier
                .weight(1f)
                .testTag("settings_storage_clearance_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = JarvisCyan.copy(alpha = 0.2f),
                contentColor = JarvisCyan
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("STORAGE CLEARANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Button(
              onClick = { viewModel.purgeDuplicates() },
              modifier = Modifier
                .weight(1f)
                .testTag("settings_purge_duplicates_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = JarvisGreen.copy(alpha = 0.2f),
                contentColor = JarvisGreen
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, JarvisGreen.copy(alpha = 0.6f)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("PURGE DUPLICATES", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Architecture Specs
      HudCard(glowColor = TextMuted) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "JARVIS OFFLINE HUD ARCHITECTURE SPEC",
              color = TextSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "• 100% Offline: Room SQLite is the exclusive, air-gapped local runtime database.\n" +
              "• Zero Cloud Services: No external servers, no cloud queues, no tracking, no login required.\n" +
              "• Zero Duplicates: Atomic duplicate shield prevents repeated submissions.\n" +
              "• Device Storage Permission: Local clearance for receipts, invoices, and exported backup files.\n" +
              "• Minor units: Long paise representation for exact currency precision.",
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
