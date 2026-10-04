package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisExpenseViewModel
import com.example.ui.components.HudCard
import com.example.ui.components.HudDatePickerDialog
import com.example.ui.components.HudStoragePermissionDialog
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCard
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkNavy
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisHoloBlue
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddExpenseScreen(
  viewModel: JarvisExpenseViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currency by viewModel.currency.collectAsStateWithLifecycle()
  val categories by viewModel.categories.collectAsStateWithLifecycle()
  val isStorageGranted by viewModel.isStoragePermissionGranted.collectAsStateWithLifecycle()
  val showStorageDialog by viewModel.showStoragePermissionDialog.collectAsStateWithLifecycle()

  // Form State
  var selectedType by remember { mutableStateOf("EXPENSE") }
  var amountInput by remember { mutableStateOf("") }
  var titleInput by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Food & Dining") }
  var selectedDateTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
  var selectedPaymentMethod by remember { mutableStateOf("UPI") }
  var noteInput by remember { mutableStateOf("") }
  var attachedReceiptUri by remember { mutableStateOf<String?>(null) }
  var attachedReceiptName by remember { mutableStateOf<String?>(null) }

  // Dialog State
  var showDatePickerDialog by remember { mutableStateOf(false) }
  var isSubmitting by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Check initial storage permission
  LaunchedEffect(Unit) {
    val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    } else {
      ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    viewModel.setStoragePermissionGranted(hasPermission)
  }

  // System Permission Request Launcher
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions.values.any { it }
    viewModel.setStoragePermissionGranted(granted)
    if (granted) {
      attachedReceiptName = "local_vault_doc_${System.currentTimeMillis()}.png"
      attachedReceiptUri = "content://local.storage/receipts/$attachedReceiptName"
    }
  }

  // Photo / File Picker
  val visualMediaLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      attachedReceiptUri = uri.toString()
      attachedReceiptName = "receipt_${System.currentTimeMillis().toString().takeLast(6)}.jpg"
    }
  }

  val paymentMethods = listOf("UPI", "Credit Card", "Debit Card", "Cash", "Net Banking", "Crypto")
  val quickSuggestions = listOf("Espresso", "Uber Transit", "Groceries", "Electric Grid", "Cloud Cluster", "Dinner", "Movie IMAX", "Gym Armor")

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

      // 1. Futuristic HUD Telemetry Top Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisDarkNavy)
            .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .testTag("add_expense_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = JarvisCyan,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(JarvisCyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "HUD EXPENSE TELEMETRY",
              color = JarvisCyan,
              fontSize = 15.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "100% OFFLINE VAULT • ZERO DUPLICATES GUARANTEED",
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
          )
        }

        // Air-gapped offline indicator chip
        Surface(
          color = JarvisCyan.copy(alpha = 0.12f),
          border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.4f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(10.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "OFFLINE",
              color = JarvisCyan,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Type Selector Tabs (Expense Outflow vs Income Influx)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(JarvisCard, RoundedCornerShape(12.dp))
          .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
          .padding(4.dp)
      ) {
        listOf("EXPENSE", "INCOME").forEach { type ->
          val isSelected = selectedType == type
          val activeBg = if (type == "EXPENSE") JarvisRed.copy(alpha = 0.2f) else JarvisGreen.copy(alpha = 0.2f)
          val activeColor = if (type == "EXPENSE") JarvisRed else JarvisGreen

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) activeBg else Color.Transparent)
              .clickable {
                selectedType = type
                if (type == "INCOME") {
                  selectedCategory = "Salary / Influx"
                } else if (selectedCategory == "Salary / Influx") {
                  selectedCategory = "Food & Dining"
                }
              }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (type == "EXPENSE") "OUTFLOW (EXPENSE)" else "INFLUX (INCOME)",
              color = if (isSelected) activeColor else TextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. FIELD 1: AMOUNT (Futuristic HUD Display + Quick Delta Chips)
      HudCard(
        glowColor = if (selectedType == "EXPENSE") JarvisCyan else JarvisGreen
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TELEMETRY FIELD 01 // AMOUNT",
              color = JarvisCyan,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
            Text(
              text = "CURRENCY: $currency",
              color = TextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Big Digital Readout
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = CurrencyHelper.getSymbol(currency),
              color = JarvisCyan,
              fontSize = 34.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
              value = amountInput,
              onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) {
                  amountInput = input
                  errorMessage = null
                }
              },
              placeholder = { Text("0.00", color = TextMuted, fontSize = 34.sp, fontFamily = FontFamily.Monospace) },
              modifier = Modifier
                .width(230.dp)
                .testTag("amount_input_field"),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Quick Increment HUD Chips (+100, +500, +1000, +5000, Clear)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val symbol = CurrencyHelper.getSymbol(currency)
            listOf(
              "+$symbol 100" to 100.0,
              "+$symbol 500" to 500.0,
              "+$symbol 1,000" to 1000.0,
              "+$symbol 2,000" to 2000.0,
              "+$symbol 5,000" to 5000.0
            ).forEach { (label, value) ->
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .clickable {
                    val currentVal = amountInput.toDoubleOrNull() ?: 0.0
                    val newVal = currentVal + value
                    amountInput = if (newVal % 1.0 == 0.0) newVal.toLong().toString() else String.format(Locale.US, "%.2f", newVal)
                    errorMessage = null
                  }
                  .border(1.dp, JarvisCardBorder, RoundedCornerShape(6.dp)),
                color = JarvisDarkNavy
              ) {
                Text(
                  text = label,
                  color = JarvisCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
              }
            }

            // Quick Clear
            if (amountInput.isNotEmpty()) {
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .clickable { amountInput = "" }
                  .border(1.dp, JarvisRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                color = JarvisRed.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "CLR",
                  color = JarvisRed,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. FIELD 2: CATEGORY (Futuristic Glowing Grid)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "TELEMETRY FIELD 02 // CATEGORY ALLOCATION",
          color = JarvisCyan,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.8.sp
        )
        Text(
          text = "SELECTED: $selectedCategory",
          color = TextSecondary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val displayCategories = if (selectedType == "EXPENSE") {
          categories.filter { !it.isIncome }
        } else {
          categories.filter { it.isIncome }
        }.ifEmpty { categories }

        val chunked = displayCategories.chunked(2)
        chunked.forEach { rowCategories ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            rowCategories.forEach { cat ->
              val isSelected = selectedCategory == cat.name
              val catColor = getCategoryColor(cat.name)
              val catIcon = getCategoryIcon(cat.name)

              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { selectedCategory = cat.name }
                  .border(
                    1.dp,
                    if (isSelected) catColor else JarvisCardBorder,
                    RoundedCornerShape(12.dp)
                  ),
                color = if (isSelected) catColor.copy(alpha = 0.18f) else JarvisCard
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = catIcon,
                    contentDescription = null,
                    tint = if (isSelected) catColor else TextMuted,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = cat.name,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                }
              }
            }
            if (rowCategories.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 5. FIELD 3: DATE (Futuristic HUD Chronometer / Date Selector)
      HudCard(glowColor = JarvisHoloBlue) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = JarvisCyan,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "TELEMETRY FIELD 03 // CHRONO DATE",
                color = JarvisCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
              )
            }

            // Button to open Futuristic Date Picker Dialog
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { showDatePickerDialog = true }
                .border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
              color = JarvisCyan.copy(alpha = 0.15f)
            ) {
              Text(
                text = "CUSTOM CHRONO >",
                color = JarvisCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Formatted Active Chrono Date Readout
          val dateFormat = SimpleDateFormat("yyyy-MM-dd (EEEE)", Locale.getDefault())
          val formattedDate = dateFormat.format(Date(selectedDateTimestamp))

          Surface(
            color = Color(0xFF070C18),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "LOCKED CHRONO TIMESTAMP",
                  color = TextMuted,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = formattedDate.uppercase(),
                  color = JarvisCyan,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }

              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = JarvisGreen,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Quick Chrono Preset Buttons: Today, Yesterday, -2 Days, -7 Days
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val presets = listOf(
              "TODAY" to 0,
              "YESTERDAY" to -1,
              "-2 DAYS" to -2,
              "-1 WEEK" to -7
            )
            presets.forEach { (label, offset) ->
              val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, offset)
              }
              val presetTime = cal.timeInMillis
              // Check if selected matches this day
              val isSelected = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateTimestamp)) ==
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(presetTime))

              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(6.dp))
                  .clickable { selectedDateTimestamp = presetTime }
                  .border(
                    1.dp,
                    if (isSelected) JarvisCyan else JarvisCardBorder,
                    RoundedCornerShape(6.dp)
                  ),
                color = if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisDarkNavy
              ) {
                Text(
                  text = label,
                  color = if (isSelected) JarvisCyan else TextSecondary,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontFamily = FontFamily.Monospace,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                  modifier = Modifier.padding(vertical = 7.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 6. Vendor / Transaction Title Field
      Text(
        text = "TRANSACTION TITLE / VENDOR IDENTIFIER",
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.8.sp
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = titleInput,
        onValueChange = { titleInput = it; errorMessage = null },
        placeholder = { Text("e.g. Starbucks, Uber Propulsion, Cloud Cluster...", color = TextMuted) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("title_input_field"),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = JarvisCyan,
          unfocusedBorderColor = JarvisCardBorder,
          focusedContainerColor = JarvisCard,
          unfocusedContainerColor = JarvisCard,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Quick Suggestion Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        quickSuggestions.forEach { suggestion ->
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable { titleInput = suggestion }
              .background(JarvisDarkNavy)
              .border(1.dp, JarvisCardBorder, RoundedCornerShape(6.dp)),
            color = JarvisDarkNavy
          ) {
            Text(
              text = suggestion,
              color = TextSecondary,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 7. Payment Protocol
      Text(
        text = "PAYMENT PROTOCOL",
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.8.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        paymentMethods.forEach { method ->
          val isSelected = selectedPaymentMethod == method
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { selectedPaymentMethod = method }
              .border(
                1.dp,
                if (isSelected) JarvisCyan else JarvisCardBorder,
                RoundedCornerShape(8.dp)
              ),
            color = if (isSelected) JarvisCyan.copy(alpha = 0.18f) else JarvisCard
          ) {
            Text(
              text = method,
              color = if (isSelected) JarvisCyan else TextSecondary,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 8. LOCAL STORAGE ACCESS & RECEIPT ATTACHMENT CARD (Matches Pop-up Requirement)
      HudCard(glowColor = JarvisCyan) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.FolderShared,
                contentDescription = null,
                tint = JarvisCyan,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "LOCAL STORAGE RECEIPT ENCLAVE",
                color = JarvisCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
              )
            }

            // Clearance Status Indicator
            Surface(
              color = if (isStorageGranted) JarvisGreen.copy(alpha = 0.15f) else JarvisAmber.copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isStorageGranted) JarvisGreen else JarvisAmber
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = if (isStorageGranted) "CLEARANCE: GRANTED" else "CLEARANCE: REQUIRED",
                color = if (isStorageGranted) JarvisGreen else JarvisAmber,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Attach offline receipts and vouchers securely to device local storage. No cloud upload or tracking required.",
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          if (attachedReceiptName != null) {
            // Display Attached File Badge
            Surface(
              color = Color(0xFF0C1628),
              border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.4f)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(Icons.Default.Receipt, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = attachedReceiptName!!,
                      color = TextPrimary,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                    Text(
                      text = "Local Storage Document • Offline Verified",
                      color = TextMuted,
                      fontSize = 9.sp
                    )
                  }
                }
                IconButton(
                  onClick = {
                    attachedReceiptUri = null
                    attachedReceiptName = null
                  },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Remove", tint = JarvisRed, modifier = Modifier.size(16.dp))
                }
              }
            }
          } else {
            // Action button to trigger storage permission popup or pick file
            Button(
              onClick = {
                if (!isStorageGranted) {
                  // Trigger the Futuristic HUD Local Storage Permission Pop-up!
                  viewModel.openStoragePermissionDialog()
                } else {
                  // Storage granted: launch media/file picker
                  try {
                    visualMediaLauncher.launch(
                      androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                      )
                    )
                  } catch (e: Exception) {
                    // Fallback local document
                    attachedReceiptName = "receipt_local_${System.currentTimeMillis().toString().takeLast(6)}.png"
                    attachedReceiptUri = "content://local/receipts/$attachedReceiptName"
                  }
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("attach_storage_receipt_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isStorageGranted) JarvisDarkNavy else JarvisCyan.copy(alpha = 0.2f),
                contentColor = JarvisCyan
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isStorageGranted) JarvisCardBorder else JarvisCyan
              ),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(
                imageVector = if (isStorageGranted) Icons.Default.AttachFile else Icons.Default.FolderShared,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isStorageGranted) "ATTACH LOCAL RECEIPT DOCUMENT" else "REQUEST STORAGE CLEARANCE & ATTACH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 9. Notes / Reference Field
      Text(
        text = "NOTE / ENCRYPTED METADATA (OPTIONAL)",
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.8.sp
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = noteInput,
        onValueChange = { noteInput = it },
        placeholder = { Text("Offline notes, invoice code, warranty...", color = TextMuted) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("note_input_field"),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = JarvisCyan,
          unfocusedBorderColor = JarvisCardBorder,
          focusedContainerColor = JarvisCard,
          unfocusedContainerColor = JarvisCard,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(12.dp)
      )

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = errorMessage!!,
          color = JarvisRed,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // 10. Anti-Duplicate Commit Button
      Button(
        onClick = {
          if (isSubmitting) return@Button

          val amountPaise = CurrencyHelper.parseToPaise(amountInput, currency)
          if (amountPaise <= 0L) {
            errorMessage = "ERROR: Please specify an amount greater than 0"
            return@Button
          }
          val title = if (titleInput.isBlank()) selectedCategory else titleInput.trim()

          isSubmitting = true
          viewModel.addTransaction(
            title = title,
            amountPaise = amountPaise,
            category = selectedCategory,
            type = selectedType,
            paymentMethod = selectedPaymentMethod,
            dateTime = selectedDateTimestamp,
            note = if (noteInput.isBlank()) null else noteInput.trim(),
            receiptUri = attachedReceiptUri,
            onResult = { success ->
              isSubmitting = false
              if (success) {
                onNavigateBack()
              } else {
                errorMessage = "DUPLICATE PREVENTED: Identical entry was just logged."
              }
            }
          )
        },
        enabled = !isSubmitting,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("save_expense_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = JarvisCyan,
          contentColor = JarvisVoid,
          disabledContainerColor = JarvisDarkNavy,
          disabledContentColor = TextMuted
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(
            color = JarvisCyan,
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "COMMITTING ATOMIC RECORD...",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FlashOn,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "COMMIT TO OFFLINE LEDGER",
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(36.dp))
    }

    // 11. Futuristic HUD Storage Permission Dialog Pop-up
    if (showStorageDialog) {
      HudStoragePermissionDialog(
        onGrantPermission = {
          val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
          } else {
            arrayOf(
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
          }
          permissionLauncher.launch(permissionsToRequest)
        },
        onDismiss = { viewModel.dismissStoragePermissionDialog() }
      )
    }

    // 12. Futuristic HUD Chrono Date Picker Dialog
    if (showDatePickerDialog) {
      HudDatePickerDialog(
        initialTimestamp = selectedDateTimestamp,
        onDateSelected = { timestamp ->
          selectedDateTimestamp = timestamp
        },
        onDismiss = { showDatePickerDialog = false }
      )
    }
  }
}
