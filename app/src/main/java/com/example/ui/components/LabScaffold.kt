package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabScaffold(
  title: String,
  subtitle: String,
  onNavigateBack: () -> Unit,
  onResetExperiment: () -> Unit,
  onSaveToNotebook: (() -> Unit)? = null,
  canvasContent: @Composable () -> Unit,
  measurementsContent: @Composable () -> Unit,
  controlsContent: @Composable ColumnScope.() -> Unit,
  theoryContent: @Composable ColumnScope.() -> Unit,
  modifier: Modifier = Modifier
) {
  var showTheoryDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1
            )
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 11.sp,
              maxLines = 1
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("lab_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Laboratories",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        actions = {
          if (onSaveToNotebook != null) {
            IconButton(
              onClick = onSaveToNotebook,
              modifier = Modifier.testTag("save_to_notebook_button")
            ) {
              Icon(
                imageVector = Icons.Default.BookmarkAdd,
                contentDescription = "Save Measurement to Notebook",
                tint = MaterialTheme.colorScheme.secondary
              )
            }
          }
          IconButton(
            onClick = { showTheoryDialog = !showTheoryDialog },
            modifier = Modifier.testTag("lab_theory_toggle_button")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Toggle Physics Theory",
              tint = if (showTheoryDialog) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(
            onClick = onResetExperiment,
            modifier = Modifier.testTag("lab_reset_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Reset Experiment",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = DarkSurface
        )
      )
    },
    containerColor = DarkBackground,
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
    ) {
      // 1. Simulation Canvas Area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp)
          .padding(8.dp)
      ) {
        canvasContent()
      }

      // 2. Telemetry and Live Measurements Area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp)
      ) {
        measurementsContent()
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Theory Panel (Expandable or default open if toggled)
      if (showTheoryDialog) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Physical Theory & Governing Equations",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = ElectricCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            theoryContent()
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      // 4. Parameter Controls Section
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Experimental Controls & Physical Parameters",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(12.dp))
          controlsContent()
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
