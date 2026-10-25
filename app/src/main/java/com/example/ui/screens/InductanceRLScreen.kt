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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physics.InductionPhysicsEngine
import com.example.ui.components.EquationCard
import com.example.ui.components.GraphSeries
import com.example.ui.components.LabScaffold
import com.example.ui.components.MeasurementBadge
import com.example.ui.components.WaveformGraph
import com.example.ui.theme.CanvasDarkBg
import com.example.ui.theme.CanvasGridLine
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InductionGreen
import com.example.ui.theme.MagneticAmber
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InductanceRLScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var supplyVoltageV by remember { mutableDoubleStateOf(12.0) }
  var resistanceOhms by remember { mutableDoubleStateOf(10.0) }
  var inductanceH by remember { mutableDoubleStateOf(0.10) }

  val tau = remember(resistanceOhms, inductanceH) { inductanceH / resistanceOhms }
  val iFinal = remember(supplyVoltageV, resistanceOhms) { supplyVoltageV / resistanceOhms }
  val maxEnergy = remember(inductanceH, iFinal) { 0.5 * inductanceH * iFinal * iFinal }

  // 5 time constants window
  val totalDuration = remember(tau) { tau * 5.0 }
  val stepPoints = remember(supplyVoltageV, resistanceOhms, inductanceH, totalDuration) {
    InductionPhysicsEngine.calculateRLStepResponse(
      supplyVoltageVolts = supplyVoltageV,
      resistanceOhms = resistanceOhms,
      inductanceHenrys = inductanceH,
      timeDurationSeconds = totalDuration,
      pointCount = 100
    )
  }

  fun resetRL() {
    supplyVoltageV = 12.0
    resistanceOhms = 10.0
    inductanceH = 0.10
  }

  LabScaffold(
    title = "Inductance & RL Circuit Energy Lab",
    subtitle = "Step Transient Response, Time Constant τ = L/R & Magnetic Energy W = ½LI²",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetRL() },
    onSaveToNotebook = {
      val settings = mapOf(
        "Supply V0" to "$supplyVoltageV V",
        "Resistance R" to "$resistanceOhms Ω",
        "Inductance L" to "$inductanceH H"
      )
      val measurements = mapOf(
        "Time Constant τ" to String.format(Locale.US, "%.3f s (%.1f ms)", tau, tau * 1000.0),
        "Final Current I_max" to String.format(Locale.US, "%.3f A", iFinal),
        "Stored Energy W_max" to String.format(Locale.US, "%.4f J", maxEnergy)
      )
      onSaveMeasurement(settings, measurements)
    },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("rl_circuit_diagram_canvas")
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)

          // Schematic series loop: Source (left), Resistor (top), Inductor (right), Ground (bottom)
          val w = 180.dp.toPx()
          val h = 110.dp.toPx()
          val left = center.x - w / 2f
          val right = center.x + w / 2f
          val top = center.y - h / 2f
          val bottom = center.y + h / 2f

          val wireColor = Color(0xFF94A3B8)

          // Wires
          drawLine(wireColor, Offset(left, top), Offset(right, top), 3f)
          drawLine(wireColor, Offset(right, top), Offset(right, bottom), 3f)
          drawLine(wireColor, Offset(right, bottom), Offset(left, bottom), 3f)
          drawLine(wireColor, Offset(left, bottom), Offset(left, top), 3f)

          // DC Source on Left
          drawCircle(Color(0xFF0F172A), radius = 18.dp.toPx(), center = Offset(left, center.y))
          drawCircle(ElectricCyan, radius = 18.dp.toPx(), center = Offset(left, center.y), style = Stroke(3f))

          // Resistor on Top (Zigzag)
          drawRect(Color(0xFF0F172A), Offset(center.x - 25.dp.toPx(), top - 8.dp.toPx()), androidx.compose.ui.geometry.Size(50.dp.toPx(), 16.dp.toPx()))
          drawRect(MagneticAmber, Offset(center.x - 25.dp.toPx(), top - 8.dp.toPx()), androidx.compose.ui.geometry.Size(50.dp.toPx(), 16.dp.toPx()), style = Stroke(2.5f))

          // Inductor on Right (Coil Loops)
          val indCenterY = center.y
          drawRect(Color(0xFF0F172A), Offset(right - 10.dp.toPx(), indCenterY - 25.dp.toPx()), androidx.compose.ui.geometry.Size(20.dp.toPx(), 50.dp.toPx()))
          drawCircle(InductionGreen, radius = 8.dp.toPx(), center = Offset(right, indCenterY - 14.dp.toPx()), style = Stroke(3f))
          drawCircle(InductionGreen, radius = 8.dp.toPx(), center = Offset(right, indCenterY), style = Stroke(3f))
          drawCircle(InductionGreen, radius = 8.dp.toPx(), center = Offset(right, indCenterY + 14.dp.toPx()), style = Stroke(3f))
        }

        Text(
          text = "Schematic: DC Source (Left) → Resistor R (Top) → Inductor L (Right)",
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
          fontSize = 11.sp,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
        )
      }
    },
    measurementsContent = {
      Column(modifier = Modifier.fillMaxWidth()) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          MeasurementBadge(
            symbol = "τ",
            value = String.format(Locale.US, "%.1f", tau * 1000.0),
            unit = "ms",
            label = "Time Constant (L/R)",
            accentColor = InductionGreen
          )
          MeasurementBadge(
            symbol = "I_final",
            value = String.format(Locale.US, "%.2f", iFinal),
            unit = "A",
            label = "Steady Current",
            accentColor = ElectricCyan
          )
          MeasurementBadge(
            symbol = "W_max",
            value = String.format(Locale.US, "%.3f", maxEnergy),
            unit = "Joules (J)",
            label = "Stored Magnetic Energy",
            accentColor = MagneticAmber
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-Trace Voltage & Current Waveform Graph
        WaveformGraph(
          seriesList = listOf(
            GraphSeries("i(t)", stepPoints.map { Pair(it.timeSeconds, it.currentAmperes) }, ElectricCyan, "A"),
            GraphSeries("v_L(t)", stepPoints.map { Pair(it.timeSeconds, it.inductorVoltageVolts) }, InductionGreen, "V"),
            GraphSeries("v_R(t)", stepPoints.map { Pair(it.timeSeconds, it.resistorVoltageVolts) }, MagneticAmber, "V")
          ),
          title = "Step Transient: Current & Voltage Profiles"
        )
      }
    },
    theoryContent = {
      EquationCard(
        title = "RL Circuit Differential Equation & Transient",
        formula = "L (di/dt) + R · i = V_0  ⟹  i(t) = (V_0 / R) [1 - e^{-t/τ}]",
        description = "Because the inductor opposes rapid change in current by generating back-EMF v_L = L di/dt, current cannot jump instantaneously.",
        assumptions = "Ideal linear inductor, constant DC voltage step at t = 0."
      )
      Spacer(modifier = Modifier.height(6.dp))
      EquationCard(
        title = "Stored Magnetic Energy in Inductor",
        formula = "W_L = ½ · L · I²",
        description = "Energy is stored physically in the magnetic field surrounding the inductor winding. At t = τ (one time constant), current reaches 63.2% of its final value.",
        accentColor = MagneticAmber
      )
    },
    controlsContent = {
      // Supply Voltage slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("DC Supply Voltage (V0):", style = MaterialTheme.typography.bodySmall)
        Text("${supplyVoltageV.toInt()} V", fontWeight = FontWeight.Bold, color = ElectricCyan)
      }
      Slider(
        value = supplyVoltageV.toFloat(),
        onValueChange = { supplyVoltageV = it.toDouble() },
        valueRange = 1.0f..48.0f,
        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan),
        modifier = Modifier.testTag("rl_voltage_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Resistance slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Circuit Resistance (R):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.1f Ω", resistanceOhms), fontWeight = FontWeight.Bold, color = MagneticAmber)
      }
      Slider(
        value = resistanceOhms.toFloat(),
        onValueChange = { resistanceOhms = it.toDouble() },
        valueRange = 1.0f..50.0f,
        colors = SliderDefaults.colors(thumbColor = MagneticAmber, activeTrackColor = MagneticAmber),
        modifier = Modifier.testTag("rl_resistance_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Inductance slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Inductance (L):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.3f H (%d mH)", inductanceH, (inductanceH * 1000).toInt()), fontWeight = FontWeight.Bold, color = InductionGreen)
      }
      Slider(
        value = inductanceH.toFloat(),
        onValueChange = { inductanceH = it.toDouble() },
        valueRange = 0.01f..1.0f,
        colors = SliderDefaults.colors(thumbColor = InductionGreen, activeTrackColor = InductionGreen),
        modifier = Modifier.testTag("rl_inductance_slider")
      )
    }
  )
}
