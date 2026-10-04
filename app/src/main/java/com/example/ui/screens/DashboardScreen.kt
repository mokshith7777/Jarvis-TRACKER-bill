package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.JarvisExpenseViewModel
import com.example.ui.components.ArcReactorGauge
import com.example.ui.components.HudCard
import com.example.ui.components.JarvisStatusBanner
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.TransactionItem
import com.example.ui.components.getCategoryColor
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCard
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkNavy
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisNeonBlue
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisHoloBlue
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyHelper

@Composable
fun DashboardScreen(
  viewModel: JarvisExpenseViewModel,
  onNavigateToAdd: () -> Unit,
  onNavigateToHistory: () -> Unit,
  onNavigateToAnalytics: () -> Unit,
  onNavigateToSyncDiagnostics: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onTransactionClick: (TransactionEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
  val syncState by viewModel.syncState.collectAsStateWithLifecycle()
  val currency by viewModel.currency.collectAsStateWithLifecycle()
  val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val jarvisFeedback by viewModel.jarvisFeedback.collectAsStateWithLifecycle()

  var naturalCommandText by remember { mutableStateOf("") }
  val keyboardController = LocalSoftwareKeyboardController.current
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val drawerScope = rememberCoroutineScope()

  fun closeDrawer() {
    drawerScope.launch { drawerState.close() }
  }

  val recentTransactions = transactions.take(4)

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = Color(0xF20A100D),
        drawerContentColor = TextPrimary
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 20.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(JarvisRed.copy(alpha = 0.14f))
                .border(1.dp, JarvisRed.copy(alpha = 0.55f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = JarvisRed, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("JARVIS TRACKER", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
              Text("COMMAND CENTER", color = JarvisGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text("NAVIGATION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))

          NavigationDrawerItem(
            label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
            selected = true,
            onClick = { closeDrawer() },
            icon = { Icon(Icons.Default.Shield, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(
              selectedContainerColor = JarvisRed.copy(alpha = 0.18f),
              selectedIconColor = JarvisRed,
              selectedTextColor = JarvisRed,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextPrimary
            ),
            modifier = Modifier.padding(vertical = 2.dp)
          )
          NavigationDrawerItem(
            label = { Text("Add Expense", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = { closeDrawer(); onNavigateToAdd() },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = JarvisGreen, unselectedTextColor = TextPrimary),
            modifier = Modifier.padding(vertical = 2.dp)
          )
          NavigationDrawerItem(
            label = { Text("History", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = { closeDrawer(); onNavigateToHistory() },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = JarvisNeonBlue, unselectedTextColor = TextPrimary),
            modifier = Modifier.padding(vertical = 2.dp)
          )
          NavigationDrawerItem(
            label = { Text("Analytics", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = { closeDrawer(); onNavigateToAnalytics() },
            icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = JarvisNeonBlue, unselectedTextColor = TextPrimary),
            modifier = Modifier.padding(vertical = 2.dp)
          )
          NavigationDrawerItem(
            label = { Text("Sync Diagnostics", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = { closeDrawer(); onNavigateToSyncDiagnostics() },
            icon = { Icon(Icons.Default.Sync, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = JarvisGreen, unselectedTextColor = TextPrimary),
            modifier = Modifier.padding(vertical = 2.dp)
          )
          NavigationDrawerItem(
            label = { Text("Settings", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = { closeDrawer(); onNavigateToSettings() },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = JarvisRed, unselectedTextColor = TextPrimary),
            modifier = Modifier.padding(vertical = 2.dp)
          )

          Spacer(modifier = Modifier.weight(1f))

          HudCard(glowColor = JarvisGreen, hasTopGlow = true) {
            Column {
              Text("SYSTEM STATUS", color = JarvisGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Text("LOCAL LEDGER ONLINE", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              Text("JARVIS CORE READY", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
          }
        }
      }
    }
  ) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(JarvisVoid)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
        // Top HUD Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(JarvisCyan.copy(alpha = 0.15f))
                .border(1.dp, JarvisCyan.copy(alpha = 0.4f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Jarvis Reactor",
                tint = JarvisCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "JARVIS FINANCE",
                color = JarvisCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "MARK-LXXXV EXPENSE PROTOCOL",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            SyncStatusBadge(
              syncStatus = syncState.status,
              pendingCount = syncState.pendingCount,
              onClick = onNavigateToSyncDiagnostics
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
              onClick = { drawerScope.launch { drawerState.open() } },
              modifier = Modifier
                .size(36.dp)
                .testTag("sidebar_button")
            ) {
              Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open navigation",
                tint = JarvisRed,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }

      // Jarvis Status Banner
      if (jarvisFeedback != null) {
        item {
          JarvisStatusBanner(
            message = jarvisFeedback!!,
            onDismiss = { viewModel.dismissJarvisFeedback() }
          )
        }
      }

      // Arc Reactor Budget Telemetry Card
      item {
        HudCard(
          glowColor = JarvisCyan,
          onClick = onNavigateToAnalytics,
          modifier = Modifier.testTag("budget_arc_card")
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.FlashOn,
                  contentDescription = null,
                  tint = JarvisCyan,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "ARC REACTOR FUEL & BUDGET",
                  color = JarvisCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              }
              Text(
                text = "DETAILS >",
                color = JarvisHoloBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val percentInt = (summary.budgetPercent * 100).toInt()
            val remainingFormatted = CurrencyHelper.formatPaise(summary.remainingBudgetPaise, currency)
            ArcReactorGauge(
              progressPercent = summary.budgetPercent,
              primaryLabel = "$percentInt%",
              subLabel = "REMAINING: $remainingFormatted"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sub metric row inside Arc card
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C1425), RoundedCornerShape(10.dp))
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = "MONTHLY CEILING", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = CurrencyHelper.formatPaise(summary.budgetLimitPaise, currency),
                  color = TextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(text = "TOTAL EXPENDITURE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = CurrencyHelper.formatPaise(summary.totalExpensePaise, currency),
                  color = JarvisRed,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }

      // Financial Metrics Row: Inflows, Outflows, Net Balance
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Inflows Card
          HudCard(
            modifier = Modifier.weight(1f),
            glowColor = JarvisGreen,
            hasTopGlow = true
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.ArrowDownward,
                  contentDescription = "Inflow",
                  tint = JarvisGreen,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "INFLOWS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = CurrencyHelper.formatPaise(summary.totalIncomePaise, currency),
                color = JarvisGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          // Net Balance Card
          HudCard(
            modifier = Modifier.weight(1f),
            glowColor = JarvisHoloBlue,
            hasTopGlow = true
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = "Net",
                  tint = JarvisHoloBlue,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "NET RUNWAY", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.height(6.dp))
              val netColor = if (summary.netBalancePaise >= 0) JarvisCyan else JarvisRed
              Text(
                text = CurrencyHelper.formatPaise(summary.netBalancePaise, currency),
                color = netColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      // Jarvis Neural Command Quick-Input Bar
      item {
        HudCard(glowColor = JarvisCyan) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = JarvisCyan,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "JARVIS NEURAL EXPENSE PARSER",
                color = JarvisCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = naturalCommandText,
                onValueChange = { naturalCommandText = it },
                placeholder = {
                  Text(
                    text = "e.g. Spent 450 on Uber with UPI",
                    color = TextMuted,
                    fontSize = 12.sp
                  )
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("natural_expense_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                  onSend = {
                    if (naturalCommandText.isNotBlank()) {
                      val success = viewModel.parseAndAddNaturalLanguage(naturalCommandText)
                      if (success) {
                        naturalCommandText = ""
                        keyboardController?.hide()
                      }
                    }
                  }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = JarvisCyan,
                  unfocusedBorderColor = JarvisCardBorder,
                  focusedContainerColor = Color(0xFF0C1425),
                  unfocusedContainerColor = Color(0xFF0C1425),
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
              )

              Spacer(modifier = Modifier.width(8.dp))

              Surface(
                modifier = Modifier
                  .size(48.dp)
                  .testTag("execute_natural_command_btn")
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    if (naturalCommandText.isNotBlank()) {
                      val success = viewModel.parseAndAddNaturalLanguage(naturalCommandText)
                      if (success) {
                        naturalCommandText = ""
                        keyboardController?.hide()
                      }
                    }
                  }
                  .background(JarvisCyan),
                color = JarvisCyan
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Execute Command",
                    tint = JarvisVoid,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Jarvis Health Diagnostics Report Card
      if (summary.healthReport != null) {
        item {
          val report = summary.healthReport!!
          HudCard(
            glowColor = if (report.healthScore > 80) JarvisGreen else JarvisAmber,
            onClick = onNavigateToAnalytics
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = JarvisGold,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = report.statusTitle,
                    color = if (report.healthScore > 80) JarvisGreen else JarvisAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
                Text(
                  text = "SCORE: ${report.healthScore}/100",
                  color = TextPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = report.aiRecommendation,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
              )
            }
          }
        }
      }

      // Recent Transactions Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = JarvisCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "RECENT LEDGER ENTRIES",
              color = TextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
          }
          TextButton(
            onClick = onNavigateToHistory,
            modifier = Modifier.testTag("view_all_history_btn")
          ) {
            Text(
              text = "VIEW ALL (${transactions.size})",
              color = JarvisCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Recent Transactions List
      if (recentTransactions.isEmpty()) {
        item {
          HudCard {
            Text(
              text = "No expenses logged yet. Tap '+' or speak to Jarvis above to record your first expense.",
              color = TextMuted,
              fontSize = 12.sp,
              modifier = Modifier.padding(8.dp)
            )
          }
        }
      } else {
        items(recentTransactions, key = { it.id }) { tx ->
          TransactionItem(
            transaction = tx,
            currency = currency,
            onClick = { onTransactionClick(tx) }
          )
        }
      }

      // Bottom Spacer for FAB
      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }

    // Floating Action Button for Add Expense
    FloatingActionButton(
      onClick = onNavigateToAdd,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(24.dp)
        .testTag("add_expense_fab"),
      containerColor = JarvisCyan,
      contentColor = JarvisVoid
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Log Expense",
        modifier = Modifier.size(28.dp)
      )
    }
  }
  }
}
