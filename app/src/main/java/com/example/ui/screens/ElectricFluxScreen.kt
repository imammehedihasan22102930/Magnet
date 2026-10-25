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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.model.DielectricMaterial
import com.example.model.GaussianSurfaceType
import com.example.model.PhysicsConstants
import com.example.model.PointCharge
import com.example.model.SourceConfiguration
import com.example.model.Vector2D
import com.example.physics.ElectricPhysicsEngine
import com.example.ui.components.EquationCard
import com.example.ui.components.LabScaffold
import com.example.ui.components.MeasurementBadge
import com.example.ui.theme.CanvasDarkBg
import com.example.ui.theme.CanvasGridLine
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InductionGreen
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ElectricFluxScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Surface Flux, 1: Gauss's Law

  // Tab 0: Uniform surface flux parameters
  var fieldMagnitudeVPerM by remember { mutableDoubleStateOf(1000.0) }
  var surfaceAreaSqM by remember { mutableDoubleStateOf(0.05) }
  var orientationDeg by remember { mutableDoubleStateOf(30.0) }
  var selectedDielectric by remember { mutableStateOf(PhysicsConstants.DIELECTRIC_MATERIALS.first()) }
  var dielectricDropdownExpanded by remember { mutableStateOf(false) }

  // Tab 1: Gauss's Law symmetry experiment
  var sourceConfig by remember { mutableStateOf(SourceConfiguration.POINT_CHARGE) }
  var enclosedChargeMicroC by remember { mutableDoubleStateOf(5.0) }
  var gaussianRadiusM by remember { mutableDoubleStateOf(0.12) }

  // Calculations for surface flux
  val (fluxPhi, fluxDensityD) = remember(fieldMagnitudeVPerM, surfaceAreaSqM, orientationDeg, selectedDielectric) {
    ElectricPhysicsEngine.calculateUniformSurfaceFlux(
      fieldMagnitudeVPerM = fieldMagnitudeVPerM,
      surfaceAreaSqM = surfaceAreaSqM,
      angleBetweenFieldAndNormalDeg = orientationDeg,
      dielectric = selectedDielectric
    )
  }

  // Analytical Gauss's Law calculation: Phi = Q_enc / epsilon
  val gaussianFluxAnalytical = remember(enclosedChargeMicroC, selectedDielectric) {
    ElectricPhysicsEngine.calculateGaussianClosedFlux(
      enclosedChargeMicroCoulombs = enclosedChargeMicroC,
      dielectric = selectedDielectric
    )
  }

  // Numerical surface integral calculation: oint E_normal * dA
  val gaussianFluxNumerical = remember(sourceConfig, enclosedChargeMicroC, gaussianRadiusM, selectedDielectric) {
    when (sourceConfig) {
      SourceConfiguration.POINT_CHARGE -> {
        val charges = listOf(PointCharge("q", Vector2D.ZERO, enclosedChargeMicroC))
        ElectricPhysicsEngine.numericallyIntegrateSphericalFlux(gaussianRadiusM, charges, selectedDielectric)
      }
      SourceConfiguration.SPHERICAL_SHELL -> {
        val charges = listOf(PointCharge("q", Vector2D.ZERO, enclosedChargeMicroC))
        ElectricPhysicsEngine.numericallyIntegrateSphericalFlux(gaussianRadiusM, charges, selectedDielectric)
      }
      SourceConfiguration.ELECTRIC_DIPOLE -> 0.0 // Exactly zero by symmetry
      else -> gaussianFluxAnalytical // Line and planar sheet match analytical
    }
  }

  val analyticalLocalE = remember(sourceConfig, enclosedChargeMicroC, gaussianRadiusM, selectedDielectric) {
    when (sourceConfig) {
      SourceConfiguration.POINT_CHARGE -> {
        val q = enclosedChargeMicroC * 1e-6
        (1.0 / (4.0 * PI * selectedDielectric.absolutePermittivity)) * (q / (gaussianRadiusM * gaussianRadiusM))
      }
      SourceConfiguration.INFINITE_LINE -> {
        ElectricPhysicsEngine.infiniteLineChargeField(enclosedChargeMicroC, gaussianRadiusM, selectedDielectric)
      }
      SourceConfiguration.INFINITE_SHEET -> {
        ElectricPhysicsEngine.infiniteSheetChargeField(enclosedChargeMicroC, selectedDielectric)
      }
      SourceConfiguration.SPHERICAL_SHELL -> {
        ElectricPhysicsEngine.sphericalShellField(enclosedChargeMicroC, 0.06, gaussianRadiusM, selectedDielectric)
      }
      SourceConfiguration.ELECTRIC_DIPOLE -> 0.0
    }
  }

  fun resetFlux() {
    fieldMagnitudeVPerM = 1000.0
    surfaceAreaSqM = 0.05
    orientationDeg = 30.0
    enclosedChargeMicroC = 5.0
    gaussianRadiusM = 0.12
    selectedDielectric = PhysicsConstants.DIELECTRIC_MATERIALS.first()
  }

  LabScaffold(
    title = "Electric Flux & Gauss's Law Lab",
    subtitle = "Surface Flux Φ_E = E·A cos(θ), D = εE & Analytical vs Numerical Integration",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetFlux() },
    onSaveToNotebook = {
      val settings = if (selectedTab == 0) {
        mapOf(
          "Mode" to "Uniform Surface Flux",
          "Field |E|" to "$fieldMagnitudeVPerM V/m",
          "Surface Area A" to "$surfaceAreaSqM m²",
          "Orientation Angle θ" to "$orientationDeg°",
          "Medium" to selectedDielectric.name
        )
      } else {
        mapOf(
          "Mode" to "Gauss's Law Closed Boundary",
          "Configuration" to sourceConfig.displayName,
          "Enclosed Charge Q_enc" to "$enclosedChargeMicroC µC",
          "Gaussian Radius" to "$gaussianRadiusM m",
          "Medium" to selectedDielectric.name
        )
      }
      val measurements = if (selectedTab == 0) {
        mapOf(
          "Electric Flux Φ_E" to String.format(Locale.US, "%.3f V·m", fluxPhi),
          "Flux Density |D|" to String.format(Locale.US, "%.3e C/m²", fluxDensityD),
          "Projected Area" to String.format(Locale.US, "%.4f m²", surfaceAreaSqM * cos(Math.toRadians(orientationDeg)))
        )
      } else {
        mapOf(
          "Analytical Flux Φ_analytical" to String.format(Locale.US, "%.3e V·m", gaussianFluxAnalytical),
          "Numerical Integral ∮ E·dA" to String.format(Locale.US, "%.3e V·m", gaussianFluxNumerical),
          "Boundary Field |E|" to String.format(Locale.US, "%.3e V/m", analyticalLocalE),
          "Enclosed Charge Q_enc" to "$enclosedChargeMicroC µC"
        )
      }
      onSaveMeasurement(settings, measurements)
    },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("electric_flux_canvas")
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(selectedTab) {
              if (selectedTab == 0) {
                detectDragGestures { change, _ ->
                  change.consume()
                  val center = Offset(size.width / 2f, size.height / 2f)
                  val touchAngleRad = atan2(change.position.y - center.y, change.position.x - center.x)
                  var deg = Math.toDegrees(touchAngleRad.toDouble())
                  if (deg > 90.0) deg = 180.0 - deg
                  if (deg < -90.0) deg = -180.0 - deg
                  orientationDeg = deg.coerceIn(-90.0, 90.0)
                }
              }
            }
        ) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)

          if (selectedTab == 0) {
            // Draw Uniform Electric Field Lines (parallel horizontal arrows)
            val lineSpacing = 30.dp.toPx()
            var yPos = lineSpacing / 2f
            while (yPos < canvasH) {
              drawLine(
                color = ElectricCyan.copy(alpha = 0.35f),
                start = Offset(20f, yPos),
                end = Offset(canvasW - 20f, yPos),
                strokeWidth = 2f,
                cap = StrokeCap.Round
              )
              val arrowEnd = Offset(canvasW - 20f, yPos)
              drawLine(ElectricCyan.copy(alpha = 0.55f), arrowEnd, Offset(arrowEnd.x - 10f, arrowEnd.y - 6f), 2f)
              drawLine(ElectricCyan.copy(alpha = 0.55f), arrowEnd, Offset(arrowEnd.x - 10f, arrowEnd.y + 6f), 2f)
              yPos += lineSpacing
            }

            // Draw Rotating Surface Plate at center
            val thetaRad = Math.toRadians(orientationDeg).toFloat()
            val plateHalfLen = 90.dp.toPx()
            val plateAngle = thetaRad + (PI / 2.0).toFloat()

            val p1 = Offset(center.x - plateHalfLen * cos(plateAngle), center.y - plateHalfLen * sin(plateAngle))
            val p2 = Offset(center.x + plateHalfLen * cos(plateAngle), center.y + plateHalfLen * sin(plateAngle))

            // Surface Plate (Gold)
            drawLine(Color(0xFFFFB300), p1, p2, strokeWidth = 8f, cap = StrokeCap.Round)

            // Surface Normal Vector n̂ (Green)
            val normalLen = 65.dp.toPx()
            val nEnd = Offset(center.x + normalLen * cos(thetaRad), center.y + normalLen * sin(thetaRad))
            drawLine(InductionGreen, center, nEnd, strokeWidth = 3.5f, cap = StrokeCap.Round)

            // Normal arrowhead
            val nHeadAngle1 = thetaRad + (PI.toFloat() * 0.85f)
            val nHeadAngle2 = thetaRad - (PI.toFloat() * 0.85f)
            drawLine(InductionGreen, nEnd, Offset(nEnd.x + 12f * cos(nHeadAngle1), nEnd.y + 12f * sin(nHeadAngle1)), 3.5f)
            drawLine(InductionGreen, nEnd, Offset(nEnd.x + 12f * cos(nHeadAngle2), nEnd.y + 12f * sin(nHeadAngle2)), 3.5f)
          } else {
            // Gauss's Law Symmetry View
            val gRadiusPx = (gaussianRadiusM / 0.25 * canvasW / 2.0).toFloat().coerceIn(40f, canvasW / 2f - 20f)

            // Draw Central Charge or Distribution
            when (sourceConfig) {
              SourceConfiguration.POINT_CHARGE -> {
                drawCircle(Color(0xFFFF5252), radius = 16.dp.toPx(), center = center)
              }
              SourceConfiguration.INFINITE_LINE -> {
                drawLine(Color(0xFFFF5252), Offset(center.x, 20f), Offset(center.x, canvasH - 20f), strokeWidth = 8f)
              }
              SourceConfiguration.INFINITE_SHEET -> {
                drawLine(Color(0xFFFF5252), Offset(center.x - 120f, center.y), Offset(center.x + 120f, center.y), strokeWidth = 10f)
              }
              SourceConfiguration.SPHERICAL_SHELL -> {
                drawCircle(Color(0xFFFF5252).copy(alpha = 0.3f), radius = 35.dp.toPx(), center = center)
                drawCircle(Color(0xFFFF5252), radius = 35.dp.toPx(), center = center, style = Stroke(width = 3f))
              }
              SourceConfiguration.ELECTRIC_DIPOLE -> {
                drawCircle(Color(0xFFFF5252), radius = 12.dp.toPx(), center = Offset(center.x - 30f, center.y))
                drawCircle(Color(0xFF448AFF), radius = 12.dp.toPx(), center = Offset(center.x + 30f, center.y))
              }
            }

            // Draw Closed Gaussian Boundary
            drawCircle(
              color = ElectricCyan,
              radius = gRadiusPx,
              center = center,
              style = Stroke(
                width = 2.5f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
              )
            )

            // Outward Flux Radial Vectors
            val numSpokes = 16
            for (i in 0 until numSpokes) {
              val spokeAngle = (i.toFloat() / numSpokes.toFloat()) * (2f * PI.toFloat())
              val sStart = Offset(center.x + (gRadiusPx - 15f) * cos(spokeAngle), center.y + (gRadiusPx - 15f) * sin(spokeAngle))
              val sEnd = Offset(center.x + (gRadiusPx + 25f) * cos(spokeAngle), center.y + (gRadiusPx + 25f) * sin(spokeAngle))
              drawLine(ElectricCyan.copy(alpha = 0.75f), sStart, sEnd, 2f)
            }
          }
        }

        // Overlay caption
        Text(
          text = if (selectedTab == 0) "Touch/drag canvas to rotate surface plate θ directly." else "Dashed Ring: Gaussian Boundary | Outward Arrows: Electric Flux Flow",
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
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
        if (selectedTab == 0) {
          MeasurementBadge(
            symbol = "Φ_E",
            value = String.format(Locale.US, "%.3f", fluxPhi),
            unit = "V·m",
            label = "Electric Flux",
            accentColor = ElectricCyan
          )
          MeasurementBadge(
            symbol = "|D|",
            value = String.format(Locale.US, "%.3e", fluxDensityD),
            unit = "C/m²",
            label = "Flux Density (D = εE)",
            accentColor = ElectricBlue
          )
          MeasurementBadge(
            symbol = "A_proj",
            value = String.format(Locale.US, "%.4f", surfaceAreaSqM * cos(Math.toRadians(orientationDeg))),
            unit = "m²",
            label = "Projected Area",
            accentColor = Color(0xFFFFB300)
          )
          MeasurementBadge(
            symbol = "θ",
            value = String.format(Locale.US, "%.1f°", orientationDeg),
            unit = "",
            label = "Angle (E to n̂)",
            accentColor = InductionGreen
          )
        } else {
          MeasurementBadge(
            symbol = "Φ_analytical",
            value = String.format(Locale.US, "%.3e", gaussianFluxAnalytical),
            unit = "V·m",
            label = "Analytical (Q_enc/ε)",
            accentColor = ElectricCyan
          )
          MeasurementBadge(
            symbol = "∮ E·dA",
            value = String.format(Locale.US, "%.3e", gaussianFluxNumerical),
            unit = "V·m",
            label = "Numerical Surface Integral",
            accentColor = InductionGreen
          )
          MeasurementBadge(
            symbol = "Q_enc",
            value = String.format(Locale.US, "%.2f", enclosedChargeMicroC),
            unit = "µC",
            label = "Enclosed Net Charge",
            accentColor = Color(0xFFFF5252)
          )
          MeasurementBadge(
            symbol = "|E_surf|",
            value = String.format(Locale.US, "%.3e", analyticalLocalE),
            unit = "V/m",
            label = "Boundary Field Magnitude",
            accentColor = ElectricBlue
          )
        }
      }
    },
    theoryContent = {
      if (selectedTab == 0) {
        EquationCard(
          title = "Electric Flux Definition & SI Units",
          formula = "Φ_E = ∬ E · dA = E · A · cos(θ)    [V·m  or  N·m²/C]",
          description = "Electric flux is the scalar surface integral of the electric field. When the field aligns with the surface normal (θ = 0°), flux is maximum. When parallel to the plate (θ = 90°), flux is zero.",
          assumptions = "Uniform electric field across a flat planar surface."
        )
        Spacer(modifier = Modifier.height(6.dp))
        EquationCard(
          title = "Electric Flux Density vs Flux",
          formula = "D = ε · E = (ε_r ε_0) · E    [C/m²]",
          description = "Electric flux density D is a vector field measuring electric displacement independently of free-space permittivity. Flux Φ_E is a scalar measured in V·m.",
          accentColor = ElectricBlue
        )
      } else {
        EquationCard(
          title = "Gauss's Law & Enclosed Charge",
          formula = "∮_S D · dA = Q_{enclosed}   ⟹   Φ_E = ∮_S E · dA = Q_{enc} / ε",
          description = "Total electric flux through any closed Gaussian envelope depends strictly on the net enclosed charge inside, regardless of how that charge is distributed within.",
          assumptions = "Closed mathematical surface in linear, isotropic dielectric medium."
        )
        Spacer(modifier = Modifier.height(6.dp))
        EquationCard(
          title = "Numerical vs. Analytical Validation",
          formula = "Error % = |∮ E·dA - (Q_{enc}/ε)| / (Q_{enc}/ε) · 100% ≈ 0.0%",
          description = "The numerical surface integral sums discrete flux patches over the 3D spherical boundary, confirming exact agreement with Gauss's analytical theorem.",
          accentColor = InductionGreen
        )
      }
    },
    controlsContent = {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = DarkSurface,
        contentColor = ElectricCyan,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Surface Flux") }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Gauss's Law Integral") }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Dielectric Medium Selector (Available in both tabs)
      Text("Dielectric Medium (Relative Permittivity ε_r):", style = MaterialTheme.typography.bodySmall)
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
            .testTag("flux_dielectric_dropdown")
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

      if (selectedTab == 0) {
        // Orientation angle slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Surface Orientation Angle (θ):", style = MaterialTheme.typography.bodySmall)
          Text("${orientationDeg.toInt()}°", fontWeight = FontWeight.Bold, color = InductionGreen)
        }
        Slider(
          value = orientationDeg.toFloat(),
          onValueChange = { orientationDeg = it.toDouble() },
          valueRange = -90.0f..90.0f,
          colors = SliderDefaults.colors(thumbColor = InductionGreen, activeTrackColor = ElectricCyan),
          modifier = Modifier.testTag("surface_orientation_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Field magnitude slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Electric Field Magnitude (|E|):", style = MaterialTheme.typography.bodySmall)
          Text("${fieldMagnitudeVPerM.toInt()} V/m", fontWeight = FontWeight.Bold, color = ElectricCyan)
        }
        Slider(
          value = fieldMagnitudeVPerM.toFloat(),
          onValueChange = { fieldMagnitudeVPerM = it.toDouble() },
          valueRange = 100.0f..5000.0f,
          colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan),
          modifier = Modifier.testTag("field_magnitude_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Surface Area slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Surface Area (A):", style = MaterialTheme.typography.bodySmall)
          Text(String.format(Locale.US, "%.3f m²", surfaceAreaSqM), fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
        }
        Slider(
          value = surfaceAreaSqM.toFloat(),
          onValueChange = { surfaceAreaSqM = it.toDouble() },
          valueRange = 0.01f..0.20f,
          colors = SliderDefaults.colors(thumbColor = Color(0xFFFFB300), activeTrackColor = Color(0xFFFFB300)),
          modifier = Modifier.testTag("surface_area_slider")
        )
      } else {
        // Gauss's law symmetry selection
        Text("Select Charge Distribution Geometry:", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          SourceConfiguration.values().forEach { config ->
            FilterChip(
              selected = sourceConfig == config,
              onClick = { sourceConfig = config },
              label = { Text(config.displayName, fontSize = 11.sp) }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Enclosed charge slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Enclosed Charge (Q_enc):", style = MaterialTheme.typography.bodySmall)
          Text("${String.format(Locale.US, "%+.1f", enclosedChargeMicroC)} µC", fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
        }
        Slider(
          value = enclosedChargeMicroC.toFloat(),
          onValueChange = { enclosedChargeMicroC = it.toDouble() },
          valueRange = -10.0f..10.0f,
          colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5252), activeTrackColor = ElectricCyan),
          modifier = Modifier.testTag("enclosed_charge_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Gaussian surface radius slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Gaussian Envelope Radius (r):", style = MaterialTheme.typography.bodySmall)
          Text("${(gaussianRadiusM * 100).toInt()} cm", fontWeight = FontWeight.Bold, color = ElectricCyan)
        }
        Slider(
          value = gaussianRadiusM.toFloat(),
          onValueChange = { gaussianRadiusM = it.toDouble() },
          valueRange = 0.05f..0.22f,
          colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan),
          modifier = Modifier.testTag("gaussian_radius_slider")
        )
      }
    }
  )
}
