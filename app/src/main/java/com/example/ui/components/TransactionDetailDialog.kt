package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransactionEntity
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCard
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkNavy
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailDialog(
  transaction: TransactionEntity,
  currency: String,
  onDismiss: () -> Unit,
  onDelete: (String) -> Unit,
  onUpdate: (TransactionEntity) -> Unit
) {
  var isEditing by remember { mutableStateOf(false) }
  var editTitle by remember { mutableStateOf(transaction.title) }
  var editNote by remember { mutableStateOf(transaction.note ?: "") }
  var showConfirmDelete by remember { mutableStateOf(false) }

  val dateFormatter = SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.getDefault())
  val dateStr = dateFormatter.format(Date(transaction.dateTime))

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
      color = JarvisCard
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        // Dialog Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "TRANSACTION TELEMETRY",
              color = JarvisCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (showConfirmDelete) {
          // Confirm Delete Tombstone Card
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF24101A), RoundedCornerShape(10.dp))
              .border(1.dp, JarvisRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
              .padding(14.dp)
          ) {
            Text(
              text = "CONFIRM SOFT DELETE",
              color = JarvisRed,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "This will mark '${transaction.title}' as tombstoned in local Room DB and dispatch a DELETE operation to the cloud sync queue.",
              color = TextPrimary,
              fontSize = 11.sp,
              lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  onDelete(transaction.id)
                  onDismiss()
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("confirm_delete_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisRed, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("CONFIRM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              OutlinedButton(
                onClick = { showConfirmDelete = false },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("CANCEL", color = TextSecondary, fontSize = 11.sp)
              }
            }
          }
        } else if (isEditing) {
          // Edit Fields
          Text(text = "TITLE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = editTitle,
            onValueChange = { editTitle = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = JarvisCyan,
              unfocusedBorderColor = JarvisCardBorder,
              focusedContainerColor = JarvisDarkNavy,
              unfocusedContainerColor = JarvisDarkNavy,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(8.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(text = "NOTE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = editNote,
            onValueChange = { editNote = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = JarvisCyan,
              unfocusedBorderColor = JarvisCardBorder,
              focusedContainerColor = JarvisDarkNavy,
              unfocusedContainerColor = JarvisDarkNavy,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(8.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = {
                val updated = transaction.copy(
                  title = editTitle.ifBlank { transaction.title },
                  note = editNote.ifBlank { null }
                )
                onUpdate(updated)
                isEditing = false
              },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("SAVE CHANGES", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            OutlinedButton(
              onClick = { isEditing = false },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("CANCEL", color = TextSecondary, fontSize = 11.sp)
            }
          }
        } else {
          // Read-only Details
          val isIncome = transaction.type == "INCOME"
          val formattedAmount = CurrencyHelper.formatPaise(transaction.amountPaise, currency)

          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = if (isIncome) "+$formattedAmount" else "-$formattedAmount",
              color = if (isIncome) JarvisGreen else TextPrimary,
              fontSize = 28.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = transaction.title,
              color = JarvisCyan,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Metadata Grid
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF0C1425), RoundedCornerShape(10.dp))
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            DetailRow("CATEGORY", transaction.category)
            DetailRow("PAYMENT PROTOCOL", transaction.paymentMethod)
            DetailRow("RECORD TIMESTAMP", dateStr)
            DetailRow("SYNC REPLICATION", transaction.syncStatus)
            DetailRow("ENTITY VERSION", "v${transaction.version} (Optimistic Locking)")
            DetailRow("SOURCE DEVICE", transaction.deviceId)
            if (!transaction.note.isNullOrBlank()) {
              DetailRow("NOTE", transaction.note)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Action buttons: Edit & Delete
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { isEditing = true },
              modifier = Modifier
                .weight(1f)
                .testTag("edit_tx_btn"),
              colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisVoid),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("EDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            OutlinedButton(
              onClick = { showConfirmDelete = true },
              modifier = Modifier
                .weight(1f)
                .testTag("delete_tx_btn"),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed),
              border = androidx.compose.foundation.BorderStroke(1.dp, JarvisRed.copy(alpha = 0.5f)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("DELETE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    Text(text = value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
  }
}
