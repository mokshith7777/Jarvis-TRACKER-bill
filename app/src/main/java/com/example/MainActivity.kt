package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.TransactionEntity
import com.example.ui.JarvisExpenseViewModel
import com.example.ui.components.TransactionDetailDialog
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SyncDiagnosticsScreen
import com.example.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      JarvisTheme {
        JarvisApp()
      }
    }
  }
}

@Composable
fun JarvisApp(
  viewModel: JarvisExpenseViewModel = viewModel()
) {
  val navController = rememberNavController()
  var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
  val currency by viewModel.currency.collectAsStateWithLifecycle()

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = "dashboard",
      modifier = Modifier.padding(innerPadding)
    ) {
      composable("dashboard") {
        DashboardScreen(
          viewModel = viewModel,
          onNavigateToAdd = { navController.navigate("add_expense") },
          onNavigateToHistory = { navController.navigate("history") },
          onNavigateToAnalytics = { navController.navigate("analytics") },
          onNavigateToSyncDiagnostics = { navController.navigate("sync_diagnostics") },
          onNavigateToSettings = { navController.navigate("settings") },
          onTransactionClick = { tx -> selectedTransaction = tx }
        )
      }

      composable("add_expense") {
        AddExpenseScreen(
          viewModel = viewModel,
          onNavigateBack = { navController.popBackStack() }
        )
      }

      composable("history") {
        HistoryScreen(
          viewModel = viewModel,
          onNavigateBack = { navController.popBackStack() },
          onTransactionClick = { tx -> selectedTransaction = tx }
        )
      }

      composable("analytics") {
        AnalyticsScreen(
          viewModel = viewModel,
          onNavigateBack = { navController.popBackStack() }
        )
      }

      composable("sync_diagnostics") {
        SyncDiagnosticsScreen(
          viewModel = viewModel,
          onNavigateBack = { navController.popBackStack() }
        )
      }

      composable("settings") {
        SettingsScreen(
          viewModel = viewModel,
          onNavigateBack = { navController.popBackStack() }
        )
      }
    }

    // Modal Transaction Details & Actions Dialog
    selectedTransaction?.let { tx ->
      TransactionDetailDialog(
        transaction = tx,
        currency = currency,
        onDismiss = { selectedTransaction = null },
        onDelete = { id ->
          viewModel.deleteTransaction(id)
          selectedTransaction = null
        },
        onUpdate = { updated ->
          viewModel.updateTransaction(updated)
          selectedTransaction = null
        }
      )
    }
  }
}
