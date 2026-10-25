package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MagneticCircuitParams
import com.example.model.PhysicsConstants
import com.example.physics.MagneticPhysicsEngine
import com.example.ui.components.EquationCard
import com.example.ui.components.LabScaffold
import com.example.ui.components.MeasurementBadge
import com.example.ui.theme.CanvasDarkBg
import com.example.ui.theme.CanvasGridLine
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MagneticAmber
import com.example.ui.theme.MagneticOrange
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MagneticCircuitScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedMaterial by remember { mutableStateOf(PhysicsConstants.MAGNETIC_MATERIALS[4]) } // Mild Steel
  var materialDropdownExpanded by remember { mutableStateOf(false) }

  var pathLengthM by remember { mutableDoubleStateOf(0.30) }
  var crossSectionMm2 by remember { mutableDoubleStateOf(1600.0) } // 1600 mm^2 (40x40mm)
  var airGapLengthMm by remember { mutableDoubleStateOf(1.0) } // 1 mm
  var coilTurnsN by remember { mutableIntStateOf(600) }
  var currentAmperes by remember { mutableDoubleStateOf(2.5) }

  val circuitParams = remember(selectedMaterial, pathLengthM, crossSectionMm2, airGapLengthMm, coilTurnsN, currentAmperes) {
    MagneticCircuitParams(
      coreMaterial = selectedMaterial,
      pathLengthMeters = pathLengthM,
      crossSectionAreaSqM = crossSectionMm2 * 1e-6,
      turnsN = coilTurnsN,
      currentAmperes = currentAmperes,
      airGapLengthMeters = airGapLengthMm * 1e-3
    )
  }

  val circuitResult = remember(circuitParams) {
    MagneticPhysicsEngine.solveMagneticCircuit(circuitParams)
  }

  fun resetCircuit() {
    selectedMaterial = PhysicsConstants.MAGNETIC_MATERIALS[4]
    pathLengthM = 0.30
    crossSectionMm2 = 1600.0
    airGapLengthMm = 1.0
    coilTurnsN = 600
    currentAmperes = 2.5
  }

  LabScaffold(
    title = "Magnetic Materials & Circuits Lab",
    subtitle = "Lumped Reluctance, Air Gaps, MMF = N·I & B-H Saturation",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetCircuit() },
    onSaveToNotebook = {
      val settings = mapOf(
        "Core Material" to selectedMaterial.name,
        "Turns N" to "$coilTurnsN",
        "Current I" to "$currentAmperes A",
        "Air Gap" to "$airGapLengthMm mm",
        "Path Length" to "$pathLengthM m"
      )
      val measurements = mapOf(
        "MMF" to "${circuitParams.mmf} A-t",
        "Total Reluctance R" to String.format(Locale.US, "%.3e A-t/Wb", circuitResult.totalReluctance),
        "Flux Φ" to String.format(Locale.US, "%.4e Wb", circuitResult.magneticFluxWebers),
        "Core Flux Density B" to String.format(Locale.US, "%.3f T", circuitResult.coreFluxDensityTesla)
      )
      onSaveMeasurement(settings, measurements)
    },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("magnetic_circuit_canvas")
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)

          // Draw Magnetic Core (C-Core with air gap on right)
          val coreW = 160.dp.toPx()
          val coreH = 140.dp.toPx()
          val thick = 32.dp.toPx()

          val left = center.x - coreW / 2f
          val top = center.y - coreH / 2f
          val right = center.x + coreW / 2f
          val bottom = center.y + coreH / 2f

          val coreColor = if (circuitResult.isSaturated) Color(0xFFFF5252) else Color(0xFF607D8B)

          // Outer and inner loop paths
          // Left limb (with coil)
          drawRect(coreColor, Offset(left, top), Size(thick, coreH))
          // Top limb
          drawRect(coreColor, Offset(left, top), Size(coreW, thick))
          // Bottom limb
          drawRect(coreColor, Offset(left, bottom - thick), Size(coreW, thick))
          // Right limb with Air Gap in center
          val gapPx = (airGapLengthMm.toFloat() * 12f).coerceIn(4f, 40f)
          val rightLimbTopH = (coreH - thick * 2f - gapPx) / 2f
          drawRect(coreColor, Offset(right - thick, top + thick), Size(thick, rightLimbTopH))
          drawRect(coreColor, Offset(right - thick, bottom - thick - rightLimbTopH), Size(thick, rightLimbTopH))

          // Air Gap indicator
          val gapCenterY = center.y
          drawRect(
            Color(0xFFFFD54F).copy(alpha = 0.4f),
            Offset(right - thick, gapCenterY - gapPx / 2f),
            Size(thick, gapPx)
          )

          // Excitation Coil wrapped around left limb
          val coilYStart = top + thick + 8f
          val coilYEnd = bottom - thick - 8f
          val turnCount = 8
          val turnSpacing = (coilYEnd - coilYStart) / turnCount.toFloat()
          for (i in 0 until turnCount) {
            val ty = coilYStart + i * turnSpacing
            drawRect(
              Color(0xFFFF9800),
              Offset(left - 6f, ty),
              Size(thick + 12f, turnSpacing * 0.7f),
              style = Stroke(width = 3f)
            )
          }

          // Flux flow arrows inside the core
          val fluxAlpha = (circuitResult.coreFluxDensityTesla / 2.0).coerceIn(0.2, 1.0).toFloat()
          val arrowColor = MagneticAmber.copy(alpha = fluxAlpha)

          // Top limb arrow
          drawLine(arrowColor, Offset(left + thick + 10f, top + thick / 2f), Offset(right - thick - 10f, top + thick / 2f), 3f)
          // Bottom limb arrow
          drawLine(arrowColor, Offset(right - thick - 10f, bottom - thick / 2f), Offset(left + thick + 10f, bottom - thick / 2f), 3f)
        }

        if (circuitResult.isSaturated) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
              .background(Color(0xFFE53935), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("⚠️ Core Saturated (B > 90% B_sat)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    measurementsContent = {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        MeasurementBadge(
          symbol = "MMF",
          value = "${circuitParams.mmf.toInt()}",
          unit = "A-turns",
          label = "Magnetomotive Force",
          accentColor = MagneticOrange
        )
        MeasurementBadge(
          symbol = "R_total",
          value = String.format(Locale.US, "%.2e", circuitResult.totalReluctance),
          unit = "A-t/Wb",
          label = "Total Reluctance",
          accentColor = ElectricCyan
        )
        MeasurementBadge(
          symbol = "Φ",
          value = String.format(Locale.US, "%.3e", circuitResult.magneticFluxWebers),
          unit = "Weber (Wb)",
          label = "Core Flux",
          accentColor = MagneticAmber
        )
        MeasurementBadge(
          symbol = "B_core",
          value = String.format(Locale.US, "%.3f", circuitResult.coreFluxDensityTesla),
          unit = "Tesla (T)",
          label = "Flux Density",
          accentColor = if (circuitResult.isSaturated) Color(0xFFFF5252) else ElectricCyan
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Nonlinear B-H Curve Graph
      val bhCurvePoints = remember(selectedMaterial) {
        val maxH = 2000.0
        (0..50).map { i ->
          val h = (i / 50.0) * maxH
          val b = MagneticPhysicsEngine.calculateBhCurvePoint(h, selectedMaterial)
          Pair(h, b)
        }
      }

      com.example.ui.components.WaveformGraph(
        seriesList = listOf(
          com.example.ui.components.GraphSeries("B(H)", bhCurvePoints, MagneticAmber, "T")
        ),
        title = "Nonlinear B-H Saturation Curve (${selectedMaterial.name}, B_sat = ${selectedMaterial.saturationB} T)",
        timeUnit = "A/m"
      )
    },
    theoryContent = {
      EquationCard(
        title = "Hopkinson's Law (Magnetic Ohm's Law)",
        formula = "Φ = MMF / R_total = (N · I) / (R_core + R_gap)",
        description = "Similar to electric circuits: MMF is analogous to voltage EMF, Magnetic Flux Φ is analogous to current, and Reluctance R is analogous to resistance.",
        assumptions = "Negligible fringing flux at air gap boundaries, lumped magnetic circuit."
      )
      Spacer(modifier = Modifier.height(6.dp))
      EquationCard(
        title = "Reluctance of Core & Air Gap",
        formula = "R_core = l_c / (µ_r µ_0 A),    R_gap = l_g / (µ_0 A)",
        description = "Even a small millimeter air gap introduces enormous reluctance because µ_air is thousands of times lower than µ_ferromagnetic core.",
        accentColor = MagneticOrange
      )
    },
    controlsContent = {
      // Core material dropdown
      Text("Select Core Material (Relative Permeability µ_r):", style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(4.dp))
      ExposedDropdownMenuBox(
        expanded = materialDropdownExpanded,
        onExpandedChange = { materialDropdownExpanded = it }
      ) {
        TextField(
          value = "${selectedMaterial.name} (µ_r = ${selectedMaterial.relativePermeability.toInt()}, B_sat = ${selectedMaterial.saturationB} T)",
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialDropdownExpanded) },
          colors = ExposedDropdownMenuDefaults.textFieldColors(),
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("circuit_core_material_dropdown")
        )
        ExposedDropdownMenu(
          expanded = materialDropdownExpanded,
          onDismissRequest = { materialDropdownExpanded = false }
        ) {
          PhysicsConstants.MAGNETIC_MATERIALS.filter { it.isFerromagnetic }.forEach { mat ->
            DropdownMenuItem(
              text = { Text("${mat.name} (µ_r = ${mat.relativePermeability.toInt()}, B_sat = ${mat.saturationB} T)") },
              onClick = {
                selectedMaterial = mat
                materialDropdownExpanded = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Air gap slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Air Gap Length (l_gap):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.1f mm", airGapLengthMm), fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
      }
      Slider(
        value = airGapLengthMm.toFloat(),
        onValueChange = { airGapLengthMm = it.toDouble() },
        valueRange = 0.0f..8.0f,
        colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD54F), activeTrackColor = Color(0xFFFFD54F)),
        modifier = Modifier.testTag("air_gap_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Current slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Excitation Current (I):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.2f A", currentAmperes), fontWeight = FontWeight.Bold, color = MagneticAmber)
      }
      Slider(
        value = currentAmperes.toFloat(),
        onValueChange = { currentAmperes = it.toDouble() },
        valueRange = 0.1f..10.0f,
        colors = SliderDefaults.colors(thumbColor = MagneticAmber, activeTrackColor = MagneticAmber),
        modifier = Modifier.testTag("circuit_current_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Turns slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Winding Turns (N):", style = MaterialTheme.typography.bodySmall)
        Text("$coilTurnsN turns", fontWeight = FontWeight.Bold, color = MagneticOrange)
      }
      Slider(
        value = coilTurnsN.toFloat(),
        onValueChange = { coilTurnsN = it.toInt() },
        valueRange = 50f..2000f,
        colors = SliderDefaults.colors(thumbColor = MagneticOrange, activeTrackColor = MagneticOrange),
        modifier = Modifier.testTag("circuit_turns_slider")
      )
    }
  )
}
