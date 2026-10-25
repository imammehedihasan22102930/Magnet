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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MagneticMaterial
import com.example.model.MagneticSourceType
import com.example.model.PhysicsConstants
import com.example.model.Vector2D
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MagneticFieldScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var sourceType by remember { mutableStateOf(MagneticSourceType.STRAIGHT_WIRE) }

  // Parameters
  var currentAmperes by remember { mutableDoubleStateOf(10.0) }
  var solenoidTurns by remember { mutableIntStateOf(500) }
  var solenoidLengthMeters by remember { mutableDoubleStateOf(0.20) }
  var solenoidRadiusMeters by remember { mutableDoubleStateOf(0.04) }

  // Probe Position (meters)
  var probePos by remember { mutableStateOf(Vector2D(0.05, 0.05)) }

  // Core/Surrounding Material
  var selectedMaterial by remember { mutableStateOf(PhysicsConstants.MAGNETIC_MATERIALS.first()) }
  var materialDropdownExpanded by remember { mutableStateOf(false) }

  // Toggles
  var showFieldVectors by remember { mutableStateOf(true) }

  // Evaluate magnetic field at probe
  val probeResult = remember(sourceType, probePos, currentAmperes, solenoidTurns, solenoidLengthMeters, solenoidRadiusMeters, selectedMaterial) {
    when (sourceType) {
      MagneticSourceType.STRAIGHT_WIRE -> {
        MagneticPhysicsEngine.straightWireField(probePos, Vector2D.ZERO, currentAmperes, selectedMaterial)
      }
      MagneticSourceType.PARALLEL_WIRES -> {
        MagneticPhysicsEngine.parallelWiresField(
          probePos,
          Vector2D(-0.06, 0.0), currentAmperes,
          Vector2D(0.06, 0.0), currentAmperes,
          selectedMaterial
        )
      }
      MagneticSourceType.CIRCULAR_LOOP -> {
        val bAxis = MagneticPhysicsEngine.circularLoopOnAxisB(probePos.x, solenoidRadiusMeters, currentAmperes, selectedMaterial)
        MagneticPhysicsEngine.straightWireField(probePos, Vector2D.ZERO, currentAmperes, selectedMaterial).copy(
          bMagnitude = bAxis,
          bVector = Vector2D(bAxis, 0.0)
        )
      }
      MagneticSourceType.SOLENOID_FINITE -> {
        val bVal = MagneticPhysicsEngine.finiteSolenoidOnAxisB(
          probePos.x, solenoidLengthMeters, solenoidRadiusMeters, solenoidTurns, currentAmperes, selectedMaterial
        )
        val hVal = bVal / selectedMaterial.absolutePermeability
        com.example.model.MagneticFieldResult(
          bVector = Vector2D(bVal, 0.0),
          hVector = Vector2D(hVal, 0.0),
          bMagnitude = kotlin.math.abs(bVal),
          hMagnitude = kotlin.math.abs(hVal),
          isNearConductor = false
        )
      }
      MagneticSourceType.SOLENOID_IDEAL -> {
        val (bVal, hVal) = MagneticPhysicsEngine.idealSolenoidField(
          solenoidTurns, solenoidLengthMeters, currentAmperes, selectedMaterial
        )
        com.example.model.MagneticFieldResult(
          bVector = Vector2D(bVal, 0.0),
          hVector = Vector2D(hVal, 0.0),
          bMagnitude = bVal,
          hMagnitude = hVal,
          isNearConductor = false
        )
      }
      MagneticSourceType.BAR_MAGNET -> {
        MagneticPhysicsEngine.barMagnetDipoleField(probePos, Vector2D.ZERO, dipoleMomentM = currentAmperes * 0.5)
      }
    }
  }

  fun resetFields() {
    currentAmperes = 10.0
    solenoidTurns = 500
    solenoidLengthMeters = 0.20
    probePos = Vector2D(0.05, 0.05)
    selectedMaterial = PhysicsConstants.MAGNETIC_MATERIALS.first()
  }

  LabScaffold(
    title = "Magnetic Field & Biot-Savart Lab",
    subtitle = "Conductors, Coils, Solenoids, Dipoles & Permeability",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetFields() },
    onSaveToNotebook = {
      val settings = mapOf(
        "Source Type" to sourceType.displayName,
        "Current I" to "$currentAmperes A",
        "Turns N" to "$solenoidTurns",
        "Material" to selectedMaterial.name,
        "Probe Position" to String.format(Locale.US, "(%.3f, %.3f) m", probePos.x, probePos.y)
      )
      val measurements = mapOf(
        "Flux Density |B|" to String.format(Locale.US, "%.3e T", probeResult.bMagnitude),
        "Field Intensity |H|" to String.format(Locale.US, "%.2f A/m", probeResult.hMagnitude),
        "Permeability µ_r" to "${selectedMaterial.relativePermeability}"
      )
      onSaveMeasurement(settings, measurements)
    },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("magnetic_field_canvas")
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(sourceType) {
              detectDragGestures { change, dragAmount ->
                change.consume()
                val scale = size.width / 0.4f
                val deltaX = (dragAmount.x / scale).toDouble()
                val deltaY = (-dragAmount.y / scale).toDouble()
                probePos = Vector2D(probePos.x + deltaX, probePos.y + deltaY)
              }
            }
        ) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)
          val meterScale = canvasW / 0.4f // 0.4m total width

          // 1. Draw Grid
          val gridStepPx = 0.05f * meterScale
          var xL = center.x % gridStepPx
          while (xL < canvasW) {
            drawLine(CanvasGridLine.copy(alpha = 0.35f), Offset(xL, 0f), Offset(xL, canvasH), 1f)
            xL += gridStepPx
          }
          var yL = center.y % gridStepPx
          while (yL < canvasH) {
            drawLine(CanvasGridLine.copy(alpha = 0.35f), Offset(0f, yL), Offset(canvasW, yL), 1f)
            yL += gridStepPx
          }

          // 2. Draw Source Geometry
          when (sourceType) {
            MagneticSourceType.STRAIGHT_WIRE -> {
              // Wire cross section at origin
              drawCircle(Color(0xFFB0BEC5), radius = 14.dp.toPx(), center = center)
              drawCircle(Color(0xFFFF9800), radius = 10.dp.toPx(), center = center)
              // Current out of screen dot
              drawCircle(Color.White, radius = 3.dp.toPx(), center = center)
            }
            MagneticSourceType.PARALLEL_WIRES -> {
              val w1 = Offset(center.x - 0.06f * meterScale, center.y)
              val w2 = Offset(center.x + 0.06f * meterScale, center.y)
              drawCircle(Color(0xFFFF9800), radius = 10.dp.toPx(), center = w1)
              drawCircle(Color(0xFFFF9800), radius = 10.dp.toPx(), center = w2)
              drawCircle(Color.White, radius = 3.dp.toPx(), center = w1)
              drawCircle(Color.White, radius = 3.dp.toPx(), center = w2)
            }
            MagneticSourceType.CIRCULAR_LOOP -> {
              drawCircle(Color(0xFFFF9800), radius = (solenoidRadiusMeters.toFloat() * meterScale), center = center, style = Stroke(width = 4f))
            }
            MagneticSourceType.SOLENOID_FINITE, MagneticSourceType.SOLENOID_IDEAL -> {
              val sLenPx = solenoidLengthMeters.toFloat() * meterScale
              val sRadPx = solenoidRadiusMeters.toFloat() * meterScale
              val rectLeft = center.x - sLenPx / 2f
              val rectTop = center.y - sRadPx
              drawRect(
                color = MagneticAmber.copy(alpha = 0.2f),
                topLeft = Offset(rectLeft, rectTop),
                size = androidx.compose.ui.geometry.Size(sLenPx, sRadPx * 2f)
              )
              drawRect(
                color = MagneticAmber,
                topLeft = Offset(rectLeft, rectTop),
                size = androidx.compose.ui.geometry.Size(sLenPx, sRadPx * 2f),
                style = Stroke(width = 2.5f)
              )
            }
            MagneticSourceType.BAR_MAGNET -> {
              val mHalfW = 0.04f * meterScale
              val mHalfH = 0.02f * meterScale
              // South half (Blue)
              drawRect(
                Color(0xFF1E88E5),
                topLeft = Offset(center.x - mHalfW, center.y - mHalfH),
                size = androidx.compose.ui.geometry.Size(mHalfW, mHalfH * 2f)
              )
              // North half (Red)
              drawRect(
                Color(0xFFE53935),
                topLeft = Offset(center.x, center.y - mHalfH),
                size = androidx.compose.ui.geometry.Size(mHalfW, mHalfH * 2f)
              )
            }
          }

          // 3. Draw Vector Field Arrows
          if (showFieldVectors) {
            val vSpacing = 32.dp.toPx()
            var gx = vSpacing / 2f
            while (gx < canvasW) {
              var gy = vSpacing / 2f
              while (gy < canvasH) {
                val worldX = ((gx - center.x) / meterScale).toDouble()
                val worldY = (-(gy - center.y) / meterScale).toDouble()

                val eval = when (sourceType) {
                  MagneticSourceType.STRAIGHT_WIRE ->
                    MagneticPhysicsEngine.straightWireField(Vector2D(worldX, worldY), Vector2D.ZERO, currentAmperes, selectedMaterial)
                  MagneticSourceType.PARALLEL_WIRES ->
                    MagneticPhysicsEngine.parallelWiresField(
                      Vector2D(worldX, worldY),
                      Vector2D(-0.06, 0.0), currentAmperes,
                      Vector2D(0.06, 0.0), currentAmperes,
                      selectedMaterial
                    )
                  MagneticSourceType.BAR_MAGNET ->
                    MagneticPhysicsEngine.barMagnetDipoleField(Vector2D(worldX, worldY), Vector2D.ZERO, currentAmperes * 0.5)
                  else -> null
                }

                if (eval != null && eval.bMagnitude > 1e-7) {
                  val angle = eval.bVector.angle.toFloat()
                  val logMag = ln(eval.bMagnitude).toFloat()
                  val normIntensity = ((logMag + 12f) / 10f).coerceIn(0.2f, 1f)
                  val arrowLen = (12f + normIntensity * 14f).coerceIn(10f, 24f)

                  val start = Offset(gx, gy)
                  val end = Offset(start.x + arrowLen * cos(-angle), start.y + arrowLen * sin(-angle))
                  val color = MagneticAmber.copy(alpha = normIntensity.coerceIn(0.25f, 0.85f))
                  drawLine(color, start, end, strokeWidth = 1.8f, cap = StrokeCap.Round)
                }
                gy += vSpacing
              }
              gx += vSpacing
            }
          }

          // 4. Draw Draggable Magnetic Probe
          val probeScreenX = center.x + (probePos.x.toFloat() * meterScale)
          val probeScreenY = center.y - (probePos.y.toFloat() * meterScale)
          val probeCenter = Offset(probeScreenX, probeScreenY)

          drawCircle(ElectricCyan.copy(alpha = 0.3f), radius = 16.dp.toPx(), center = probeCenter)
          drawCircle(ElectricCyan, radius = 7.dp.toPx(), center = probeCenter)

          // Magnetic B vector arrow from probe
          val bMag = probeResult.bMagnitude
          if (bMag > 1e-9) {
            val bAngle = probeResult.bVector.angle.toFloat()
            val bArrowLen = 42f
            val bEnd = Offset(probeScreenX + bArrowLen * cos(-bAngle), probeScreenY + bArrowLen * sin(-bAngle))
            drawLine(MagneticOrange, probeCenter, bEnd, strokeWidth = 3f, cap = StrokeCap.Round)
          }
        }

        Text(
          text = "Drag cyan probe across field. Amber arrows: Magnetic Flux Density B.",
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
          symbol = "|B|",
          value = String.format(Locale.US, "%.3e", probeResult.bMagnitude),
          unit = "Tesla (T)",
          label = "Magnetic Flux Density",
          accentColor = MagneticAmber
        )
        MeasurementBadge(
          symbol = "|H|",
          value = String.format(Locale.US, "%.2f", probeResult.hMagnitude),
          unit = "A/m",
          label = "Magnetic Field Intensity",
          accentColor = MagneticOrange
        )
        MeasurementBadge(
          symbol = "µ_r",
          value = "${selectedMaterial.relativePermeability}",
          unit = "rel",
          label = "Relative Permeability",
          accentColor = ElectricCyan
        )
      }
    },
    theoryContent = {
      EquationCard(
        title = "Ampère's Law (Straight Conductor)",
        formula = "H = I / (2πr),    B = µ · H = (µ_r · µ_0) · H",
        description = "B is the magnetic flux density (Tesla) which includes material magnetization. H is the magnetic field intensity (A/m) set up by electric currents.",
        accentColor = MagneticAmber
      )
      Spacer(modifier = Modifier.height(6.dp))
      EquationCard(
        title = "Solenoid: Real Finite vs Ideal Long",
        formula = "B_{ideal} = µ · n · I,    B_{finite}(z) = (µnI/2)[cos θ_1 - cos θ_2]",
        description = "Inside an ideal infinitely long solenoid, the field is perfectly uniform. In a real finite solenoid, the field decays towards the ends to exactly 50% of the center value.",
        accentColor = MagneticOrange
      )
    },
    controlsContent = {
      // Source Type Chips
      Text("Select Magnetic Source Geometry:", style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        MagneticSourceType.values().forEach { st ->
          FilterChip(
            selected = sourceType == st,
            onClick = { sourceType = st },
            label = { Text(st.displayName, fontSize = 11.sp) },
            modifier = Modifier.testTag("mag_source_${st.name}")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Current Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Conductor Current (I):", style = MaterialTheme.typography.bodySmall)
        Text("${currentAmperes.toInt()} A", fontWeight = FontWeight.Bold, color = MagneticAmber)
      }
      Slider(
        value = currentAmperes.toFloat(),
        onValueChange = { currentAmperes = it.toDouble() },
        valueRange = 0.5f..50.0f,
        colors = SliderDefaults.colors(thumbColor = MagneticAmber, activeTrackColor = MagneticAmber),
        modifier = Modifier.testTag("magnetic_current_slider")
      )

      if (sourceType == MagneticSourceType.SOLENOID_FINITE || sourceType == MagneticSourceType.SOLENOID_IDEAL) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Solenoid Turns (N):", style = MaterialTheme.typography.bodySmall)
          Text("$solenoidTurns turns", fontWeight = FontWeight.Bold, color = MagneticOrange)
        }
        Slider(
          value = solenoidTurns.toFloat(),
          onValueChange = { solenoidTurns = it.toInt() },
          valueRange = 50f..2000f,
          colors = SliderDefaults.colors(thumbColor = MagneticOrange, activeTrackColor = MagneticOrange),
          modifier = Modifier.testTag("solenoid_turns_slider")
        )
      }

      // Material Dropdown
      Spacer(modifier = Modifier.height(10.dp))
      Text("Magnetic Core Material:", style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(4.dp))
      ExposedDropdownMenuBox(
        expanded = materialDropdownExpanded,
        onExpandedChange = { materialDropdownExpanded = it }
      ) {
        TextField(
          value = "${selectedMaterial.name} (µ_r = ${selectedMaterial.relativePermeability})",
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialDropdownExpanded) },
          colors = ExposedDropdownMenuDefaults.textFieldColors(),
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("magnetic_material_dropdown")
        )
        ExposedDropdownMenu(
          expanded = materialDropdownExpanded,
          onDismissRequest = { materialDropdownExpanded = false }
        ) {
          PhysicsConstants.MAGNETIC_MATERIALS.forEach { mat ->
            DropdownMenuItem(
              text = { Text("${mat.name} (µ_r = ${mat.relativePermeability})") },
              onClick = {
                selectedMaterial = mat
                materialDropdownExpanded = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Display Magnetic Field Vectors (B)")
        Switch(
          checked = showFieldVectors,
          onCheckedChange = { showFieldVectors = it },
          modifier = Modifier.testTag("toggle_mag_vectors")
        )
      }
    }
  )
}
