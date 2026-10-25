package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CanvasDarkBg
import com.example.ui.theme.CanvasGridLine
import kotlin.math.abs
import kotlin.math.max

data class GraphSeries(
  val name: String,
  val points: List<Pair<Double, Double>>, // (time, value)
  val color: Color,
  val unit: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaveformGraph(
  seriesList: List<GraphSeries>,
  title: String,
  timeUnit: String = "s",
  modifier: Modifier = Modifier,
  yMinOverride: Double? = null,
  yMaxOverride: Double? = null
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(CanvasDarkBg, RoundedCornerShape(12.dp))
      .border(1.dp, CanvasGridLine, RoundedCornerShape(12.dp))
      .padding(12.dp)
      .testTag("waveform_graph_${title.replace("\\s+".toRegex(), "_")}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 13.sp
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        seriesList.forEach { series ->
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(series.color, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${series.name} (${series.unit})",
              fontSize = 10.sp,
              color = series.color,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .background(Color(0xFF070B14), RoundedCornerShape(8.dp))
        .padding(8.dp)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Find global bounds
        var minX = 0.0
        var maxX = 1.0
        var minY = yMinOverride ?: 0.0
        var maxY = yMaxOverride ?: 1.0

        if (yMinOverride == null || yMaxOverride == null) {
          var foundAny = false
          for (s in seriesList) {
            for (p in s.points) {
              if (!foundAny) {
                minX = p.first
                maxX = p.first
                if (yMinOverride == null) minY = p.second
                if (yMaxOverride == null) maxY = p.second
                foundAny = true
              } else {
                if (p.first > maxX) maxX = p.first
                if (p.first < minX) minX = p.first
                if (yMinOverride == null && p.second < minY) minY = p.second
                if (yMaxOverride == null && p.second > maxY) maxY = p.second
              }
            }
          }
        }

        if (abs(maxX - minX) < 1e-9) maxX = minX + 1.0
        if (abs(maxY - minY) < 1e-9) {
          maxY = minY + 1.0
          minY -= 1.0
        }

        val rangeX = maxX - minX
        val rangeY = maxY - minY

        // Draw horizontal grid lines
        val gridCountY = 4
        for (i in 0..gridCountY) {
          val y = height * (i.toFloat() / gridCountY.toFloat())
          drawLine(
            color = CanvasGridLine.copy(alpha = 0.6f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
          )
        }

        // Draw zero line if visible
        if (minY <= 0.0 && maxY >= 0.0) {
          val zeroY = height - ((0.0 - minY) / rangeY * height).toFloat()
          drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(0f, zeroY),
            end = Offset(width, zeroY),
            strokeWidth = 1.5f
          )
        }

        // Draw series paths
        for (series in seriesList) {
          if (series.points.size < 2) continue
          val path = Path()
          var first = true

          for (p in series.points) {
            val px = ((p.first - minX) / rangeX * width).toFloat().coerceIn(0f, width)
            val py = (height - ((p.second - minY) / rangeY * height)).toFloat().coerceIn(0f, height)
            if (first) {
              path.moveTo(px, py)
              first = false
            } else {
              path.lineTo(px, py)
            }
          }

          drawPath(
            path = path,
            color = series.color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
          )
        }
      }
    }
  }
}
