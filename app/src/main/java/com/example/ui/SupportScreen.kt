package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import com.example.data.PdfExportService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val allExternalTickets by viewModel.allSupportTickets.collectAsStateWithLifecycle()
    val allInternalTickets by viewModel.allInternalSupportTickets.collectAsStateWithLifecycle()
    val allArticles by viewModel.allKnowledgeArticles.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("DASHBOARD") } // "DASHBOARD", "TICKETS", "KNOWLEDGE_BASE"
    var selectedTicket by remember { mutableStateOf<SupportTicket?>(null) }
    var showCreateTicketDialog by remember { mutableStateOf(false) }
    var showCreateArticleDialog by remember { mutableStateOf(false) }

    // Check if the current user is an employee vs external customer
    val userRole = currentSession?.role ?: "PUBLIC"
    val isEmployee = userRole != "CUSTOMER" && userRole != "PUBLIC"

    // Filter tickets based on tab and authorization
    var viewExternalTicketsByStaffToggle by remember { mutableStateOf(false) }

    val activeTicketSystem = if (isEmployee && !viewExternalTicketsByStaffToggle) "INTERNAL" else "EXTERNAL"
    val ticketsList = if (activeTicketSystem == "INTERNAL") allInternalTickets else {
        if (isEmployee) {
            allExternalTickets
        } else {
            // Customers only see their own tickets
            val custId = currentSession?.userId ?: 0
            allExternalTickets.filter { it.customerId == custId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "COLORJET AI SUPPORT CENTRE",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isEmployee) "Enterprise Multi-Agent Helpdesk" else "Customer Service Portal",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTicket != null) {
                            selectedTicket = null
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue),
                actions = {
                    // Quick Status Indicator
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI ACTIVE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (selectedTicket == null) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == "DASHBOARD",
                        onClick = { activeTab = "DASHBOARD" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "TICKETS",
                        onClick = { activeTab = "TICKETS" },
                        icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = "Tickets") },
                        label = { Text("Tickets", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "KNOWLEDGE_BASE",
                        onClick = { activeTab = "KNOWLEDGE_BASE" },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Knowledge Base") },
                        label = { Text("Knowledge", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTicket == null) {
                if (activeTab == "TICKETS") {
                    FloatingActionButton(
                        onClick = { showCreateTicketDialog = true },
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("create_ticket_fab")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "New Ticket")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Ticket", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (activeTab == "KNOWLEDGE_BASE" && isEmployee) {
                    FloatingActionButton(
                        onClick = { showCreateArticleDialog = true },
                        containerColor = SuccessGreen,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("create_article_fab")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PostAdd, contentDescription = "New Article")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Article", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundGray)
        ) {
            if (selectedTicket != null) {
                TicketDetailScreen(
                    viewModel = viewModel,
                    ticket = selectedTicket!!,
                    onBack = { selectedTicket = null }
                )
            } else {
                when (activeTab) {
                    "DASHBOARD" -> SupportDashboardView(
                        viewModel = viewModel,
                        allExternalTickets = allExternalTickets,
                        allInternalTickets = allInternalTickets,
                        isEmployee = isEmployee,
                        userRole = userRole,
                        viewExternalTicketsByStaffToggle = viewExternalTicketsByStaffToggle,
                        onToggleSystem = { viewExternalTicketsByStaffToggle = it },
                        onNavigateToTickets = { activeTab = "TICKETS" },
                        onSelectTicket = { selectedTicket = it }
                    )
                    "TICKETS" -> SupportTicketsListView(
                        tickets = ticketsList,
                        isEmployee = isEmployee,
                        viewExternalTicketsByStaffToggle = viewExternalTicketsByStaffToggle,
                        onToggleSystem = { viewExternalTicketsByStaffToggle = it },
                        onSelectTicket = { selectedTicket = it }
                    )
                    "KNOWLEDGE_BASE" -> KnowledgeBaseView(
                        allArticles = allArticles,
                        isEmployee = isEmployee
                    )
                }
            }
        }
    }

    if (showCreateTicketDialog) {
        CreateTicketDialog(
            viewModel = viewModel,
            isEmployee = isEmployee,
            allCustomers = allCustomers,
            currentSession = currentSession,
            onDismiss = { showCreateTicketDialog = false }
        )
    }

    if (showCreateArticleDialog) {
        CreateArticleDialog(
            viewModel = viewModel,
            currentSession = currentSession,
            onDismiss = { showCreateArticleDialog = false }
        )
    }
}

@Composable
fun SupportDashboardView(
    viewModel: InventoryViewModel,
    allExternalTickets: List<SupportTicket>,
    allInternalTickets: List<SupportTicket>,
    isEmployee: Boolean,
    userRole: String,
    viewExternalTicketsByStaffToggle: Boolean,
    onToggleSystem: (Boolean) -> Unit,
    onNavigateToTickets: () -> Unit,
    onSelectTicket: (SupportTicket) -> Unit
) {
    val tickets = if (isEmployee) {
        if (viewExternalTicketsByStaffToggle) allExternalTickets else allInternalTickets
    } else {
        val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
        val custId = currentSession?.userId ?: 0
        allExternalTickets.filter { it.customerId == custId }
    }

    val totalCount = tickets.size
    val openCount = tickets.count { it.status == "OPEN" || it.status == "IN_PROGRESS" }
    val escalatedCount = tickets.count { it.isEscalated || it.status == "ESCALATED" }
    val resolvedCount = tickets.count { it.status == "RESOLVED" || it.status == "CLOSED" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Support Center Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "COLORJET Bangladesh",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "How can we assist you today?",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Support Hero",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Authorized support systems powered by secure, enterprise-level Gemini. Only approved manual and database logs are referenced to guarantee zero hallucination.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Isolated Portal Switcher (For Employees Only)
        if (isEmployee) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PORTAL ENVIRONMENT SELECTOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BackgroundGray)
                                .padding(4.dp)
                        ) {
                            Button(
                                onClick = { onToggleSystem(false) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!viewExternalTicketsByStaffToggle) PrimaryBlue else Color.Transparent,
                                    contentColor = if (!viewExternalTicketsByStaffToggle) Color.White else TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Domain, contentDescription = "Internal", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Internal Helpdesk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = { onToggleSystem(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewExternalTicketsByStaffToggle) PrimaryBlue else Color.Transparent,
                                    contentColor = if (viewExternalTicketsByStaffToggle) Color.White else TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Support, contentDescription = "External", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Customer Support", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // KPI Metric Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ACTIVE HELPDESK METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "All Tickets",
                        value = totalCount.toString(),
                        color = Color(0xFF1E3A8A),
                        icon = Icons.Default.ConfirmationNumber
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Open/Pending",
                        value = openCount.toString(),
                        color = AccentOrange,
                        icon = Icons.Default.HourglassEmpty
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Escalated SLA",
                        value = escalatedCount.toString(),
                        color = Color.Red,
                        icon = Icons.Default.Warning
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Resolved",
                        value = resolvedCount.toString(),
                        color = SuccessGreen,
                        icon = Icons.Default.CheckCircle
                    )
                }
            }
        }

        // SLA Critical Reminders / Countdown List
        val criticalSlaTickets = tickets.filter {
            (it.status == "OPEN" || it.status == "IN_PROGRESS" || it.status == "ESCALATED") &&
                    (it.priority == "CRITICAL" || it.priority == "HIGH" || it.isEscalated)
        }
        if (criticalSlaTickets.isNotEmpty()) {
            item {
                Text(
                    text = "SLA CRISIS & REMINDERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Red,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            items(criticalSlaTickets.take(3)) { ticket ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTicket(ticket) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = "SLA Alert", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ticket.ticketNumber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Red
                                )
                                Text(
                                    text = "PRIORITY: ${ticket.priority}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Red
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ticket.subject,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                            Text(
                                text = "SLA Deadline: ${sdf.format(Date(ticket.slaDeadline))}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Recent Tickets Block
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT INCOMING TICKETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                TextButton(onClick = onNavigateToTickets) {
                    Text("View All", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (tickets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = "No tickets", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No active tickets found in this portal", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(tickets.take(4)) { ticket ->
                TicketItemRow(ticket = ticket, onSelect = onSelectTicket)
            }
        }
    }
}

@Composable
fun SupportTicketsListView(
    tickets: List<SupportTicket>,
    isEmployee: Boolean,
    viewExternalTicketsByStaffToggle: Boolean,
    onToggleSystem: (Boolean) -> Unit,
    onSelectTicket: (SupportTicket) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedPriorityFilter by remember { mutableStateOf("ALL") }
    var selectedTicketIds by remember { mutableStateOf(setOf<Int>()) }

    val categories = listOf("ALL", "SOFTWARE", "HARDWARE", "HR", "ACCOUNTS", "SPARE_PARTS", "GENERAL")
    val priorities = listOf("ALL", "LOW", "MEDIUM", "HIGH", "CRITICAL")

    val filteredTickets = tickets.filter { ticket ->
        val matchesSearch = ticket.subject.contains(searchQuery, ignoreCase = true) ||
                ticket.ticketNumber.contains(searchQuery, ignoreCase = true) ||
                (ticket.customerName?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (ticket.employeeName?.contains(searchQuery, ignoreCase = true) ?: false)
        val matchesCategory = selectedCategoryFilter == "ALL" || ticket.category == selectedCategoryFilter
        val matchesPriority = selectedPriorityFilter == "ALL" || ticket.priority == selectedPriorityFilter
        matchesSearch && matchesCategory && matchesPriority
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle system in Ticket Tab
        if (isEmployee) {
            TabRow(
                selectedTabIndex = if (viewExternalTicketsByStaffToggle) 1 else 0,
                containerColor = Color.White,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = !viewExternalTicketsByStaffToggle,
                    onClick = { onToggleSystem(false) },
                    text = { Text("Internal Helpdesk", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = viewExternalTicketsByStaffToggle,
                    onClick = { onToggleSystem(true) },
                    text = { Text("Customer Support", fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Search and Filters Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Ticket #, subject, customer...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ticket_search_input"),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                // Category Chips Scrollable Row
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.12f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }

                // Priority Chips Scrollable Row
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorities.forEach { prio ->
                        FilterChip(
                            selected = selectedPriorityFilter == prio,
                            onClick = { selectedPriorityFilter = prio },
                            label = { Text("$prio PRIO", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Red.copy(alpha = 0.1f),
                                selectedLabelColor = Color.Red
                            )
                        )
                    }
                }
            }
        }

        // Main List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredTickets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = "No results", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No matching tickets found", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(filteredTickets) { ticket ->
                    TicketItemRow(
                        ticket = ticket, 
                        onSelect = onSelectTicket,
                        isSelected = selectedTicketIds.contains(ticket.id),
                        onSelectionChanged = { isSelected ->
                            selectedTicketIds = if (isSelected) {
                                selectedTicketIds + ticket.id
                            } else {
                                selectedTicketIds - ticket.id
                            }
                        }
                    )
                }
            }
        }
        
        if (selectedTicketIds.isNotEmpty()) {
            Button(
                onClick = {
                    val selectedTickets = tickets.filter { selectedTicketIds.contains(it.id) }
                    PdfExportService.exportMaintenanceReport(context, selectedTickets)
                    selectedTicketIds = emptySet()
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Export ${selectedTicketIds.size} Selected Records")
            }
        }
    }
}

@Composable
fun TicketItemRow(
    ticket: SupportTicket, 
    onSelect: (SupportTicket) -> Unit,
    isSelected: Boolean = false,
    onSelectionChanged: (Boolean) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(ticket) }
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.05f) else Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSelected, onCheckedChange = onSelectionChanged)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlue.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = ticket.ticketNumber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ticket.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary
                        )
                    }

                    // Priority Badge
                    val (bg, textCol) = when (ticket.priority) {
                        "CRITICAL" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                        "HIGH" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
                        "MEDIUM" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
                        else -> Color(0xFFF3F4F6) to Color(0xFF374151)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = ticket.priority,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = textCol
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ticket.subject,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Sub-details (Who created / Who assigned)
                val infoStr = if (ticket.systemType == "INTERNAL") {
                    "Staff: ${ticket.employeeName ?: "System"} | Assignee: ${ticket.assignedToName ?: "Unassigned"}"
                } else {
                    "Customer: ${ticket.customerName ?: "Public User"} | Engineer: ${ticket.assignedToName ?: "Unassigned"}"
                }
                Text(
                    text = infoStr,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Badge & Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusCol = when (ticket.status) {
                        "OPEN" -> AccentOrange
                        "IN_PROGRESS" -> PrimaryBlue
                        "RESOLVED", "CLOSED" -> SuccessGreen
                        else -> Color.Red
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusCol)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ticket.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = statusCol
                        )
                    }

                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = sdf.format(Date(ticket.createdAt)),
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun KnowledgeBaseView(
    allArticles: List<KnowledgeArticle>,
    isEmployee: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("ALL") }

    val tabs = if (isEmployee) {
        listOf("ALL", "Troubleshooting", "HR Rules", "Product Manual", "Payment Policy")
    } else {
        listOf("ALL", "Troubleshooting", "Product Manual")
    }

    // Filter articles depending on external/internal isolation
    val systemFiltered = allArticles.filter { art ->
        if (isEmployee) true else (art.systemType == "EXTERNAL" || art.systemType == "BOTH")
    }

    val finalFiltered = systemFiltered.filter { art ->
        val matchesSearch = art.title.contains(searchQuery, ignoreCase = true) ||
                art.content.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedTab == "ALL" || art.category == selectedTab
        matchesSearch && matchesCategory
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Category
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search knowledge articles...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("article_search_input"),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.12f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }
            }
        }

        // Articles List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (finalFiltered.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MenuBook, contentDescription = "No articles", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No articles found in this section", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(finalFiltered) { article ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .animateContentSize()
                            .shadow(1.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = article.category.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PrimaryBlue,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = article.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand",
                                    tint = TextSecondary
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = article.content,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "By: ${article.createdBy}",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (article.systemType == "INTERNAL") Color(0xFFF3F4F6) else Color(0xFFECFDF5)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${article.systemType} ARTICLE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (article.systemType == "INTERNAL") Color.DarkGray else SuccessGreen
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
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier.shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 16.sp, color = color, fontWeight = FontWeight.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketDialog(
    viewModel: InventoryViewModel,
    isEmployee: Boolean,
    allCustomers: List<Customer>,
    currentSession: UserSession?,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GENERAL") }
    var priority by remember { mutableStateOf("LOW") }

    val categories = listOf("SOFTWARE", "HARDWARE", "HR", "ACCOUNTS", "SPARE_PARTS", "GENERAL")
    val priorities = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var expandedCust by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Support Ticket", fontWeight = FontWeight.Black, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Problem Subject / Title") },
                    modifier = Modifier.fillMaxWidth().testTag("ticket_subject_input")
                )

                Text("Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Priority Level", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priorities.forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio, fontSize = 11.sp) }
                        )
                    }
                }

                if (isEmployee) {
                    Text("Select customer (If customer facing ticket)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    ExposedDropdownMenuBox(
                        expanded = expandedCust,
                        onExpandedChange = { expandedCust = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCustomer?.company ?: "Internal Support / General",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCust) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCust,
                            onDismissRequest = { expandedCust = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Internal Support / General (No Customer)") },
                                onClick = {
                                    selectedCustomer = null
                                    expandedCust = false
                                }
                            )
                            allCustomers.forEach { cust ->
                                DropdownMenuItem(
                                    text = { Text(cust.company) },
                                    onClick = {
                                        selectedCustomer = cust
                                        expandedCust = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        val sysType = if (isEmployee && selectedCustomer == null) "INTERNAL" else "EXTERNAL"
                        viewModel.createSupportTicket(
                            subject = subject,
                            category = category,
                            priority = priority,
                            systemType = sysType,
                            customerId = selectedCustomer?.id,
                            customerName = selectedCustomer?.company,
                            employeeId = if (sysType == "INTERNAL") currentSession?.userId else null,
                            employeeName = if (sysType == "INTERNAL") currentSession?.fullName else null
                        ) { success, msg ->
                            if (success) onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateArticleDialog(
    viewModel: InventoryViewModel,
    currentSession: UserSession?,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Troubleshooting") }
    var systemType by remember { mutableStateOf("EXTERNAL") }

    val categories = listOf("Troubleshooting", "HR Rules", "Product Manual", "Payment Policy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Knowledge Base Article", fontWeight = FontWeight.Black, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Article Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content / Guide details") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 10
                )

                Text("Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Audience / Visibility", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = systemType == "EXTERNAL", onClick = { systemType = "EXTERNAL" })
                        Text("External Customers", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = systemType == "INTERNAL", onClick = { systemType = "INTERNAL" })
                        Text("Internal Only", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        viewModel.addKnowledgeArticle(
                            title = title,
                            content = content,
                            category = category,
                            systemType = systemType,
                            createdBy = currentSession?.fullName ?: "Verified Operator"
                        ) { success, _ ->
                            if (success) onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    viewModel: InventoryViewModel,
    ticket: SupportTicket,
    onBack: () -> Unit
) {
    val messages by viewModel.getMessagesForTicket(ticket.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var newMessageText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()

    val canEscalate = currentSession?.role in listOf("OWNER", "ADMIN", "SERVICE_MANAGER")

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        // Ticket Header Info Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlue.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = ticket.ticketNumber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ticket.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    // Escalated Alert Badge
                    if (ticket.isEscalated) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ESCALATED SLA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ticket.subject,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("PORTAL TYPE", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(ticket.systemType, fontSize = 12.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                    }
                    Column {
                        Text("SLA COUNTDOWN", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        val remaining = ticket.slaDeadline - System.currentTimeMillis()
                        if (remaining > 0) {
                            val hours = remaining / (3600 * 1000)
                            Text("$hours Hours Remaining", fontSize = 12.sp, fontWeight = FontWeight.Black, color = SuccessGreen)
                        } else {
                            Text("SLA BREACHED", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Red)
                        }
                    }
                    Column {
                        Text("STATUS", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(ticket.status, fontSize = 12.sp, fontWeight = FontWeight.Black, color = AccentOrange)
                    }
                }

                // Escalation action
                if (canEscalate && !ticket.isEscalated) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val updated = ticket.copy(
                                isEscalated = true,
                                status = "ESCALATED",
                                escalatedToName = "Atiq Faisal Ani"
                            )
                            viewModel.updateSupportTicket(updated) { _, _ -> }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PriorityHigh, contentDescription = "Escalate")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escalate Ticket to Operations Head (SLA Overrule)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Message Thread (Scrollable List)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == (currentSession?.userId ?: -1)
                val isAi = msg.isAiResponse

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .shadow(1.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isMe -> PrimaryBlue
                                isAi -> Color(0xFFEFF6FF) // Light Blue AI
                                else -> Color.White
                            }
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 16.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAi) "COLORJET AI Assistant" else msg.senderName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isMe) Color.White.copy(alpha = 0.8f) else PrimaryBlue
                                )
                                Text(
                                    text = if (isAi) "AI AGENT" else msg.senderRole,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) Color.White.copy(alpha = 0.6f) else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.messageText,
                                fontSize = 13.sp,
                                color = if (isMe) Color.White else TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Send Input Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    placeholder = { Text("Ask AI or reply to thread...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("support_message_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            viewModel.sendSupportMessage(
                                ticketId = ticket.id,
                                messageText = newMessageText,
                                isAiResponse = false
                            ) { success, _ ->
                                if (success) {
                                    newMessageText = ""
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                        .testTag("send_support_msg_button"),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}
