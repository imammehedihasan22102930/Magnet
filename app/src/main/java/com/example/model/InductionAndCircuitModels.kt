package com.example.model

data class InductionSimulationState(
  val magnetPositionZ: Double = 0.15, // Magnet distance from coil center (m)
  val magnetVelocityZ: Double = 0.0, // Velocity in m/s (positive = moving away, negative = moving towards)
  val magnetMomentAm2: Double = 5.0, // Dipole moment m (A*m^2)
  val coilTurnsN: Int = 200, // Number of coil turns
  val coilRadiusMeters: Double = 0.04, // Radius of coil (m)
  val circuitResistanceOhms: Double = 5.0, // Resistance of loop circuit (Ohms)
  val circuitInductanceHenrys: Double = 0.005, // Inductance of coil (H)
  val currentFluxWebers: Double = 0.0,
  val currentFluxLinkage: Double = 0.0,
  val inducedEmfVolts: Double = 0.0,
  val inducedCurrentAmperes: Double = 0.0,
  val galvanometerAngleDeg: Double = 0.0
)

data class RLStepResponsePoint(
  val timeSeconds: Double,
  val currentAmperes: Double,
  val inductorVoltageVolts: Double,
  val resistorVoltageVolts: Double,
  val storedEnergyJoules: Double
)

data class TransformerParams(
  val primaryTurnsNp: Int = 500,
  val secondaryTurnsNs: Int = 100,
  val primaryVoltageRms: Double = 120.0, // V_rms
  val frequencyHz: Double = 60.0, // 50Hz or 60Hz
  val loadResistanceOhms: Double = 20.0, // Load resistance in Ohms
  val primaryWindingResistanceOhms: Double = 1.2,
  val secondaryWindingResistanceOhms: Double = 0.15,
  val couplingCoefficientK: Double = 0.98, // 0.0 to 1.0 (ideal = 1.0)
  val coreLossResistanceOhms: Double = 5000.0 // Rc core loss
)

data class TransformerResult(
  val turnsRatio: Double, // a = Np / Ns
  val secondaryVoltageRms: Double,
  val secondaryCurrentRms: Double,
  val primaryCurrentRms: Double,
  val outputPowerWatts: Double,
  val inputPowerWatts: Double,
  val efficiencyPercent: Double,
  val coreFluxPeakWebers: Double,
  val voltageRegulationPercent: Double,
  val isIdeal: Boolean
)
