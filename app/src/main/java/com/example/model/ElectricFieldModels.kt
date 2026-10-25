package com.example.model

data class PointCharge(
  val id: String,
  val position: Vector2D, // In meters (simulation scale)
  val chargeMicroCoulombs: Double, // In microCoulombs (µC)
  val isFixed: Boolean = false
) {
  val chargeCoulombs: Double get() = chargeMicroCoulombs * 1.0e-6
  val isPositive: Boolean get() = chargeMicroCoulombs > 0
  val isNegative: Boolean get() = chargeMicroCoulombs < 0
}

data class TestProbe(
  val position: Vector2D, // In meters
  val testChargeNanoCoulombs: Double = 1.0 // In nanoCoulombs (nC)
) {
  val testChargeCoulombs: Double get() = testChargeNanoCoulombs * 1.0e-9
}

data class FieldPointResult(
  val fieldVector: Vector2D, // Electric field in V/m or N/C
  val potentialVolts: Double, // Electric potential V in Volts
  val forceVector: Vector2D, // Force on test charge in Newtons
  val fluxDensityD: Vector2D, // Electric flux density D in C/m^2
  val isNearSingularity: Boolean = false
)

enum class GaussianSurfaceType(val displayName: String, val symmetry: String) {
  SPHERE("Concentric Sphere", "Spherical Symmetry"),
  CYLINDER("Coaxial Cylinder", "Cylindrical Symmetry"),
  PILLBOX("Planar Pillbox", "Planar Symmetry")
}

enum class SourceConfiguration(val displayName: String) {
  POINT_CHARGE("Point Charge"),
  SPHERICAL_SHELL("Spherical Charge Shell"),
  INFINITE_LINE("Ideal Infinite Line Charge"),
  INFINITE_SHEET("Ideal Infinite Sheet of Charge"),
  ELECTRIC_DIPOLE("Electric Dipole (+q, -q)")
}
