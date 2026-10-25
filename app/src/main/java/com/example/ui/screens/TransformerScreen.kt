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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransformerParams
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
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransformerScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var primaryTurnsNp by remember { mutableIntStateOf(500) }
  var secondaryTurnsNs by remember { mutableIntStateOf(100) }
  var primaryVoltageV by remember { mutableDoubleStateOf(120.0) }
  var loadResistanceOhms by remember { mutableDoubleStateOf(20.0) }
  var isIdealMode by remember { mutableStateOf(false) }

  val transformerParams = remember(primaryTurnsNp, secondaryTurnsNs, primaryVoltageV, loadResistanceOhms, isIdealMode) {
    TransformerParams(
      primaryTurnsNp = primaryTurnsNp,
      secondaryTurnsNs = secondaryTurnsNs,
      primaryVoltageRms = primaryVoltageV,
      loadResistanceOhms = loadResistanceOhms,
      primaryWindingResistanceOhms = if (isIdealMode) 0.0 else 1.5,
      secondaryWindingResistanceOhms = if (isIdealMode) 0.0 else 0.25,
      couplingCoefficientK = if (isIdealMode) 1.0 else 0.96
    )
  }

  val transformerResult = remember(transformerParams) {
    InductionPhysicsEngine.solveTransformer(transformerParams)
  }

  // Generate 2 full cycles of sinusoidal AC waveforms for primary and secondary voltages
  val waveformPoints = remember(transformerParams, transformerResult) {
    val f = 60.0
    val period = 1.0 / f
    val numPoints = 80
    val pList = mutableListOf<Pair<Double, Double>>()
    val sList = mutableListOf<Pair<Double, Double>>()

    val vpPeak = transformerParams.primaryVoltageRms * sqrt(2.0)
    val vsPeak = transformerResult.secondaryVoltageRms * sqrt(2.0)

    for (i in 0 until numPoints) {
      val t = (i / (numPoints - 1).toDouble()) * (2 * period)
      val phase = 2 * PI * f * t
      pList.add(Pair(t * 1000.0, vpPeak * sin(phase))) // ms on x-axis
      sList.add(Pair(t * 1000.0, vsPeak * sin(phase)))
    }
    Pair(pList, sList)
  }

  fun resetTransformer() {
    primaryTurnsNp = 500
    secondaryTurnsNs = 100
    primaryVoltageV = 120.0
    loadResistanceOhms = 20.0
    isIdealMode = false
  }

  LabScaffold(
    title = "Virtual Transformer Laboratory",
    subtitle = "Turns Ratio Vs/Vp = Ns/Np, Core Coupling, Voltage Regulation & Efficiency",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetTransformer() },
    onSaveToNotebook = {
      val settings = mapOf(
        "Primary Turns Np" to "$primaryTurnsNp",
        "Secondary Turns Ns" to "$secondaryTurnsNs",
        "Primary V_rms" to "$primaryVoltageV V",
        "Load R" to "$loadResistanceOhms Ω",
        "Model" to if (isIdealMode) "Ideal (Lossless)" else "Real (Lossy with Winding R & Core Loss)"
      )
      val measurements = mapOf(
        "Secondary V_rms" to String.format(Locale.US, "%.2f V", transformerResult.secondaryVoltageRms),
        "Secondary I_rms" to String.format(Locale.US, "%.3f A", transformerResult.secondaryCurrentRms),
        "Primary I_rms" to String.format(Locale.US, "%.3f A", transformerResult.primaryCurrentRms),
        "Efficiency η" to String.format(Locale.US, "%.1f%%", transformerResult.efficiencyPercent),
        "Voltage Regulation" to String.format(Locale.US, "%.2f%%", transformerResult.voltageRegulationPercent)
      )
      onSaveMeasurement(settings, measurements)
    },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("transformer_canvas")
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)

          // Draw Core (Laminated Silicon Iron Core)
          val coreW = 150.dp.toPx()
          val coreH = 130.dp.toPx()
          val legThick = 26.dp.toPx()
          val left = center.x - coreW / 2f
          val top = center.y - coreH / 2f

          val coreColor = Color(0xFF475569)

          // Outer frame
          drawRect(coreColor, Offset(left, top), Size(coreW, coreH))
          // Center window
          drawRect(
            CanvasDarkBg,
            Offset(left + legThick, top + legThick),
            Size(coreW - legThick * 2f, coreH - legThick * 2f)
          )

          // Primary Winding (Left leg - Cyan)
          val pYStart = top + legThick + 10f
          val pHeight = coreH - legThick * 2f - 20f
          drawRect(
            ElectricCyan.copy(alpha = 0.25f),
            Offset(left - 6f, pYStart),
            Size(legThick + 12f, pHeight)
          )
          drawRect(
            ElectricCyan,
            Offset(left - 6f, pYStart),
            Size(legThick + 12f, pHeight),
            style = Stroke(width = 3f)
          )

          // Secondary Winding (Right leg - Amber)
          val rightLegLeft = left + coreW - legThick
          drawRect(
            MagneticAmber.copy(alpha = 0.25f),
            Offset(rightLegLeft - 6f, pYStart),
            Size(legThick + 12f, pHeight)
          )
          drawRect(
            MagneticAmber,
            Offset(rightLegLeft - 6f, pYStart),
            Size(legThick + 12f, pHeight),
            style = Stroke(width = 3f)
          )
        }

        Text(
          text = "Left: Primary Winding ($primaryTurnsNp turns) | Right: Secondary Winding ($secondaryTurnsNs turns)",
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
            symbol = "V_s",
            value = String.format(Locale.US, "%.2f", transformerResult.secondaryVoltageRms),
            unit = "V_rms",
            label = "Secondary Voltage",
            accentColor = InductionGreen
          )
          MeasurementBadge(
            symbol = "I_s",
            value = String.format(Locale.US, "%.3f", transformerResult.secondaryCurrentRms),
            unit = "A_rms",
            label = "Secondary Current",
            accentColor = MagneticAmber
          )
          MeasurementBadge(
            symbol = "P_out",
            value = String.format(Locale.US, "%.1f", transformerResult.outputPowerWatts),
            unit = "Watts",
            label = "Output Power",
            accentColor = ElectricCyan
          )
          MeasurementBadge(
            symbol = "η",
            value = String.format(Locale.US, "%.1f%%", transformerResult.efficiencyPercent),
            unit = "eff",
            label = "Efficiency",
            accentColor = if (transformerResult.efficiencyPercent > 90) InductionGreen else Color(0xFFFFB300)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AC Sinusoidal Voltage Waveforms (Primary vs Secondary)
        WaveformGraph(
          seriesList = listOf(
            GraphSeries("V_primary", waveformPoints.first, ElectricCyan, "V"),
            GraphSeries("V_secondary", waveformPoints.second, MagneticAmber, "V")
          ),
          title = "AC Steady-State Voltage Waveforms (60 Hz)",
          timeUnit = "ms"
        )
      }
    },
    theoryContent = {
      EquationCard(
        title = "Transformer Voltage & Current Ratios",
        formula = "Vs / Vp = Ns / Np = a^{-1},    Is / Ip = Np / Ns = a",
        description = "By electromagnetic mutual induction, magnetic flux in the core links both windings. In an ideal transformer, input power equals output power: Vp · Ip = Vs · Is.",
        assumptions = "Lossless magnetic core (k = 1), zero winding resistance."
      )
      Spacer(modifier = Modifier.height(6.dp))
      EquationCard(
        title = "Practical Non-Ideal Losses & Regulation",
        formula = "VR = (V_{no-load} - V_{load}) / V_{load} · 100%,    η = P_{out} / P_{in} · 100%",
        description = "Real transformers exhibit ohmic I²R copper losses in windings, core hysteresis and eddy current losses, and leakage flux reducing terminal voltage under heavy loads.",
        accentColor = MagneticAmber
      )
    },
    controlsContent = {
      // Model toggle: Ideal vs Real
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Lossless Ideal Model (100% Coupling)")
        Switch(
          checked = isIdealMode,
          onCheckedChange = { isIdealMode = it },
          modifier = Modifier.testTag("toggle_ideal_transformer")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Primary Voltage
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Primary AC Supply Voltage (V_p):", style = MaterialTheme.typography.bodySmall)
        Text("${primaryVoltageV.toInt()} V_rms", fontWeight = FontWeight.Bold, color = ElectricCyan)
      }
      Slider(
        value = primaryVoltageV.toFloat(),
        onValueChange = { primaryVoltageV = it.toDouble() },
        valueRange = 24f..240f,
        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan),
        modifier = Modifier.testTag("primary_voltage_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Primary Turns Np
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Primary Turns (Np):", style = MaterialTheme.typography.bodySmall)
        Text("$primaryTurnsNp turns", fontWeight = FontWeight.Bold, color = ElectricBlue)
      }
      Slider(
        value = primaryTurnsNp.toFloat(),
        onValueChange = { primaryTurnsNp = it.toInt() },
        valueRange = 100f..1000f,
        colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue),
        modifier = Modifier.testTag("primary_turns_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Secondary Turns Ns
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Secondary Turns (Ns):", style = MaterialTheme.typography.bodySmall)
        Text("$secondaryTurnsNs turns (Ratio ${primaryTurnsNp / secondaryTurnsNs}:1)", fontWeight = FontWeight.Bold, color = MagneticAmber)
      }
      Slider(
        value = secondaryTurnsNs.toFloat(),
        onValueChange = { secondaryTurnsNs = it.toInt() },
        valueRange = 20f..500f,
        colors = SliderDefaults.colors(thumbColor = MagneticAmber, activeTrackColor = MagneticAmber),
        modifier = Modifier.testTag("secondary_turns_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Load Resistance
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Secondary Load Resistance (R_load):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.1f Ω", loadResistanceOhms), fontWeight = FontWeight.Bold, color = InductionGreen)
      }
      Slider(
        value = loadResistanceOhms.toFloat(),
        onValueChange = { loadResistanceOhms = it.toDouble() },
        valueRange = 2f..100f,
        colors = SliderDefaults.colors(thumbColor = InductionGreen, activeTrackColor = InductionGreen),
        modifier = Modifier.testTag("load_resistance_slider")
      )
    }
  )
}
