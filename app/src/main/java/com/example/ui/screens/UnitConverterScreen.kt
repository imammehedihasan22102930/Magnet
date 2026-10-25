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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhysicsConstants
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MagneticAmber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var bTeslaInput by remember { mutableStateOf("1.0") }
  var fluxWeberInput by remember { mutableStateOf("0.05") }
  var freqGhzInput by remember { mutableStateOf("2.4") }
  var chargeMicroCInput by remember { mutableStateOf("5.0") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Units & Dimensional Analysis", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("converter_back_button")
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
        .testTag("converter_content"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Magnetic Flux Density (Tesla <-> Gauss)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Magnetic Flux Density (B)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MagneticAmber
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = bTeslaInput,
              onValueChange = { bTeslaInput = it },
              label = { Text("Tesla (T)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MagneticAmber)
            )
            val tVal = bTeslaInput.toDoubleOrNull() ?: 0.0
            Spacer(modifier = Modifier.height(8.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                .padding(10.dp)
            ) {
              Column {
                Text(
                  text = "• In Gauss (G): ${String.format(Locale.US, "%.2f G", tVal * 10000.0)} (1 T = 10,000 G)",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "• In microTesla (µT): ${String.format(Locale.US, "%.1f µT", tVal * 1e6)}",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      // 2. Frequency <-> Wavelength in Vacuum
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "EM Wave Frequency ⟷ Wavelength (c = f · λ)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = ElectricCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = freqGhzInput,
              onValueChange = { freqGhzInput = it },
              label = { Text("Frequency (GHz)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricCyan)
            )
            val fGhz = freqGhzInput.toDoubleOrNull() ?: 1.0
            val fHz = fGhz * 1e9
            val lambdaM = if (fHz > 0) PhysicsConstants.C / fHz else 0.0
            Spacer(modifier = Modifier.height(8.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                .padding(10.dp)
            ) {
              Column {
                Text(
                  text = "• Wavelength λ: ${String.format(Locale.US, "%.4f m", lambdaM)} (${String.format(Locale.US, "%.2f cm", lambdaM * 100.0)})",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "• Speed: 299,792,458 m/s (c in vacuum)",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      // 3. Dimensional Analysis & SI Base Units Breakdown Table
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "SI Base Units Dimensional Analysis",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            val baseUnitTable = listOf(
              Triple("Volt (V)", "J/C = W/A", "kg·m²·s⁻³·A⁻¹"),
              Triple("Tesla (T)", "N/(A·m) = Wb/m²", "kg·s⁻²·A⁻¹"),
              Triple("Weber (Wb)", "V·s = T·m²", "kg·m²·s⁻²·A⁻¹"),
              Triple("Henry (H)", "Wb/A = V·s/A", "kg·m²·s⁻²·A⁻²"),
              Triple("Farad (F)", "C/V = s/Ω", "kg⁻¹·m⁻²·s⁴·A²"),
              Triple("Ohm (Ω)", "V/A", "kg·m²·s⁻³·A⁻²"),
              Triple("Coulomb (C)", "A·s", "A·s"),
              Triple("Permittivity ε_0", "F/m", "kg⁻¹·m⁻³·s⁴·A²"),
              Triple("Permeability µ_0", "H/m = N/A²", "kg·m·s⁻²·A⁻²")
            )

            baseUnitTable.forEach { (name, derived, siBase) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = name,
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = derived,
                  style = MaterialTheme.typography.bodySmall,
                  color = ElectricCyan,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = siBase,
                  style = MaterialTheme.typography.bodySmall,
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
}
