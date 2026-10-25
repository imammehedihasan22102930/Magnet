package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.model.EducationalContentRepository
import com.example.model.LabRecord
import com.example.ui.screens.ElectricFieldScreen
import com.example.ui.screens.ElectricFluxScreen
import com.example.ui.screens.ElectricalMachinesScreen
import com.example.ui.screens.ElectromagneticWavesScreen
import com.example.ui.screens.FormulaReferenceScreen
import com.example.ui.screens.GuidedExperimentsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InductanceRLScreen
import com.example.ui.screens.InductionScreen
import com.example.ui.screens.LabNotebookScreen
import com.example.ui.screens.MagneticCircuitScreen
import com.example.ui.screens.MagneticFieldScreen
import com.example.ui.screens.MagneticFluxScreen
import com.example.ui.screens.MaxwellsEquationsScreen
import com.example.ui.screens.QuizAssessmentScreen
import com.example.ui.screens.TransformerScreen
import com.example.ui.screens.UnitConverterScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        VirtualLabApp()
      }
    }
  }
}

@Composable
fun VirtualLabApp() {
  var currentDestination by remember { mutableStateOf("HOME") }
  val savedLabRecords = remember {
    mutableStateListOf<LabRecord>().apply {
      addAll(EducationalContentRepository.DEFAULT_LAB_RECORDS)
    }
  }

  fun navigateTo(dest: String) {
    currentDestination = dest
  }

  fun navigateBack() {
    currentDestination = "HOME"
  }

  fun saveRecord(title: String, params: Map<String, String>, measurements: Map<String, String>) {
    savedLabRecords.add(
      0,
      LabRecord(
        experimentTitle = title,
        parameterSettings = params,
        measuredValues = measurements,
        notes = "Recorded live from simulation session."
      )
    )
  }

  when (currentDestination) {
    "HOME" -> {
      HomeScreen(
        onNavigateTo = { dest -> navigateTo(dest) },
        savedRecordsCount = savedLabRecords.size
      )
    }

    "ELECTRIC_FIELDS" -> {
      BackHandler { navigateBack() }
      ElectricFieldScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("Electric Fields Lab", params, measurements)
        }
      )
    }

    "ELECTRIC_FLUX" -> {
      BackHandler { navigateBack() }
      ElectricFluxScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("Electric Flux & Gauss's Law", params, measurements)
        }
      )
    }

    "MAGNETIC_FIELDS" -> {
      BackHandler { navigateBack() }
      MagneticFieldScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("Magnetic Field & Biot-Savart", params, measurements)
        }
      )
    }

    "MAGNETIC_FLUX" -> {
      BackHandler { navigateBack() }
      MagneticFluxScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("Magnetic Flux & Linkage", params, measurements)
        }
      )
    }

    "MAGNETIC_CIRCUITS" -> {
      BackHandler { navigateBack() }
      MagneticCircuitScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("Magnetic Materials & Circuits", params, measurements)
        }
      )
    }

    "INDUCTION" -> {
      BackHandler { navigateBack() }
      InductionScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("Faraday Electromagnetic Induction", params, measurements)
        }
      )
    }

    "INDUCTANCE_RL" -> {
      BackHandler { navigateBack() }
      InductanceRLScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("Inductance & RL Circuit Energy", params, measurements)
        }
      )
    }

    "TRANSFORMERS" -> {
      BackHandler { navigateBack() }
      TransformerScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("Transformer Virtual Lab", params, measurements)
        }
      )
    }

    "MAXWELLS_EQUATIONS" -> {
      BackHandler { navigateBack() }
      MaxwellsEquationsScreen(
        onNavigateBack = { navigateBack() }
      )
    }

    "EM_WAVES" -> {
      BackHandler { navigateBack() }
      ElectromagneticWavesScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("EM Waves & Dispersion", params, measurements)
        }
      )
    }

    "ELECTRICAL_MACHINES" -> {
      BackHandler { navigateBack() }
      ElectricalMachinesScreen(
        onNavigateBack = { navigateBack() },
        onSaveMeasurement = { params, measurements ->
          saveRecord("Electrical Machines Application", params, measurements)
        }
      )
    }

    "GUIDED_EXPERIMENTS" -> {
      BackHandler { navigateBack() }
      GuidedExperimentsScreen(
        onNavigateBack = { navigateBack() },
        onLaunchLab = { targetLab -> navigateTo(targetLab) }
      )
    }

    "FORMULA_REFERENCE" -> {
      BackHandler { navigateBack() }
      FormulaReferenceScreen(
        onNavigateBack = { navigateBack() },
        onNavigateToLab = { targetLab -> navigateTo(targetLab) }
      )
    }

    "QUIZZES" -> {
      BackHandler { navigateBack() }
      QuizAssessmentScreen(
        onNavigateBack = { navigateBack() }
      )
    }

    "LAB_NOTEBOOK" -> {
      BackHandler { navigateBack() }
      LabNotebookScreen(
        records = savedLabRecords,
        onDeleteRecord = { id -> savedLabRecords.removeAll { it.id == id } },
        onClearAll = { savedLabRecords.clear() },
        onNavigateBack = { navigateBack() }
      )
    }

    "UNIT_CONVERTER" -> {
      BackHandler { navigateBack() }
      UnitConverterScreen(
        onNavigateBack = { navigateBack() }
      )
    }

    else -> {
      HomeScreen(
        onNavigateTo = { dest -> navigateTo(dest) },
        savedRecordsCount = savedLabRecords.size
      )
    }
  }
}
