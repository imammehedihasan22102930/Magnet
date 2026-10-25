package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InductionSimulationState
import com.example.physics.InductionPhysicsEngine
import com.example.ui.components.EquationCard
import com.example.ui.components.GraphSeries
import com.example.ui.components.LabScaffold
import com.example.ui.components.MeasurementBadge
import com.example.ui.components.WaveformGraph
import com.example.ui.theme.CanvasDarkBg
import com.example.ui.theme.CanvasGridLine
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InductionGreen
import com.example.ui.theme.MagneticAmber
import com.example.ui.theme.MagneticOrange
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InductionScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var velocityZ by remember { mutableDoubleStateOf(0.0) } // m/s
  var coilTurnsN by remember { mutableIntStateOf(200) }
  var circuitResistance by remember { mutableDoubleStateOf(5.0) }
  var circuitInductance by remember { mutableDoubleStateOf(0.005) } // 5 mH
  var isPurelyResistive by remember { mutableStateOf(false) }
  var isMagnetReversed by remember { mutableStateOf(false) } // North facing left vs right

  var state by remember {
    mutableStateOf(
      InductionSimulationState(
        magnetPositionZ = 0.12,
        magnetVelocityZ = 0.0,
        magnetMomentAm2 = 5.0,
        coilTurnsN = 200,
        circuitResistanceOhms = 5.0,
        circuitInductanceHenrys = 0.005
      )
    )
  }

  // Waveform history buffers for synchronized oscilloscope
  val fluxHistory = remember { mutableStateListOf<Pair<Double, Double>>() }
  val emfHistory = remember { mutableStateListOf<Pair<Double, Double>>() }
  val currentHistory = remember { mutableStateListOf<Pair<Double, Double>>() }
  var simTimeSeconds by remember { mutableDoubleStateOf(0.0) }

  // Simulation tick loop (30 fps)
  LaunchedEffect(velocityZ, coilTurnsN, circuitResistance, circuitInductance, isPurelyResistive, isMagnetReversed) {
    while (true) {
      val dt = 0.033
      simTimeSeconds += dt
      val effectiveMoment = if (isMagnetReversed) -5.0 else 5.0
      val effectiveInductance = if (isPurelyResistive) 1e-6 else circuitInductance

      state = state.copy(
        magnetVelocityZ = velocityZ,
        magnetMomentAm2 = effectiveMoment,
        coilTurnsN = coilTurnsN,
        circuitResistanceOhms = circuitResistance,
        circuitInductanceHenrys = effectiveInductance
      )
      val nextState = InductionPhysicsEngine.evaluateMagnetMotionInduction(state, dt)

      // Clamp position between -0.25 and +0.25
      val boundedZ = nextState.magnetPositionZ.coerceIn(-0.25, 0.25)
      if (boundedZ <= -0.25 || boundedZ >= 0.25) {
        velocityZ = -velocityZ
      }
      state = nextState.copy(magnetPositionZ = boundedZ)

      // Append to synchronized waveform history
      if (fluxHistory.size > 80) fluxHistory.removeAt(0)
      if (emfHistory.size > 80) emfHistory.removeAt(0)
      if (currentHistory.size > 80) currentHistory.removeAt(0)

      // Multiply flux linkage by 10 for convenient visibility on same scale as V & I
      fluxHistory.add(Pair(simTimeSeconds, state.currentFluxLinkage * 100.0))
      emfHistory.add(Pair(simTimeSeconds, state.inducedEmfVolts))
      currentHistory.add(Pair(simTimeSeconds, state.inducedCurrentAmperes))

      delay(33)
    }
  }

  fun resetInduction() {
    velocityZ = 0.0
    coilTurnsN = 200
    circuitResistance = 5.0
    circuitInductance = 0.005
    isPurelyResistive = false
    isMagnetReversed = false
    state = InductionSimulationState(
      magnetPositionZ = 0.12,
      magnetVelocityZ = 0.0,
      magnetMomentAm2 = 5.0,
      coilTurnsN = 200,
      circuitResistanceOhms = 5.0,
      circuitInductanceHenrys = 0.005
    )
    fluxHistory.clear()
    emfHistory.clear()
    currentHistory.clear()
  }

  LabScaffold(
    title = "Electromagnetic Induction & Lenz's Law",
    subtitle = "Faraday's Law emf = -dλ/dt, Speed Dependence, Polarity & Synchronized Oscilloscope",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetInduction() },
    onSaveToNotebook = {
      val settings = mapOf(
        "Magnet Distance z" to String.format(Locale.US, "%.3f m", state.magnetPositionZ),
        "Velocity v" to String.format(Locale.US, "%.2f m/s", state.magnetVelocityZ),
        "Coil Turns N" to "$coilTurnsN",
        "Circuit Model" to if (isPurelyResistive) "Pure Resistive" else "Inductive-Resistive (RL)",
        "Resistance R" to "$circuitResistance Ω",
        "Magnet Orientation" to if (isMagnetReversed) "Reversed (South Leading)" else "Normal (North Leading)"
      )
      val measurements = mapOf(
        "Induced EMF" to String.format(Locale.US, "%.3f V", state.inducedEmfVolts),
        "Circuit Current I" to String.format(Locale.US, "%.3f A", state.inducedCurrentAmperes),
        "Flux Linkage λ" to String.format(Locale.US, "%.4e Wb-t", state.currentFluxLinkage)
      )
      onSaveMeasurement(settings, measurements)
    },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("induction_canvas")
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
              detectDragGestures(
                onDragStart = { velocityZ = 0.0 },
                onDrag = { change, dragAmount ->
                  change.consume()
                  val scale = size.width / 0.5f
                  val deltaZ = (dragAmount.x / scale).toDouble()
                  state = state.copy(
                    magnetPositionZ = (state.magnetPositionZ + deltaZ).coerceIn(-0.25, 0.25),
                    magnetVelocityZ = deltaZ / 0.033
                  )
                },
                onDragEnd = {
                  state = state.copy(magnetVelocityZ = 0.0)
                  velocityZ = 0.0
                }
              )
            }
        ) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)
          val meterScale = canvasW / 0.5f // 0.5m across canvas

          // 1. Central Coil
          val coilWidth = 26.dp.toPx()
          val coilHeight = 115.dp.toPx()
          val coilLeft = center.x - coilWidth / 2f
          val coilTop = center.y - coilHeight / 2f

          drawRect(
            color = Color(0xFFFF9800),
            topLeft = Offset(coilLeft, coilTop),
            size = Size(coilWidth, coilHeight),
            style = Stroke(width = 5f)
          )

          // Winding turns
          for (i in 0..10) {
            val yMark = coilTop + (i / 10f) * coilHeight
            drawLine(
              Color(0xFFFFD54F),
              Offset(coilLeft - 4f, yMark),
              Offset(coilLeft + coilWidth + 4f, yMark),
              2f
            )
          }

          // 2. Bar Magnet
          val magnetScreenX = center.x + (state.magnetPositionZ.toFloat() * meterScale)
          val mWidth = 74.dp.toPx()
          val mHeight = 34.dp.toPx()

          val leftColor = if (isMagnetReversed) Color(0xFFE53935) else Color(0xFF1E88E5)
          val rightColor = if (isMagnetReversed) Color(0xFF1E88E5) else Color(0xFFE53935)

          // Left Half
          drawRect(
            color = leftColor,
            topLeft = Offset(magnetScreenX - mWidth / 2f, center.y - mHeight / 2f),
            size = Size(mWidth / 2f, mHeight)
          )
          // Right Half
          drawRect(
            color = rightColor,
            topLeft = Offset(magnetScreenX, center.y - mHeight / 2f),
            size = Size(mWidth / 2f, mHeight)
          )

          // 3. Galvanometer Meter (at top right)
          val gMeterCenter = Offset(canvasW - 55.dp.toPx(), 45.dp.toPx())
          val gRadius = 32.dp.toPx()
          drawCircle(Color(0xFF1E293B), radius = gRadius, center = gMeterCenter)
          drawCircle(ElectricCyan, radius = gRadius, center = gMeterCenter, style = Stroke(width = 2f))

          // Deflection needle
          val needleAngleRad = Math.toRadians(-90.0 + state.galvanometerAngleDeg).toFloat()
          val needleLen = gRadius * 0.85f
          val nTip = Offset(gMeterCenter.x + needleLen * cos(needleAngleRad), gMeterCenter.y + needleLen * sin(needleAngleRad))
          drawLine(Color(0xFFFFD54F), gMeterCenter, nTip, strokeWidth = 2.5f, cap = StrokeCap.Round)
          drawCircle(Color.White, radius = 3.dp.toPx(), center = gMeterCenter)
        }

        Text(
          text = "Drag magnet left/right. Galvanometer deflects strictly according to Lenz's law.",
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
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
            symbol = "emf(t)",
            value = String.format(Locale.US, "%+.3f", state.inducedEmfVolts),
            unit = "Volts",
            label = "Induced Voltage",
            accentColor = InductionGreen
          )
          MeasurementBadge(
            symbol = "i(t)",
            value = String.format(Locale.US, "%+.3f", state.inducedCurrentAmperes),
            unit = "Amperes",
            label = "Circuit Current",
            accentColor = ElectricCyan
          )
          MeasurementBadge(
            symbol = "λ(t)",
            value = String.format(Locale.US, "%.4e", state.currentFluxLinkage),
            unit = "Wb-t",
            label = "Flux Linkage",
            accentColor = MagneticAmber
          )
          MeasurementBadge(
            symbol = "v",
            value = String.format(Locale.US, "%+.2f", state.magnetVelocityZ),
            unit = "m/s",
            label = "Magnet Speed",
            accentColor = MagneticOrange
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Synchronized Tri-Channel Oscilloscope
        WaveformGraph(
          seriesList = listOf(
            GraphSeries("EMF", emfHistory.toList(), InductionGreen, "V"),
            GraphSeries("Current", currentHistory.toList(), ElectricCyan, "A"),
            GraphSeries("Flux Linkage (×100)", fluxHistory.toList(), MagneticAmber, "Wb-t")
          ),
          title = "Synchronized Faraday-Lenz Oscilloscope (Flux, EMF & Current)"
        )
      }
    },
    theoryContent = {
      EquationCard(
        title = "Faraday's Law of Electromagnetic Induction",
        formula = "emf = - dλ/dt = - N (dΦ_B/dt) = - N (dΦ_B/dz) · v",
        description = "Induced voltage is directly proportional to the time rate of change of magnetic flux linkage. If the magnet is stationary (v = 0), dΦ/dt is exactly zero and induced voltage is zero.",
        accentColor = InductionGreen
      )
      Spacer(modifier = Modifier.height(6.dp))
      EquationCard(
        title = "Lenz's Law & Circuit Dynamics",
        formula = "L (di/dt) + R · i = emf(t)",
        description = "The negative sign in Faraday's law enforces energy conservation: induced current produces a magnetic field that repels an approaching pole and attracts a departing pole.",
        accentColor = ElectricCyan
      )
    },
    controlsContent = {
      // Circuit Model Selection
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Circuit Model: ${if (isPurelyResistive) "Purely Resistive (i = emf/R)" else "Series RL Circuit (L di/dt + Ri = emf)"}")
        Switch(
          checked = !isPurelyResistive,
          onCheckedChange = { isPurelyResistive = !it },
          modifier = Modifier.testTag("toggle_circuit_model")
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Magnet Pole Orientation Flip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Reverse Magnet Poles (South Leading)")
        Switch(
          checked = isMagnetReversed,
          onCheckedChange = { isMagnetReversed = it },
          modifier = Modifier.testTag("toggle_magnet_poles")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Speed presets and velocity slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Continuous Magnet Speed (v):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%+.2f m/s", velocityZ), fontWeight = FontWeight.Bold, color = MagneticOrange)
      }
      Slider(
        value = velocityZ.toFloat(),
        onValueChange = { velocityZ = it.toDouble() },
        valueRange = -2.0f..2.0f,
        colors = SliderDefaults.colors(thumbColor = MagneticOrange, activeTrackColor = MagneticOrange),
        modifier = Modifier.testTag("magnet_velocity_slider")
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = { velocityZ = 0.0 },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
          modifier = Modifier.testTag("stop_magnet_button")
        ) {
          Text("Stationary (v = 0)")
        }
        Button(
          onClick = { velocityZ = 0.6 },
          colors = ButtonDefaults.buttonColors(containerColor = InductionGreen),
          modifier = Modifier.testTag("fwd_magnet_button")
        ) {
          Text("+0.6 m/s")
        }
        Button(
          onClick = { velocityZ = -0.6 },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
          modifier = Modifier.testTag("rev_magnet_button")
        ) {
          Text("-0.6 m/s")
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Turns slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Coil Turns (N):", style = MaterialTheme.typography.bodySmall)
        Text("$coilTurnsN turns", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
      }
      Slider(
        value = coilTurnsN.toFloat(),
        onValueChange = { coilTurnsN = it.toInt() },
        valueRange = 20f..500f,
        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF9800), activeTrackColor = Color(0xFFFF9800)),
        modifier = Modifier.testTag("induction_turns_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Resistance slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Circuit Resistance (R):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.1f Ω", circuitResistance), fontWeight = FontWeight.Bold, color = ElectricCyan)
      }
      Slider(
        value = circuitResistance.toFloat(),
        onValueChange = { circuitResistance = it.toDouble() },
        valueRange = 0.5f..25.0f,
        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan),
        modifier = Modifier.testTag("circuit_resistance_slider")
      )
    }
  )
}
