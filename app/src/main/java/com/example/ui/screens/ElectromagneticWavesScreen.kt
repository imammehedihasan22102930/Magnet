package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WaveMedium
import com.example.physics.WaveAndMachinePhysicsEngine
import com.example.ui.components.EquationCard
import com.example.ui.components.LabScaffold
import com.example.ui.components.MeasurementBadge
import com.example.ui.theme.CanvasDarkBg
import com.example.ui.theme.CanvasGridLine
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MagneticAmber
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ElectromagneticWavesScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var frequencyGigaHz by remember { mutableDoubleStateOf(1.0) }
  var electricAmplitudeVPerM by remember { mutableDoubleStateOf(100.0) }
  var selectedMedium by remember { mutableStateOf(WaveAndMachinePhysicsEngine.DEFAULT_WAVE_MEDIA.first()) }
  var mediumDropdownExpanded by remember { mutableStateOf(false) }

  val waveProps = remember(frequencyGigaHz, electricAmplitudeVPerM, selectedMedium) {
    WaveAndMachinePhysicsEngine.calculateWaveProperties(
      frequencyHz = frequencyGigaHz * 1e9,
      electricFieldAmplitudeVPerM = electricAmplitudeVPerM,
      medium = selectedMedium
    )
  }

  // Animation phase timer for continuous wave movement
  val infiniteTransition = rememberInfiniteTransition(label = "wavePhase")
  val animatedPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 2f * PI.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wavePhaseFloat"
  )

  fun resetWave() {
    frequencyGigaHz = 1.0
    electricAmplitudeVPerM = 100.0
    selectedMedium = WaveAndMachinePhysicsEngine.DEFAULT_WAVE_MEDIA.first()
  }

  LabScaffold(
    title = "Electromagnetic Waves & Dispersion Lab",
    subtitle = "Transverse Plane Waves (E ⊥ B ⊥ k), v = 1/√(με) = f·λ & Poynting Vector",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetWave() },
    onSaveToNotebook = {
      val settings = mapOf(
        "Medium" to selectedMedium.name,
        "Frequency f" to "$frequencyGigaHz GHz",
        "E-field Amplitude E0" to "$electricAmplitudeVPerM V/m",
        "Relative Permittivity ε_r" to "${selectedMedium.relativePermittivity}"
      )
      val measurements = mapOf(
        "Wave Speed v" to String.format(Locale.US, "%.3e m/s", waveProps.speedMetersPerSec),
        "Wavelength λ" to String.format(Locale.US, "%.4f m (%.2f cm)", waveProps.wavelengthMeters, waveProps.wavelengthMeters * 100.0),
        "B-field Amplitude B0" to String.format(Locale.US, "%.3e Tesla", waveProps.magneticFieldAmplitudeTesla),
        "Impedance η" to String.format(Locale.US, "%.1f Ω", waveProps.intrinsicImpedance),
        "Poynting Power S_avg" to String.format(Locale.US, "%.2f W/m²", waveProps.poyntingVectorAvgPower)
      )
      onSaveMeasurement(settings, measurements)
    },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("em_wave_canvas")
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)

          // Propagation Axis z (Center horizontal line)
          drawLine(
            Color(0xFF64748B),
            Offset(20f, center.y),
            Offset(canvasW - 20f, center.y),
            strokeWidth = 2f
          )

          // Propagation direction arrow (+z to the right)
          val zEnd = Offset(canvasW - 20f, center.y)
          drawLine(Color.White, zEnd, Offset(zEnd.x - 12f, zEnd.y - 6f), 2.5f)
          drawLine(Color.White, zEnd, Offset(zEnd.x - 12f, zEnd.y + 6f), 2.5f)

          // Draw Transverse Waves:
          // Electric field (Vertical, Electric Cyan)
          // Magnetic field (Isometric slanted horizontal, Magnetic Amber)
          val numSamples = 60
          val zStart = 30f
          val zEndPos = canvasW - 40f
          val zSpan = zEndPos - zStart

          val eMaxHeight = 70.dp.toPx()
          val bMaxSlant = 45.dp.toPx()

          val spatialCycles = 2.5f // Display 2.5 full wavelengths across width

          for (i in 0..numSamples) {
            val frac = i.toFloat() / numSamples.toFloat()
            val screenZ = zStart + frac * zSpan
            val phase = (frac * spatialCycles * 2f * PI.toFloat()) - animatedPhase

            val eVal = sin(phase)
            val bVal = sin(phase) // in phase for TEM lossless plane wave

            // Electric Field Vector (Vertical: along Y)
            val eEnd = Offset(screenZ, center.y - eVal * eMaxHeight)
            drawLine(
              ElectricCyan.copy(alpha = 0.8f),
              Offset(screenZ, center.y),
              eEnd,
              strokeWidth = 2f,
              cap = StrokeCap.Round
            )

            // Magnetic Field Vector (Slanted: isometric X-axis perpendicular to Y and Z)
            val slantX = bVal * bMaxSlant * 0.7f
            val slantY = bVal * bMaxSlant * 0.4f
            val bEnd = Offset(screenZ + slantX, center.y + slantY)
            drawLine(
              MagneticAmber.copy(alpha = 0.8f),
              Offset(screenZ, center.y),
              bEnd,
              strokeWidth = 2f,
              cap = StrokeCap.Round
            )
          }
        }

        Text(
          text = "Cyan (Vertical): E-field | Amber (Transverse): B-field | Direction: +z (Right)",
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
          fontSize = 11.sp,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
        )
      }
    },
    measurementsContent = {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        MeasurementBadge(
          symbol = "v",
          value = String.format(Locale.US, "%.3e", waveProps.speedMetersPerSec),
          unit = "m/s",
          label = "Phase Velocity",
          accentColor = ElectricCyan
        )
        MeasurementBadge(
          symbol = "λ",
          value = String.format(Locale.US, "%.2f", waveProps.wavelengthMeters * 100.0),
          unit = "cm",
          label = "Wavelength",
          accentColor = Color(0xFFC084FC)
        )
        MeasurementBadge(
          symbol = "|B_0|",
          value = String.format(Locale.US, "%.3e", waveProps.magneticFieldAmplitudeTesla),
          unit = "Tesla",
          label = "Magnetic Amplitude",
          accentColor = MagneticAmber
        )
        MeasurementBadge(
          symbol = "η",
          value = String.format(Locale.US, "%.1f", waveProps.intrinsicImpedance),
          unit = "Ω",
          label = "Wave Impedance",
          accentColor = Color(0xFFFF9800)
        )
        MeasurementBadge(
          symbol = "S_avg",
          value = String.format(Locale.US, "%.2f", waveProps.poyntingVectorAvgPower),
          unit = "W/m²",
          label = "Poynting Power",
          accentColor = Color(0xFF4CAF50)
        )
      }
    },
    theoryContent = {
      EquationCard(
        title = "Electromagnetic Dispersion & Wave Velocity",
        formula = "v = 1 / √(µ · ε) = f · λ,    c = 1 / √(µ_0 · ε_0) ≈ 3.0 × 10⁸ m/s",
        description = "Wave velocity is fixed by medium permittivity and permeability. When frequency f changes, wavelength λ inversely adjusts to maintain v = f · λ.",
        assumptions = "Monochromatic sinusoidal uniform TEM plane wave in lossless medium."
      )
      Spacer(modifier = Modifier.height(6.dp))
      EquationCard(
        title = "Field Ratio & Poynting Vector Power",
        formula = "E_0 / B_0 = v,    S = (1 / µ) (E × B),    S_avg = E_0² / (2 η)",
        description = "The electric and magnetic fields oscillate in phase and maintain a constant ratio set by the wave speed. The Poynting vector represents directional energy flow.",
        accentColor = MagneticAmber
      )
    },
    controlsContent = {
      // Frequency Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Electromagnetic Frequency (f):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.2f GHz", frequencyGigaHz), fontWeight = FontWeight.Bold, color = ElectricCyan)
      }
      Slider(
        value = frequencyGigaHz.toFloat(),
        onValueChange = { frequencyGigaHz = it.toDouble() },
        valueRange = 0.2f..5.0f,
        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan),
        modifier = Modifier.testTag("em_wave_frequency_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // E-field amplitude slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Electric Field Amplitude (E_0):", style = MaterialTheme.typography.bodySmall)
        Text("${electricAmplitudeVPerM.toInt()} V/m", fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
      }
      Slider(
        value = electricAmplitudeVPerM.toFloat(),
        onValueChange = { electricAmplitudeVPerM = it.toDouble() },
        valueRange = 20f..500f,
        colors = SliderDefaults.colors(thumbColor = Color(0xFFC084FC), activeTrackColor = Color(0xFFC084FC)),
        modifier = Modifier.testTag("em_wave_amplitude_slider")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Medium Selector Dropdown
      Text("Select Propagation Medium (v = c / √ε_r):", style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(4.dp))
      ExposedDropdownMenuBox(
        expanded = mediumDropdownExpanded,
        onExpandedChange = { mediumDropdownExpanded = it }
      ) {
        TextField(
          value = "${selectedMedium.name} (ε_r = ${selectedMedium.relativePermittivity}, n = ${String.format(Locale.US, "%.2f", selectedMedium.refractiveIndex)})",
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mediumDropdownExpanded) },
          colors = ExposedDropdownMenuDefaults.textFieldColors(),
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("em_wave_medium_dropdown")
        )
        ExposedDropdownMenu(
          expanded = mediumDropdownExpanded,
          onDismissRequest = { mediumDropdownExpanded = false }
        ) {
          WaveAndMachinePhysicsEngine.DEFAULT_WAVE_MEDIA.forEach { med ->
            DropdownMenuItem(
              text = { Text("${med.name} (ε_r = ${med.relativePermittivity})") },
              onClick = {
                selectedMedium = med
                mediumDropdownExpanded = false
              }
            )
          }
        }
      }
    }
  )
}
