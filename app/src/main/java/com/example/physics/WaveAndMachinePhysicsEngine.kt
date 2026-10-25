package com.example.physics

import com.example.model.MachineDemonstrationState
import com.example.model.MachineType
import com.example.model.PhysicsConstants
import com.example.model.WaveMedium
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

object WaveAndMachinePhysicsEngine {

  val DEFAULT_WAVE_MEDIA = listOf(
    WaveMedium("Vacuum", 1.0, 1.0),
    WaveMedium("Air (STP)", 1.00059, 1.0),
    WaveMedium("Fresh Water", 80.1, 1.0),
    WaveMedium("Pyrex Glass", 4.7, 1.0),
    WaveMedium("Teflon Dielectric", 2.1, 1.0)
  )

  /**
   * Calculates wave properties for a monochromatic sinusoidal plane wave in a given medium:
   * v = 1 / sqrt(mu * eps)
   * lambda = v / f
   * B_0 = E_0 / v
   * S_avg = E_0^2 / (2 * eta)
   */
  fun calculateWaveProperties(
    frequencyHz: Double,
    electricFieldAmplitudeVPerM: Double,
    medium: WaveMedium
  ): WavePropertiesResult {
    val v = medium.waveSpeedMetersPerSec
    val lambda = v / max(frequencyHz, 1e-6)
    val bAmplitude = electricFieldAmplitudeVPerM / v
    val omega = 2.0 * PI * frequencyHz
    val kWaveNumber = 2.0 * PI / lambda
    val eta = medium.intrinsicImpedanceOhms
    val poyntingAvgWattsPerSqM = (electricFieldAmplitudeVPerM * electricFieldAmplitudeVPerM) / (2.0 * eta)

    return WavePropertiesResult(
      speedMetersPerSec = v,
      wavelengthMeters = lambda,
      frequencyHz = frequencyHz,
      electricFieldAmplitude = electricFieldAmplitudeVPerM,
      magneticFieldAmplitudeTesla = bAmplitude,
      waveNumberK = kWaveNumber,
      angularFrequencyOmega = omega,
      intrinsicImpedance = eta,
      poyntingVectorAvgPower = poyntingAvgWattsPerSqM
    )
  }

  data class WavePropertiesResult(
    val speedMetersPerSec: Double,
    val wavelengthMeters: Double,
    val frequencyHz: Double,
    val electricFieldAmplitude: Double,
    val magneticFieldAmplitudeTesla: Double,
    val waveNumberK: Double,
    val angularFrequencyOmega: Double,
    val intrinsicImpedance: Double,
    val poyntingVectorAvgPower: Double
  )

  /**
   * Calculates instantaneous E and B vectors at position z (meters) and time t (seconds)
   */
  fun calculateWaveInstantaneousFields(
    zDistanceMeters: Double,
    timeSeconds: Double,
    props: WavePropertiesResult
  ): Pair<Double, Double> {
    val phase = props.waveNumberK * zDistanceMeters - props.angularFrequencyOmega * timeSeconds
    val eVal = props.electricFieldAmplitude * cos(phase)
    val bVal = props.magneticFieldAmplitudeTesla * cos(phase)
    return Pair(eVal, bVal)
  }

  /**
   * Evaluates performance and physical state for Electrical Machines demonstration
   */
  fun evaluateMachineState(state: MachineDemonstrationState): MachineDemonstrationState {
    val omega = (state.speedRpm * 2.0 * PI) / 60.0 // rad/s
    val b = state.magneticFieldTesla
    val area = state.coilAreaSqMeters
    val turns = state.coilTurns

    return when (state.type) {
      MachineType.DC_MOTOR -> {
        // Torque: T = N * I * A * B * sin(theta)
        val torque = turns * state.armatureCurrentAmperes * area * b * sin(state.rotorAngleRad + PI / 2.0)
        // Back-EMF: E_b = N * B * A * omega
        val backEmf = turns * b * area * omega
        state.copy(
          generatedTorqueNm = torque,
          inducedBackEmfVolts = backEmf,
          transferredPowerWatts = torque * omega
        )
      }
      MachineType.AC_GENERATOR -> {
        // Induced EMF: emf = N * B * A * omega * sin(theta)
        val inducedEmf = turns * b * area * omega * sin(state.rotorAngleRad)
        val rLoad = 10.0
        val current = inducedEmf / rLoad
        state.copy(
          inducedBackEmfVolts = inducedEmf,
          armatureCurrentAmperes = current,
          transferredPowerWatts = current * inducedEmf
        )
      }
      MachineType.EDDY_BRAKE -> {
        // Magnetic braking force F_brake = - sigma * v * B^2 * Vol
        val velocity = omega * 0.1 // 10cm radius disk rim
        val conductivityCopper = 5.96e7
        val activeVolume = area * 0.003 // 3mm thickness
        val brakingForce = conductivityCopper * velocity * (b * b) * activeVolume * 0.001 // scaled
        state.copy(
          brakingForceNewtons = brakingForce,
          transferredPowerWatts = brakingForce * velocity
        )
      }
      MachineType.WIRELESS_POWER -> {
        // Inductive coupling: Mutual Inductance M = k * sqrt(L1 * L2)
        val l1 = 0.0005 // 500 uH
        val l2 = 0.0005
        val k = 0.65 // coupling coefficient
        val m = k * sqrt(l1 * l2)
        val f = 100000.0 // 100 kHz resonant frequency
        val omegaRes = 2.0 * PI * f
        val vInducedSecondary = omegaRes * m * state.armatureCurrentAmperes
        val rLoad = 50.0
        val pSecondary = (vInducedSecondary * vInducedSecondary) / (2.0 * rLoad)
        state.copy(
          inducedBackEmfVolts = vInducedSecondary,
          transferredPowerWatts = pSecondary
        )
      }
    }
  }
}
