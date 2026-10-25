package com.example.model

object EducationalContentRepository {

  val GUIDED_EXPERIMENTS = listOf(
    GuidedExperiment(
      id = "exp_a",
      code = "EXPERIMENT A",
      title = "Inverse-Square Law of Electric Field",
      targetLabScreen = "ELECTRIC_FIELDS",
      objective = "Investigate how the electric field magnitude E varies with radial distance r from an isolated point charge.",
      theory = "Coulomb's law dictates that the electric field of an isolated point charge decays proportionally to the inverse square of the distance: E = (1 / 4πε) * (|q| / r²). When the distance r is doubled (2×), the field magnitude must drop to one-quarter (1/4 = 0.25×).",
      initialSettingsSummary = "Charge: +5.0 µC at origin (0, 0), Medium: Vacuum (ε_r = 1.0)",
      procedureSteps = listOf(
        "Place a positive point charge of +5.0 µC at position (0.0, 0.0) m.",
        "Position the test probe at distance r = 0.10 m (10 cm). Record the electric field magnitude E_1.",
        "Move the test probe outward to double the distance, r = 0.20 m (20 cm). Record E_2.",
        "Calculate the ratio E_2 / E_1 and compare with the theoretical prediction of 1/4 = 0.25."
      ),
      predictionPrompt = "If the probe distance from a point charge is increased from 0.10 m to 0.20 m (doubled), what happens to the electric field magnitude?",
      predictionOptions = listOf(
        "Halves to 50% of original value (E/2)",
        "Drops to 25% of original value (E/4)",
        "Drops to 12.5% of original value (E/8)",
        "Remains constant"
      ),
      correctPredictionIndex = 1,
      predictionExplanation = "Because the field varies as 1/r², doubling r produces (1/2)² = 1/4 of the initial field magnitude.",
      expectedRelationship = "E ∝ 1 / r²",
      reflectionQuestions = listOf(
        "Why does a 2D line charge follow 1/r rather than 1/r²?",
        "How would changing the dielectric medium from vacuum to water (ε_r = 80) affect the field at the same distance?"
      )
    ),

    GuidedExperiment(
      id = "exp_b",
      code = "EXPERIMENT B",
      title = "Surface Orientation and Electric Flux",
      targetLabScreen = "ELECTRIC_FLUX",
      objective = "Determine how surface orientation angle θ relative to a uniform electric field determines total electric flux Φ_E.",
      theory = "Electric flux is defined as the surface integral of electric field: Φ_E = ∮ E · dA = E · A · cos(θ), where θ is the angle between the electric field vectors and the surface normal unit vector n̂.",
      initialSettingsSummary = "Field: 1000 V/m, Area: 0.05 m², Medium: Vacuum",
      procedureSteps = listOf(
        "Set the uniform electric field to E = 1000 V/m and surface area A = 0.05 m².",
        "Start with angle θ = 0° (normal parallel to field). Record maximum flux Φ_max.",
        "Rotate the surface to θ = 60° and observe the projected area and live flux.",
        "Rotate to θ = 90° (field grazing parallel to surface) and verify zero net crossing."
      ),
      predictionPrompt = "At what angle θ between the field lines and the surface normal does the electric flux reach exactly half of its maximum value?",
      predictionOptions = listOf(
        "30 degrees",
        "45 degrees",
        "60 degrees",
        "90 degrees"
      ),
      correctPredictionIndex = 2,
      predictionExplanation = "Because Φ_E = E · A · cos(θ), and cos(60°) = 0.5, the flux is halved at exactly 60°.",
      expectedRelationship = "Φ_E = E · A · cos(θ)",
      reflectionQuestions = listOf(
        "What is the difference between electric flux Φ_E (V·m) and electric flux density D (C/m²)?",
        "What is the physical meaning of negative flux when θ > 90°?"
      )
    ),

    GuidedExperiment(
      id = "exp_c",
      code = "EXPERIMENT C",
      title = "Magnetic Field of a Straight Conductor",
      targetLabScreen = "MAGNETIC_FIELDS",
      objective = "Verify Ampère's law relationship H = I / (2πr) for a long current-carrying wire.",
      theory = "For a long straight wire carrying current I, circular magnetic field loops form concentric with the wire. The magnetic field intensity is H = I / (2πr) and flux density B = µ · H.",
      initialSettingsSummary = "Current: 10.0 A, Medium: Air/Vacuum (µ_r = 1.0)",
      procedureSteps = listOf(
        "Set wire current to 10.0 A.",
        "Place the magnetic field probe at radial distance r = 0.05 m (5 cm). Record B_1.",
        "Increase the current from 10 A to 20 A at the same radial distance and record B_2.",
        "Verify that doubling current doubles B, confirming the linear relationship B ∝ I."
      ),
      predictionPrompt = "According to Ampère's law, what is the effect on B if current is doubled AND distance is doubled simultaneously?",
      predictionOptions = listOf(
        "Field quadruples (4×)",
        "Field doubles (2×)",
        "Field remains exactly the same (1×)",
        "Field drops to half (0.5×)"
      ),
      correctPredictionIndex = 2,
      predictionExplanation = "B = (µ I) / (2πr). Multiplying both numerator (I) and denominator (r) by 2 leaves the ratio unchanged.",
      expectedRelationship = "B ∝ I / r",
      reflectionQuestions = listOf(
        "How do you determine the direction of the field vectors using the right-hand rule?",
        "What happens to the field if two parallel wires carry equal currents in opposite directions?"
      )
    ),

    GuidedExperiment(
      id = "exp_d",
      code = "EXPERIMENT D",
      title = "Coil Orientation and Magnetic Flux Linkage",
      targetLabScreen = "MAGNETIC_FLUX",
      objective = "Investigate magnetic flux Φ_B and total flux linkage λ = N · Φ_B in a rotating multi-turn coil.",
      theory = "A multi-turn coil with N turns enclosing area A inside uniform magnetic field B has flux linkage λ = N · B · A · cos(θ).",
      initialSettingsSummary = "Magnetic Field: 0.5 T, Turns: 200, Coil Area: 0.02 m²",
      procedureSteps = listOf(
        "Set magnetic field B = 0.5 T and coil turns N = 200.",
        "Rotate coil angle θ from 0° to 180° in steps of 30°.",
        "Observe the separate readouts for single-turn flux Φ_B and total linkage λ.",
        "Note the maximum at 0°, zero at 90°, and negative maximum at 180°."
      ),
      predictionPrompt = "If a coil has 200 turns and the magnetic flux through one turn is 0.01 Weber, what is the total flux linkage λ?",
      predictionOptions = listOf(
        "0.01 Weber",
        "2.0 Weber-turns",
        "20.0 Weber-turns",
        "200 Weber-turns"
      ),
      correctPredictionIndex = 1,
      predictionExplanation = "Flux linkage λ = N · Φ = 200 turns × 0.01 Wb = 2.0 Wb-turns.",
      expectedRelationship = "λ = N · Φ_B = N · B · A · cos(θ)",
      reflectionQuestions = listOf(
        "Why is flux linkage rather than single-turn flux used in Faraday's law for multi-turn inductors?",
        "Why does rotating a coil at steady angular velocity generate an alternating sinusoidal voltage?"
      )
    ),

    GuidedExperiment(
      id = "exp_e",
      code = "EXPERIMENT E",
      title = "Faraday's Law and Magnet Speed",
      targetLabScreen = "INDUCTION",
      objective = "Demonstrate that induced electromotive force (EMF) is proportional to the rate of change of magnetic flux (dΦ/dt).",
      theory = "Faraday's law states that emf = -N (dΦ/dt). By moving a magnet faster, the time rate of change of flux linkage increases, yielding a higher induced voltage peak.",
      initialSettingsSummary = "Coil: 200 turns, Magnet: 5.0 A·m², Circuit R: 5.0 Ω",
      procedureSteps = listOf(
        "Set magnet approach speed to a slow velocity (e.g., 0.2 m/s). Observe the galvanometer peak deflection.",
        "Increase magnet approach speed to 0.8 m/s (4× faster).",
        "Compare the induced EMF oscilloscope waveforms.",
        "Verify that when the magnet stops moving inside the coil (v = 0), the induced voltage drops to 0."
      ),
      predictionPrompt = "If a strong magnet is held completely stationary directly in the center of the coil, what is the induced EMF?",
      predictionOptions = listOf(
        "Maximum positive voltage",
        "Maximum negative voltage",
        "Exactly 0 Volts",
        "Oscillates sinusoidally"
      ),
      correctPredictionIndex = 2,
      predictionExplanation = "Faraday's law depends on the TIME DERIVATIVE of flux (dΦ/dt). If stationary, flux is constant, so dΦ/dt = 0 and emf = 0.",
      expectedRelationship = "emf = -N · (dΦ/dt)",
      reflectionQuestions = listOf(
        "How does Lenz's law explain why you feel physical resistance when thrusting a magnet into a closed copper ring?",
        "What happens to the galvanometer needle when the magnet is pulled back out?"
      )
    ),

    GuidedExperiment(
      id = "exp_f",
      code = "EXPERIMENT F",
      title = "Transformer Voltage and Turns Ratio",
      targetLabScreen = "TRANSFORMERS",
      objective = "Verify the ideal transformer voltage ratio Vs / Vp = Ns / Np and power conservation.",
      theory = "In an ideal transformer with 100% magnetic coupling and negligible winding resistance, the voltage ratio equals the turns ratio: Vs / Vp = Ns / Np, and current transforms inversely: Is / Ip = Np / Ns.",
      initialSettingsSummary = "Primary: 500 turns, Secondary: 100 turns, Vp: 120 Vrms",
      procedureSteps = listOf(
        "Set primary turns Np = 500 and secondary turns Ns = 100 (step-down ratio 5:1).",
        "Verify secondary voltage Vs = 120 V / 5 = 24.0 Vrms.",
        "Change secondary turns to Ns = 1000 (step-up ratio 1:2) and verify Vs = 240.0 Vrms.",
        "Observe primary and secondary current to verify power balance P_in ≈ P_out."
      ),
      predictionPrompt = "With primary turns Np = 600 and secondary turns Ns = 200, if primary voltage is 120 V, what is secondary voltage?",
      predictionOptions = listOf(
        "360 V",
        "120 V",
        "40 V",
        "20 V"
      ),
      correctPredictionIndex = 2,
      predictionExplanation = "Vs = Vp × (Ns / Np) = 120 V × (200 / 600) = 120 / 3 = 40 V.",
      expectedRelationship = "Vs / Vp = Ns / Np",
      reflectionQuestions = listOf(
        "Why can't an ideal transformer operate on direct current (DC)?",
        "How does adding winding resistance and core losses alter the efficiency from 100%?"
      )
    ),

    GuidedExperiment(
      id = "exp_g",
      code = "EXPERIMENT G",
      title = "RL Circuit Transient and Time Constant",
      targetLabScreen = "INDUCTANCE_RL",
      objective = "Measure the RL circuit time constant τ = L / R and verify current growth i(t) = (V0 / R)(1 - e^(-t/τ)).",
      theory = "When DC voltage V0 is applied to an inductor in series with resistance R, the inductor opposes instantaneous change in current. Current reaches 63.2% of steady state at t = τ = L / R.",
      initialSettingsSummary = "V0: 12 V, L: 0.1 H (100 mH), R: 10 Ω -> τ = 0.010 s (10 ms)",
      procedureSteps = listOf(
        "Set supply voltage V0 = 12.0 V, R = 10.0 Ω, and L = 0.10 H.",
        "Calculate theoretical time constant τ = 0.10 / 10.0 = 0.010 s (10 ms).",
        "Observe the current rise curve and check the value at t = 10 ms (should be 0.632 × 1.2 A = 0.758 A).",
        "Double resistance to R = 20 Ω and verify that time constant cuts in half (τ = 5 ms)."
      ),
      predictionPrompt = "At exactly one time constant (t = τ), what percentage of final steady-state current is reached in an RL circuit?",
      predictionOptions = listOf(
        "50.0%",
        "63.2%",
        "70.7%",
        "99.3%"
      ),
      correctPredictionIndex = 1,
      predictionExplanation = "1 - e^(-1) = 1 - 0.3679 = 0.6321 or 63.2%.",
      expectedRelationship = "τ = L / R, i(t) = I_final (1 - e^(-t/τ))",
      reflectionQuestions = listOf(
        "Why is inductor voltage maximum at t = 0 and zero at steady state?",
        "Where is energy stored during the steady-state current condition?"
      )
    ),

    GuidedExperiment(
      id = "exp_h",
      code = "EXPERIMENT H",
      title = "Electromagnetic Wave Frequency and Wavelength",
      targetLabScreen = "EM_WAVES",
      objective = "Investigate the inverse relationship between frequency f and wavelength λ in a constant medium: v = f · λ.",
      theory = "In any homogeneous linear medium, the speed of electromagnetic waves is fixed by permittivity and permeability: v = 1 / √(µε). Therefore, doubling the frequency f forces the spatial wavelength λ to halve.",
      initialSettingsSummary = "Medium: Vacuum (v = c = 3.0×10⁸ m/s), Frequency: 1.0 GHz",
      procedureSteps = listOf(
        "Set frequency f = 1.0 GHz in Vacuum. Observe wavelength λ = 0.30 m (30 cm).",
        "Increase frequency to f = 2.0 GHz. Observe wavelength reduce to 0.15 m (15 cm).",
        "Switch medium to fresh water (ε_r = 80). Observe the wave speed drop to c / √80 ≈ 0.335×10⁸ m/s and wavelength shrink proportionally."
      ),
      predictionPrompt = "If propagation speed v remains constant, what happens to wavelength when frequency is doubled?",
      predictionOptions = listOf(
        "Wavelength doubles (2×)",
        "Wavelength cuts in half (0.5×)",
        "Wavelength quadruples (4×)",
        "Wavelength is unaffected"
      ),
      correctPredictionIndex = 1,
      predictionExplanation = "Since λ = v / f, when f doubles with v constant, λ is halved.",
      expectedRelationship = "v = f · λ, λ ∝ 1 / f",
      reflectionQuestions = listOf(
        "Why are electric and magnetic field vectors perpendicular to each other in a TEM plane wave?",
        "What is the physical meaning of the Poynting vector S = (1/µ)(E × B)?"
      )
    )
  )

