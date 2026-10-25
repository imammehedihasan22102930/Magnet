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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MaxwellLawType
import com.example.ui.components.EquationCard
import com.example.ui.components.LabScaffold
import com.example.ui.components.MeasurementBadge
import com.example.ui.theme.CanvasDarkBg
import com.example.ui.theme.CanvasGridLine
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InductionGreen
import com.example.ui.theme.MagneticAmber
import com.example.ui.theme.MagneticOrange
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaxwellsEquationsScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedLaw by remember { mutableStateOf(MaxwellLawType.GAUSS_ELECTRIC) }
  var isIntegralForm by remember { mutableStateOf(false) }

  LabScaffold(
    title = "Maxwell's Equations Suite",
    subtitle = "Unified Electrodynamic Foundations: Differential & Integral Forms",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { selectedLaw = MaxwellLawType.GAUSS_ELECTRIC },
    canvasContent = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CanvasDarkBg, RoundedCornerShape(12.dp))
          .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
          .testTag("maxwells_equations_canvas")
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)

          when (selectedLaw) {
            MaxwellLawType.GAUSS_ELECTRIC -> {
              // Central charge with outward diverging radial vectors
              drawCircle(Color(0xFFFF5252), radius = 16.dp.toPx(), center = center)
              drawCircle(ElectricCyan, radius = 70.dp.toPx(), center = center, style = Stroke(width = 2.5f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))

              val numRays = 12
              for (i in 0 until numRays) {
                val ang = (i.toFloat() / numRays) * (2f * PI.toFloat())
                val r1 = 20.dp.toPx()
                val r2 = 90.dp.toPx()
                drawLine(
                  ElectricCyan,
                  Offset(center.x + r1 * cos(ang), center.y + r1 * sin(ang)),
                  Offset(center.x + r2 * cos(ang), center.y + r2 * sin(ang)),
                  strokeWidth = 2.5f,
                  cap = StrokeCap.Round
                )
              }
            }

            MaxwellLawType.GAUSS_MAGNETIC -> {
              // Bar magnet dipole with closed continuous magnetic loops
              val mWidth = 70.dp.toPx()
              val mHeight = 24.dp.toPx()
              drawRect(Color(0xFF1E88E5), Offset(center.x - mWidth / 2f, center.y - mHeight / 2f), Size(mWidth / 2f, mHeight))
              drawRect(Color(0xFFE53935), Offset(center.x, center.y - mHeight / 2f), Size(mWidth / 2f, mHeight))

              // Closed oval field loops (Net flux through any closed box = 0)
              drawOval(MagneticAmber.copy(alpha = 0.6f), Offset(center.x - 70.dp.toPx(), center.y - 45.dp.toPx()), Size(140.dp.toPx(), 90.dp.toPx()), style = Stroke(2.5f))
              drawOval(MagneticAmber.copy(alpha = 0.4f), Offset(center.x - 95.dp.toPx(), center.y - 65.dp.toPx()), Size(190.dp.toPx(), 130.dp.toPx()), style = Stroke(2.5f))

              // Closed boundary showing field lines entering and exiting equally
              drawCircle(Color.White.copy(alpha = 0.6f), radius = 60.dp.toPx(), center = Offset(center.x + 30.dp.toPx(), center.y), style = Stroke(2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f))))
            }

            MaxwellLawType.FARADAY -> {
              // Time-varying B-field perpendicular to canvas inducing circulating E-field loop
              drawCircle(MagneticAmber.copy(alpha = 0.35f), radius = 35.dp.toPx(), center = center)
              drawCircle(MagneticAmber, radius = 35.dp.toPx(), center = center, style = Stroke(3f))
              // dB/dt dot
              drawCircle(Color.White, radius = 4.dp.toPx(), center = center)

              // Circulating Electric Field Loop
              drawCircle(InductionGreen, radius = 75.dp.toPx(), center = center, style = Stroke(3f))
              // Direction arrowheads
              for (angDeg in listOf(0.0, 90.0, 180.0, 270.0)) {
                val rad = Math.toRadians(angDeg).toFloat()
                val r = 75.dp.toPx()
                val ax = center.x + r * cos(rad)
                val ay = center.y + r * sin(rad)
                drawCircle(InductionGreen, radius = 5.dp.toPx(), center = Offset(ax, ay))
              }
            }

            MaxwellLawType.AMPERE_MAXWELL -> {
              // Parallel Plate Capacitor gap showing displacement current dD/dt
              val capX = center.x
              val plateH = 80.dp.toPx()
              // Left Plate (+Q)
              drawLine(Color(0xFFFF5252), Offset(capX - 30.dp.toPx(), center.y - plateH / 2f), Offset(capX - 30.dp.toPx(), center.y + plateH / 2f), 8f)
              // Right Plate (-Q)
              drawLine(Color(0xFF448AFF), Offset(capX + 30.dp.toPx(), center.y - plateH / 2f), Offset(capX + 30.dp.toPx(), center.y + plateH / 2f), 8f)

              // Displacement current field lines across dielectric gap
              for (i in -2..2) {
                val yL = center.y + i * 14.dp.toPx()
                drawLine(ElectricCyan, Offset(capX - 25.dp.toPx(), yL), Offset(capX + 25.dp.toPx(), yL), 2f, StrokeCap.Round)
              }

              // Induced circulating magnetic field loop around displacement current
              drawCircle(MagneticOrange, radius = 55.dp.toPx(), center = center, style = Stroke(2.5f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 8f))))
            }
          }
        }

        Text(
          text = "Active Visualization: ${selectedLaw.title}",
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
          symbol = "Law",
          value = selectedLaw.shortName,
          unit = "",
          label = "Equation Selected",
          accentColor = ElectricCyan
        )
        MeasurementBadge(
          symbol = "D",
          value = "ε · E",
          unit = "C/m²",
          label = "Electric Flux Density",
          accentColor = ElectricBlue
        )
        MeasurementBadge(
          symbol = "B",
          value = "µ · H",
          unit = "Tesla",
          label = "Magnetic Flux Density",
          accentColor = MagneticAmber
        )
      }
    },
    theoryContent = {
      EquationCard(
        title = selectedLaw.title,
        formula = if (isIntegralForm) selectedLaw.integralForm else selectedLaw.differentialForm,
        description = selectedLaw.primaryPhysicalConcept,
        assumptions = "Maxwell's equations in macroscopic form for linear, isotropic media.",
        accentColor = Color(0xFFC084FC)
      )
    },
    controlsContent = {
      Text("Select Equation to Explore:", style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        MaxwellLawType.values().forEach { law ->
          FilterChip(
            selected = selectedLaw == law,
            onClick = { selectedLaw = law },
            label = { Text(law.shortName, fontSize = 11.sp) },
            modifier = Modifier.testTag("maxwell_chip_${law.name}")
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      TabRow(
        selectedTabIndex = if (isIntegralForm) 1 else 0,
        containerColor = DarkBackground,
        contentColor = ElectricCyan,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = !isIntegralForm,
          onClick = { isIntegralForm = false },
          text = { Text("Differential Form (Point)") }
        )
        Tab(
          selected = isIntegralForm,
          onClick = { isIntegralForm = true },
          text = { Text("Integral Form (Boundary)") }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Mathematical Notation & SI Dimensions:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "• D: Electric Flux Density [C/m²]\n• E: Electric Field Intensity [V/m]\n• B: Magnetic Flux Density [Tesla or Wb/m²]\n• H: Magnetic Field Intensity [A/m]\n• J: Conduction Current Density [A/m²]\n• ρ_v: Volume Charge Density [C/m³]",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
        }
      }
    }
  )
}
