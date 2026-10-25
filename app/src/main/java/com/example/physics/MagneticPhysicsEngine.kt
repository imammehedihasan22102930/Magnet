package com.example.physics

import com.example.model.MagneticCircuitParams
import com.example.model.MagneticCircuitResult
import com.example.model.MagneticFieldResult
import com.example.model.MagneticMaterial
import com.example.model.PhysicsConstants
import com.example.model.Vector2D
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

object MagneticPhysicsEngine {

  /** Minimum distance cutoff for finite-wire and dipole numerical safety */
  const val SINGULARITY_CUTOFF = 0.005 // 5 mm

  /**
   * Field of an ideal long straight wire at (wireX, wireY) carrying current I (A).
   * Direction: Right-hand rule (counter-clockwise for +I coming out of the page).
   * H = I / (2 * pi * r), B = mu * H
   */
  fun straightWireField(
    probePos: Vector2D,
    wirePos: Vector2D,
    currentAmperes: Double,
    material: MagneticMaterial = PhysicsConstants.MAGNETIC_MATERIALS.first()
  ): MagneticFieldResult {
    val dx = probePos.x - wirePos.x
    val dy = probePos.y - wirePos.y
    val r = sqrt(dx * dx + dy * dy)
    val isNear = r < SINGULARITY_CUTOFF

    val effectiveR = max(r, SINGULARITY_CUTOFF)
    val hMag = currentAmperes / (2.0 * PI * effectiveR) // H = I / (2 * pi * r)
    val bMag = material.absolutePermeability * hMag // B = mu * H

    // Tangential unit vector: (-dy/r, dx/r) for current pointing out (+z)
    val unitTangentialX = -dy / effectiveR
    val unitTangentialY = dx / effectiveR

    val hVec = Vector2D(unitTangentialX * hMag, unitTangentialY * hMag)
    val bVec = Vector2D(unitTangentialX * bMag, unitTangentialY * bMag)

    return MagneticFieldResult(
      bVector = bVec,
      hVector = hVec,
      bMagnitude = bMag,
      hMagnitude = hMag,
      isNearConductor = isNear
    )
  }

  /**
   * Field of two parallel conductors with currents I1 and I2 separated by distance D.
   */
  fun parallelWiresField(
    probePos: Vector2D,
    wire1Pos: Vector2D,
    current1: Double,
    wire2Pos: Vector2D,
    current2: Double,
    material: MagneticMaterial = PhysicsConstants.MAGNETIC_MATERIALS.first()
  ): MagneticFieldResult {
    val res1 = straightWireField(probePos, wire1Pos, current1, material)
    val res2 = straightWireField(probePos, wire2Pos, current2, material)

    val netB = res1.bVector + res2.bVector
    val netH = res1.hVector + res2.hVector

    return MagneticFieldResult(
      bVector = netB,
      hVector = netH,
      bMagnitude = netB.magnitude,
      hMagnitude = netH.magnitude,
      isNearConductor = res1.isNearConductor || res2.isNearConductor
    )
  }

  /**
   * Exact on-axis magnetic flux density B_z of a circular current loop:
   * B_z = (mu * I * R^2) / (2 * (z^2 + R^2)^(3/2))
   */
  fun circularLoopOnAxisB(
    zDistanceMeters: Double,
    radiusMeters: Double,
    currentAmperes: Double,
    material: MagneticMaterial = PhysicsConstants.MAGNETIC_MATERIALS.first()
  ): Double {
    val rSq = radiusMeters * radiusMeters
    val denom = 2.0 * (zDistanceMeters * zDistanceMeters + rSq).let { it * sqrt(it) }
    return (material.absolutePermeability * currentAmperes * rSq) / denom
  }

  /**
   * Finite Solenoid on-axis field profile:
   * B_z(z) = (mu * n * I / 2) * [ (z + L/2)/sqrt((z + L/2)^2 + R^2) - (z - L/2)/sqrt((z - L/2)^2 + R^2) ]
   */
  fun finiteSolenoidOnAxisB(
    zDistanceMeters: Double,
    lengthMeters: Double,
    radiusMeters: Double,
    turnsN: Int,
    currentAmperes: Double,
    material: MagneticMaterial = PhysicsConstants.MAGNETIC_MATERIALS.first()
  ): Double {
    val n = turnsN / max(lengthMeters, 1e-4) // turns per meter
    val mu = material.absolutePermeability
    val halfL = lengthMeters / 2.0
    val z1 = zDistanceMeters + halfL
    val z2 = zDistanceMeters - halfL

    val term1 = z1 / sqrt(z1 * z1 + radiusMeters * radiusMeters)
    val term2 = z2 / sqrt(z2 * z2 + radiusMeters * radiusMeters)

    return (mu * n * currentAmperes / 2.0) * (term1 - term2)
  }

  /**
   * Ideal Infinite Solenoid internal field:
   * B = mu * n * I, H = n * I
   */
  fun idealSolenoidField(
    turnsN: Int,
    lengthMeters: Double,
    currentAmperes: Double,
    material: MagneticMaterial = PhysicsConstants.MAGNETIC_MATERIALS.first()
  ): Pair<Double, Double> {
    val n = turnsN / max(lengthMeters, 1e-4)
    val h = n * currentAmperes
    val b = material.absolutePermeability * h
    return Pair(b, h)
  }

