package com.example.model

data class GuidedExperiment(
  val id: String,
  val code: String, // "EXPERIMENT A", "EXPERIMENT B", etc.
  val title: String,
  val targetLabScreen: String,
  val objective: String,
  val theory: String,
  val initialSettingsSummary: String,
  val procedureSteps: List<String>,
  val predictionPrompt: String,
  val predictionOptions: List<String>,
  val correctPredictionIndex: Int,
  val predictionExplanation: String,
  val expectedRelationship: String,
  val reflectionQuestions: List<String>
)

data class FormulaEntry(
  val id: String,
  val category: String, // "Electric Fields", "Gauss's Law", "Magnetic Fields", "Induction", "Circuits", "Waves"
  val name: String,
  val expressionLatex: String,
  val symbolsDefinition: String,
  val siUnits: String,
  val assumptions: String,
  val physicalInterpretation: String,
  val exampleCalculation: String,
  val targetLabScreen: String
)

data class QuizQuestion(
  val id: String,
  val category: String,
  val question: String,
  val options: List<String>,
  val correctOptionIndex: Int,
  val explanation: String,
  val formulaHint: String
)

data class LabRecord(
  val id: String = java.util.UUID.randomUUID().toString(),
  val experimentTitle: String,
  val timestampEpochMs: Long = System.currentTimeMillis(),
  val parameterSettings: Map<String, String>,
  val measuredValues: Map<String, String>,
  val notes: String = ""
)