  val FORMULA_ENTRIES = listOf(
    FormulaEntry(
      id = "form_coulomb",
      category = "Electric Fields",
      name = "Coulomb's Law (Point Charge)",
      expressionLatex = "E = \\frac{1}{4\\pi\\varepsilon} \\frac{q}{r^2} \\hat{r}",
      symbolsDefinition = "E: Electric Field vector, q: Point charge, r: Radial distance, ε: Medium permittivity, r̂: Unit vector",
      siUnits = "E in V/m or N/C, q in C, r in m, ε in F/m",
      assumptions = "Point charge or spherical symmetry, static charge, isotropic medium",
      physicalInterpretation = "Inverse-square electrostatic field radiating radially away from positive charges and towards negative charges.",
      exampleCalculation = "q = 2 µC, r = 0.1 m in vacuum -> E = (8.988×10⁹)(2×10⁻⁶)/(0.01) = 1.798×10⁶ V/m",
      targetLabScreen = "ELECTRIC_FIELDS"
    ),
    FormulaEntry(
      id = "form_flux_e",
      category = "Gauss's Law",
      name = "Electric Flux (Uniform Field)",
      expressionLatex = "\\Phi_E = E \\cdot A \\cos(\\theta)",
      symbolsDefinition = "Φ_E: Electric flux, E: Field magnitude, A: Surface area, θ: Angle between field and surface normal",
      siUnits = "Φ_E in V·m or N·m²/C, E in V/m, A in m²",
      assumptions = "Uniform electric field across a planar flat surface",
      physicalInterpretation = "Measures the net number of electric field lines penetrating through a surface area.",
      exampleCalculation = "E = 500 V/m, A = 0.04 m², θ = 60° -> Φ_E = 500 × 0.04 × cos(60°) = 10.0 V·m",
      targetLabScreen = "ELECTRIC_FLUX"
    ),
    FormulaEntry(
      id = "form_gauss_law",
      category = "Gauss's Law",
      name = "Gauss's Law (Enclosed Charge)",
      expressionLatex = "\\oint_S D \\cdot dA = Q_{enclosed}",
      symbolsDefinition = "D: Electric flux density (D = ε E), dA: Differential surface area vector, Q_enclosed: Net charge inside closed surface",
      siUnits = "D in C/m², Q in C",
      assumptions = "Any closed surface S enclosing net charge Q",
      physicalInterpretation = "The total outbound electric flux through any closed Gaussian envelope is determined strictly by the net charge trapped inside.",
      exampleCalculation = "Q_enc = 10 nC in vacuum -> Φ_E = (10×10⁻⁹) / (8.854×10⁻¹²) = 1129.4 V·m",
      targetLabScreen = "ELECTRIC_FLUX"
    ),
    FormulaEntry(
      id = "form_ampere_wire",
      category = "Magnetic Fields",
      name = "Magnetic Field of Straight Wire",
      expressionLatex = "H = \\frac{I}{2\\pi r}, \\quad B = \\mu H",
      symbolsDefinition = "H: Magnetic field intensity, B: Flux density, I: Conductor current, r: Radial distance, µ: Permeability",
      siUnits = "H in A/m, B in Tesla (T), I in A, r in m, µ in H/m",
      assumptions = "Infinitely long, straight, slender cylindrical conductor",
      physicalInterpretation = "Concentric circular magnetic field loops enclosing the current path with strength inversely proportional to radius.",
      exampleCalculation = "I = 5 A, r = 0.02 m in air -> H = 5 / (2π × 0.02) = 39.79 A/m; B = µ0 × H = 5.0×10⁻⁵ T (50 µT)",
      targetLabScreen = "MAGNETIC_FIELDS"
    ),
    FormulaEntry(
      id = "form_solenoid",
      category = "Magnetic Fields",
      name = "Ideal Solenoid Internal Field",
      expressionLatex = "B = \\mu n I = \\mu \\frac{N}{L} I",
      symbolsDefinition = "B: Internal flux density, n: Turns per unit length (N/L), I: Current, µ: Core permeability",
      siUnits = "B in Tesla (T), N dimensionless, L in m, I in A",
      assumptions = "Long solenoid (L >> R), tightly wound turns, uniform axial field inside",
      physicalInterpretation = "Nearly perfectly uniform axial magnetic field generated inside a cylindrical helix.",
      exampleCalculation = "N = 1000 turns, L = 0.5 m (n = 2000 m⁻¹), I = 2 A, µr = 1 -> B = (4π×10⁻⁷)(2000)(2) = 5.027 mT",
      targetLabScreen = "MAGNETIC_FIELDS"
    ),
    FormulaEntry(
      id = "form_mag_flux",
      category = "Magnetic Flux",
      name = "Magnetic Flux Linkage",
      expressionLatex = "\\lambda = N \\Phi_B = N B A \\cos(\\theta)",
      symbolsDefinition = "λ: Flux linkage, N: Number of coil turns, Φ_B: Single-turn magnetic flux, B: Flux density, A: Area, θ: Normal angle",
      siUnits = "λ in Wb-turns, Φ_B in Webers (Wb), B in Tesla (T), A in m²",
      assumptions = "Uniform magnetic field B across planar coil surface",
      physicalInterpretation = "Total magnetic flux linked through all consecutive turns of an electrical winding.",
      exampleCalculation = "N = 250, B = 0.4 T, A = 0.01 m², θ = 0° -> λ = 250 × 0.4 × 0.01 × 1 = 1.00 Wb-turns",
      targetLabScreen = "MAGNETIC_FLUX"
    ),
    FormulaEntry(
      id = "form_faraday",
      category = "Induction",
      name = "Faraday's Law of Induction",
      expressionLatex = "\\mathrm{emf} = -\\frac{d\\lambda}{dt} = -N \\frac{d\\Phi_B}{dt}",
      symbolsDefinition = "emf: Induced Electromotive Force, λ: Flux linkage, t: Time, N: Coil turns",
      siUnits = "emf in Volts (V), λ in Wb-turns, t in s",
      assumptions = "Closed conducting circuit or open-circuit terminal voltage",
      physicalInterpretation = "A changing magnetic flux induces an electric potential that drives current opposing the flux change (Lenz's law).",
      exampleCalculation = "N = 100 turns, dΦ/dt = 0.05 Wb/s -> emf = -100 × 0.05 = -5.0 Volts",
      targetLabScreen = "INDUCTION"
    ),
    FormulaEntry(
      id = "form_rl_transient",
      category = "Circuits",
      name = "Series RL Circuit Transient",
      expressionLatex = "i(t) = \\frac{V_0}{R} \\left(1 - e^{-t/\\tau}\\right), \\quad \\tau = \\frac{L}{R}",
      symbolsDefinition = "i(t): Instantaneous current, V_0: Supply voltage, R: Resistance, L: Inductance, τ: Time constant",
      siUnits = "i in A, V in V, R in Ω, L in H, τ in s",
      assumptions = "Linear lumped elements, zero initial current at t = 0",
      physicalInterpretation = "Inductor opposes instantaneous current change by generating counter-EMF, resulting in exponential rise.",
      exampleCalculation = "V0 = 24 V, R = 12 Ω, L = 0.24 H -> τ = 0.24/12 = 0.02 s (20 ms); I_final = 24/12 = 2.0 A",
      targetLabScreen = "INDUCTANCE_RL"
    ),
    FormulaEntry(
      id = "form_transformer",
      category = "Transformers",
      name = "Ideal Transformer Turns Ratio",
      expressionLatex = "\\frac{V_s}{V_p} = \\frac{N_s}{N_p} = \\frac{I_p}{I_s}",
      symbolsDefinition = "Vp, Vs: Primary & secondary voltages, Np, Ns: Winding turns, Ip, Is: Winding currents",
      siUnits = "V in Volts, I in Amperes, N dimensionless",
      assumptions = "Unity magnetic coupling (k = 1), zero winding resistance, infinite core permeability",
      physicalInterpretation = "AC voltage is stepped up or down in proportion to winding turn ratio while conserving electrical power.",
      exampleCalculation = "Np = 800, Ns = 200, Vp = 240 V -> Vs = 240 × (200/800) = 60 V",
      targetLabScreen = "TRANSFORMERS"
    ),
    FormulaEntry(
      id = "form_wave_speed",
      category = "Electromagnetic Waves",
      name = "EM Wave Speed and Dispersion",
      expressionLatex = "v = \\frac{1}{\\sqrt{\\mu \\varepsilon}} = f \\lambda, \\quad c = \\frac{1}{\\sqrt{\\mu_0 \\varepsilon_0}}",
      symbolsDefinition = "v: Wave speed, f: Frequency, λ: Wavelength, µ: Permeability, ε: Permittivity, c: Speed of light in vacuum",
      siUnits = "v, c in m/s, f in Hz, λ in m",
      assumptions = "Homogeneous, linear, isotropic, lossless medium",
      physicalInterpretation = "Electromagnetic plane waves travel at a characteristic velocity set by the dielectric and magnetic properties of space.",
      exampleCalculation = "In vacuum: c = 2.998×10⁸ m/s. At f = 100 MHz -> λ = (3×10⁸)/(100×10⁶) = 3.0 meters",
      targetLabScreen = "EM_WAVES"
    )
  )

