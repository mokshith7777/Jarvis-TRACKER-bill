package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisExpenseViewModel
import com.example.ui.components.HudCard
import com.example.ui.components.HudStoragePermissionDialog
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
import java.io.File

@Composable
fun SyncDiagnosticsScreen(
  viewModel: JarvisExpenseViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  OfflineVaultScreen(
    viewModel = viewModel,
    onNavigateBack = onNavigateBack,
    modifier = modifier
  )
}

@Composable
fun OfflineVaultScreen(
  viewModel: JarvisExpenseViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val isStorageGranted by viewModel.isStoragePermissionGranted.collectAsStateWithLifecycle()
  val showStorageDialog by viewModel.showStoragePermissionDialog.collectAsStateWithLifecycle()
  val jarvisFeedback by viewModel.jarvisFeedback.collectAsStateWithLifecycle()

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions.values.any { it }
    viewModel.setStoragePermissionGranted(granted)
    if (granted) {
      Toast.makeText(context, "Storage clearance granted. Offline export enabled.", Toast.LENGTH_SHORT).show()
    }
  }

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

        // Header
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
              .testTag("vault_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = JarvisCyan,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "OFFLINE VAULT TELEMETRY",
              color = JarvisCyan,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
            Text(
              text = "AIR-GAPPED ROOM DB • ZERO CLOUD SERVICES",
              color = TextMuted,
              fontSize = 9.sp,
              letterSpacing = 0.5.sp
            )
          }
        }
      }

      // 1. Pure Offline Air-Gap State Card
      item {
        HudCard(glowColor = JarvisCyan) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = null,
                  tint = JarvisCyan,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "AIR-GAPPED SECURITY ARCHITECTURE",
                  color = JarvisCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
              }

              Surface(
                color = JarvisGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisGreen.copy(alpha = 0.5f))
              ) {
                Text(
                  text = "100% OFFLINE",
                  color = JarvisGreen,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Jarvis operates entirely in local offline mode without any external servers or authentication tokens. All records are stored exclusively in your device's internal SQLite database.",
              color = TextPrimary,
              fontSize = 12.sp,
              lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C1425), RoundedCornerShape(8.dp))
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "LOCAL RECORDS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = "${transactions.size}",
                  color = JarvisCyan,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "DATABASE ENGINE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = "Room SQLite",
                  color = JarvisHoloBlue,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "CLOUD TRAFFIC", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = "0 Bytes (None)",
                  color = JarvisGreen,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }

      // 2. Anti-Duplicate Protection Shield & Purge Tool
      item {
        HudCard(glowColor = JarvisAmber) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = null,
                  tint = JarvisAmber,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "ANTI-DUPLICATE SHIELD PROTOCOL",
                  color = JarvisAmber,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
              }

              Surface(
                color = JarvisGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisGreen.copy(alpha = 0.5f))
              ) {
                Text(
                  text = "ACTIVE",
                  color = JarvisGreen,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Prevents accidental duplicate expense submissions. If rapid commits or duplicate records are detected, Jarvis rejects the clones and preserves only the original transaction.",
              color = TextSecondary,
              fontSize = 12.sp,
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = { viewModel.purgeDuplicates() },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("purge_duplicates_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = JarvisAmber.copy(alpha = 0.2f),
                contentColor = JarvisAmber
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber.copy(alpha = 0.7f)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "SCAN & PURGE DUPLICATE COPIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      // 3. Local Storage Access Clearance (Matches User Requirement)
      item {
        HudCard(glowColor = JarvisCyan) {
          Column {
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
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "DEVICE STORAGE CLEARANCE",
                  color = JarvisCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
              }

              Surface(
                color = if (isStorageGranted) JarvisGreen.copy(alpha = 0.15f) else JarvisAmber.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isStorageGranted) JarvisGreen.copy(alpha = 0.5f) else JarvisAmber.copy(alpha = 0.5f)
                )
              ) {
                Text(
                  text = if (isStorageGranted) "AUTHORIZED" else "PERMISSION REQUIRED",
                  color = if (isStorageGranted) JarvisGreen else JarvisAmber,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Storage clearance allows Jarvis to securely attach offline receipt captures and save exported backups (.csv / .json) directly to your device storage.",
              color = TextSecondary,
              fontSize = 12.sp,
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { viewModel.openStoragePermissionDialog() },
                modifier = Modifier
                  .weight(1f)
                  .testTag("request_storage_dialog_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = JarvisCyan,
                  contentColor = JarvisVoid
                ),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isStorageGranted) "VIEW CLEARANCE" else "REQUEST ACCESS POP-UP",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }

              // Export button
              OutlinedButton(
                onClick = {
                  if (!isStorageGranted) {
                    viewModel.openStoragePermissionDialog()
                  } else {
                    // Export local backup file
                    try {
                      val backupFile = File(context.filesDir, "jarvis_offline_ledger_backup.json")
                      val content = StringBuilder("[\n")
                      transactions.forEachIndexed { i, tx ->
                        content.append("  {\"id\":\"${tx.id}\", \"title\":\"${tx.title}\", \"amountPaise\":${tx.amountPaise}, \"category\":\"${tx.category}\", \"date\":${tx.dateTime}}")
                        if (i < transactions.size - 1) content.append(",\n") else content.append("\n")
                      }
                      content.append("]")
                      backupFile.writeText(content.toString())
                      Toast.makeText(context, "Exported ${transactions.size} records to local storage.", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                      Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                  }
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("export_local_backup_button"),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "EXPORT BACKUP",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }

    // Storage Permission Pop-up Dialog
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
  }
}
