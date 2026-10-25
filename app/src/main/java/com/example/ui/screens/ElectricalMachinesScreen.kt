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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.model.MachineDemonstrationState
import com.example.model.MachineType
import com.example.physics.WaveAndMachinePhysicsEngine
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
fun ElectricalMachinesScreen(
  onNavigateBack: () -> Unit,
  onSaveMeasurement: (Map<String, String>, Map<String, String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedMachine by remember { mutableStateOf(MachineType.DC_MOTOR) }
  var speedRpm by remember { mutableDoubleStateOf(1200.0) }
  var currentAmps by remember { mutableDoubleStateOf(5.0) }
  var bFieldTesla by remember { mutableDoubleStateOf(0.8) }

  // Rotor angle animation
  val infiniteTransition = rememberInfiniteTransition(label = "rotorRotation")
  val animatedAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 2f * PI.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotorAngleFloat"
  )

  val machineState = remember(selectedMachine, speedRpm, currentAmps, bFieldTesla, animatedAngle) {
    WaveAndMachinePhysicsEngine.evaluateMachineState(
      MachineDemonstrationState(
        type = selectedMachine,
        speedRpm = speedRpm,
        armatureCurrentAmperes = currentAmps,
        magneticFieldTesla = bFieldTesla,
        rotorAngleRad = animatedAngle.toDouble()
      )
    )
  }

  fun resetMachine() {
    selectedMachine = MachineType.DC_MOTOR
    speedRpm = 1200.0
    currentAmps = 5.0
    bFieldTesla = 0.8
  }

  LabScaffold(
    title = "Electrical Machines & Applications",
    subtitle = "Lorentz Motors, AC Generators, Eddy Current Brakes & Wireless Power",
    onNavigateBack = onNavigateBack,
    onResetExperiment = { resetMachine() },
    onSaveToNotebook = {
      val settings = mapOf(
        "Application" to selectedMachine.title,
        "Rotor Speed" to "$speedRpm RPM",
        "Current I" to "$currentAmps A",
        "Magnetic Field B" to "$bFieldTesla T"
      )
      val measurements = when (selectedMachine) {
        MachineType.DC_MOTOR -> mapOf(
          "Torque T" to String.format(Locale.US, "%.3f N·m", machineState.generatedTorqueNm),
          "Back-EMF" to String.format(Locale.US, "%.2f V", machineState.inducedBackEmfVolts),
          "Power" to String.format(Locale.US, "%.1f Watts", machineState.transferredPowerWatts)
        )
        MachineType.AC_GENERATOR -> mapOf(
          "Generated EMF" to String.format(Locale.US, "%.2f V", machineState.inducedBackEmfVolts),
          "Load Current" to String.format(Locale.US, "%.2f A", machineState.armatureCurrentAmperes),
          "Power" to String.format(Locale.US, "%.1f Watts", machineState.transferredPowerWatts)
        )
        MachineType.EDDY_BRAKE -> mapOf(
          "Braking Force" to String.format(Locale.US, "%.2f N", machineState.brakingForceNewtons),
          "Dissipated Power" to String.format(Locale.US, "%.1f Watts", machineState.transferredPowerWatts)
        )
        MachineType.WIRELESS_POWER -> mapOf(
          "Secondary EMF" to String.format(Locale.US, "%.2f V", machineState.inducedBackEmfVolts),
          "Received Power" to String.format(Locale.US, "%.2f Watts", machineState.transferredPowerWatts)
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
          .testTag("machine_canvas")
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasW = size.width
          val canvasH = size.height
          val center = Offset(canvasW / 2f, canvasH / 2f)

          when (selectedMachine) {
            MachineType.DC_MOTOR, MachineType.AC_GENERATOR -> {
              // Stator Pole Magnets (North on left, South on right)
              val poleW = 40.dp.toPx()
              val poleH = 100.dp.toPx()
              drawRect(Color(0xFFE53935), Offset(center.x - 110.dp.toPx(), center.y - poleH / 2f), Size(poleW, poleH))
              drawRect(Color(0xFF1E88E5), Offset(center.x + 70.dp.toPx(), center.y - poleH / 2f), Size(poleW, poleH))

              // Stator magnetic field lines crossing gap
              for (i in -2..2) {
                val yL = center.y + i * 18.dp.toPx()
                drawLine(MagneticAmber.copy(alpha = 0.35f), Offset(center.x - 70.dp.toPx(), yL), Offset(center.x + 70.dp.toPx(), yL), 2f)
              }

              // Rotating Armature Loop
              val ang = animatedAngle
              val rLen = 45.dp.toPx()
              val arm1 = Offset(center.x + rLen * cos(ang), center.y + rLen * sin(ang))
              val arm2 = Offset(center.x - rLen * cos(ang), center.y - rLen * sin(ang))

              drawLine(Color(0xFFFF9800), arm1, arm2, strokeWidth = 8f, cap = StrokeCap.Round)
              drawCircle(Color.White, radius = 5.dp.toPx(), center = center)

              // Force vectors on armature conductor tips
              val fAng = ang + (PI / 2.0).toFloat()
              val fLen = 25.dp.toPx()
              drawLine(InductionGreen, arm1, Offset(arm1.x + fLen * cos(fAng), arm1.y + fLen * sin(fAng)), 3f, StrokeCap.Round)
              drawLine(InductionGreen, arm2, Offset(arm2.x - fLen * cos(fAng), arm2.y - fLen * sin(fAng)), 3f, StrokeCap.Round)
            }

            MachineType.EDDY_BRAKE -> {
              // Rotating Copper Disk
              drawCircle(Color(0xFFD97706).copy(alpha = 0.3f), radius = 75.dp.toPx(), center = center)
              drawCircle(Color(0xFFD97706), radius = 75.dp.toPx(), center = center, style = Stroke(width = 4f))

              // Electromagnet Pole positioned over the rim
              val magX = center.x + 50.dp.toPx()
              drawCircle(Color(0xFF1E293B), radius = 22.dp.toPx(), center = Offset(magX, center.y))
              drawCircle(MagneticAmber, radius = 22.dp.toPx(), center = Offset(magX, center.y), style = Stroke(3f))

              // Induced Eddy Current swirl rings in copper
              drawCircle(ElectricCyan, radius = 14.dp.toPx(), center = Offset(magX, center.y), style = Stroke(2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))

              // Opposing Lorentz braking force vector
              drawLine(Color(0xFFFF5252), Offset(magX, center.y), Offset(magX, center.y + 35.dp.toPx()), 4f, StrokeCap.Round)
            }

            MachineType.WIRELESS_POWER -> {
              // Primary Transmitter Coil (Left)
              val txCenter = Offset(center.x - 55.dp.toPx(), center.y)
              drawCircle(ElectricCyan.copy(alpha = 0.25f), radius = 45.dp.toPx(), center = txCenter)
              drawCircle(ElectricCyan, radius = 45.dp.toPx(), center = txCenter, style = Stroke(4f))

              // Secondary Receiver Coil (Right)
              val rxCenter = Offset(center.x + 55.dp.toPx(), center.y)
              drawCircle(InductionGreen.copy(alpha = 0.25f), radius = 45.dp.toPx(), center = rxCenter)
              drawCircle(InductionGreen, radius = 45.dp.toPx(), center = rxCenter, style = Stroke(4f))

              // Mutual flux linking both coils
              drawOval(
                MagneticAmber.copy(alpha = 0.5f),
                Offset(center.x - 75.dp.toPx(), center.y - 30.dp.toPx()),
                Size(150.dp.toPx(), 60.dp.toPx()),
                style = Stroke(2.5f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
              )
            }
          }
        }

        Text(
          text = "Active Mechanism: ${selectedMachine.title}",
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
        when (selectedMachine) {
          MachineType.DC_MOTOR -> {
            MeasurementBadge(
              symbol = "Torque",
              value = String.format(Locale.US, "%.3f", machineState.generatedTorqueNm),
              unit = "N·m",
              label = "Electromagnetic Torque",
              accentColor = InductionGreen
            )
            MeasurementBadge(
              symbol = "E_back",
              value = String.format(Locale.US, "%.2f", machineState.inducedBackEmfVolts),
              unit = "Volts",
              label = "Back-EMF",
              accentColor = ElectricCyan
            )
            MeasurementBadge(
              symbol = "Power",
              value = String.format(Locale.US, "%.1f", machineState.transferredPowerWatts),
              unit = "Watts",
              label = "Mechanical Power",
              accentColor = MagneticOrange
            )
          }
          MachineType.AC_GENERATOR -> {
            MeasurementBadge(
              symbol = "emf(t)",
              value = String.format(Locale.US, "%.2f", machineState.inducedBackEmfVolts),
              unit = "Volts",
              label = "Generated EMF",
              accentColor = InductionGreen
            )
            MeasurementBadge(
              symbol = "I_load",
              value = String.format(Locale.US, "%.2f", machineState.armatureCurrentAmperes),
              unit = "Amperes",
              label = "AC Load Current",
              accentColor = ElectricCyan
            )
            MeasurementBadge(
              symbol = "P_gen",
              value = String.format(Locale.US, "%.1f", machineState.transferredPowerWatts),
              unit = "Watts",
              label = "Delivered Power",
              accentColor = MagneticAmber
            )
          }
          MachineType.EDDY_BRAKE -> {
            MeasurementBadge(
              symbol = "F_brake",
              value = String.format(Locale.US, "%.2f", machineState.brakingForceNewtons),
              unit = "Newtons",
              label = "Magnetic Braking Force",
              accentColor = Color(0xFFFF5252)
            )
            MeasurementBadge(
              symbol = "P_diss",
              value = String.format(Locale.US, "%.1f", machineState.transferredPowerWatts),
              unit = "Watts",
              label = "Ohmic Heat Dissipation",
              accentColor = MagneticOrange
            )
          }
          MachineType.WIRELESS_POWER -> {
            MeasurementBadge(
              symbol = "V_sec",
              value = String.format(Locale.US, "%.2f", machineState.inducedBackEmfVolts),
              unit = "Volts",
              label = "Secondary Induced EMF",
              accentColor = InductionGreen
            )
            MeasurementBadge(
              symbol = "P_rx",
              value = String.format(Locale.US, "%.2f", machineState.transferredPowerWatts),
              unit = "Watts",
              label = "Transferred Power",
              accentColor = ElectricCyan
            )
          }
        }
      }
    },
    theoryContent = {
      EquationCard(
        title = "Electromechanical Energy Conversion",
        formula = "F = I (L × B),    T = N · I · A · B · sin(θ),    emf = N · B · A · ω · sin(ωt)",
        description = "In motors, current in a magnetic field experiences Lorentz force creating torque. In generators, mechanical rotation varies flux linkage, generating terminal AC voltage.",
        accentColor = MagneticOrange
      )
    },
    controlsContent = {
      Text("Select Machine Application:", style = MaterialTheme.typography.bodySmall)
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        MachineType.values().forEach { mt ->
          FilterChip(
            selected = selectedMachine == mt,
            onClick = { selectedMachine = mt },
            label = { Text(mt.name.replace("_", " "), fontSize = 11.sp) },
            modifier = Modifier.testTag("machine_chip_${mt.name}")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Speed Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Rotor Speed:", style = MaterialTheme.typography.bodySmall)
        Text("${speedRpm.toInt()} RPM", fontWeight = FontWeight.Bold, color = InductionGreen)
      }
      Slider(
        value = speedRpm.toFloat(),
        onValueChange = { speedRpm = it.toDouble() },
        valueRange = 100f..3000f,
        colors = SliderDefaults.colors(thumbColor = InductionGreen, activeTrackColor = InductionGreen),
        modifier = Modifier.testTag("machine_speed_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Current Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Current (I):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.1f A", currentAmps), fontWeight = FontWeight.Bold, color = ElectricCyan)
      }
      Slider(
        value = currentAmps.toFloat(),
        onValueChange = { currentAmps = it.toDouble() },
        valueRange = 0.5f..20.0f,
        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan),
        modifier = Modifier.testTag("machine_current_slider")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Magnetic field slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Stator Field (B):", style = MaterialTheme.typography.bodySmall)
        Text(String.format(Locale.US, "%.2f T", bFieldTesla), fontWeight = FontWeight.Bold, color = MagneticAmber)
      }
      Slider(
        value = bFieldTesla.toFloat(),
        onValueChange = { bFieldTesla = it.toDouble() },
        valueRange = 0.1f..2.0f,
        colors = SliderDefaults.colors(thumbColor = MagneticAmber, activeTrackColor = MagneticAmber),
        modifier = Modifier.testTag("machine_b_slider")
      )
    }
  )
}
