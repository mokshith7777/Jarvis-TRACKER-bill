package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.JarvisExpenseViewModel
import com.example.ui.components.HudCard
import com.example.ui.components.TransactionItem
import com.example.ui.theme.JarvisCard
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkNavy
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyHelper

@Composable
fun HistoryScreen(
  viewModel: JarvisExpenseViewModel,
  onNavigateBack: () -> Unit,
  onTransactionClick: (TransactionEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
  val filterState by viewModel.filterState.collectAsStateWithLifecycle()
  val currency by viewModel.currency.collectAsStateWithLifecycle()
  val categories by viewModel.categories.collectAsStateWithLifecycle()

  val totalFilteredPaise = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amountPaise }
  val totalFilteredIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amountPaise }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(JarvisVoid)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("history_back_button")
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
            text = "FINANCIAL LEDGER",
            color = JarvisCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "AIR-GAPPED ROOM DB • ZERO DUPLICATES PROTOCOL",
            color = TextMuted,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Field
      OutlinedTextField(
        value = filterState.searchQuery,
        onValueChange = { viewModel.updateSearchQuery(it) },
        placeholder = { Text("Search transactions, notes, vendors...", color = TextMuted, fontSize = 13.sp) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = JarvisCyan, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
          if (filterState.searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ledger_search_field"),
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

      Spacer(modifier = Modifier.height(10.dp))

      // Date Range Pills
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("This Month", "This Week", "Today", "All").forEach { range ->
          val isSelected = filterState.dateRange == range
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.setDateRangeFilter(range) },
            label = { Text(range, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = JarvisCyan.copy(alpha = 0.2f),
              selectedLabelColor = JarvisCyan,
              containerColor = JarvisCard,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = JarvisCardBorder,
              selectedBorderColor = JarvisCyan
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Type & Category Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Type filters
        listOf("ALL", "EXPENSE", "INCOME").forEach { type ->
          val isSelected = (filterState.selectedType == null && type == "ALL") || filterState.selectedType == type
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.setTypeFilter(if (type == "ALL") null else type) },
            label = { Text(type, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFF1E355B),
              selectedLabelColor = TextPrimary,
              containerColor = JarvisDarkNavy,
              labelColor = TextMuted
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = JarvisCardBorder,
              selectedBorderColor = JarvisCyan
            )
          )
        }

        // Category filter chips
        categories.forEach { cat ->
          val isSelected = filterState.selectedCategory == cat.name
          FilterChip(
            selected = isSelected,
            onClick = {
              viewModel.setCategoryFilter(if (isSelected) null else cat.name)
            },
            label = { Text(cat.name, fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = JarvisCyan.copy(alpha = 0.25f),
              selectedLabelColor = JarvisCyan,
              containerColor = JarvisDarkNavy,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = JarvisCardBorder,
              selectedBorderColor = JarvisCyan
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Summary Count & Total bar
      Surface(
        color = Color(0xFF0F1A2D),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${transactions.size} ENTRIES FOUND",
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "OUT: ${CurrencyHelper.formatPaise(totalFilteredPaise, currency)}",
            color = JarvisCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Transactions List
      if (transactions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
          contentAlignment = Alignment.TopCenter
        ) {
          HudCard {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(16.dp)
            ) {
              Text(
                text = "NO MATCHING RECORDS",
                color = JarvisCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Adjust your search parameters or filter criteria to inspect other telemetry.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
              )
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(transactions, key = { it.id }) { tx ->
            TransactionItem(
              transaction = tx,
              currency = currency,
              onClick = { onTransactionClick(tx) }
            )
          }
          item {
            Spacer(modifier = Modifier.height(24.dp))
          }
        }
      }
    }
  }
}
