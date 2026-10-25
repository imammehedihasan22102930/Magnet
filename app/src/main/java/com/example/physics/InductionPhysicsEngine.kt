package com.example.physics

import com.example.model.InductionSimulationState
import com.example.model.PhysicsConstants
import com.example.model.RLStepResponsePoint
import com.example.model.TransformerParams
import com.example.model.TransformerResult
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt

object InductionPhysicsEngine {

  /**
   * Evaluates instantaneous magnetic flux, flux linkage, Faraday EMF, and current
   * for a bar magnet moving along the z-axis relative to a coaxial circular coil of N turns.
   */
  fun evaluateMagnetMotionInduction(
    state: InductionSimulationState,
    dtSeconds: Double = 0.01
  ): InductionSimulationState {
    val z = state.magnetPositionZ
    val v = state.magnetVelocityZ
    val rCoil = state.coilRadiusMeters
    val m = state.magnetMomentAm2
    val n = state.coilTurnsN
    val rCir = max(state.circuitResistanceOhms, 0.1)
    val lCir = max(state.circuitInductanceHenrys, 1e-6)

    val rSq = rCoil * rCoil
    val zSq = z * z
    val denomFactor = zSq + rSq
    val denom32 = denomFactor * sqrt(denomFactor)
    val denom52 = denomFactor * denomFactor * sqrt(denomFactor)

    // Flux through single turn: Phi(z) = (mu_0 * m * R^2) / (2 * (z^2 + R^2)^(3/2))
    val singleTurnFlux = (PhysicsConstants.MU_0 * m * rSq) / (2.0 * denom32)
    val fluxLinkage = n * singleTurnFlux

    // Spatial derivative: dPhi/dz = - (3 * mu_0 * m * R^2 * z) / (2 * (z^2 + R^2)^(5/2))
    val dPhiDz = - (3.0 * PhysicsConstants.MU_0 * m * rSq * z) / (2.0 * denom52)

    // Time derivative: dPhi/dt = (dPhi/dz) * (dz/dt) = (dPhi/dz) * v
    val dPhiDt = dPhiDz * v

    // Faraday's Law with Lenz's Law: emf = -N * (dPhi/dt)
    val inducedEmf = -n * dPhiDt

    // Circuit ODE solution: L * di/dt + R * i = emf
    // Using backward-Euler / exponential decay: i_next = i_prev * exp(-R*dt/L) + (emf/R)*(1 - exp(-R*dt/L))
    val tau = lCir / rCir
    val decay = exp(-dtSeconds / tau)
    val newCurrent = state.inducedCurrentAmperes * decay + (inducedEmf / rCir) * (1.0 - decay)

    // Galvanometer pointer angle (-45 to +45 deg) proportional to current
    val maxCurrentScale = 1.0 // 1 Amp full scale
    val deflectionDeg = (newCurrent / maxCurrentScale * 45.0).coerceIn(-45.0, 45.0)

    val nextZ = z + v * dtSeconds

    return state.copy(
      magnetPositionZ = nextZ,
      currentFluxWebers = singleTurnFlux,
      currentFluxLinkage = fluxLinkage,
      inducedEmfVolts = inducedEmf,
      inducedCurrentAmperes = newCurrent,
      galvanometerAngleDeg = deflectionDeg
    )
  }