  /**
   * Bar Magnet 2D Magnetic Dipole Field:
   * B(r) = (mu_0 / (4*pi)) * (1 / r^3) * [ 3 (m · r_hat) r_hat - m ]
   */
  fun barMagnetDipoleField(
    probePos: Vector2D,
    magnetCenter: Vector2D,
    dipoleMomentM: Double, // Along x-axis
    poleSeparationM: Double = 0.08
  ): MagneticFieldResult {
    // Two opposite magnetic poles (+qm at +x, -qm at -x)
    val qm = dipoleMomentM / max(poleSeparationM, 0.01)
    val northPos = magnetCenter + Vector2D(poleSeparationM / 2.0, 0.0)
    val southPos = magnetCenter - Vector2D(poleSeparationM / 2.0, 0.0)

    val dNorth = probePos - northPos
    val dSouth = probePos - southPos

    val rN = max(dNorth.magnitude, SINGULARITY_CUTOFF)
    val rS = max(dSouth.magnitude, SINGULARITY_CUTOFF)

    val kMag = (PhysicsConstants.MU_0) / (4.0 * PI)
    // Field points away from North (+qm) and towards South (-qm)
    val bFromN = dNorth.normalized() * (kMag * qm / (rN * rN))
    val bFromS = dSouth.normalized() * (-kMag * qm / (rS * rS))

    val netB = bFromN + bFromS
    val netH = netB / PhysicsConstants.MU_0

    return MagneticFieldResult(
      bVector = netB,
      hVector = netH,
      bMagnitude = netB.magnitude,
      hMagnitude = netH.magnitude,
      isNearConductor = rN < SINGULARITY_CUTOFF || rS < SINGULARITY_CUTOFF
    )
  }

  /**
   * Magnetic Flux and Linkage through a flat coil:
   * Phi_B = B * A * cos(theta), lambda = N * Phi_B
   */
  fun calculateMagneticFluxAndLinkage(
    magneticFieldTesla: Double,
    coilAreaSqMeters: Double,
    angleBetweenFieldAndNormalDeg: Double,
    numberOfTurns: Int
  ): Pair<Double, Double> {
    val thetaRad = Math.toRadians(angleBetweenFieldAndNormalDeg)
    val fluxPhi = magneticFieldTesla * coilAreaSqMeters * cos(thetaRad)
    val fluxLinkageLambda = numberOfTurns * fluxPhi
    return Pair(fluxPhi, fluxLinkageLambda)
  }

  /**
   * Lumped Magnetic Circuit Solver with core and air-gap:
   * R_core = l_c / (mu_r * mu_0 * A)
   * R_gap  = l_g / (mu_0 * A)
   * R_tot  = R_core + R_gap
   * Phi    = (N * I) / R_tot
   * B_core = Phi / A
   */
  fun solveMagneticCircuit(params: MagneticCircuitParams): MagneticCircuitResult {
    val muCore = params.coreMaterial.absolutePermeability
    val mu0 = PhysicsConstants.MU_0
    val area = max(params.crossSectionAreaSqM, 1e-6)

    // Linear reluctance
    val coreReluctance = params.pathLengthMeters / (muCore * area)
    val airGapReluctance = params.airGapLengthMeters / (mu0 * area)
    val totalReluctance = coreReluctance + airGapReluctance

    // Linear flux calculation
    val linearFlux = params.mmf / max(totalReluctance, 1.0)
    val linearB = linearFlux / area

    // Check non-linear saturation
    val isSaturated = params.coreMaterial.isFerromagnetic && linearB > params.coreMaterial.saturationB * 0.9
    val effectiveB = if (params.coreMaterial.isFerromagnetic) {
      // Froelich / tanh saturation curve
      params.coreMaterial.saturationB * tanh(linearB / params.coreMaterial.saturationB)
    } else {
      linearB
    }

    val effectiveFlux = effectiveB * area
    val coreH = effectiveB / muCore
    val gapH = effectiveB / mu0

    return MagneticCircuitResult(
      coreReluctance = coreReluctance,
      airGapReluctance = airGapReluctance,
      totalReluctance = totalReluctance,
      magneticFluxWebers = effectiveFlux,
      coreFluxDensityTesla = effectiveB,
      coreFieldIntensityH = coreH,
      airGapFluxDensityTesla = effectiveB,
      airGapFieldIntensityH = gapH,
      isSaturated = isSaturated
    )
  }

  /**
   * Nonlinear B-H Curve point calculation using tanh saturation model:
   * B(H) = B_sat * tanh( (mu_r * mu_0 * H) / B_sat )
   */
  fun calculateBhCurvePoint(
    hFieldAmpPerMeter: Double,
    material: MagneticMaterial
  ): Double {
    if (!material.isFerromagnetic) {
      return material.absolutePermeability * hFieldAmpPerMeter
    }
    val initialSlope = material.absolutePermeability
    val bLinear = initialSlope * hFieldAmpPerMeter
    return material.saturationB * tanh(bLinear / material.saturationB)
  }
}
