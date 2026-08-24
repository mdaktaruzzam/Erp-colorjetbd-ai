package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
fun ServiceScreen(viewModel: InventoryViewModel) {
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val allTickets by viewModel.allTickets.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var showAddTicketDialog by remember { mutableStateOf(false) }
    var selectedTicketDetail by remember { mutableStateOf<ServiceTicket?>(null) }
    var showResolveDialog by remember { mutableStateOf<ServiceTicket?>(null) }

    // Enforce data restriction: Engineers (pre-seeded under "staff" user / Tanvir Hasan id=3)
    // can ONLY view tickets assigned to them! Owners and admins see everything.
    val isEngineerOnly = currentSession?.role == "STAFF" && currentSession?.username == "staff"
    val engineerId = 3 // Tanvir Hasan

    val visibleTickets = if (isEngineerOnly) {
        allTickets.filter { it.assignedEngineerId == engineerId }
    } else {
        allTickets
    }

    var ticketFilter by remember { mutableStateOf("ALL") }
    val filteredTickets = when (ticketFilter) {
        "OPEN" -> visibleTickets.filter { it.status == "OPEN" }
        "IN_PROGRESS" -> visibleTickets.filter { it.status == "IN_PROGRESS" }
        "RESOLVED" -> visibleTickets.filter { it.status == "RESOLVED" }
        else -> visibleTickets
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEngineerOnly) "MY SERVICE SCHEDULE" else "ENGINEER SERVICE CENTER",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue),
                actions = {
                    // Engineers cannot create tickets, only Owners/Admins
                    if (!isEngineerOnly) {
                        IconButton(onClick = { showAddTicketDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Ticket", tint = Color.White)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            // Priority Restriction Banner for Engineers
            if (isEngineerOnly) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AccentOrange.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Showing only jobs assigned to you under direct engineer safety protocol.",
                        color = AccentOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Tabs / Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = ticketFilter == "ALL",
                    onClick = { ticketFilter = "ALL" },
                    label = { Text("All Tickets") }
                )
                FilterChip(
                    selected = ticketFilter == "OPEN",
                    onClick = { ticketFilter = "OPEN" },
                    label = { Text("Open") }
                )
                FilterChip(
                    selected = ticketFilter == "IN_PROGRESS",
                    onClick = { ticketFilter = "IN_PROGRESS" },
                    label = { Text("In Progress") }
                )
                FilterChip(
                    selected = ticketFilter == "RESOLVED",
                    onClick = { ticketFilter = "RESOLVED" },
                    label = { Text("Resolved") }
                )
            }

            if (filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No technical service tickets found.", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTickets) { ticket ->
                        ServiceTicketRow(
                            ticket = ticket,
                            onResolveClick = { showResolveDialog = ticket },
                            onRowClick = { selectedTicketDetail = ticket }
                        )
                    }
                }
            }
        }

        // Add Ticket Dialog
        if (showAddTicketDialog) {
            AddTicketDialog(
                customers = allCustomers,
                users = allUsers.filter { it.role == "STAFF" || it.role == "ADMIN" },
                onDismiss = { showAddTicketDialog = false },
                onConfirm = { ticketNum, custId, custName, model, serial, issue, engId, engName, priority ->
                    viewModel.addServiceTicket(ticketNum, custId, custName, model, serial, issue, engId, engName, priority)
                    showAddTicketDialog = false
                }
            )
        }

        // Resolve Ticket Dialog
        if (showResolveDialog != null) {
            val t = showResolveDialog!!
            ResolveTicketDialog(
                ticket = t,
                onDismiss = { showResolveDialog = null },
                onConfirm = { status, report, parts ->
                    val updated = t.copy(
                        status = status,
                        serviceReport = report,
                        partsReplaced = parts,
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.updateServiceTicket(updated)
                    showResolveDialog = null
                }
            )
        }

        // View Detail Dialog
        if (selectedTicketDetail != null) {
            val t = selectedTicketDetail!!
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            AlertDialog(
                onDismissRequest = { selectedTicketDetail = null },
                confirmButton = {
                    TextButton(onClick = { selectedTicketDetail = null }) {
                        Text("CLOSE")
                    }
                },
                title = {
                    Text("TICKET: ${t.ticketNumber}", fontWeight = FontWeight.Black, color = PrimaryBlue)
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Customer: ${t.customerName}", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Model: ${t.deviceModel} (S/N: ${t.serialNumber})", fontSize = 13.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Issue Logged:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                        Text(t.issueDescription, fontSize = 13.sp, color = TextPrimary)

                        if (!t.serviceReport.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Service Resolution Report:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                            Text(t.serviceReport, fontSize = 13.sp, color = TextPrimary)
                        }

                        if (!t.partsReplaced.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Consumables / Parts Replaced:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentOrange)
                            Text(t.partsReplaced, fontSize = 13.sp, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Engineer:", fontSize = 12.sp, color = TextSecondary)
                            Text(t.assignedEngineerName ?: "Unassigned", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Priority:", fontSize = 12.sp, color = TextSecondary)
                            Text(t.priority, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (t.priority == "HIGH") Color.Red else AccentOrange)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Status:", fontSize = 12.sp, color = TextSecondary)
                            Text(t.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (t.status == "RESOLVED") SuccessGreen else PrimaryBlue)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Logged At:", fontSize = 12.sp, color = TextSecondary)
                            Text(sdf.format(Date(t.createdAt)), fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ServiceTicketRow(
    ticket: ServiceTicket,
    onResolveClick: () -> Unit,
    onRowClick: () -> Unit
) {
    val isResolved = ticket.status == "RESOLVED"
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onRowClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ticket.ticketNumber,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = PrimaryBlue
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (ticket.status) {
                                "RESOLVED" -> SuccessGreen.copy(alpha = 0.15f)
                                "IN_PROGRESS" -> AccentOrange.copy(alpha = 0.15f)
                                else -> Color.Red.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = ticket.status,
                        color = when (ticket.status) {
                            "RESOLVED" -> SuccessGreen
                            "IN_PROGRESS" -> AccentOrange
                            else -> Color.Red
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = ticket.customerName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Text(
                text = "Model: ${ticket.deviceModel}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ticket.issueDescription,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ASSIGNED ENGINEER",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = ticket.assignedEngineerName ?: "Unassigned",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (!isResolved) {
                    Button(
                        onClick = onResolveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("UPDATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTicketDialog(
    customers: List<Customer>,
    users: List<User>,
    onDismiss: () -> Unit,
    onConfirm: (
        ticketNum: String,
        customerId: Int,
        customerName: String,
        deviceModel: String,
        serialNumber: String,
        issueDescription: String,
        assignedEngineerId: Int?,
        assignedEngineerName: String?,
        priority: String
    ) -> Unit
) {
    var ticketNum by remember { mutableStateOf("") }
    var deviceModel by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var issueDescription by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }

    var custDropdown by remember { mutableStateOf(false) }
    var selectedCust by remember { mutableStateOf<Customer?>(null) }

    var userDropdown by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Service Ticket", fontWeight = FontWeight.Black, color = PrimaryBlue) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ticketNum,
                    onValueChange = { ticketNum = it },
                    label = { Text("Ticket ID (Auto if blank)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Select Customer
                ExposedDropdownMenuBox(
                    expanded = custDropdown,
                    onExpandedChange = { custDropdown = !custDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCust?.name ?: "Select Customer *",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Customer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = custDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = custDropdown,
                        onDismissRequest = { custDropdown = false }
                    ) {
                        customers.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedCust = c
                                    custDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = deviceModel,
                    onValueChange = { deviceModel = it },
                    label = { Text("Device Model *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    label = { Text("Serial Number *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = issueDescription,
                    onValueChange = { issueDescription = it },
                    label = { Text("Description of Issue *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Select Engineer
                ExposedDropdownMenuBox(
                    expanded = userDropdown,
                    onExpandedChange = { userDropdown = !userDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedUser?.fullName ?: "Assign Engineer...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Engineer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = userDropdown,
                        onDismissRequest = { userDropdown = false }
                    ) {
                        users.forEach { u ->
                            DropdownMenuItem(
                                text = { Text("${u.fullName} (${u.role})") },
                                onClick = {
                                    selectedUser = u
                                    userDropdown = false
                                }
                            )
                        }
                    }
                }

                // Priority Selection
                Column {
                    Text("Ticket Severity / Priority", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                            val isSelected = priority == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                    .clickable { priority = p }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p, color = if (isSelected) Color.White else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedCust != null && deviceModel.isNotBlank() && serialNumber.isNotBlank() && issueDescription.isNotBlank()) {
                        onConfirm(
                            ticketNum.trim(),
                            selectedCust!!.id,
                            selectedCust!!.name,
                            deviceModel.trim(),
                            serialNumber.trim(),
                            issueDescription.trim(),
                            selectedUser?.id,
                            selectedUser?.fullName,
                            priority
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("LOG TICKET")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolveTicketDialog(
    ticket: ServiceTicket,
    onDismiss: () -> Unit,
    onConfirm: (status: String, report: String, parts: String) -> Unit
) {
    var status by remember { mutableStateOf(if (ticket.status == "OPEN") "IN_PROGRESS" else "RESOLVED") }
    var report by remember { mutableStateOf(ticket.serviceReport ?: "") }
    var parts by remember { mutableStateOf(ticket.partsReplaced ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Ticket ${ticket.ticketNumber}", fontWeight = FontWeight.Black, color = PrimaryBlue) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Select Status
                Column {
                    Text("Service Ticket Status", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("IN_PROGRESS", "RESOLVED").forEach { s ->
                            val isSelected = status == s
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                    .clickable { status = s }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(s.replace("_", " "), color = if (isSelected) Color.White else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = report,
                    onValueChange = { report = it },
                    label = { Text("Service Resolution Report *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = parts,
                    onValueChange = { parts = it },
                    label = { Text("Spare Parts / Consumables Replaced") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (report.isNotBlank()) {
                        onConfirm(status, report.trim(), parts.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("SAVE STATUS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
