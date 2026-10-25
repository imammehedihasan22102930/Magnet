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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physics.MagneticPhysicsEngine
import com.example.ui.components.EquationCard
import com.example.ui.components.LabScaffold
import com.example.ui.components.MeasurementBadge
import com.example.ui.theme.CanvasDarkBg
import com.example.ui.theme.CanvasGridLine
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InductionGreen
import com.example.ui.theme.MagneticAmber
import com.example.ui.theme.MagneticOrange
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MagneticFluxScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var bFieldTesla by remember { mutableDoubleStateOf(0.5) }
  var coilAreaSqM by remember { mutableDoubleStateOf(0.02) }
  var coilTurnsN by remember { mutableIntStateOf(200) }
  var manualAngleDeg by remember { mutableDoubleStateOf(0.0) }
  var isAutoRotating by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "coilRotation")
  val animatedAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "coilAngle"
  )

  val currentAngleDeg = if (isAutoRotating) animatedAngle.toDouble() else manualAngleDeg

  val (fluxPhi, fluxLinkageLambda) = remember(bFieldTesla, coilAreaSqM, currentAngleDeg, coilTurnsN) {
    MagneticPhysicsEngine.calculateMagneticFluxAndLinkage(
      magneticFieldTesla = bFieldTesla,
      coilAreaSqMeters = coilAreaSqM,
      angleBetweenFieldAndNormalDeg = currentAngleDeg,
      numberOfTurns = coilTurnsN
    )
  }

  fun resetFlux() {
    bFieldTesla = 0.5
    coilAreaSqM = 0.02
    coilTurnsN = 200
    manualAngleDeg = 0.0
    isAutoRotating = false
  }

  LabScaffold(
    title = "Magnetic Flux & Linkage Lab",
    subtitle = "Single-Turn Flux Φ_B = B·A cos(θ) vs Total Linkage λ = N·Φ_B",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetFlux() },
    onSaveToNotebook = {
      val settings = mapOf(
        "B Field" to "$bFieldTesla T",
        "Coil Area A" to "$coilAreaSqM m²",
        "Turns N" to "$coilTurnsN",
        "Angle θ" to String.format(Locale.US, "%.1f°", currentAngleDeg)
      )
      val measurements = mapOf(
        "Single-turn Flux Φ_B" to String.format(Locale.US, "%.4e Wb", fluxPhi),
        "Total Flux Linkage λ" to String.format(Locale.US, "%.4e Wb-t", fluxLinkageLambda)
      )
      onSaveMeasurement(settings, measurements)
    },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("magnetic_flux_canvas")
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)

          // 1. Draw Uniform Magnetic Field Lines (Vertical arrows pointing down)
          val colSpacing = 36.dp.toPx()
          var xPos = colSpacing / 2f
          while (xPos < canvasW) {
            drawLine(
              color = MagneticAmber.copy(alpha = 0.35f),
              start = Offset(xPos, 20f),
              end = Offset(xPos, canvasH - 20f),
              strokeWidth = 2f,
              cap = StrokeCap.Round
            )
            // Arrowhead pointing down
            val arrowEnd = Offset(xPos, canvasH - 20f)
            drawLine(MagneticAmber.copy(alpha = 0.5f), arrowEnd, Offset(arrowEnd.x - 6f, arrowEnd.y - 10f), 2f)
            drawLine(MagneticAmber.copy(alpha = 0.5f), arrowEnd, Offset(arrowEnd.x + 6f, arrowEnd.y - 10f), 2f)
            xPos += colSpacing
          }

          // 2. Draw Multi-Turn Rotating Coil
          val thetaRad = Math.toRadians(currentAngleDeg).toFloat()
          val coilLen = 110.dp.toPx()
          // Surface orientation
          val coilPlaneAngle = thetaRad + (PI / 2.0).toFloat()

          val cp1 = Offset(center.x - coilLen * cos(coilPlaneAngle), center.y - coilLen * sin(coilPlaneAngle))
          val cp2 = Offset(center.x + coilLen * cos(coilPlaneAngle), center.y + coilLen * sin(coilPlaneAngle))

          // Draw coil layers
          drawLine(Color(0xFFFF9800), cp1, cp2, strokeWidth = 10f, cap = StrokeCap.Round)
          drawLine(Color(0xFFFFE082), cp1, cp2, strokeWidth = 4f, cap = StrokeCap.Round)

          // 3. Coil Surface Normal Unit Vector n̂
          val normalLen = 65.dp.toPx()
          val nEnd = Offset(center.x + normalLen * cos(thetaRad), center.y + normalLen * sin(thetaRad))
          drawLine(InductionGreen, center, nEnd, strokeWidth = 3.5f, cap = StrokeCap.Round)

          val nHead1 = thetaRad + (PI.toFloat() * 0.85f)
          val nHead2 = thetaRad - (PI.toFloat() * 0.85f)
          drawLine(InductionGreen, nEnd, Offset(nEnd.x + 12f * cos(nHead1), nEnd.y + 12f * sin(nHead1)), 3.5f)
          drawLine(InductionGreen, nEnd, Offset(nEnd.x + 12f * cos(nHead2), nEnd.y + 12f * sin(nHead2)), 3.5f)
        }

        Text(
          text = "Green Arrow: Surface Normal n̂ | Gold Loop: $coilTurnsN-turn coil | Amber Lines: B-Field",
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
          symbol = "Φ_B",
          value = String.format(Locale.US, "%.4e", fluxPhi),
          unit = "Weber (Wb)",
          label = "Single-Turn Flux",
          accentColor = MagneticAmber
        )
        MeasurementBadge(
          symbol = "λ",
          value = String.format(Locale.US, "%.4e", fluxLinkageLambda),
          unit = "Wb-turns",
          label = "Total Flux Linkage",
          accentColor = MagneticOrange
        )
        MeasurementBadge(
          symbol = "θ",
          value = String.format(Locale.US, "%.1f", currentAngleDeg),
          unit = "deg",
          label = "Coil Angle",
          accentColor = InductionGreen
        )
      }
    },
    theoryContent = {
      EquationCard(
        title = "Magnetic Flux & Flux Linkage",
        formula = "Φ_B = B · A · cos(θ),    λ = N · Φ_B = N · B · A · cos(θ)",
        description = "Magnetic flux Φ_B is the integral of B over a single surface turn. When N turns are connected in series, the total magnetic linkage is N times the single-turn flux.",
        assumptions = "Uniform B-field across the entire planar coil area."
      )
    },
    controlsContent = {
      // Auto-rotation toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Continuous Motor Rotation (ω = const)")
        Switch(
          checked = isAutoRotating,
          onCheckedChange = { isAutoRotating = it },
          modifier = Modifier.testTag("toggle_auto_rotation")
        )
      }

      if (!isAutoRotating) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Manual Coil Angle (θ):", style = MaterialTheme.typography.bodySmall)
          Text("${manualAngleDeg.toInt()}°", fontWeight = FontWeight.Bold, color = InductionGreen)
        }
        Slider(
          value = manualAngleDeg.toFloat(),
          onValueChange = { manualAngleDeg = it.toDouble() },
          valueRange = -180.0f..180.0f,
          colors = SliderDefaults.colors(thumbColor = InductionGreen, activeTrackColor = InductionGreen),
          modifier = Modifier.testTag("coil_angle_slider")
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Magnetic field slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Magnetic Flux Density (B):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.2f T", bFieldTesla), fontWeight = FontWeight.Bold, color = MagneticAmber)
      }
      Slider(
        value = bFieldTesla.toFloat(),
        onValueChange = { bFieldTesla = it.toDouble() },
        valueRange = 0.05f..2.0f,
        colors = SliderDefaults.colors(thumbColor = MagneticAmber, activeTrackColor = MagneticAmber),
        modifier = Modifier.testTag("flux_b_field_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Turns Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Number of Coil Turns (N):", style = MaterialTheme.typography.bodySmall)
        Text("$coilTurnsN turns", fontWeight = FontWeight.Bold, color = MagneticOrange)
      }
      Slider(
        value = coilTurnsN.toFloat(),
        onValueChange = { coilTurnsN = it.toInt() },
        valueRange = 1f..1000f,
        colors = SliderDefaults.colors(thumbColor = MagneticOrange, activeTrackColor = MagneticOrange),
        modifier = Modifier.testTag("flux_turns_slider")
      )
    }
  )
}
