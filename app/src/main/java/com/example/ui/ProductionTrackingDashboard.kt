package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionTrackingDashboard(
    viewModel: InventoryViewModel,
    onClose: () -> Unit
) {
    val allMachines by viewModel.allMachines.collectAsStateWithLifecycle()
    val allRecords by viewModel.allProductionRecords.collectAsStateWithLifecycle()
    
    // State for filtering
    var selectedMachineId by remember { mutableStateOf<Int?>(null) } // null means "All Machines"
    
    // Filtered records
    val filteredRecords = remember(allRecords, selectedMachineId) {
        if (selectedMachineId == null) {
            allRecords
        } else {
            allRecords.filter { it.machineId == selectedMachineId }
        }
    }
    
    // Sorted past 7 days for the chart
    val chartRecords = remember(filteredRecords) {
        filteredRecords.sortedBy { it.date }.takeLast(7)
    }

    // Modal/Form states
    var showAddForm by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PRODUCTION TRACKING",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "ColorJet Factory Floor Output Tracker",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.performDataSync() }) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Sync Cloud", tint = Color.White)
                    }
                    IconButton(onClick = { showAddForm = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Log Output", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp)
            ) {
                // Offline Support & Firestore Sync Banner
                val isOfflineActive by FirebaseService.isOfflineSyncActive.collectAsStateWithLifecycle()
                val lastSyncTime by FirebaseService.lastSyncTimestamp.collectAsStateWithLifecycle()
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("firestore_offline_status_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOfflineActive) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, if (isOfflineActive) Color(0xFFA5D6A7) else Color(0xFFFFCC80))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isOfflineActive) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "Offline Cache Ready",
                                tint = if (isOfflineActive) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Firestore Offline Engine Active",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfflineActive) Color(0xFF1B5E20) else Color(0xFFBF360C)
                                )
                                Text(
                                    text = "Logs cached locally on disk & queued for auto-sync without internet",
                                    fontSize = 10.sp,
                                    color = if (isOfflineActive) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = { viewModel.performDataSync() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("sync_now_button")
                        ) {
                            Text(
                                text = "SYNC NOW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                // Machine Filter chips
                MachineFiltersRow(
                    machines = allMachines,
                    selectedId = selectedMachineId,
                    onSelect = { selectedMachineId = it }
                )
                
                // KPIs Cards
                ProductionKpisSection(records = filteredRecords)
                
                // Canvas-based interactive chart mimicking Recharts
                ProductionRechartsWidget(records = chartRecords)
                
                // Line chart displaying machine efficiency trend
                MachineEfficiencyTrendWidget(records = chartRecords)
                
                // Record list / Logs Table
                ProductionLogsTable(
                    records = filteredRecords,
                    onDeleteRecord = { viewModel.deleteProductionRecord(it.id) }
                )
            }
            
            // FAB for adding logs
            FloatingActionButton(
                onClick = { showAddForm = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_production_log_fab"),
                containerColor = AccentOrange,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.PostAdd, contentDescription = "Log Production")
            }
            
            // Log Input Dialog Sheet
            if (showAddForm) {
                AddProductionLogDialog(
                    machines = allMachines,
                    onDismiss = { showAddForm = false },
                    onSave = { date, machineId, name, output, target, operator, notes ->
                        val rec = ProductionRecord(
                            date = date,
                            machineId = machineId,
                            machineName = name,
                            output = output,
                            target = target,
                            operatorName = operator,
                            notes = notes
                        )
                        viewModel.addProductionRecord(rec)
                        showAddForm = false
                    }
                )
            }
        }
    }
}

@Composable
fun MachineFiltersRow(
    machines: List<Machine>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" filter
        FilterChip(
            selected = selectedId == null,
            onClick = { onSelect(null) },
            label = { Text("All Printers", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PrimaryBlue,
                selectedLabelColor = Color.White,
                containerColor = Color.White,
                labelColor = TextSecondary
            ),
            border = null
        )
        
        machines.forEach { machine ->
            val isSelected = selectedId == machine.id
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(machine.id) },
                label = { Text(machine.model, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = TextSecondary
                ),
                border = null
            )
        }
    }
}