  /**
   * Generates step response waveform points for an ideal series RL circuit:
   * i(t) = (V0 / R) * (1 - e^(-t / tau)), where tau = L / R
   * v_L(t) = V0 * e^(-t / tau)
   * v_R(t) = V0 * (1 - e^(-t / tau))
   * W(t) = 0.5 * L * i(t)^2
   */
  fun calculateRLStepResponse(
    supplyVoltageVolts: Double,
    resistanceOhms: Double,
    inductanceHenrys: Double,
    timeDurationSeconds: Double,
    pointCount: Int = 100
  ): List<RLStepResponsePoint> {
    val r = max(resistanceOhms, 0.01)
    val l = max(inductanceHenrys, 1e-6)
    val tau = l / r
    val dt = timeDurationSeconds / max(pointCount - 1, 1)

    val points = mutableListOf<RLStepResponsePoint>()
    for (i in 0 until pointCount) {
      val t = i * dt
      val expFactor = exp(-t / tau)
      val current = (supplyVoltageVolts / r) * (1.0 - expFactor)
      val vL = supplyVoltageVolts * expFactor
      val vR = supplyVoltageVolts * (1.0 - expFactor)
      val energy = 0.5 * l * current * current

      points.add(
        RLStepResponsePoint(
          timeSeconds = t,
          currentAmperes = current,
          inductorVoltageVolts = vL,
          resistorVoltageVolts = vR,
          storedEnergyJoules = energy
        )
      )
    }
    return points
  }

  /**
   * Solves transformer operation under sinusoidal steady state:
   * Handles ideal turns ratio (Vs / Vp = Ns / Np) and non-ideal losses
   * (winding resistance, leakage inductance, core losses).
   */
  fun solveTransformer(params: TransformerParams): TransformerResult {
    val a = params.primaryTurnsNp.toDouble() / max(params.secondaryTurnsNs.toDouble(), 1.0)
    val isIdeal = params.couplingCoefficientK >= 0.999 &&
      params.primaryWindingResistanceOhms <= 0.01 &&
      params.secondaryWindingResistanceOhms <= 0.01

    if (isIdeal) {
      val vs = params.primaryVoltageRms / a
      val isCurrent = vs / max(params.loadResistanceOhms, 0.1)
      val ipCurrent = isCurrent / a
      val power = vs * isCurrent
      val omega = 2.0 * PI * params.frequencyHz
      val peakFlux = params.primaryVoltageRms * sqrt(2.0) / (params.primaryTurnsNp * omega)

      return TransformerResult(
        turnsRatio = a,
        secondaryVoltageRms = vs,
        secondaryCurrentRms = isCurrent,
        primaryCurrentRms = ipCurrent,
        outputPowerWatts = power,
        inputPowerWatts = power,
        efficiencyPercent = 100.0,
        coreFluxPeakWebers = peakFlux,
        voltageRegulationPercent = 0.0,
        isIdeal = true
      )
    }

    // Real model calculation
    val vsNoLoad = params.primaryVoltageRms / a
    val reqSecondary = params.secondaryWindingResistanceOhms + (params.primaryWindingResistanceOhms / (a * a))
    val zTotal = reqSecondary + params.loadResistanceOhms
    val isCurrent = vsNoLoad / max(zTotal, 0.1)
    val vsLoad = isCurrent * params.loadResistanceOhms

    val pOut = vsLoad * isCurrent
    val copperLoss = isCurrent * isCurrent * reqSecondary
    val coreLoss = (params.primaryVoltageRms * params.primaryVoltageRms) / params.coreLossResistanceOhms
    val pIn = pOut + copperLoss + coreLoss
    val ipCurrent = pIn / max(params.primaryVoltageRms, 1.0)

    val efficiency = if (pIn > 0.0) (pOut / pIn) * 100.0 else 0.0
    val voltageReg = if (vsLoad > 0.0) ((vsNoLoad - vsLoad) / vsLoad) * 100.0 else 0.0

    val omega = 2.0 * PI * params.frequencyHz
    val peakFlux = params.primaryVoltageRms * sqrt(2.0) / (params.primaryTurnsNp * omega)

    return TransformerResult(
      turnsRatio = a,
      secondaryVoltageRms = vsLoad,
      secondaryCurrentRms = isCurrent,
      primaryCurrentRms = ipCurrent,
      outputPowerWatts = pOut,
      inputPowerWatts = pIn,
      efficiencyPercent = efficiency.coerceIn(0.0, 100.0),
      coreFluxPeakWebers = peakFlux,
      voltageRegulationPercent = voltageReg,
      isIdeal = false
    )
  }
}