  val QUIZ_QUESTIONS = listOf(
    QuizQuestion(
      id = "q1",
      category = "Electric Fields",
      question = "If the radial distance from a point charge is tripled (3×), what is the factor by which the electric field magnitude changes?",
      options = listOf("Reduces to 1/3", "Reduces to 1/9", "Reduces to 1/27", "Triples (3×)"),
      correctOptionIndex = 1,
      explanation = "By Coulomb's law, E ∝ 1/r². If distance is multiplied by 3, field is multiplied by 1/(3²) = 1/9.",
      formulaHint = "E = k |q| / r²"
    ),
    QuizQuestion(
      id = "q2",
      category = "Gauss's Law",
      question = "A closed cubical Gaussian surface of edge length 0.2 m encloses a net charge of +17.7 pC in vacuum. What is the total outbound electric flux through all six faces?",
      options = listOf("0.0 V·m", "1.0 V·m", "2.0 V·m", "17.7 V·m"),
      correctOptionIndex = 2,
      explanation = "By Gauss's law, Φ_E = Q_enc / ε_0 = (17.7 × 10⁻¹² C) / (8.854 × 10⁻¹² F/m) ≈ 2.0 V·m.",
      formulaHint = "Φ_E = Q_enclosed / ε_0"
    ),
    QuizQuestion(
      id = "q3",
      category = "Magnetic Fields",
      question = "What is the primary physical difference between magnetic flux density B and magnetic field intensity H?",
      options = listOf(
        "B is a scalar while H is a vector",
        "B includes the magnetic response of the material (B = µ H), whereas H depends solely on external currents",
        "H is measured in Tesla while B is measured in A/m",
        "B is only present in vacuum"
      ),
      correctOptionIndex = 1,
      explanation = "H represents the magnetizing field set up by free currents, while B accounts for the magnetization of the medium via permeability: B = µ_r µ_0 H.",
      formulaHint = "B = µ H = µ_r µ_0 (H + M)"
    ),
    QuizQuestion(
      id = "q4",
      category = "Electromagnetic Induction",
      question = "According to Lenz's law, why is there a negative sign in Faraday's law: emf = -dλ/dt?",
      options = listOf(
        "Because voltage is always negative",
        "To enforce conservation of energy by inducing current that creates a magnetic field opposing the flux change",
        "Because electrons have positive charge",
        "Due to mathematical convention only"
      ),
      correctOptionIndex = 1,
      explanation = "If the induced EMF aided the flux change instead of opposing it, perpetual energy creation would occur, violating the First Law of Thermodynamics.",
      formulaHint = "emf = -N (dΦ/dt)"
    ),
    QuizQuestion(
      id = "q5",
      category = "RL Circuits",
      question = "In an RL circuit with R = 20 Ω and L = 0.1 H, what is the time constant τ?",
      options = listOf("2.0 seconds", "0.5 seconds", "0.005 seconds (5 ms)", "0.02 seconds (20 ms)"),
      correctOptionIndex = 2,
      explanation = "τ = L / R = 0.1 H / 20 Ω = 0.005 seconds = 5 ms.",
      formulaHint = "τ = L / R"
    ),
    QuizQuestion(
      id = "q6",
      category = "Maxwell's Equations",
      question = "Which term did James Clerk Maxwell add to Ampère's law to make it consistent with charge conservation and predict EM waves?",
      options = listOf(
        "Lorentz force term",
        "Displacement current density ∂D/∂t",
        "Magnetic monopole density",
        "Ohmic dissipation term"
      ),
      correctOptionIndex = 1,
      explanation = "Maxwell recognized that time-varying electric flux acts like a current (displacement current ∂D/∂t), completing the symmetry with Faraday's law.",
      formulaHint = "∇ × H = J + ∂D/∂t"
    ),
    QuizQuestion(
      id = "q7",
      category = "Transformers",
      question = "An ideal step-down transformer has Np = 1000 turns and Ns = 200 turns. If the secondary is connected to a 10 Ω load and Vp = 120 V, what is secondary current?",
      options = listOf("2.4 A", "12.0 A", "0.48 A", "24.0 A"),
      correctOptionIndex = 0,
      explanation = "Vs = 120 V × (200 / 1000) = 24 V. Then Is = Vs / R = 24 V / 10 Ω = 2.4 A.",
      formulaHint = "Vs = Vp (Ns / Np), Is = Vs / R_load"
    ),
    QuizQuestion(
      id = "q8",
      category = "Electromagnetic Waves",
      question = "In a uniform plane wave propagating in free space, what is the spatial orientation of E, B, and the propagation direction k?",
      options = listOf(
        "All three vectors are parallel",
        "E and B are parallel, k is perpendicular",
        "E, B, and k are mutually perpendicular (orthogonal) to each other",
        "E and B oscillate randomly"
      ),
      correctOptionIndex = 2,
      explanation = "TEM (transverse electromagnetic) waves require E ⊥ B, and both are transverse (perpendicular) to the propagation direction k (E × B || k).",
      formulaHint = "S = (1/µ) (E × B)"
    )
  )

  val DEFAULT_LAB_RECORDS = listOf(
    LabRecord(
      experimentTitle = "Inverse-Square Law Benchmark",
      parameterSettings = mapOf("Charge" to "+5.0 µC", "r1" to "0.10 m", "r2" to "0.20 m", "Medium" to "Vacuum"),
      measuredValues = mapOf("E1" to "4.494×10⁶ V/m", "E2" to "1.123×10⁶ V/m", "Ratio E2/E1" to "0.2499 (1/4)"),
      notes = "Verified inverse-square behavior within numerical precision. Singularity cutoff protects origin."
    ),
    LabRecord(
      experimentTitle = "Transformer Turns Ratio Verification",
      parameterSettings = mapOf("Np" to "500 turns", "Ns" to "100 turns", "Vp" to "120.0 Vrms", "Load" to "20.0 Ω"),
      measuredValues = mapOf("Vs" to "24.00 Vrms", "Is" to "1.200 Arms", "Ip" to "0.240 Arms", "P_out" to "28.8 W"),
      notes = "Ideal transformer power balance confirmed: Pin = Pout = 28.8 W."
    )
  )
}
