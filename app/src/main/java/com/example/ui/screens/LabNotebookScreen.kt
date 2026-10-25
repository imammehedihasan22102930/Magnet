package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LabRecord
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabNotebookScreen(
  records: List<LabRecord>,
  onDeleteRecord: (String) -> Unit,
  onClearAll: () -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US)

  fun exportToCsv() {
    val sb = StringBuilder()
    sb.append("Experiment Title,Timestamp,Parameters,Measurements,Notes\n")
    for (r in records) {
      val paramStr = r.parameterSettings.entries.joinToString("; ") { "${it.key}=${it.value}" }
      val measStr = r.measuredValues.entries.joinToString("; ") { "${it.key}=${it.value}" }
      sb.append("\"${r.experimentTitle}\",\"${dateFormat.format(Date(r.timestampEpochMs))}\",\"$paramStr\",\"$measStr\",\"${r.notes}\"\n")
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Lab_Report_CSV", sb.toString())
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Exported ${records.size} records to CSV in clipboard!", Toast.LENGTH_SHORT).show()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Lab Notebook & Saved Reports", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("notebook_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (records.isNotEmpty()) {
            IconButton(
              onClick = { exportToCsv() },
              modifier = Modifier.testTag("export_csv_button")
            ) {
              Icon(Icons.Default.Share, contentDescription = "Export CSV", tint = ElectricCyan)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
      )
    },
    containerColor = DarkBackground,
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("notebook_records_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${records.size} Saved Laboratory Measurements",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Simulated experiment data with parameters & telemetry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            if (records.isNotEmpty()) {
              Button(
                onClick = { exportToCsv() },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
              ) {
                Text("Export CSV", color = Color.Black, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      if (records.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No saved measurements yet.\nUse the Bookmark button in any virtual lab to record live experiment observations.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }

      items(records, key = { it.id }) { record ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("record_card_${record.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = record.experimentTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
              )
              IconButton(onClick = { onDeleteRecord(record.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
              }
            }

            Text(
              text = dateFormat.format(Date(record.timestampEpochMs)),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Parameter Settings Table
            Text(
              text = "Physical Settings:",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                .padding(8.dp)
            ) {
              Column {
                record.parameterSettings.forEach { (k, v) ->
                  Text(
                    text = "• $k: $v",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Measured Simulation Values Table
            Text(
              text = "Measured Simulation Results:",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFFB300)
            )
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                .padding(8.dp)
            ) {
              Column {
                record.measuredValues.forEach { (k, v) ->
                  Text(
                    text = "• $k: $v",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFFD54F)
                  )
                }
              }
            }

            if (record.notes.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Notes: ${record.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}