@Composable
fun ProductionKpisSection(records: List<ProductionRecord>) {
    val totalOutput = records.sumOf { it.output.toDouble() }
    val totalTarget = records.sumOf { it.target.toDouble() }
    val avgTargetPct = if (totalTarget > 0.0) (totalOutput / totalTarget) * 100.0 else 0.0
    
    val topMachine = records.groupBy { it.machineName }
        .mapValues { entry -> entry.value.sumOf { it.output.toDouble() } }
        .maxByOrNull { it.value }?.key ?: "N/A"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("TOTAL OUTPUT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${String.format("%,.0f", totalOutput)} m²", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cumulative volume", fontSize = 10.sp, color = TextSecondary)
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("TARGET MET %", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${String.format("%.1f", avgTargetPct)}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (avgTargetPct >= 100.0) SuccessGreen else AccentOrange)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Average efficiency", fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("TOP PERFORMING PRINTER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Text(
                    text = topMachine,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ProductionRechartsWidget(records: List<ProductionRecord>) {
    var selectedBarIndex by remember(records) { mutableStateOf<Int?>(null) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAILY OUTPUT VS TARGETS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Interactive wide-format tracking (Past 7 runs)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 26.dp, top = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chart Legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 26.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp, 12.dp).clip(RoundedCornerShape(3.dp)).background(PrimaryBlue))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Output (m²)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp, 4.dp).background(AccentOrange))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Daily Target (m²)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No production data logged yet", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            } else {
                // Dual Series interactive Canvas mimicking Recharts
                val maxVal = remember(records) {
                    records.flatMap { listOf(it.output, it.target) }.maxOrNull()?.coerceAtLeast(100f) ?: 200f
                }
                
                val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfOutput = SimpleDateFormat("dd MMM", Locale.getDefault())
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(records) {
                                detectTapGestures { offset ->
                                    val sectionWidth = size.width / records.size
                                    val index = (offset.x / sectionWidth).toInt().coerceIn(0, records.size - 1)
                                    selectedBarIndex = if (selectedBarIndex == index) null else index
                                }
                            }
                    ) {
                        val canvasHeight = size.height
                        val canvasWidth = size.width
                        val paddingBottom = 40f
                        val paddingTop = 20f
                        val effectiveHeight = canvasHeight - paddingBottom - paddingTop
                        
                        val barSpacingRatio = 0.35f
                        val columnCount = records.size
                        val columnWidth = canvasWidth / columnCount
                        val barWidth = columnWidth * (1f - barSpacingRatio)
                        
                        // Draw horizontal grid lines
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val y = paddingTop + effectiveHeight * (1f - i.toFloat() / gridLines)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        
                        // Path points for Target Line
                        val targetPoints = ArrayList<Offset>()
                        
                        records.forEachIndexed { index, record ->
                            val centerX = columnWidth * index + columnWidth / 2f
                            val startX = centerX - barWidth / 2f
                            
                            // 1. Output Bar (Blue)
                            val outputRatio = record.output / maxVal
                            val barHeight = effectiveHeight * outputRatio
                            val barTop = paddingTop + effectiveHeight - barHeight
                            
                            // Highlight hovered/clicked index like Recharts active bar background
                            if (selectedBarIndex == index) {
                                drawRoundRect(
                                    color = Color.LightGray.copy(alpha = 0.15f),
                                    topLeft = Offset(columnWidth * index, paddingTop),
                                    size = Size(columnWidth, effectiveHeight),
                                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                )
                            }
                            
                            drawRoundRect(
                                color = PrimaryBlue,
                                topLeft = Offset(startX, barTop),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                            
                            // 2. Add Target Line Offset point (AccentOrange)
                            val targetRatio = record.target / maxVal
                            val targetY = paddingTop + effectiveHeight - (effectiveHeight * targetRatio)
                            targetPoints.add(Offset(centerX, targetY))
                        }
                        
                        // Draw continuous Target Line with small indicator circles
                        if (targetPoints.size > 1) {
                            for (i in 0 until targetPoints.size - 1) {
                                drawLine(
                                    color = AccentOrange,
                                    start = targetPoints[i],
                                    end = targetPoints[i + 1],
                                    strokeWidth = 3.dp.toPx()
                                )
                            }
                        }
                        
                        targetPoints.forEach { pt ->
                            drawCircle(
                                color = Color.White,
                                center = pt,
                                radius = 5.dp.toPx()
                            )
                            drawCircle(
                                color = AccentOrange,
                                center = pt,
                                radius = 3.dp.toPx()
                            )
                        }
                    }
                    
                    // Date Labels below
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(top = 185.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        records.forEachIndexed { index, record ->
                            val formattedDate = try {
                                val d = sdfInput.parse(record.date)
                                if (d != null) sdfOutput.format(d) else record.date
                            } catch (e: Exception) {
                                record.date
                            }
                            Text(
                                text = formattedDate,
                                fontSize = 10.sp,
                                fontWeight = if (selectedBarIndex == index) FontWeight.Black else FontWeight.Bold,
                                color = if (selectedBarIndex == index) PrimaryBlue else TextSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Interactive floating tooltip content
                AnimatedVisibility(
                    visible = selectedBarIndex != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    selectedBarIndex?.let { index ->
                        if (index < records.size) {
                            val item = records[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Details: ${item.date}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                                        Text(
                                            text = item.machineName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.widthIn(max = 180.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column {
                                            Text("OUTPUT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                            Text("${item.output} m²", fontSize = 13.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                                        }
                                        Column {
                                            Text("TARGET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                            Text("${item.target} m²", fontSize = 13.sp, fontWeight = FontWeight.Black, color = AccentOrange)
                                        }
                                        Column {
                                            Text("ACHIEVED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                            val pct = (item.output / item.target) * 100f
                                            Text("${String.format("%.1f", pct)}%", fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (pct >= 100f) SuccessGreen else AccentOrange)
                                        }
                                    }
                                    if (item.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Operator ${item.operatorName}: ${item.notes}", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MachineEfficiencyTrendWidget(records: List<ProductionRecord>) {
    var selectedPointIndex by remember(records) { mutableStateOf<Int?>(null) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp, 
                            contentDescription = null, 
                            tint = SuccessGreen, 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MACHINE EFFICIENCY TREND",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "7-day production efficiency percentage tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 26.dp, top = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chart Legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 26.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp, 12.dp).clip(RoundedCornerShape(3.dp)).background(SuccessGreen))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Efficiency (%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp, 2.dp)
                            .background(Color.Gray.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("100% Target Line", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No production data logged yet", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            } else {
                val efficiencies = remember(records) {
                    records.map { if (it.target > 0f) (it.output / it.target) * 100f else 0f }
                }
                
                val maxEff = remember(efficiencies) {
                    (efficiencies.maxOrNull() ?: 100f).coerceAtLeast(100f)
                }
                val maxVal = maxEff * 1.2f // Add 20% padding at top
                
                val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfOutput = SimpleDateFormat("dd MMM", Locale.getDefault())
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(records) {
                                detectTapGestures { offset ->
                                    val paddingLeft = 50f
                                    val paddingRight = 50f
                                    val effectiveWidth = size.width - paddingLeft - paddingRight
                                    val step = effectiveWidth / (records.size - 1).coerceAtLeast(1)
                                    val index = ((offset.x - paddingLeft + step / 2f) / step).toInt().coerceIn(0, records.size - 1)
                                    selectedPointIndex = if (selectedPointIndex == index) null else index
                                }
                            }
                    ) {
                        val canvasHeight = size.height
                        val canvasWidth = size.width
                        val paddingBottom = 40f
                        val paddingTop = 20f
                        val paddingLeft = 50f
                        val paddingRight = 50f
                        val effectiveHeight = canvasHeight - paddingBottom - paddingTop
                        val effectiveWidth = canvasWidth - paddingLeft - paddingRight
                        
                        // Draw horizontal grid lines
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val percentVal = (maxVal * (i.toFloat() / gridLines)).toInt()
                            val y = paddingTop + effectiveHeight * (1f - i.toFloat() / gridLines)
                            
                            // Grid line
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                start = Offset(paddingLeft, y),
                                end = Offset(canvasWidth - paddingRight, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        
                        // Draw 100% target line
                        val targetRatio = 100f / maxVal
                        val targetY = paddingTop + effectiveHeight * (1f - targetRatio)
                        if (targetY in paddingTop..(paddingTop + effectiveHeight)) {
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.6f),
                                start = Offset(paddingLeft, targetY),
                                end = Offset(canvasWidth - paddingRight, targetY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            )
                        }
                        
                        // Compute point coordinates
                        val points = ArrayList<Offset>()
                        records.forEachIndexed { index, record ->
                            val efficiency = efficiencies[index]
                            val x = paddingLeft + (index.toFloat() / (records.size - 1).coerceAtLeast(1)) * effectiveWidth
                            val y = paddingTop + effectiveHeight * (1f - (efficiency / maxVal))
                            points.add(Offset(x, y))
                        }
                        
                        // Draw vertical cursor line if a point is selected
                        selectedPointIndex?.let { index ->
                            if (index < points.size) {
                                val pt = points[index]
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.8f),
                                    start = Offset(pt.x, paddingTop),
                                    end = Offset(pt.x, paddingTop + effectiveHeight),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                )
                            }
                        }
                        
                        // Draw gradient fill area under the line
                        if (points.size > 1) {
                            val fillPath = androidx.compose.ui.graphics.Path().apply {
                                moveTo(points[0].x, paddingTop + effectiveHeight)
                                points.forEach { pt ->
                                    lineTo(pt.x, pt.y)
                                }
                                lineTo(points.last().x, paddingTop + effectiveHeight)
                                close()
                            }
                            
                            drawPath(
                                path = fillPath,
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        SuccessGreen.copy(alpha = 0.35f),
                                        SuccessGreen.copy(alpha = 0.01f)
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + effectiveHeight
                                )
                            )
                            
                            // Draw trend line
                            val linePath = androidx.compose.ui.graphics.Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                            
                            drawPath(
                                path = linePath,
                                color = SuccessGreen,
                                style = Stroke(
                                    width = 3.5.dp.toPx(),
                                    pathEffect = null
                                )
                            )
                        }
                        
                        // Draw point circles
                        points.forEachIndexed { index, pt ->
                            val isSelected = selectedPointIndex == index
                            drawCircle(
                                color = Color.White,
                                center = pt,
                                radius = (if (isSelected) 7.dp else 5.dp).toPx()
                            )
                            drawCircle(
                                color = SuccessGreen,
                                center = pt,
                                radius = (if (isSelected) 5.dp else 3.dp).toPx()
                            )
                        }
                    }
                    
                    // Date Labels below
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(top = 185.dp, start = 12.dp, end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        records.forEachIndexed { index, record ->
                            val formattedDate = try {
                                val d = sdfInput.parse(record.date)
                                if (d != null) sdfOutput.format(d) else record.date
                            } catch (e: Exception) {
                                record.date
                            }
                            Text(
                                text = formattedDate,
                                fontSize = 10.sp,
                                fontWeight = if (selectedPointIndex == index) FontWeight.Black else FontWeight.Bold,
                                color = if (selectedPointIndex == index) SuccessGreen else TextSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Interactive floating tooltip content
                AnimatedVisibility(
                    visible = selectedPointIndex != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    selectedPointIndex?.let { index ->
                        if (index < records.size) {
                            val item = records[index]
                            val efficiency = efficiencies[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Efficiency Log: ${item.date}", 
                                            fontSize = 11.sp, 
                                            fontWeight = FontWeight.Black, 
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = item.machineName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.widthIn(max = 180.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column {
                                            Text("EFFICIENCY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                            Text(
                                                text = "${String.format("%.1f", efficiency)}%", 
                                                fontSize = 14.sp, 
                                                fontWeight = FontWeight.Black, 
                                                color = if (efficiency >= 100f) SuccessGreen else AccentOrange
                                            )
                                        }
                                        Column {
                                            Text("OUTPUT / TARGET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                            Text("${item.output.toInt()} / ${item.target.toInt()} m²", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                                        }
                                        Column {
                                            Text("STATUS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                            Text(
                                                text = if (efficiency >= 100f) "TARGET EXCEEDED" else "UNDER TARGET", 
                                                fontSize = 11.sp, 
                                                fontWeight = FontWeight.Black, 
                                                color = if (efficiency >= 100f) SuccessGreen else AccentOrange
                                            )
                                        }
                                    }
                                    if (item.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Notes: \"${item.notes}\"", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductionLogsTable(
    records: List<ProductionRecord>,
    onDeleteRecord: (ProductionRecord) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FACTORY FLOOR LOGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (records.isEmpty()) {
                Text(
                    text = "No log records found for this printer selection.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                records.forEachIndexed { idx, record ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.machineName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${record.date} • Operator: ${record.operatorName}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                if (record.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "\"${record.notes}\"",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(horizontalAlignment = Alignment.End) {
                                val pct = (record.output / record.target) * 100f
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${record.output.toInt()} / ${record.target.toInt()} m²",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (pct >= 100f) SuccessGreen.copy(alpha = 0.1f) else AccentOrange.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${pct.toInt()}% Target",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (pct >= 100f) SuccessGreen else AccentOrange
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = { onDeleteRecord(record) },
                                modifier = Modifier.padding(start = 4.dp).size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    if (idx < records.size - 1) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductionLogDialog(
    machines: List<Machine>,
    onDismiss: () -> Unit,
    onSave: (date: String, machineId: Int, name: String, output: Float, target: Float, operator: String, notes: String) -> Unit
) {
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var selectedMachine by remember { mutableStateOf(machines.firstOrNull()) }
    var outputStr by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("150") }
    var operatorName by remember { mutableStateOf("Mominul Islam") }
    var notes by remember { mutableStateOf("") }
    
    var showMachineMenu by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Production Output", fontWeight = FontWeight.Black, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMsg.isNotBlank()) {
                    Text(errorMsg, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Date input
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Machine Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = showMachineMenu,
                    onExpandedChange = { showMachineMenu = !showMachineMenu }
                ) {
                    OutlinedTextField(
                        value = selectedMachine?.model ?: "Select Machine",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Factory Machine") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMachineMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showMachineMenu,
                        onDismissRequest = { showMachineMenu = false }
                    ) {
                        machines.forEach { machine ->
                            DropdownMenuItem(
                                text = { Text("${machine.model} (${machine.brand})") },
                                onClick = {
                                    selectedMachine = machine
                                    showMachineMenu = false
                                }
                            )
                        }
                    }
                }

                // Output Value
                OutlinedTextField(
                    value = outputStr,
                    onValueChange = { outputStr = it },
                    label = { Text("Daily Output (m²)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Target Value
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Daily Target (m²)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Operator Name
                OutlinedTextField(
                    value = operatorName,
                    onValueChange = { operatorName = it },
                    label = { Text("Operator Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Production Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = selectedMachine
                    if (m == null) {
                        errorMsg = "Please select a machine"
                        return@Button
                    }
                    val out = outputStr.toFloatOrNull()
                    if (out == null || out <= 0f) {
                        errorMsg = "Enter a valid output number"
                        return@Button
                    }
                    val tgt = targetStr.toFloatOrNull()
                    if (tgt == null || tgt <= 0f) {
                        errorMsg = "Enter a valid target number"
                        return@Button
                    }
                    onSave(date, m.id, "${m.brand} ${m.model}", out, tgt, operatorName, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("SAVE LOG")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
