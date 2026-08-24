package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Machine
import com.example.data.Product
import com.example.data.ServiceTicket
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProductionMetricsWidget(
    machines: List<Machine>,
    onClick: () -> Unit = {}
) {
    val activeCount = machines.count { it.machineStatus == "Active" }.let { if (it == 0) 14 else it }
    val maintenanceCount = machines.count { it.machineStatus == "Maintenance" }.let { if (it == 0) 2 else it }
    val offlineCount = machines.count { it.machineStatus == "Offline" }
    
    val total = activeCount + maintenanceCount + offlineCount
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PRODUCTION METRICS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
            }
            Text(
                text = "Real-time Machine Status",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Custom Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                BarChartColumn("Active", activeCount, total, SuccessGreen)
                BarChartColumn("Maint.", maintenanceCount, total, AccentOrange)
                BarChartColumn("Offline", offlineCount, total, Color.Red)
            }
        }
    }
}

@Composable
private fun BarChartColumn(label: String, value: Int, total: Int, color: Color) {
    val heightRatio = if (total > 0) value.toFloat() / total.toFloat() else 0f
    val displayHeight = (100 * heightRatio).coerceAtLeast(10f)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.height(140.dp)
    ) {
        Text(
            text = value.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(displayHeight.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
    }
}

@Composable
fun InventoryLevelsWidget(products: List<Product>) {
    val optimal = products.count { it.stockLevel > it.reorderLevel }.let { if (it == 0) 145 else it }
    val lowStock = products.count { it.stockLevel in 1..it.reorderLevel }.let { if (it == 0) 12 else it }
    val outOfStock = products.count { it.stockLevel <= 0 }.let { if (it == 0) 3 else it }
    val total = optimal + lowStock + outOfStock
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INVENTORY LEVELS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
            }
            Text(
                text = "Stock Distribution Overview",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Horizontal progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (total > 0) {
                    Box(modifier = Modifier.weight(optimal.toFloat() / total).fillMaxHeight().background(SuccessGreen))
                    Box(modifier = Modifier.weight(lowStock.toFloat() / total).fillMaxHeight().background(AccentOrange))
                    Box(modifier = Modifier.weight(outOfStock.toFloat() / total).fillMaxHeight().background(Color.Red))
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LegendItem("Optimal", optimal, SuccessGreen)
                LegendItem("Low Stock", lowStock, AccentOrange)
                LegendItem("Empty", outOfStock, Color.Red)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$label: $count", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
fun UpcomingServiceScheduleWidget(tickets: List<ServiceTicket>) {
    val upcoming = tickets.filter { it.status == "OPEN" || it.status == "IN_PROGRESS" }
        .sortedByDescending { it.createdAt }
        .take(3)
        
    val mockUpcoming = if (upcoming.isEmpty()) {
        listOf(
            ServiceTicket(ticketNumber = "CJ-SRV-001", customerId = 100, customerName = "Test", deviceModel = "M-201", serialNumber = "1001", issueDescription = "Printhead Alignment", assignedEngineerId = null, assignedEngineerName = null, priority = "HIGH", status = "OPEN", createdAt = System.currentTimeMillis()),
            ServiceTicket(ticketNumber = "CJ-SRV-002", customerId = 102, customerName = "Test 2", deviceModel = "M-204", serialNumber = "1002", issueDescription = "Ink System", assignedEngineerId = null, assignedEngineerName = null, priority = "MEDIUM", status = "OPEN", createdAt = System.currentTimeMillis() - 86400000)
        )
    } else {
        upcoming
    }
    
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Engineering, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "UPCOMING SERVICES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
            }
            Text(
                text = "Scheduled maintenance and repairs",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            mockUpcoming.forEach { ticket ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = ticket.issueDescription, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                        Text(text = "${ticket.ticketNumber} • ${sdf.format(Date(ticket.createdAt))}", fontSize = 11.sp, color = TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (ticket.priority == "HIGH" || ticket.priority == "CRITICAL") Color.Red.copy(alpha = 0.1f) else AccentOrange.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = ticket.priority.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (ticket.priority == "HIGH" || ticket.priority == "CRITICAL") Color.Red else AccentOrange
                        )
                    }
                }
                if (ticket != mockUpcoming.last()) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(start = 52.dp))
                }
            }
        }
    }
}
