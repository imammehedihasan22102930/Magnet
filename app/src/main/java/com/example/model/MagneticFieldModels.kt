package com.example.model

enum class MagneticSourceType(val displayName: String) {
  STRAIGHT_WIRE("Long Straight Wire"),
  PARALLEL_WIRES("Two Parallel Conductors"),
  CIRCULAR_LOOP("Circular Current Loop"),
  SOLENOID_FINITE("Finite Solenoid (Real Ends)"),
  SOLENOID_IDEAL("Ideal Long Solenoid"),
  BAR_MAGNET("Permanent Bar Magnet (Dipole)")
}

data class MagneticFieldResult(
  val bVector: Vector2D, // Magnetic Flux Density B in Tesla (T)
  val hVector: Vector2D, // Magnetic Field Intensity H in A/m
  val bMagnitude: Double,
  val hMagnitude: Double,
  val isNearConductor: Boolean = false
)

data class MagneticCircuitParams(
  val coreMaterial: MagneticMaterial,
  val pathLengthMeters: Double = 0.25, // Mean magnetic path length l_c (m)
  val crossSectionAreaSqM: Double = 0.0016, // A_c (m^2), e.g. 40mm x 40mm
  val turnsN: Int = 500, // Number of coil turns
  val currentAmperes: Double = 2.0, // Coil current (A)
  val airGapLengthMeters: Double = 0.001 // Air gap length l_g (m)
) {
  val mmf: Double get() = turnsN * currentAmperes // Magnetomotive Force in Ampere-turns (A-t)
}

data class MagneticCircuitResult(
  val coreReluctance: Double, // Reluctance of core R_c in A-t/Wb (or 1/H)
  val airGapReluctance: Double, // Reluctance of air gap R_g in A-t/Wb
  val totalReluctance: Double, // R_total = R_c + R_g
  val magneticFluxWebers: Double, // Phi = MMF / R_total (Wb)
  val coreFluxDensityTesla: Double, // B = Phi / A_c (T)
  val coreFieldIntensityH: Double, // H_c = B / (mu_r * mu_0) (A/m)
  val airGapFluxDensityTesla: Double, // B_gap (T)
  val airGapFieldIntensityH: Double, // H_gap = B / mu_0 (A/m)
  val isSaturated: Boolean
)
