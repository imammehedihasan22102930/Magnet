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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan

@Composable
fun MeasurementBadge(
  symbol: String,
  value: String,
  unit: String,
  label: String,
  modifier: Modifier = Modifier,
  accentColor: Color = ElectricCyan
) {
  Box(
    modifier = modifier
      .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
      .background(DarkSurfaceVariant.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
      .padding(horizontal = 12.dp, vertical = 8.dp)
      .testTag("measurement_${symbol.replace("\\s+".toRegex(), "_")}")
  ) {
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        fontSize = 11.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = symbol,
          fontWeight = FontWeight.Bold,
          color = accentColor,
          fontSize = 14.sp,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = " = ",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 13.sp
        )
        Text(
          text = value,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          fontSize = 14.sp,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = unit,
          color = accentColor.copy(alpha = 0.9f),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
fun EquationCard(
  title: String,
  formula: String,
  description: String,
  assumptions: String? = null,
  accentColor: Color = ElectricCyan,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("equation_card_${title.replace("\\s+".toRegex(), "_")}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = accentColor,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "SI Law",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
          .padding(vertical = 8.dp, horizontal = 12.dp)
      ) {
        Text(
          text = formula,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 17.sp
      )
      if (!assumptions.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Assumptions: $assumptions",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
          fontSize = 11.sp
        )
      }
    }
  }
}
