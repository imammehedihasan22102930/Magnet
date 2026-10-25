package com.example.physics

import com.example.model.DielectricMaterial
import com.example.model.FieldPointResult
import com.example.model.PhysicsConstants
import com.example.model.PointCharge
import com.example.model.TestProbe
import com.example.model.Vector2D
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

data class FieldLine(
  val points: List<Vector2D>,
  val isPositiveOrigin: Boolean = true
)

data class EquipotentialContour(
  val voltageLevel: Double,
  val points: List<Vector2D>
)

object ElectricPhysicsEngine {

  /** Minimum distance to prevent mathematical singularity at the charge center */
  const val SINGULARITY_CUTOFF_RADIUS = 0.015 // 1.5 cm

  /**
   * Calculates the electric field vector E, electric potential V, and force F on a test charge
   * resulting from an arbitrary configuration of point charges in a uniform dielectric medium.
   */
  fun evaluatePointField(
    probePosition: Vector2D,
    charges: List<PointCharge>,
    dielectric: DielectricMaterial = PhysicsConstants.DIELECTRIC_MATERIALS.first(),
    testProbe: TestProbe? = null
  ): FieldPointResult {
    var netEx = 0.0
    var netEy = 0.0
    var netPotential = 0.0
    var isSingular = false

    val eps = dielectric.absolutePermittivity
    val kDielectric = 1.0 / (4.0 * PI * eps)

    for (charge in charges) {
      val dx = probePosition.x - charge.position.x
      val dy = probePosition.y - charge.position.y
      val distSq = dx * dx + dy * dy
      val dist = sqrt(distSq)

      if (dist < SINGULARITY_CUTOFF_RADIUS) {
        isSingular = true
      }

      val effectiveDist = max(dist, SINGULARITY_CUTOFF_RADIUS)
      val effectiveDistSq = effectiveDist * effectiveDist

      // Coulomb field: E = (1 / (4*pi*eps)) * (q / r^2) * r_hat
      val eMag = kDielectric * charge.chargeCoulombs / effectiveDistSq
      val unitX = dx / effectiveDist
      val unitY = dy / effectiveDist

      netEx += eMag * unitX
      netEy += eMag * unitY

      // Electric potential: V = (1 / (4*pi*eps)) * (q / r)
      netPotential += kDielectric * charge.chargeCoulombs / effectiveDist
    }

    val fieldVec = Vector2D(netEx, netEy)
    val fluxDensityD = fieldVec * eps // D = eps * E

    val forceVec = if (testProbe != null) {
      fieldVec * testProbe.testChargeCoulombs // F = q * E
    } else {
      Vector2D.ZERO
    }

    return FieldPointResult(
      fieldVector = fieldVec,
      potentialVolts = netPotential,
      forceVector = forceVec,
      fluxDensityD = fluxDensityD,
      isNearSingularity = isSingular
    )
  }

  /**
   * Traces continuous electric field lines starting from point charges.
   * Field lines follow the trajectory dr/ds = E / |E| using 2nd-order Runge-Kutta numerical integration.
   */
  fun traceFieldLines(
    charges: List<PointCharge>,
    dielectric: DielectricMaterial = PhysicsConstants.DIELECTRIC_MATERIALS.first(),
    linesPerCharge: Int = 12,
    boundsLimitMeters: Double = 0.32,
    stepSizeMeters: Double = 0.005,
    maxSteps: Int = 90
  ): List<FieldLine> {
    if (charges.isEmpty()) return emptyList()

    val fieldLines = mutableListOf<FieldLine>()
    val rSeed = SINGULARITY_CUTOFF_RADIUS * 1.3

    // 1. Trace outward from positive charges
    val positiveCharges = charges.filter { it.isPositive }
    val negativeCharges = charges.filter { it.isNegative }

    for (q in positiveCharges) {
      val rayCount = max(6, (linesPerCharge * (abs(q.chargeMicroCoulombs) / 3.0)).toInt().coerceIn(6, 24))
      for (i in 0 until rayCount) {
        val angle = (i.toDouble() / rayCount.toDouble()) * (2.0 * PI)
        val seedPoint = q.position + Vector2D(rSeed * cos(angle), rSeed * sin(angle))
        val linePoints = traceSingleStreamline(
          start = seedPoint,
          charges = charges,
          dielectric = dielectric,
          direction = 1.0, // Forward along E
          stepSize = stepSizeMeters,
          maxSteps = maxSteps,
          bounds = boundsLimitMeters,
          targetCharges = negativeCharges
        )
        if (linePoints.size > 2) {
          fieldLines.add(FieldLine(points = linePoints, isPositiveOrigin = true))
        }
      }
    }

    // 2. If there are no positive charges (only negative charges), trace inward from boundaries
    if (positiveCharges.isEmpty() && negativeCharges.isNotEmpty()) {
      for (q in negativeCharges) {
        val rayCount = max(6, (linesPerCharge * (abs(q.chargeMicroCoulombs) / 3.0)).toInt().coerceIn(6, 24))
        for (i in 0 until rayCount) {
          val angle = (i.toDouble() / rayCount.toDouble()) * (2.0 * PI)
          val seedPoint = q.position + Vector2D(rSeed * cos(angle), rSeed * sin(angle))
          // Trace backward (-E) away from negative charge into space
          val linePoints = traceSingleStreamline(
            start = seedPoint,
            charges = charges,
            dielectric = dielectric,
            direction = -1.0,
            stepSize = stepSizeMeters,
            maxSteps = maxSteps,
            bounds = boundsLimitMeters,
            targetCharges = emptyList()
          )
          if (linePoints.size > 2) {
            fieldLines.add(FieldLine(points = linePoints.reversed(), isPositiveOrigin = false))
          }
        }
      }
    }

    return fieldLines
  }

