package com.example

import com.example.model.DielectricMaterial
import com.example.model.InductionSimulationState
import com.example.model.MachineDemonstrationState
import com.example.model.MachineType
import com.example.model.MagneticCircuitParams
import com.example.model.MagneticMaterial
import com.example.model.PhysicsConstants
import com.example.model.PointCharge
import com.example.model.TransformerParams
import com.example.model.Vector2D
import com.example.model.WaveMedium
import com.example.physics.ElectricPhysicsEngine
import com.example.physics.InductionPhysicsEngine
import com.example.physics.MagneticPhysicsEngine
import com.example.physics.WaveAndMachinePhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class PhysicsEngineValidationTest {

  private val vacuum = PhysicsConstants.DIELECTRIC_MATERIALS.first()
  private val airMag = PhysicsConstants.MAGNETIC_MATERIALS.first()

  // 1. ELECTRIC FIELD TESTS
  @Test
  fun testElectricFieldInverseSquareLaw() {
    val q = PointCharge(id = "q1", position = Vector2D.ZERO, chargeMicroCoulombs = 5.0)
    val charges = listOf(q)

    val r1 = 0.10 // 10 cm
    val r2 = 0.20 // 20 cm (doubled distance)

    val res1 = ElectricPhysicsEngine.evaluatePointField(Vector2D(r1, 0.0), charges, vacuum)
    val res2 = ElectricPhysicsEngine.evaluatePointField(Vector2D(r2, 0.0), charges, vacuum)

    val e1 = res1.fieldVector.magnitude
    val e2 = res2.fieldVector.magnitude

    // E2 should be 1/4 of E1: E2 / E1 = 0.25
    val ratio = e2 / e1
    assertEquals("Doubling distance must reduce electric field to one-quarter", 0.25, ratio, 0.001)
  }

  @Test
  fun testElectricFieldSuperposition() {
    val q1 = PointCharge(id = "q1", position = Vector2D(-0.1, 0.0), chargeMicroCoulombs = 4.0)
    val q2 = PointCharge(id = "q2", position = Vector2D(0.1, 0.0), chargeMicroCoulombs = 4.0)

    val probeAtCenter = Vector2D.ZERO
    val res = ElectricPhysicsEngine.evaluatePointField(probeAtCenter, listOf(q1, q2), vacuum)

    // At the exact midpoint between two equal charges, net E-field must be zero by symmetry
    assertEquals("Symmetric equal positive charges must produce net zero field at midpoint", 0.0, res.fieldVector.magnitude, 1e-4)
  }

  @Test
  fun testElectricFieldDirectionPositiveVsNegative() {
    val qPos = PointCharge(id = "qp", position = Vector2D.ZERO, chargeMicroCoulombs = 2.0)
    val qNeg = PointCharge(id = "qn", position = Vector2D.ZERO, chargeMicroCoulombs = -2.0)

    val probe = Vector2D(0.1, 0.0)
    val resPos = ElectricPhysicsEngine.evaluatePointField(probe, listOf(qPos), vacuum)
    val resNeg = ElectricPhysicsEngine.evaluatePointField(probe, listOf(qNeg), vacuum)

    // Positive charge points radially outward (+x), Negative points radially inward (-x)
    assertTrue("Field of positive charge must point outward", resPos.fieldVector.x > 0)
    assertTrue("Field of negative charge must point inward", resNeg.fieldVector.x < 0)
  }

  // 2. ELECTRIC FLUX & FLUX DENSITY TESTS
  @Test
  fun testElectricFluxMaxZeroAndSignReversal() {
    val eMag = 1000.0
    val area = 0.05

    // Aligned (0 deg) -> Maximum positive flux
    val (flux0, d0) = ElectricPhysicsEngine.calculateUniformSurfaceFlux(eMag, area, 0.0, vacuum)
    assertEquals(eMag * area, flux0, 1e-6)

    // Parallel to surface (90 deg) -> Zero flux
    val (flux90, _) = ElectricPhysicsEngine.calculateUniformSurfaceFlux(eMag, area, 90.0, vacuum)
    assertEquals("Flux through surface parallel to field must be zero", 0.0, flux90, 1e-6)

    // Reversed normal (180 deg) -> Negative maximum flux
    val (flux180, _) = ElectricPhysicsEngine.calculateUniformSurfaceFlux(eMag, area, 180.0, vacuum)
    assertEquals(-eMag * area, flux180, 1e-6)

    // Verify D = epsilon * E
    val expectedD = vacuum.absolutePermittivity * eMag
    assertEquals("D = epsilon * E must hold", expectedD, d0, 1e-12)
  }

  // 3. MAGNETIC FIELD TESTS
  @Test
  fun testMagneticFieldStraightWireInverseDistance() {
    val wirePos = Vector2D.ZERO
    val current = 10.0

    val r1 = 0.05
    val r2 = 0.10 // 2x distance

    val res1 = MagneticPhysicsEngine.straightWireField(Vector2D(0.0, r1), wirePos, current, airMag)
    val res2 = MagneticPhysicsEngine.straightWireField(Vector2D(0.0, r2), wirePos, current, airMag)

    val ratio = res2.bMagnitude / res1.bMagnitude
    assertEquals("Doubling radial distance from straight wire must halve magnetic field B", 0.5, ratio, 0.001)

    // Verify B = mu * H relationship
    val muRatio = res1.bMagnitude / res1.hMagnitude
    assertEquals("B / H must equal material permeability", airMag.absolutePermeability, muRatio, 1e-10)
  }

  // 4. MAGNETIC FLUX & LINKAGE TESTS
  @Test
  fun testMagneticFluxAndLinkageTurnsScaling() {
    val b = 0.4
    val area = 0.02
    val turns = 300

    val (phi0, lambda0) = MagneticPhysicsEngine.calculateMagneticFluxAndLinkage(b, area, 0.0, turns)
    assertEquals(b * area, phi0, 1e-7)
    assertEquals("Flux linkage must equal N * Phi", turns * phi0, lambda0, 1e-6)

    val (phi90, lambda90) = MagneticPhysicsEngine.calculateMagneticFluxAndLinkage(b, area, 90.0, turns)
    assertEquals(0.0, phi90, 1e-7)
    assertEquals(0.0, lambda90, 1e-7)
  }

  // 5. INDUCTION & FARADAY-LENZ TESTS
  @Test
  fun testInductionVelocityAndStationaryZeroEmf() {
    // Stationary magnet (v = 0)
    val stationaryState = InductionSimulationState(
      magnetPositionZ = 0.10,
      magnetVelocityZ = 0.0,
      coilTurnsN = 200
    )
    val evaluatedStationary = InductionPhysicsEngine.evaluateMagnetMotionInduction(stationaryState, 0.01)
    assertEquals("Stationary magnet must induce exactly 0 EMF", 0.0, evaluatedStationary.inducedEmfVolts, 1e-9)

    // Moving forward (v = -0.5 m/s approaching coil from positive z)
    val fwdState = stationaryState.copy(magnetVelocityZ = -0.5)
    val evalFwd = InductionPhysicsEngine.evaluateMagnetMotionInduction(fwdState, 0.01)

    // Moving backward (v = +0.5 m/s retreating)
    val revState = stationaryState.copy(magnetVelocityZ = 0.5)
    val evalRev = InductionPhysicsEngine.evaluateMagnetMotionInduction(revState, 0.01)

    assertTrue("Approaching magnet must generate nonzero EMF", abs(evalFwd.inducedEmfVolts) > 1e-4)
    // Polarity reversal when velocity reverses
    assertEquals(
      "Reversing velocity must reverse the sign of induced EMF",
      -evalFwd.inducedEmfVolts,
      evalRev.inducedEmfVolts,
      1e-6
    )
  }

  // 6. RL CIRCUIT TRANSIENT TESTS
  @Test
  fun testRLCircuitTimeConstantAndInitialZeroCurrent() {
    val v0 = 24.0
    val r = 12.0
    val l = 0.24 // tau = 0.24 / 12 = 0.02 s (20 ms)
    val expectedTau = l / r

    val points = InductionPhysicsEngine.calculateRLStepResponse(v0, r, l, expectedTau * 5.0, 100)

    // Initial current at t = 0 must be 0
    assertEquals("Initial current in inductor must be 0 A", 0.0, points.first().currentAmperes, 1e-6)
    // Initial inductor voltage at t = 0 must equal supply voltage
    assertEquals("Initial inductor voltage must equal V0", v0, points.first().inductorVoltageVolts, 1e-6)

    // At t = tau, current should be 63.2% of steady state (V0 / R)
    val steadyCurrent = v0 / r
    val tauPoint = points.minByOrNull { abs(it.timeSeconds - expectedTau) }!!
    val currentAtTau = tauPoint.currentAmperes
    assertEquals(0.632 * steadyCurrent, currentAtTau, 0.05)
  }

  // 7. TRANSFORMER TURNS RATIO & POWER BALANCE TESTS
  @Test
  fun testIdealTransformerTurnsRatioAndPowerBalance() {
    val params = TransformerParams(
      primaryTurnsNp = 600,
      secondaryTurnsNs = 200, // 3:1 step down
      primaryVoltageRms = 120.0,
      loadResistanceOhms = 10.0,
      primaryWindingResistanceOhms = 0.0,
      secondaryWindingResistanceOhms = 0.0,
      couplingCoefficientK = 1.0
    )
    val result = InductionPhysicsEngine.solveTransformer(params)

    assertEquals("Turns ratio must equal Np / Ns", 3.0, result.turnsRatio, 1e-6)
    assertEquals("Secondary voltage must equal Vp / 3", 40.0, result.secondaryVoltageRms, 1e-6)
    assertEquals("Ideal efficiency must be 100%", 100.0, result.efficiencyPercent, 1e-6)
    assertEquals("Input power must equal output power", result.inputPowerWatts, result.outputPowerWatts, 1e-4)
  }

  // 8. ELECTROMAGNETIC WAVES TESTS
  @Test
  fun testElectromagneticWaveVelocityAndFrequencyDispersion() {
    val f1 = 1.0e9 // 1 GHz
    val f2 = 2.0e9 // 2 GHz (doubled)
    val medium = WaveMedium("Vacuum", 1.0, 1.0)

    val wave1 = WaveAndMachinePhysicsEngine.calculateWaveProperties(f1, 100.0, medium)
    val wave2 = WaveAndMachinePhysicsEngine.calculateWaveProperties(f2, 100.0, medium)

    // Speed in vacuum = c
    assertEquals(PhysicsConstants.C, wave1.speedMetersPerSec, 1.0)
    assertEquals(PhysicsConstants.C, wave2.speedMetersPerSec, 1.0)

    // v = f * lambda
    assertEquals(wave1.speedMetersPerSec, wave1.frequencyHz * wave1.wavelengthMeters, 1.0)

    // Doubling frequency must halve wavelength
    val lambdaRatio = wave2.wavelengthMeters / wave1.wavelengthMeters
    assertEquals("Doubling wave frequency must halve wavelength", 0.5, lambdaRatio, 1e-6)
  }

  // 9. MAGNETIC CIRCUIT TESTS
  @Test
  fun testMagneticCircuitHopkinsonLaw() {
    val iron = PhysicsConstants.MAGNETIC_MATERIALS.first { it.name.contains("Mild Steel") }
    val params = MagneticCircuitParams(
      coreMaterial = iron,
      pathLengthMeters = 0.30,
      crossSectionAreaSqM = 0.0016,
      turnsN = 500,
      currentAmperes = 2.0,
      airGapLengthMeters = 0.001 // 1 mm
    )
    val res = MagneticPhysicsEngine.solveMagneticCircuit(params)

    // MMF = N * I = 1000 A-t
    assertEquals(1000.0, params.mmf, 1e-6)
    // Reluctance total = R_core + R_gap
    assertEquals(res.coreReluctance + res.airGapReluctance, res.totalReluctance, 1e-6)
    // Flux is positive and nonzero
    assertTrue(res.magneticFluxWebers > 0.0)
    assertTrue(res.coreFluxDensityTesla > 0.0)
  }

  // 10. ADVANCED ELECTRIC FIELD LAB TESTS
  @Test
  fun testFieldLineTracingTangencyToElectricField() {
    val q1 = PointCharge("q1", Vector2D(-0.06, 0.0), 3.0)
    val q2 = PointCharge("q2", Vector2D(0.06, 0.0), -3.0)
    val charges = listOf(q1, q2)

    val fieldLines = ElectricPhysicsEngine.traceFieldLines(charges, vacuum, linesPerCharge = 8)
    assertTrue("Field lines must be generated for dipole", fieldLines.isNotEmpty())

    // Verify for segments along field lines that displacement vector is parallel to E
    for (line in fieldLines.take(4)) {
      if (line.points.size < 4) continue
      val p0 = line.points[1]
      val p1 = line.points[2]
      val dr = p1 - p0
      val eval = ElectricPhysicsEngine.evaluatePointField(p0, charges, vacuum)

      // dr must be in the same direction as E (dot product positive)
      val dotProduct = dr.dot(eval.fieldVector)
      assertTrue("Field line displacement must align with electric field vector", dotProduct > 0)

      // Normalized cross-product (sin of angle between dr and E) should be close to 0 (tangent)
      val crossMag = abs(dr.x * eval.fieldVector.y - dr.y * eval.fieldVector.x)
      val norm = dr.magnitude * eval.fieldVector.magnitude
      val sinAngle = crossMag / norm
      assertTrue("Field line must be tangent to E vector (sin(theta) < 0.15)", sinAngle < 0.15)
    }
  }

  @Test
  fun testEquipotentialOrthogonalityToField() {
    val q = PointCharge("q1", Vector2D.ZERO, 4.0)
    val charges = listOf(q)

    val seed = Vector2D(0.10, 0.0)
    val contourPoints = ElectricPhysicsEngine.traceEquipotentialCurve(seed, charges, vacuum)
    assertTrue("Equipotential curve must contain points", contourPoints.size > 8)

    // For an isolated charge at origin, potential V = k*q/r is constant along circles of radius r
    // Check that radial distance remains approximately constant along the contour
    for (pt in contourPoints.take(8)) {
      assertEquals("Radial distance on equipotential of isolated charge must remain r = 0.10 m", 0.10, pt.magnitude, 0.015)
    }

    // Check orthogonality: tangent vector along contour dot E must be approximately 0
    val p0 = contourPoints[3]
    val p1 = contourPoints[4]
    val tangent = p1 - p0
    val eval = ElectricPhysicsEngine.evaluatePointField(p0, charges, vacuum)

    val cosAngle = abs(tangent.dot(eval.fieldVector)) / (tangent.magnitude * eval.fieldVector.magnitude)
    assertTrue("Equipotential contour must be orthogonal to E-field (cos(theta) < 0.15)", cosAngle < 0.15)
  }

  @Test
  fun testTestChargeForceDirectionAndReversal() {
    val qSource = PointCharge("qs", Vector2D.ZERO, 5.0)
    val charges = listOf(qSource)
    val probePos = Vector2D(0.12, 0.0)

    val probePosCharge = com.example.model.TestProbe(probePos, testChargeNanoCoulombs = 2.0)
    val probeNegCharge = com.example.model.TestProbe(probePos, testChargeNanoCoulombs = -2.0)

    val resPos = ElectricPhysicsEngine.evaluatePointField(probePos, charges, vacuum, probePosCharge)
    val resNeg = ElectricPhysicsEngine.evaluatePointField(probePos, charges, vacuum, probeNegCharge)

    // E is pointing along +x
    assertTrue("E-field points along +x", resPos.fieldVector.x > 0)
    // F on positive test charge points along +x (same as E)
    assertTrue("Force on positive test charge must be parallel to E", resPos.forceVector.x > 0)
    // F on negative test charge points along -x (opposite to E)
    assertTrue("Force on negative test charge must oppose E", resNeg.forceVector.x < 0)

    // Magnitudes must be equal: |F| = |q| * |E|
    assertEquals(resPos.forceVector.magnitude, resNeg.forceVector.magnitude, 1e-12)
    val expectedForceMag = 2.0e-9 * resPos.fieldVector.magnitude
    assertEquals("F = q * E must hold", expectedForceMag, resPos.forceVector.magnitude, 1e-12)
  }

  @Test
  fun testElectricDipoleMidpointFieldAndPotential() {
    val d = 0.08
    val qPos = PointCharge("qp", Vector2D(-d, 0.0), 3.0)
    val qNeg = PointCharge("qn", Vector2D(d, 0.0), -3.0)
    val charges = listOf(qPos, qNeg)

    val midpoint = Vector2D.ZERO
    val res = ElectricPhysicsEngine.evaluatePointField(midpoint, charges, vacuum)

    // Midpoint potential of symmetric dipole must be exactly 0 V
    assertEquals("Potential at dipole midpoint must be 0 Volts", 0.0, res.potentialVolts, 1e-4)

    // E-field at midpoint points from positive charge to negative charge (+x direction)
    assertTrue("E-field at dipole midpoint must point along +x towards negative charge", res.fieldVector.x > 0)
    assertEquals("E-field y-component at dipole axis must be zero", 0.0, res.fieldVector.y, 1e-4)
  }

  // 11. INDUCTION POLE REVERSAL TEST
  @Test
  fun testMagnetPoleReversalInvertsInducedEmf() {
    val stateNorthFirst = InductionSimulationState(
      magnetPositionZ = 0.08,
      magnetVelocityZ = -0.4,
      magnetMomentAm2 = 2.5, // North pole leading
      coilTurnsN = 150
    )
    val stateSouthFirst = stateNorthFirst.copy(
      magnetMomentAm2 = -2.5 // South pole leading (reversed)
    )

    val resNorth = InductionPhysicsEngine.evaluateMagnetMotionInduction(stateNorthFirst, 0.01)
    val resSouth = InductionPhysicsEngine.evaluateMagnetMotionInduction(stateSouthFirst, 0.01)

    // Flux and EMF must be exactly equal in magnitude and opposite in sign
    assertEquals("Flux must invert with pole reversal", -resNorth.currentFluxWebers, resSouth.currentFluxWebers, 1e-9)
    assertEquals("EMF must invert with pole reversal", -resNorth.inducedEmfVolts, resSouth.inducedEmfVolts, 1e-9)
    assertEquals("Current must invert with pole reversal", -resNorth.inducedCurrentAmperes, resSouth.inducedCurrentAmperes, 1e-9)
  }

  // 12. B-H CURVE ASYMPTOTIC SATURATION TEST
  @Test
  fun testBHSaturationCurveAsymptoticBehavior() {
    val transformerSteel = PhysicsConstants.MAGNETIC_MATERIALS.first { it.name.contains("Silicon") }
    val bSat = transformerSteel.saturationB // typically ~1.95 T

    // Low H: linear region B ~ mu * H
    val hLow = 10.0
    val bLow = MagneticPhysicsEngine.calculateBhCurvePoint(hLow, transformerSteel)
    val expectedLowLinearB = transformerSteel.absolutePermeability * hLow
    // For small argument, tanh(x) ~ x
    assertEquals("At low H, B-H curve should follow linear permeability", expectedLowLinearB, bLow, expectedLowLinearB * 0.05)

    // Very high H: saturation region B -> B_sat
    val hExtreme = 500000.0 // 500 kA/m
    val bSaturated = MagneticPhysicsEngine.calculateBhCurvePoint(hExtreme, transformerSteel)
    assertTrue("At extreme H, B must approach B_sat", bSaturated <= bSat)
    assertEquals("At extreme H, B must be within 0.1% of B_sat", bSat, bSaturated, bSat * 0.001)
  }

  // 13. FINITE SOLENOID FIELD PROFILE TEST
  @Test
  fun testFiniteSolenoidCenterVsEndField() {
    val turns = 1000
    val length = 0.50 // 50 cm
    val radius = 0.02 // 2 cm (long solenoid L >> R, L/R = 25)
    val current = 3.0

    val bCenter = MagneticPhysicsEngine.finiteSolenoidOnAxisB(0.0, length, radius, turns, current, airMag)
    val bEnd = MagneticPhysicsEngine.finiteSolenoidOnAxisB(length / 2.0, length, radius, turns, current, airMag)
    val (bIdeal, _) = MagneticPhysicsEngine.idealSolenoidField(turns, length, current, airMag)

    // At center of long solenoid, finite B is very close to ideal infinite solenoid B = mu0 * n * I
    assertEquals("Finite solenoid center field must match ideal solenoid within 1%", bIdeal, bCenter, bIdeal * 0.01)

    // At the end (z = L/2), field must be exactly half the center field (within 2%)
    val ratioEndToCenter = bEnd / bCenter
    assertEquals("Field at the end of long solenoid must be approximately half the center field", 0.5, ratioEndToCenter, 0.02)
  }

  // 14. ELECTRICAL MACHINES PHYSICS TEST
  @Test
  fun testMachineBackEmfAndEddyBraking() {
    // DC Motor back-emf E_b = N * B * A * omega
    val motorState = MachineDemonstrationState(
      type = MachineType.DC_MOTOR,
      speedRpm = 1200.0,
      armatureCurrentAmperes = 4.0,
      magneticFieldTesla = 0.5,
      coilTurns = 200,
      coilAreaSqMeters = 0.02
    )
    val evaluatedMotor = WaveAndMachinePhysicsEngine.evaluateMachineState(motorState)

    // omega = 1200 * 2 * pi / 60 = 40 * pi rad/s
    val omega = (1200.0 * 2.0 * kotlin.math.PI) / 60.0
    val expectedBackEmf = 200 * 0.5 * 0.02 * omega
    assertEquals("Back EMF must equal N * B * A * omega", expectedBackEmf, evaluatedMotor.inducedBackEmfVolts, 1e-4)

    // Eddy Current Brake: Force is directly proportional to speed (omega)
    val brakeSlow = MachineDemonstrationState(
      type = MachineType.EDDY_BRAKE,
      speedRpm = 600.0,
      magneticFieldTesla = 0.6,
      coilAreaSqMeters = 0.01
    )
    val brakeFast = brakeSlow.copy(speedRpm = 1200.0) // 2x speed

    val evalBrakeSlow = WaveAndMachinePhysicsEngine.evaluateMachineState(brakeSlow)
    val evalBrakeFast = WaveAndMachinePhysicsEngine.evaluateMachineState(brakeFast)

    val ratio = evalBrakeFast.brakingForceNewtons / evalBrakeSlow.brakingForceNewtons
    assertEquals("Doubling disk speed must double eddy current braking force", 2.0, ratio, 1e-4)
  }
}


