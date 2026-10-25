package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.EducationalContentRepository
import com.example.model.GuidedExperiment
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InductionGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedExperimentsScreen(
  onNavigateBack: () -> Unit,
  onLaunchLab: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedExperiment by remember { mutableStateOf<GuidedExperiment?>(null) }
  var userPredictionIndex by remember { mutableIntStateOf(-1) }
  var isPredictionSubmitted by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = selectedExperiment?.code ?: "Guided Experiments",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(
            onClick = {
              if (selectedExperiment != null) {
                selectedExperiment = null
                userPredictionIndex = -1
                isPredictionSubmitted = false
              } else {
                onNavigateBack()
              }
            },
            modifier = Modifier.testTag("guided_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
      )
    },
    containerColor = DarkBackground,
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    val exp = selectedExperiment
    if (exp == null) {
      // List of all Guided Experiments A through H
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .testTag("experiments_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(EducationalContentRepository.GUIDED_EXPERIMENTS, key = { it.id }) { item ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                selectedExperiment = item
                userPredictionIndex = -1
                isPredictionSubmitted = false
              }
              .testTag("exp_card_${item.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = item.code,
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = item.objective,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    } else {
      // Detailed Guided Experiment Workflow
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .testTag("experiment_detail_${exp.id}"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Title & Objective Card
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = exp.code + ": " + exp.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Objective: ${exp.objective}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = exp.theory,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
              )
            }
          }
        }

        // Procedure Steps Card
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Experimental Procedure Steps",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(8.dp))
              exp.procedureSteps.forEachIndexed { idx, step ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                  Text(
                    text = "${idx + 1}. ",
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                  )
                  Text(
                    text = step,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }

        // Hypothesis & Prediction Card (Answers hidden until user predicts!)
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Step 1: Formulate Your Hypothesis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB300)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = exp.predictionPrompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(10.dp))

              exp.predictionOptions.forEachIndexed { oIdx, opt ->
                val isSelected = userPredictionIndex == oIdx
                val isCorrect = oIdx == exp.correctPredictionIndex
                val optBorderColor = when {
                  isPredictionSubmitted && isCorrect -> InductionGreen
                  isPredictionSubmitted && isSelected -> Color(0xFFFF5252)
                  isSelected -> ElectricCyan
                  else -> Color(0xFF334155)
                }

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.5.dp, optBorderColor, RoundedCornerShape(8.dp))
                    .clickable(enabled = !isPredictionSubmitted) { userPredictionIndex = oIdx }
                    .padding(12.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                      contentDescription = null,
                      tint = optBorderColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = opt,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              if (!isPredictionSubmitted) {
                Button(
                  onClick = { isPredictionSubmitted = true },
                  enabled = userPredictionIndex >= 0,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_prediction_button"),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                ) {
                  Text("Lock In Prediction & Reveal Physics", color = Color.Black, fontWeight = FontWeight.Bold)
                }
              } else {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(
                      if (userPredictionIndex == exp.correctPredictionIndex) InductionGreen.copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f),
                      RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
                ) {
                  Column {
                    Text(
                      text = if (userPredictionIndex == exp.correctPredictionIndex) "✅ Prediction Confirmed!" else "💡 Theoretical Explanation:",
                      fontWeight = FontWeight.Bold,
                      color = if (userPredictionIndex == exp.correctPredictionIndex) InductionGreen else Color(0xFFFF8A80)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = exp.predictionExplanation,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        }

        // Live Simulation Launch Action
        item {
          Button(
            onClick = { onLaunchLab(exp.targetLabScreen) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("launch_experiment_lab_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Launch Lab", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Interactive Simulation Workspace", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