  private fun traceSingleStreamline(
    start: Vector2D,
    charges: List<PointCharge>,
    dielectric: DielectricMaterial,
    direction: Double,
    stepSize: Double,
    maxSteps: Int,
    bounds: Double,
    targetCharges: List<PointCharge>
  ): List<Vector2D> {
    val points = mutableListOf<Vector2D>()
    var current = start
    points.add(current)

    for (step in 0 until maxSteps) {
      if (abs(current.x) > bounds || abs(current.y) > bounds) break

      // Stop if near any target absorbing charge
      var absorbed = false
      for (tc in targetCharges) {
        if (current.distanceTo(tc.position) < SINGULARITY_CUTOFF_RADIUS * 1.1) {
          points.add(tc.position)
          absorbed = true
          break
        }
      }
      if (absorbed) break

      val eval1 = evaluatePointField(current, charges, dielectric)
      val eMag1 = eval1.fieldVector.magnitude
      if (eMag1 < 1.0) break

      val unitE1 = eval1.fieldVector.normalized() * direction
      val midpoint = current + unitE1 * (stepSize / 2.0)

      val eval2 = evaluatePointField(midpoint, charges, dielectric)
      val unitE2 = eval2.fieldVector.normalized() * direction

      val next = current + unitE2 * stepSize
      points.add(next)
      current = next
    }

    return points
  }

  /**
   * Traces an equipotential contour starting from a seed point.
   * Since E = -grad(V), the contour line is strictly tangent to (-Ey, Ex) / |E|.
   */
  fun traceEquipotentialCurve(
    seed: Vector2D,
    charges: List<PointCharge>,
    dielectric: DielectricMaterial = PhysicsConstants.DIELECTRIC_MATERIALS.first(),
    boundsLimitMeters: Double = 0.30,
    stepSizeMeters: Double = 0.005,
    maxSteps: Int = 100
  ): List<Vector2D> {
    val seedEval = evaluatePointField(seed, charges, dielectric)
    if (seedEval.fieldVector.magnitude < 1.0) return emptyList()

    // Trace in forward tangent direction
    val fwdPoints = traceEquiDirection(seed, charges, dielectric, 1.0, boundsLimitMeters, stepSizeMeters, maxSteps)
    // Trace in reverse tangent direction
    val revPoints = traceEquiDirection(seed, charges, dielectric, -1.0, boundsLimitMeters, stepSizeMeters, maxSteps)

    return revPoints.reversed() + fwdPoints.drop(1)
  }

  private fun traceEquiDirection(
    start: Vector2D,
    charges: List<PointCharge>,
    dielectric: DielectricMaterial,
    dirSign: Double,
    bounds: Double,
    stepSize: Double,
    maxSteps: Int
  ): List<Vector2D> {
    val path = mutableListOf<Vector2D>()
    var current = start
    path.add(current)

    for (s in 0 until maxSteps) {
      if (abs(current.x) > bounds || abs(current.y) > bounds) break

      // Avoid entering inside charge radius
      var tooClose = false
      for (c in charges) {
        if (current.distanceTo(c.position) < SINGULARITY_CUTOFF_RADIUS * 1.1) {
          tooClose = true
          break
        }
      }
      if (tooClose) break

      val eval = evaluatePointField(current, charges, dielectric)
      val eMag = eval.fieldVector.magnitude
      if (eMag < 1.0) break

      // Tangent perpendicular to E: (-Ey, Ex)
      val unitTangent = Vector2D(-eval.fieldVector.y / eMag, eval.fieldVector.x / eMag) * dirSign
      val next = current + unitTangent * stepSize

      // Check if looped back to start
      if (path.size > 15 && next.distanceTo(start) < stepSize * 1.5) {
        path.add(start)
        break
      }

      path.add(next)
      current = next
    }

    return path
  }

