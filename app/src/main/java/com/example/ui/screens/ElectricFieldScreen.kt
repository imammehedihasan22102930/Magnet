package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DielectricMaterial
import com.example.model.PhysicsConstants
import com.example.model.PointCharge
import com.example.model.TestProbe
import com.example.model.Vector2D
import com.example.physics.ElectricPhysicsEngine
import com.example.ui.components.EquationCard
import com.example.ui.components.LabScaffold
import com.example.ui.components.MeasurementBadge
import com.example.ui.theme.CanvasDarkBg
import com.example.ui.theme.CanvasGridLine
import com.example.ui.theme.ChargeNegativeBlue
import com.example.ui.theme.ChargePositiveRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EquipotentialPurple
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ElectricFieldScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  // Charges list
  val charges = remember {
    mutableStateListOf(
      PointCharge(id = "q1", position = Vector2D(-0.08, 0.0), chargeMicroCoulombs = 3.0),
      PointCharge(id = "q2", position = Vector2D(0.08, 0.0), chargeMicroCoulombs = -3.0)
    )
  }

  // Selected charge ID for parameter editing
  var selectedChargeId by remember { mutableStateOf<String?>("q1") }

  // Test Probe position & test charge
  var probePosition by remember { mutableStateOf(Vector2D(0.0, 0.08)) }
  var testChargeMagnitudeNC by remember { mutableDoubleStateOf(1.0) } // Can be positive or negative

  // Visual toggles
  var showFieldLines by remember { mutableStateOf(true) }
  var showEquipotentialContours by remember { mutableStateOf(true) }
  var showProbeEquipotential by remember { mutableStateOf(true) }
  var showFieldVectors by remember { mutableStateOf(false) }
  var viewScale by remember { mutableFloatStateOf(1.0f) }

  // Medium
  var selectedDielectric by remember { mutableStateOf(PhysicsConstants.DIELECTRIC_MATERIALS.first()) }
  var dielectricDropdownExpanded by remember { mutableStateOf(false) }

  // Dragging state tracking (0: none, 1: probe, 2: charge)
  var draggedChargeId by remember { mutableStateOf<String?>(null) }
  var isDraggingProbe by remember { mutableStateOf(false) }

  // Live physics evaluation at probe
  val testProbe = remember(probePosition, testChargeMagnitudeNC) {
    TestProbe(position = probePosition, testChargeNanoCoulombs = testChargeMagnitudeNC)
  }

  val probeResult = remember(probePosition, charges.toList(), selectedDielectric, testProbe) {
    ElectricPhysicsEngine.evaluatePointField(
      probePosition = probePosition,
      charges = charges,
      dielectric = selectedDielectric,
      testProbe = testProbe
    )
  }

  // Calculated Field Lines (Streamlines)
  val calculatedFieldLines = remember(charges.toList(), selectedDielectric, showFieldLines) {
    if (showFieldLines) {
      ElectricPhysicsEngine.traceFieldLines(
        charges = charges,
        dielectric = selectedDielectric,
        linesPerCharge = 12,
        boundsLimitMeters = 0.35,
        stepSizeMeters = 0.005,
        maxSteps = 80
      )
    } else {
      emptyList()
    }
  }

  // Calculated Equipotential Contours
  val calculatedContours = remember(charges.toList(), selectedDielectric, showEquipotentialContours) {
    if (showEquipotentialContours) {
      ElectricPhysicsEngine.generateEquipotentialContours(
        charges = charges,
        dielectric = selectedDielectric,
        contourLevelsCount = 5
      )
    } else {
      emptyList()
    }
  }

  // Equipotential contour passing through the test probe position
  val probeContour = remember(probePosition, charges.toList(), selectedDielectric, showProbeEquipotential) {
    if (showProbeEquipotential && charges.isNotEmpty()) {
      ElectricPhysicsEngine.traceEquipotentialCurve(
        seed = probePosition,
        charges = charges,
        dielectric = selectedDielectric,
        boundsLimitMeters = 0.35,
        stepSizeMeters = 0.004,
        maxSteps = 120
      )
    } else {
      emptyList()
    }
  }

  fun loadPreset(presetName: String) {
    charges.clear()
    when (presetName) {
      "POINT_CHARGE" -> {
        charges.add(PointCharge("q1", Vector2D(0.0, 0.0), 4.0))
        selectedChargeId = "q1"
      }
      "DIPOLE" -> {
        charges.add(PointCharge("q1", Vector2D(-0.08, 0.0), 3.0))
        charges.add(PointCharge("q2", Vector2D(0.08, 0.0), -3.0))
        selectedChargeId = "q1"
      }
      "LIKE_CHARGES" -> {
        charges.add(PointCharge("q1", Vector2D(-0.08, 0.0), 3.0))
        charges.add(PointCharge("q2", Vector2D(0.08, 0.0), 3.0))
        selectedChargeId = "q1"
      }
      "QUADRUPOLE" -> {
        val d = 0.07
        charges.add(PointCharge("q1", Vector2D(-d, d), 3.0))
        charges.add(PointCharge("q2", Vector2D(d, d), -3.0))
        charges.add(PointCharge("q3", Vector2D(-d, -d), -3.0))
        charges.add(PointCharge("q4", Vector2D(d, -d), 3.0))
        selectedChargeId = "q1"
      }
      "PARALLEL_PLATES" -> {
        for (i in -2..2) {
          val y = i * 0.035
          charges.add(PointCharge("qp$i", Vector2D(-0.08, y), 1.5))
          charges.add(PointCharge("qn$i", Vector2D(0.08, y), -1.5))
        }
        selectedChargeId = "qp0"
      }
    }
    probePosition = Vector2D(0.0, 0.08)
  }

  fun resetExperiment() {
    loadPreset("DIPOLE")
    selectedDielectric = PhysicsConstants.DIELECTRIC_MATERIALS.first()
    testChargeMagnitudeNC = 1.0
    viewScale = 1.0f
    showFieldLines = true
    showEquipotentialContours = true
    showProbeEquipotential = true
    showFieldVectors = false
  }

  LabScaffold(
    title = "Electric Field & Potential Lab",
    subtitle = "Coulomb's Law, Superposition, Field Lines & Equipotentials",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetExperiment() },
    onSaveToNotebook = {
      val settings = mapOf(
        "Dielectric" to selectedDielectric.name,
        "Relative Permittivity ε_r" to String.format(Locale.US, "%.2f", selectedDielectric.relativePermittivity),
        "Total Charges" to "${charges.size}",
        "Probe Position" to String.format(Locale.US, "(%.3f, %.3f) m", probePosition.x, probePosition.y),
        "Test Charge q_test" to String.format(Locale.US, "%+.2f nC", testChargeMagnitudeNC)
      )
      val measurements = mapOf(
        "Field Magnitude |E|" to String.format(Locale.US, "%.3e V/m", probeResult.fieldVector.magnitude),
        "E-Field Angle θ_E" to String.format(Locale.US, "%.1f°", Math.toDegrees(probeResult.fieldVector.angle)),
        "Electric Potential V" to String.format(Locale.US, "%+.2f V", probeResult.potentialVolts),
        "Electric Force |F|" to String.format(Locale.US, "%.3e N", probeResult.forceVector.magnitude),
        "Flux Density |D|" to String.format(Locale.US, "%.3e C/m²", probeResult.fluxDensityD.magnitude)
      )
      onSaveMeasurement(settings, measurements)
    },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("electric_field_canvas")
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(charges.toList(), probePosition, viewScale) {
              detectDragGestures(
                onDragStart = { startPos ->
                  val scale = (size.width / 0.5f) * viewScale
                  val centerX = size.width / 2f
                  val centerY = size.height / 2f

                  // Check if probe was clicked/touched
                  val probeScreenX = centerX + (probePosition.x * scale).toFloat()
                  val probeScreenY = centerY - (probePosition.y * scale).toFloat()
                  val distToProbe = (startPos - Offset(probeScreenX, probeScreenY)).getDistance()

                  if (distToProbe < 40f) {
                    isDraggingProbe = true
                    draggedChargeId = null
                  } else {
                    isDraggingProbe = false
                    // Find nearest charge
                    var closestId: String? = null
                    var minDist = Float.MAX_VALUE
                    for (c in charges) {
                      val cx = centerX + (c.position.x * scale).toFloat()
                      val cy = centerY - (c.position.y * scale).toFloat()
                      val d = (startPos - Offset(cx, cy)).getDistance()
                      if (d < 50f && d < minDist) {
                        minDist = d
                        closestId = c.id
                      }
                    }
                    draggedChargeId = closestId
                    if (closestId != null) {
                      selectedChargeId = closestId
                    }
                  }
                },
                onDrag = { change, dragAmount ->
                  change.consume()
                  val scale = (size.width / 0.5f) * viewScale
                  val deltaX = (dragAmount.x / scale).toDouble()
                  val deltaY = (-dragAmount.y / scale).toDouble()

                  if (isDraggingProbe) {
                    probePosition = Vector2D(
                      (probePosition.x + deltaX).coerceIn(-0.24, 0.24),
                      (probePosition.y + deltaY).coerceIn(-0.24, 0.24)
                    )
                  } else if (draggedChargeId != null) {
                    val idx = charges.indexOfFirst { it.id == draggedChargeId }
                    if (idx >= 0) {
                      val curr = charges[idx]
                      charges[idx] = curr.copy(
                        position = Vector2D(
                          (curr.position.x + deltaX).coerceIn(-0.24, 0.24),
                          (curr.position.y + deltaY).coerceIn(-0.24, 0.24)
                        )
                      )
                    }
                  }
                },
                onDragEnd = {
                  isDraggingProbe = false
                  draggedChargeId = null
                },
                onDragCancel = {
                  isDraggingProbe = false
                  draggedChargeId = null
                }
              )
            }
        ) {
          val canvasWidth = size.width
          val canvasHeight = size.height
          val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
          val simMeterScale = (canvasWidth / 0.5f) * viewScale

          // 1. Grid Lines
          val gridStepM = 0.05
          val gridStepPx = (gridStepM * simMeterScale).toFloat()
          var xLine = center.x % gridStepPx
          while (xLine < canvasWidth) {
            drawLine(CanvasGridLine.copy(alpha = 0.4f), Offset(xLine, 0f), Offset(xLine, canvasHeight), 1f)
            xLine += gridStepPx
          }
          var yLine = center.y % gridStepPx
          while (yLine < canvasHeight) {
            drawLine(CanvasGridLine.copy(alpha = 0.4f), Offset(0f, yLine), Offset(canvasWidth, yLine), 1f)
            yLine += gridStepPx
          }

          // 2. Vector Field Grid (Arrows)
          if (showFieldVectors && charges.isNotEmpty()) {
            val vSpacing = 28.dp.toPx()
            var gx = vSpacing / 2f
            while (gx < canvasWidth) {
              var gy = vSpacing / 2f
              while (gy < canvasHeight) {
                val worldX = ((gx - center.x) / simMeterScale).toDouble()
                val worldY = (-(gy - center.y) / simMeterScale).toDouble()
                val eval = ElectricPhysicsEngine.evaluatePointField(
                  probePosition = Vector2D(worldX, worldY),
                  charges = charges,
                  dielectric = selectedDielectric
                )

                val mag = eval.fieldVector.magnitude
                if (mag > 10.0) {
                  val angle = eval.fieldVector.angle.toFloat()
                  val logMag = ln(mag).toFloat()
                  val normIntensity = ((logMag - 8f) / 12f).coerceIn(0.15f, 1f)
                  val arrowLen = (12f + normIntensity * 16f).coerceIn(10f, 24f)

                  val start = Offset(gx, gy)
                  val end = Offset(start.x + arrowLen * cos(-angle), start.y + arrowLen * sin(-angle))
                  val arrowColor = ElectricCyan.copy(alpha = normIntensity.coerceIn(0.2f, 0.7f))
                  drawLine(arrowColor, start, end, strokeWidth = 1.5f, cap = StrokeCap.Round)
                }
                gy += vSpacing
              }
              gx += vSpacing
            }
          }

          // 3. Render Calculated Continuous Field Lines
          if (showFieldLines) {
            for (line in calculatedFieldLines) {
              if (line.points.size < 2) continue
              val path = Path()
              var first = true
              for (pt in line.points) {
                val px = center.x + (pt.x * simMeterScale).toFloat()
                val py = center.y - (pt.y * simMeterScale).toFloat()
                if (first) {
                  path.moveTo(px, py)
                  first = false
                } else {
                  path.lineTo(px, py)
                }
              }

              val lineColor = if (line.isPositiveOrigin) ElectricCyan.copy(alpha = 0.65f) else ElectricBlue.copy(alpha = 0.55f)
              drawPath(path = path, color = lineColor, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

              // Direction arrowhead along midpoint
              if (line.points.size > 8) {
                val midIdx = line.points.size / 2
                val pPrev = line.points[midIdx - 1]
                val pNext = line.points[midIdx]
                val dx = pNext.x - pPrev.x
                val dy = pNext.y - pPrev.y
                val segAngle = atan2(-dy, dx).toFloat()

                val arrowCenter = Offset(
                  center.x + (pNext.x * simMeterScale).toFloat(),
                  center.y - (pNext.y * simMeterScale).toFloat()
                )
                val headLen = 7f
                val h1 = segAngle + (PI.toFloat() * 0.82f)
                val h2 = segAngle - (PI.toFloat() * 0.82f)
                drawLine(lineColor, arrowCenter, Offset(arrowCenter.x + headLen * cos(h1), arrowCenter.y + headLen * sin(h1)), 1.8f)
                drawLine(lineColor, arrowCenter, Offset(arrowCenter.x + headLen * cos(h2), arrowCenter.y + headLen * sin(h2)), 1.8f)
              }
            }
          }

          // 4. Render Calculated Equipotential Contours
          if (showEquipotentialContours) {
            for (contour in calculatedContours) {
              if (contour.points.size < 3) continue
              val path = Path()
              var first = true
              for (pt in contour.points) {
                val px = center.x + (pt.x * simMeterScale).toFloat()
                val py = center.y - (pt.y * simMeterScale).toFloat()
                if (first) {
                  path.moveTo(px, py)
                  first = false
                } else {
                  path.lineTo(px, py)
                }
              }

              drawPath(
                path = path,
                color = EquipotentialPurple.copy(alpha = 0.45f),
                style = Stroke(
                  width = 1.5f,
                  pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
              )
            }
          }

          // 5. Render Probe Equipotential Contour (Passing directly through probe)
          if (showProbeEquipotential && probeContour.size > 2) {
            val probePath = Path()
            var first = true
            for (pt in probeContour) {
              val px = center.x + (pt.x * simMeterScale).toFloat()
              val py = center.y - (pt.y * simMeterScale).toFloat()
              if (first) {
                probePath.moveTo(px, py)
                first = false
              } else {
                probePath.lineTo(px, py)
              }
            }
            drawPath(
              path = probePath,
              color = Color(0xFFFFD54F).copy(alpha = 0.8f),
              style = Stroke(
                width = 2.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
              )
            )
          }

          // 6. Draw Point Charges
          charges.forEach { charge ->
            val screenX = center.x + (charge.position.x * simMeterScale).toFloat()
            val screenY = center.y - (charge.position.y * simMeterScale).toFloat()
            val isSelected = charge.id == selectedChargeId

            val chargeColor = if (charge.isPositive) ChargePositiveRed else ChargeNegativeBlue

            // Halo for selection
            if (isSelected) {
              drawCircle(Color.White.copy(alpha = 0.45f), radius = 24.dp.toPx(), center = Offset(screenX, screenY))
            }
            drawCircle(chargeColor.copy(alpha = 0.35f), radius = 18.dp.toPx(), center = Offset(screenX, screenY))
            drawCircle(chargeColor, radius = 12.dp.toPx(), center = Offset(screenX, screenY))

            // Plus or minus symbol
            val symLen = 6.dp.toPx()
            drawLine(Color.White, Offset(screenX - symLen, screenY), Offset(screenX + symLen, screenY), strokeWidth = 2.5f)
            if (charge.isPositive) {
              drawLine(Color.White, Offset(screenX, screenY - symLen), Offset(screenX, screenY + symLen), strokeWidth = 2.5f)
            }
          }

          // 7. Draw Draggable Test Charge Probe
          val probeScreenX = center.x + (probePosition.x * simMeterScale).toFloat()
          val probeScreenY = center.y - (probePosition.y * simMeterScale).toFloat()
          val probeCenter = Offset(probeScreenX, probeScreenY)

          val testChargeColor = if (testChargeMagnitudeNC >= 0) Color(0xFFFFB300) else Color(0xFF64B5F6)
          drawCircle(testChargeColor.copy(alpha = 0.3f), radius = 18.dp.toPx(), center = probeCenter)
          drawCircle(testChargeColor, radius = 8.dp.toPx(), center = probeCenter)

          // Electric Force Vector F = q * E
          val fMag = probeResult.forceVector.magnitude
          if (fMag > 1e-12) {
            val fAngle = probeResult.forceVector.angle.toFloat()
            val fArrowLen = 42f
            val fEnd = Offset(
              probeScreenX + fArrowLen * cos(-fAngle),
              probeScreenY + fArrowLen * sin(-fAngle)
            )
            // Force arrow
            drawLine(Color(0xFFFFD54F), probeCenter, fEnd, strokeWidth = 3.5f, cap = StrokeCap.Round)
            // Arrowhead
            val a1 = -fAngle + (PI.toFloat() * 0.82f)
            val a2 = -fAngle - (PI.toFloat() * 0.82f)
            val hLen = 10f
            drawLine(Color(0xFFFFD54F), fEnd, Offset(fEnd.x + hLen * cos(a1), fEnd.y + hLen * sin(a1)), 3.5f)
            drawLine(Color(0xFFFFD54F), fEnd, Offset(fEnd.x + hLen * cos(a2), fEnd.y + hLen * sin(a2)), 3.5f)
          }
        }

        // Help text overlay
        Text(
          text = "Cyan: Field Lines | Purple: Equipotentials | Gold: Test Probe & Force F",
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
          fontSize = 11.sp,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
        )

        if (probeResult.isNearSingularity) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
              .background(Color(0xFFE53935).copy(alpha = 0.9f), RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "⚠️ Singularity Cutoff Active (r < 1.5 cm)",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
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
          symbol = "|E|",
          value = String.format(Locale.US, "%.3e", probeResult.fieldVector.magnitude),
          unit = "V/m",
          label = "Electric Field",
          accentColor = ElectricCyan
        )
        MeasurementBadge(
          symbol = "V",
          value = String.format(Locale.US, "%+.1f", probeResult.potentialVolts),
          unit = "Volts",
          label = "Electric Potential",
          accentColor = EquipotentialPurple
        )
        MeasurementBadge(
          symbol = "|F|",
          value = String.format(Locale.US, "%.3e", probeResult.forceVector.magnitude),
          unit = "Newtons",
          label = "Force on Test Charge",
          accentColor = Color(0xFFFFD54F)
        )
        MeasurementBadge(
          symbol = "q_test",
          value = String.format(Locale.US, "%+.2f", testChargeMagnitudeNC),
          unit = "nC",
          label = "Test Charge",
          accentColor = if (testChargeMagnitudeNC >= 0) Color(0xFFFFB300) else Color(0xFF64B5F6)
        )
        MeasurementBadge(
          symbol = "θ_E",
          value = String.format(Locale.US, "%.1f", Math.toDegrees(probeResult.fieldVector.angle)),
          unit = "deg",
          label = "Field Vector Angle",
          accentColor = ElectricBlue
        )
        MeasurementBadge(
          symbol = "|D|",
          value = String.format(Locale.US, "%.3e", probeResult.fluxDensityD.magnitude),
          unit = "C/m²",
          label = "Electric Flux Density",
          accentColor = Color(0xFF81D4FA)
        )
      }
    },
    theoryContent = {
      EquationCard(
        title = "Coulomb's Law with Superposition",
        formula = "E = ∑_{i} [ (1 / 4πε) * (q_i / r_i²) * r̂_i ]",
        description = "By the principle of linear superposition, the total electric field vector at any point is the exact vector sum of fields produced by all individual point charges.",
        assumptions = "Static point charges in linear, isotropic, homogeneous dielectric medium."
      )
      Spacer(modifier = Modifier.height(6.dp))
      EquationCard(
        title = "Electric Force on Test Charge",
        formula = "F = q_{test} · E",
        description = "If the test charge is positive (q > 0), the force is collinear and parallel to E. If negative (q < 0), the force points opposite to E.",
        accentColor = Color(0xFFFFD54F)
      )
      Spacer(modifier = Modifier.height(6.dp))
      EquationCard(
        title = "Equipotential Contours & Orthogonality",
        formula = "E = -∇V  ⟹  E · t_{contour} = 0,    W = -q ΔV = 0",
        description = "Because electric field is the negative gradient of potential, electric field lines are always strictly perpendicular (orthogonal) to equipotential lines at every intersection. Zero net work is done moving a charge along an equipotential.",
        accentColor = EquipotentialPurple
      )
    },
    controlsContent = {
      // 1. Preset Charge Configurations
      Text("Load Standard Laboratory Presets:", style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf(
          "DIPOLE" to "Dipole (+q, -q)",
          "LIKE_CHARGES" to "Like (+q, +q)",
          "POINT_CHARGE" to "Point (+q)",
          "QUADRUPOLE" to "Quadrupole",
          "PARALLEL_PLATES" to "Parallel Plates"
        ).forEach { (key, label) ->
          OutlinedButton(
            onClick = { loadPreset(key) },
            modifier = Modifier.height(34.dp).testTag("preset_$key")
          ) {
            Text(label, fontSize = 11.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 2. Manage Point Charges
      Text("Manage Point Charges:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        charges.forEachIndexed { index, charge ->
          FilterChip(
            selected = charge.id == selectedChargeId,
            onClick = { selectedChargeId = charge.id },
            label = {
              Text(
                text = "q${index + 1}: ${String.format(Locale.US, "%+.1f", charge.chargeMicroCoulombs)} µC",
                fontSize = 11.sp
              )
            },
            modifier = Modifier.testTag("charge_chip_${charge.id}")
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = {
            val newId = UUID.randomUUID().toString().take(4)
            charges.add(PointCharge(id = newId, position = Vector2D(0.0, 0.0), chargeMicroCoulombs = 3.0))
            selectedChargeId = newId
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChargePositiveRed),
          modifier = Modifier.testTag("add_positive_charge_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Positive")
          Spacer(modifier = Modifier.width(4.dp))
          Text("+ Charge")
        }

        Button(
          onClick = {
            val newId = UUID.randomUUID().toString().take(4)
            charges.add(PointCharge(id = newId, position = Vector2D(0.0, 0.0), chargeMicroCoulombs = -3.0))
            selectedChargeId = newId
          },
          colors = ButtonDefaults.buttonColors(containerColor = ChargeNegativeBlue),
          modifier = Modifier.testTag("add_negative_charge_button")
        ) {
          Icon(Icons.Default.Remove, contentDescription = "Add Negative")
          Spacer(modifier = Modifier.width(4.dp))
          Text("- Charge")
        }

        if (charges.size > 1 && selectedChargeId != null) {
          IconButton(
            onClick = {
              charges.removeAll { it.id == selectedChargeId }
              selectedChargeId = charges.firstOrNull()?.id
            },
            modifier = Modifier.testTag("delete_charge_button")
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Remove Charge", tint = Color(0xFFFF5252))
          }
        }
      }

      // 3. Selected Charge Magnitude Slider
      val selectedCharge = charges.find { it.id == selectedChargeId }
      if (selectedCharge != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Selected Charge Magnitude:", style = MaterialTheme.typography.bodySmall)
          Text(
            text = "${String.format(Locale.US, "%+.1f", selectedCharge.chargeMicroCoulombs)} µC",
            fontWeight = FontWeight.Bold,
            color = if (selectedCharge.isPositive) ChargePositiveRed else ChargeNegativeBlue,
            fontFamily = FontFamily.Monospace
          )
        }
        Slider(
          value = selectedCharge.chargeMicroCoulombs.toFloat(),
          onValueChange = { newVal ->
            val idx = charges.indexOfFirst { it.id == selectedChargeId }
            if (idx >= 0) {
              charges[idx] = selectedCharge.copy(chargeMicroCoulombs = newVal.toDouble())
            }
          },
          valueRange = -10.0f..10.0f,
          colors = SliderDefaults.colors(
            thumbColor = if (selectedCharge.isPositive) ChargePositiveRed else ChargeNegativeBlue,
            activeTrackColor = ElectricCyan
          ),
          modifier = Modifier.testTag("charge_magnitude_slider")
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 4. Test Charge Magnitude Slider (Can be positive or negative)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Test Probe Charge (q_test):", style = MaterialTheme.typography.bodySmall)
        Text(
          text = String.format(Locale.US, "%+.2f nC", testChargeMagnitudeNC),
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFFD54F),
          fontFamily = FontFamily.Monospace
        )
      }
      Slider(
        value = testChargeMagnitudeNC.toFloat(),
        onValueChange = { testChargeMagnitudeNC = it.toDouble() },
        valueRange = -10.0f..10.0f,
        colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD54F), activeTrackColor = Color(0xFFFFD54F)),
        modifier = Modifier.testTag("test_charge_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 5. Dielectric Medium Dropdown
      Text("Dielectric Medium (Permittivity ε = ε_r · ε_0):", style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(4.dp))
      ExposedDropdownMenuBox(
        expanded = dielectricDropdownExpanded,
        onExpandedChange = { dielectricDropdownExpanded = it }
      ) {
        TextField(
          value = "${selectedDielectric.name} (ε_r = ${selectedDielectric.relativePermittivity})",
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dielectricDropdownExpanded) },
          colors = ExposedDropdownMenuDefaults.textFieldColors(),
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("dielectric_dropdown")
        )
        ExposedDropdownMenu(
          expanded = dielectricDropdownExpanded,
          onDismissRequest = { dielectricDropdownExpanded = false }
        ) {
          PhysicsConstants.DIELECTRIC_MATERIALS.forEach { mat ->
            DropdownMenuItem(
              text = { Text("${mat.name} (ε_r = ${mat.relativePermittivity})") },
              onClick = {
                selectedDielectric = mat
                dielectricDropdownExpanded = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 6. Visual Layer Toggles
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Render Calculated Field Lines (Streamlines)")
        Switch(
          checked = showFieldLines,
          onCheckedChange = { showFieldLines = it },
          modifier = Modifier.testTag("toggle_field_lines")
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Render Equipotential Contours (V = const)")
        Switch(
          checked = showEquipotentialContours,
          onCheckedChange = { showEquipotentialContours = it },
          modifier = Modifier.testTag("toggle_equipotential_contours")
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Draw Probe Equipotential Line")
        Switch(
          checked = showProbeEquipotential,
          onCheckedChange = { showProbeEquipotential = it },
          modifier = Modifier.testTag("toggle_probe_equipotential")
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Render Field Vector Grid (Arrows)")
        Switch(
          checked = showFieldVectors,
          onCheckedChange = { showFieldVectors = it },
          modifier = Modifier.testTag("toggle_field_vectors")
        )
      }
    }
  )
}
