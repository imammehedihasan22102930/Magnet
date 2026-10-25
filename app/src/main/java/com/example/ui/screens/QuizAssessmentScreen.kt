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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EducationalContentRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InductionGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizAssessmentScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val userSelections = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> selectedOptionIndex
  val questions = EducationalContentRepository.QUIZ_QUESTIONS

  val correctCount = userSelections.entries.count { (qIdx, optIdx) ->
    questions.getOrNull(qIdx)?.correctOptionIndex == optIdx
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Learning Assessment & Quiz", fontWeight = FontWeight.Bold)
            Text("Score: $correctCount / ${questions.size} Correct", style = MaterialTheme.typography.bodySmall, color = InductionGreen)
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("quiz_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = { userSelections.clear() },
            modifier = Modifier.testTag("quiz_reset_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset Quiz")
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
        .testTag("quiz_questions_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      itemsIndexed(questions, key = { _, q -> q.id }) { index, q ->
        val selectedOption = userSelections[index]
        val isAnswered = selectedOption != null
        val isCorrect = selectedOption == q.correctOptionIndex

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_$index"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "QUESTION ${index + 1} • ${q.category.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold
              )
              if (isAnswered) {
                Text(
                  text = if (isCorrect) "CORRECT (+1)" else "INCORRECT",
                  color = if (isCorrect) InductionGreen else Color(0xFFFF5252),
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = q.question,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            q.options.forEachIndexed { optIdx, optText ->
              val isThisSelected = selectedOption == optIdx
              val isThisCorrect = optIdx == q.correctOptionIndex

              val borderColor = when {
                isAnswered && isThisCorrect -> InductionGreen
                isAnswered && isThisSelected -> Color(0xFFFF5252)
                isThisSelected -> ElectricCyan
                else -> Color(0xFF334155)
              }

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
                  .clickable { userSelections[index] = optIdx }
                  .padding(12.dp)
                  .testTag("question_${index}_option_$optIdx")
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (isThisSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = borderColor
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = optText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }

            if (isAnswered) {
              Spacer(modifier = Modifier.height(8.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(
                    if (isCorrect) InductionGreen.copy(alpha = 0.12f) else Color(0xFFFF5252).copy(alpha = 0.12f),
                    RoundedCornerShape(8.dp)
                  )
                  .padding(10.dp)
              ) {
                Column {
                  Text(
                    text = if (isCorrect) "Explanation:" else "Correct Answer Explanation:",
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) InductionGreen else Color(0xFFFF8A80),
                    fontSize = 12.sp
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = q.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 17.sp
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Governing Law: ${q.formulaHint}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
