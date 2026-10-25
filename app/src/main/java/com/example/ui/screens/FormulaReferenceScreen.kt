package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EducationalContentRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MagneticAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaReferenceScreen(
  onNavigateBack: () -> Unit,
  onNavigateToLab: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredFormulas = remember(searchQuery) {
    if (searchQuery.isBlank()) {
      EducationalContentRepository.FORMULA_ENTRIES
    } else {
      EducationalContentRepository.FORMULA_ENTRIES.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
          it.category.contains(searchQuery, ignoreCase = true) ||
          it.symbolsDefinition.contains(searchQuery, ignoreCase = true) ||
          it.siUnits.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Formula Reference Library", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("formula_back_button")
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("formulas_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by law, symbol, or unit...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("formula_search_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElectricCyan,
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface
          )
        )
      }

      items(filteredFormulas, key = { it.id }) { formula ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("formula_card_${formula.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = formula.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold
              )
              Button(
                onClick = { onNavigateToLab(formula.targetLabScreen) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Simulate", modifier = Modifier.height(14.dp), tint = ElectricCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open Lab", fontSize = 11.sp, color = ElectricCyan)
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = formula.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mathematical Box
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF070B14), RoundedCornerShape(8.dp))
                .padding(10.dp)
            ) {
              Text(
                text = formula.expressionLatex,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "Symbols: ${formula.symbolsDefinition}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "SI Units: ${formula.siUnits}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium,
              color = MagneticAmber
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Interpretation: ${formula.physicalInterpretation}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                .padding(8.dp)
            ) {
              Text(
                text = "Example: ${formula.exampleCalculation}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }
  }
}
