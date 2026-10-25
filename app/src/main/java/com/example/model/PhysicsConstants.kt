package com.example.model

import kotlin.math.PI
import kotlin.math.sqrt

object PhysicsConstants {
  /** Permittivity of free space (F/m or C^2/(N*m^2)) */
  const val EPSILON_0 = 8.8541878128e-12

  /** Permeability of free space (H/m or T*m/A) */
  const val MU_0 = 4.0 * PI * 1.0e-7

  /** Speed of light in vacuum (m/s) */
  const val C = 299792458.0

  /** Coulomb constant k = 1 / (4 * pi * epsilon_0) (N*m^2/C^2) */
  const val K_E = 1.0 / (4.0 * PI * EPSILON_0)

  /** Elementary charge (C) */
  const val ELEMENTARY_CHARGE = 1.602176634e-19

  /** Common Dielectric Materials with Relative Permittivity (epsilon_r) */
  val DIELECTRIC_MATERIALS = listOf(
    DielectricMaterial("Vacuum", 1.0),
    DielectricMaterial("Air (STP)", 1.00059),
    DielectricMaterial("Teflon (PTFE)", 2.1),
    DielectricMaterial("Paper", 3.0),
    DielectricMaterial("Glass (Pyrex)", 4.7),
    DielectricMaterial("Mica", 5.4),
    DielectricMaterial("Silicon", 11.68),
    DielectricMaterial("Distilled Water", 80.1)
  )

  /** Common Magnetic Core Materials with Relative Permeability (mu_r) & Saturation B_sat (T) */
  val MAGNETIC_MATERIALS = listOf(
    MagneticMaterial("Air / Vacuum", 1.0, 100.0, isFerromagnetic = false),
    MagneticMaterial("Aluminum (Paramagnetic)", 1.00002, 100.0, isFerromagnetic = false),
    MagneticMaterial("Ferrite Mn-Zn", 800.0, 0.45, isFerromagnetic = true),
    MagneticMaterial("Cast Iron", 600.0, 1.2, isFerromagnetic = true),
    MagneticMaterial("Mild Steel", 2000.0, 1.6, isFerromagnetic = true),
    MagneticMaterial("Silicon Grain-Oriented Steel", 5000.0, 1.95, isFerromagnetic = true),
    MagneticMaterial("Mu-Metal (High Perm)", 25000.0, 0.8, isFerromagnetic = true)
  )
}

data class DielectricMaterial(
  val name: String,
  val relativePermittivity: Double
) {
  val absolutePermittivity: Double get() = relativePermittivity * PhysicsConstants.EPSILON_0
}

data class MagneticMaterial(
  val name: String,
  val relativePermeability: Double,
  val saturationB: Double, // Saturation flux density in Tesla
  val isFerromagnetic: Boolean = true
) {
  val absolutePermeability: Double get() = relativePermeability * PhysicsConstants.MU_0
}
