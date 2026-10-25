package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InductionGreen
import com.example.ui.theme.MagneticAmber
import com.example.ui.theme.MagneticOrange

data class LabModuleItem(
  val id: String,
  val number: Int,
  val title: String,
  val subtitle: String,
  val category: String,
  val icon: ImageVector,
  val accentColor: Color,
  val destination: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  onNavigateTo: (String) -> Unit,
  savedRecordsCount: Int,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val allModules = remember {
    listOf(
      LabModuleItem(
        id = "mod_1",
        number = 1,
        title = "Electric Fields & Potential",
        subtitle = "Coulomb's law, superposition, 2D charge vectors & equipotentials",
        category = "Electrostatics",
        icon = Icons.Default.ElectricBolt,
        accentColor = ElectricCyan,
        destination = "ELECTRIC_FIELDS"
      ),
      LabModuleItem(
        id = "mod_2",
        number = 2,
        title = "Electric Flux & Gauss's Law",
        subtitle = "Surface flux Φ_E = E·A cos(θ), D = εE, symmetric Gaussian boundaries",
        category = "Electrostatics",
        icon = Icons.Default.Science,
        accentColor = ElectricBlue,
        destination = "ELECTRIC_FLUX"
      ),
      LabModuleItem(
        id = "mod_3",
        number = 3,
        title = "Magnetic Fields & Biot-Savart",
        subtitle = "Straight wire, parallel conductors, loops, finite solenoid & dipole",
        category = "Magnetostatics",
        icon = Icons.Default.Speed,
        accentColor = MagneticAmber,
        destination = "MAGNETIC_FIELDS"
      ),
      LabModuleItem(
        id = "mod_4",
        number = 4,
        title = "Magnetic Flux & Linkage",
        subtitle = "Rotating coil in B-field, Φ_B = B·A cos(θ), flux linkage λ = N Φ_B",
        category = "Magnetostatics",
        icon = Icons.Default.Waves,
        accentColor = MagneticOrange,
        destination = "MAGNETIC_FLUX"
      ),
      LabModuleItem(
        id = "mod_5",
        number = 5,
        title = "Magnetic Materials & Circuits",
        subtitle = "Reluctance, air gaps, core permeabilities, B-H saturation & hysteresis",
        category = "Magnetic Media",
        icon = Icons.Default.Science,
        accentColor = MagneticAmber,
        destination = "MAGNETIC_CIRCUITS"
      ),
      LabModuleItem(
        id = "mod_6",
        number = 6,
        title = "Electromagnetic Induction",
        subtitle = "Faraday's law, Lenz's law, moving magnet, galvanometer & waveforms",
        category = "Electrodynamics",
        icon = Icons.Default.ElectricBolt,
        accentColor = InductionGreen,
        destination = "INDUCTION"
      ),
      LabModuleItem(
        id = "mod_7",
        number = 7,
        title = "Inductance & RL Circuit Energy",
        subtitle = "Series RL step transient, time constant τ = L/R, magnetic energy W_L",
        category = "Electrodynamics",
        icon = Icons.Default.Speed,
        accentColor = InductionGreen,
        destination = "INDUCTANCE_RL"
      ),
      LabModuleItem(
        id = "mod_8",
        number = 8,
        title = "Transformer Virtual Lab",
        subtitle = "Turns ratio Vs/Vp = Ns/Np, core losses, leakage, regulation & efficiency",
        category = "Power Engineering",
        icon = Icons.Default.ElectricBolt,
        accentColor = ElectricCyan,
        destination = "TRANSFORMERS"
      ),
      LabModuleItem(
        id = "mod_9",
        number = 9,
        title = "Maxwell's Equations Suite",
        subtitle = "All 4 fundamental equations in integral & differential forms with live demos",
        category = "Field Theory",
        icon = Icons.Default.AutoAwesome,
        accentColor = Color(0xFFC084FC),
        destination = "MAXWELLS_EQUATIONS"
      ),
      LabModuleItem(
        id = "mod_10",
        number = 10,
        title = "Electromagnetic Wave Propagation",
        subtitle = "Orthogonal E & B vectors, dispersion v = f λ, Poynting vector & media",
        category = "Wave Optics",
        icon = Icons.Default.Waves,
        accentColor = ElectricCyan,
        destination = "EM_WAVES"
      ),
      LabModuleItem(
        id = "mod_11",
        number = 11,
        title = "Electrical Machines & Applications",
        subtitle = "DC motors, AC generators, eddy current braking & wireless power transfer",
        category = "Applied Engineering",
        icon = Icons.Default.Speed,
        accentColor = MagneticOrange,
        destination = "ELECTRICAL_MACHINES"
      ),
      LabModuleItem(
        id = "mod_12",
        number = 12,
        title = "Guided Experiments & Challenges",
        subtitle = "Interactive lab procedures from Experiment A through H with hypothesis testing",
        category = "Practical Labs",
        icon = Icons.Default.Science,
        accentColor = InductionGreen,
        destination = "GUIDED_EXPERIMENTS"
      ),
      LabModuleItem(
        id = "mod_13",
        number = 13,
        title = "Electromagnetism Formula Reference",
        subtitle = "Searchable formula library with SI units, derivations & quick calculators",
        category = "Reference",
        icon = Icons.Default.MenuBook,
        accentColor = ElectricBlue,
        destination = "FORMULA_REFERENCE"
      ),
      LabModuleItem(
        id = "mod_14",
        number = 14,
        title = "Conceptual & Numerical Quizzes",
        subtitle = "Test your mastery of field physics, Faraday induction, and Maxwell laws",
        category = "Assessment",
        icon = Icons.Default.AutoAwesome,
        accentColor = Color(0xFFF472B6),
        destination = "QUIZZES"
      ),
      LabModuleItem(
        id = "mod_15",
        number = 15,
        title = "Lab Notebook & Saved Reports",
        subtitle = "Export simulation observations and measurement tables to CSV",
        category = "Data Logging",
        icon = Icons.Default.MenuBook,
        accentColor = ElectricCyan,
        destination = "LAB_NOTEBOOK"
      ),
      LabModuleItem(
        id = "mod_16",
        number = 16,
        title = "Units & Dimensional Analysis",
        subtitle = "SI electromagnetism unit converter and dimensional consistency validator",
        category = "Tools",
        icon = Icons.Default.Calculate,
        accentColor = MagneticAmber,
        destination = "UNIT_CONVERTER"
      )
    )
  }

  val filteredModules = remember(searchQuery) {
    if (searchQuery.isBlank()) {
      allModules
    } else {
      allModules.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
          it.subtitle.contains(searchQuery, ignoreCase = true) ||
          it.category.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "EM Virtual Laboratory",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Electric & Magnetic Field Simulator",
              style = MaterialTheme.typography.bodySmall,
              color = ElectricCyan,
              fontSize = 12.sp
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
      )
    },
    containerColor = DarkBackground,
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("home_modules_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Hero Banner
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "PHYSICAL SIMULATOR SUITE",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
              Box(
                modifier = Modifier
                  .background(InductionGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "Offline-Ready",
                  color = InductionGreen,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Explore 16 Interactive Virtual Labs",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Coulomb superposition, Gauss's law, Biot-Savart, Faraday induction, RL transients, transformers, and Maxwell electromagnetic waves.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              Text(
                text = "📊 16 Modules",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "🔬 8 Guided Experiments",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "📝 $savedRecordsCount Saved",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
              )
            }
          }
        }
      }

      // Search Bar
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by law, formula, or module name...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search Labs", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("home_search_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElectricCyan,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface
          )
        )
      }

      // Section Header
      item {
        Text(
          text = if (searchQuery.isBlank()) "All Laboratory Modules" else "Search Results (${filteredModules.size})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Lab Items
      items(filteredModules, key = { it.id }) { module ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateTo(module.destination) }
            .testTag("module_card_${module.number}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .background(module.accentColor.copy(alpha = 0.15f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = module.icon,
                contentDescription = module.title,
                tint = module.accentColor,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "LAB ${module.number} • ${module.category.uppercase()}",
                  style = MaterialTheme.typography.labelSmall,
                  color = module.accentColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = module.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = module.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 2
              )
            }
          }
        }
      }
    }
  }
}