  /**
   * Generates a family of representative equipotential contours across the simulation area.
   */
  fun generateEquipotentialContours(
    charges: List<PointCharge>,
    dielectric: DielectricMaterial = PhysicsConstants.DIELECTRIC_MATERIALS.first(),
    contourLevelsCount: Int = 6
  ): List<EquipotentialContour> {
    if (charges.isEmpty()) return emptyList()

    val contours = mutableListOf<EquipotentialContour>()
    val radii = listOf(0.04, 0.08, 0.12, 0.18, 0.24)

    for (c in charges) {
      val numSeedAngles = 4
      for (r in radii.take(contourLevelsCount)) {
        val seed = c.position + Vector2D(r, 0.0)
        val eval = evaluatePointField(seed, charges, dielectric)
        val curve = traceEquipotentialCurve(seed, charges, dielectric)
        if (curve.size > 8) {
          contours.add(EquipotentialContour(voltageLevel = eval.potentialVolts, points = curve))
        }
      }
    }

    return contours
  }

  /**
   * Calculates electric flux through a flat surface oriented at angle theta to uniform electric field:
   * Phi_E = E * A * cos(theta_rad)
   */
  fun calculateUniformSurfaceFlux(
    fieldMagnitudeVPerM: Double,
    surfaceAreaSqM: Double,
    angleBetweenFieldAndNormalDeg: Double,
    dielectric: DielectricMaterial
  ): Pair<Double, Double> {
    val thetaRad = Math.toRadians(angleBetweenFieldAndNormalDeg)
    val fluxPhi = fieldMagnitudeVPerM * surfaceAreaSqM * cos(thetaRad)
    val fluxDensityD = dielectric.absolutePermittivity * fieldMagnitudeVPerM
    return Pair(fluxPhi, fluxDensityD)
  }

  /**
   * Gauss's law analytical solution for total flux through any closed Gaussian surface enclosing charge Q_enc:
   * Phi_E = Q_enclosed / epsilon
   */
  fun calculateGaussianClosedFlux(
    enclosedChargeMicroCoulombs: Double,
    dielectric: DielectricMaterial
  ): Double {
    val qCoulombs = enclosedChargeMicroCoulombs * 1.0e-6
    return qCoulombs / dielectric.absolutePermittivity
  }

  /**
   * Field of an infinite line charge: E = lambda / (2 * pi * epsilon * r)
   */
  fun infiniteLineChargeField(
    lineChargeDensityMicroCPerM: Double,
    radialDistanceM: Double,
    dielectric: DielectricMaterial
  ): Double {
    val lambda = lineChargeDensityMicroCPerM * 1.0e-6
    val r = max(radialDistanceM, 1e-4)
    return lambda / (2.0 * PI * dielectric.absolutePermittivity * r)
  }

  /**
   * Field of an infinite sheet of charge: E = sigma / (2 * epsilon)
   */
  fun infiniteSheetChargeField(
    surfaceChargeDensityMicroCPerM2: Double,
    dielectric: DielectricMaterial
  ): Double {
    val sigma = surfaceChargeDensityMicroCPerM2 * 1.0e-6
    return sigma / (2.0 * dielectric.absolutePermittivity)
  }

  /**
   * Field of a spherical charge shell (radius R, charge Q):
   * Inside (r < R): E = 0
   * Outside (r >= R): E = k * Q / r^2
   */
  fun sphericalShellField(
    totalChargeMicroCoulombs: Double,
    shellRadiusM: Double,
    radialDistanceM: Double,
    dielectric: DielectricMaterial
  ): Double {
    if (radialDistanceM < shellRadiusM) return 0.0
    val q = totalChargeMicroCoulombs * 1.0e-6
    val k = 1.0 / (4.0 * PI * dielectric.absolutePermittivity)
    return k * q / (radialDistanceM * radialDistanceM)
  }

  /**
   * Numerically integrates electric flux over a closed spherical Gaussian surface of radius r
   * by summing E_normal * dA over discrete surface elements.
   * Compares with analytical Gauss's Law (Q_enc / epsilon).
   */
  fun numericallyIntegrateSphericalFlux(
    radiusM: Double,
    charges: List<PointCharge>,
    dielectric: DielectricMaterial = PhysicsConstants.DIELECTRIC_MATERIALS.first(),
    latitudeBands: Int = 20,
    longitudeBands: Int = 36
  ): Double {
    var totalFlux = 0.0
    val dTheta = PI / latitudeBands
    val dPhi = (2.0 * PI) / longitudeBands

    for (i in 0 until latitudeBands) {
      val theta = (i + 0.5) * dTheta
      val sinTheta = sin(theta)
      val dA = (radiusM * radiusM) * sinTheta * dTheta * dPhi

      for (j in 0 until longitudeBands) {
        val phi = (j + 0.5) * dPhi
        val sx = radiusM * sinTheta * cos(phi)
        val sy = radiusM * sinTheta * sin(phi)
        val eval = evaluatePointField(Vector2D(sx, sy), charges, dielectric)
        val unitNormal = Vector2D(sx, sy).normalized()
        val eNormal = eval.fieldVector.dot(unitNormal)
        totalFlux += eNormal * dA
      }
    }
    return totalFlux
  }
}
