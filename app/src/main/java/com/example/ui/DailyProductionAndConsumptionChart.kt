package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductionRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class DailyTrendDataPoint(
  val dayLabel: String,
  val fullDate: String,
  val outputSqMeters: Float,
  val targetSqMeters: Float,
  val inkConsumptionLiters: Float,
  val mediaConsumptionSqMeters: Float,
  val machineName: String = "ColorJet Vulcan 6090 UV"
)

/**
 * High-fidelity, Recharts-inspired Interactive Chart Component
 * Visualizes Daily Production Output (Bar / Area) and Material Consumption Trends (Spline / Line)
 */
@Composable
fun DailyProductionAndConsumptionChartWidget(
  productionRecords: List<ProductionRecord> = emptyList(),
  modifier: Modifier = Modifier,
  onNavigateToProduction: () -> Unit = {}
) {
  var selectedMetricTab by remember { mutableStateOf("All Trends") } // "All Trends", "Output vs Target", "Material Usage"
  var selectedIndex by remember { mutableStateOf<Int?>(null) }
  var isAnimated by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    isAnimated = true
  }

  val animProgress by animateFloatAsState(
    targetValue = if (isAnimated) 1f else 0f,
    animationSpec = tween(durationMillis = 900),
    label = "chartAnim"
  )

  // Generate or map data points for the past 7 days
  val trendPoints = remember(productionRecords) {
    if (productionRecords.isNotEmpty()) {
      val sorted = productionRecords.sortedBy { it.date }.takeLast(7)
      val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      val sdfOut = SimpleDateFormat("EEE", Locale.getDefault())

      sorted.mapIndexed { idx, record ->
        val dateObj = try { sdfIn.parse(record.date) } catch (e: Exception) { null }
        val label = if (dateObj != null) sdfOut.format(dateObj) else "D${idx + 1}"
        // Calculated proportional consumption: ~0.012L ink per m2 output, ~1.05m2 media per m2 printed
        val estInk = (record.output * 0.0125f).coerceAtLeast(1.5f)
        val estMedia = record.output * 1.05f

        DailyTrendDataPoint(
          dayLabel = label,
          fullDate = record.date,
          outputSqMeters = record.output,
          targetSqMeters = record.target.coerceAtLeast(50f),
          inkConsumptionLiters = estInk,
          mediaConsumptionSqMeters = estMedia,
          machineName = record.machineName.ifBlank { "ColorJet Wide UV" }
        )
      }
    } else {
      // High-precision standard sample past 7 operational days
      listOf(
        DailyTrendDataPoint("Mon", "2026-08-18", 340f, 320f, 4.25f, 357f, "Vulcan 6090 UV"),
        DailyTrendDataPoint("Tue", "2026-08-19", 420f, 350f, 5.30f, 441f, "Verona 3200 Hybrid"),
        DailyTrendDataPoint("Wed", "2026-08-20", 310f, 350f, 3.85f, 325f, "AuraJet Dye-Sub"),
        DailyTrendDataPoint("Thu", "2026-08-21", 490f, 400f, 6.10f, 514f, "Vulcan Prime UV"),
        DailyTrendDataPoint("Fri", "2026-08-22", 530f, 450f, 6.65f, 556f, "Vulcan Prime UV"),
        DailyTrendDataPoint("Sat", "2026-08-23", 280f, 300f, 3.50f, 294f, "Verona 3200 Hybrid"),
        DailyTrendDataPoint("Sun", "2026-08-24", 460f, 400f, 5.75f, 483f, "Vulcan 6090 UV")
      )
    }
  }

  val totalOutput = trendPoints.sumOf { it.outputSqMeters.toDouble() }
  val totalTarget = trendPoints.sumOf { it.targetSqMeters.toDouble() }
  val totalInk = trendPoints.sumOf { it.inkConsumptionLiters.toDouble() }
  val avgEfficiency = if (totalTarget > 0.0) (totalOutput / totalTarget) * 100.0 else 0.0

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .shadow(4.dp, RoundedCornerShape(24.dp))
      .testTag("production_recharts_widget"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      // Header with badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(PrimaryBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Analytics,
              contentDescription = "Production Trends",
              tint = PrimaryBlue,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "PRODUCTION & MATERIAL TRENDS",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp,
              color = TextPrimary
            )
            Text(
              text = "Daily output (m²) vs. Ink & Media usage",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        IconButton(
          onClick = onNavigateToProduction,
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(PrimaryBlue.copy(alpha = 0.08f))
        ) {
          Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open Dashboard",
            tint = PrimaryBlue,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Tab selector (Recharts style filter pill)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("All Trends", "Output vs Target", "Material Usage").forEach { tab ->
          val isSelected = selectedMetricTab == tab
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(50))
              .background(if (isSelected) PrimaryBlue else Color(0xFFF1F5F9))
              .clickable { selectedMetricTab = tab }
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = tab,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Summary mini KPI row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFFF8FAFC))
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("7-DAY OUTPUT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
          Text("${String.format("%,.0f", totalOutput)} m²", fontSize = 14.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
        }
        Column {
          Text("INK CONSUMED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
          Text("${String.format("%.1f", totalInk)} Liters", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AccentOrange)
        }
        Column {
          Text("EFFICIENCY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
          Text("${String.format("%.0f", avgEfficiency)}%", fontSize = 14.sp, fontWeight = FontWeight.Black, color = if (avgEfficiency >= 100) SuccessGreen else AccentOrange)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Chart Legend (Interactive Series Legend like Recharts)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (selectedMetricTab != "Material Usage") {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(PrimaryBlue))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Output (m²)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp, 3.dp).background(Color(0xFF64748B)))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Target", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
          }
        }
        if (selectedMetricTab != "Output vs Target") {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentOrange))
            Spacer(modifier = Modifier.width(5.dp))
            Text("Ink (L)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Interactive Canvas Recharts Surface
      val maxOutput = remember(trendPoints) {
        trendPoints.flatMap { listOf(it.outputSqMeters, it.targetSqMeters) }.maxOrNull()?.coerceAtLeast(100f) ?: 500f
      }
      val maxInk = remember(trendPoints) {
        trendPoints.map { it.inkConsumptionLiters }.maxOrNull()?.coerceAtLeast(1f) ?: 10f
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(trendPoints) {
              detectTapGestures { tapOffset ->
                val colWidth = size.width / trendPoints.size
                val idx = (tapOffset.x / colWidth).toInt().coerceIn(0, trendPoints.size - 1)
                selectedIndex = if (selectedIndex == idx) null else idx
              }
            }
        ) {
          val canvasWidth = size.width
          val canvasHeight = size.height
          val paddingBottom = 28.dp.toPx()
          val paddingTop = 16.dp.toPx()
          val effectiveHeight = canvasHeight - paddingBottom - paddingTop

          val count = trendPoints.size
          val columnWidth = canvasWidth / count
          val barWidth = columnWidth * 0.42f

          // 1. Draw horizontal dotted/grid lines
          val gridSteps = 4
          for (i in 0..gridSteps) {
            val y = paddingTop + effectiveHeight * (1f - i.toFloat() / gridSteps)
            drawLine(
              color = Color(0xFFE2E8F0),
              start = Offset(0f, y),
              end = Offset(canvasWidth, y),
              strokeWidth = 1.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
          }

          // Target line points
          val targetPath = Path()
          val inkPath = Path()
          val inkFillPath = Path()
          val inkPoints = mutableListOf<Offset>()

          // 2. Draw Bars and build line paths
          trendPoints.forEachIndexed { i, pt ->
            val centerX = columnWidth * i + columnWidth / 2f
            val barLeft = centerX - barWidth / 2f

            // Background hover highlight for active bar
            if (selectedIndex == i) {
              drawRoundRect(
                color = Color(0xFFF1F5F9),
                topLeft = Offset(columnWidth * i, paddingTop),
                size = Size(columnWidth, effectiveHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
              )
            }

            // Output Bar (Dual Color Gradient)
            if (selectedMetricTab != "Material Usage") {
              val outputRatio = (pt.outputSqMeters / maxOutput) * animProgress
              val barH = effectiveHeight * outputRatio
              val barTop = paddingTop + effectiveHeight - barH

              val barBrush = Brush.verticalGradient(
                colors = listOf(PrimaryBlue, Color(0xFF38BDF8)),
                startY = barTop,
                endY = barTop + barH
              )

              drawRoundRect(
                brush = barBrush,
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
              )
            }

            // Target Line point calculation
            val targetRatio = (pt.targetSqMeters / maxOutput) * animProgress
            val targetY = paddingTop + effectiveHeight * (1f - targetRatio)
            if (i == 0) {
              targetPath.moveTo(centerX, targetY)
            } else {
              targetPath.lineTo(centerX, targetY)
            }

            // Ink Consumption Point calculation (Secondary axis scaled)
            val inkRatio = (pt.inkConsumptionLiters / maxInk) * animProgress
            val inkY = paddingTop + effectiveHeight * (1f - inkRatio)
            inkPoints.add(Offset(centerX, inkY))

            if (i == 0) {
              inkPath.moveTo(centerX, inkY)
              inkFillPath.moveTo(centerX, paddingTop + effectiveHeight)
              inkFillPath.lineTo(centerX, inkY)
            } else {
              inkPath.lineTo(centerX, inkY)
              inkFillPath.lineTo(centerX, inkY)
            }
            if (i == count - 1) {
              inkFillPath.lineTo(centerX, paddingTop + effectiveHeight)
              inkFillPath.close()
            }
          }

          // 3. Draw Target Reference Line
          if (selectedMetricTab != "Material Usage") {
            drawPath(
              path = targetPath,
              color = Color(0xFF64748B),
              style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                cap = StrokeCap.Round
              )
            )
          }

          // 4. Draw Ink Usage Trend Line with soft gradient area
          if (selectedMetricTab != "Output vs Target") {
            drawPath(
              path = inkFillPath,
              brush = Brush.verticalGradient(
                colors = listOf(AccentOrange.copy(alpha = 0.25f), Color.Transparent),
                startY = paddingTop,
                endY = paddingTop + effectiveHeight
              )
            )

            drawPath(
              path = inkPath,
              color = AccentOrange,
              style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw circular data nodes on ink line
            inkPoints.forEachIndexed { idx, ptOffset ->
              val isSelected = selectedIndex == idx
              drawCircle(
                color = Color.White,
                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                center = ptOffset
              )
              drawCircle(
                color = AccentOrange,
                radius = if (isSelected) 4.5.dp.toPx() else 2.5.dp.toPx(),
                center = ptOffset
              )
            }
          }
        }
      }

      // X-Axis Labels Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        trendPoints.forEachIndexed { idx, item ->
          val isSelected = selectedIndex == idx
          Text(
            text = item.dayLabel,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            color = if (isSelected) PrimaryBlue else TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable {
              selectedIndex = if (selectedIndex == idx) null else idx
            }
          )
        }
      }

      // Interactive Hover Tooltip (Recharts Floating Tooltip)
      AnimatedVisibility(
        visible = selectedIndex != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        val activeItem = selectedIndex?.let { trendPoints.getOrNull(it) }
        if (activeItem != null) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 8.dp,
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 12.dp)
          ) {
            Row(
              modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "${activeItem.dayLabel} • ${activeItem.fullDate}",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = activeItem.machineName,
                  color = Color.White.copy(alpha = 0.7f),
                  fontSize = 10.sp
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                  Text("Output / Target", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                  Text(
                    text = "${activeItem.outputSqMeters.toInt()} / ${activeItem.targetSqMeters.toInt()} m²",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("Ink Consumed", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                  Text(
                    text = "${String.format("%.2f", activeItem.inkConsumptionLiters)} L",
                    color = AccentOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
