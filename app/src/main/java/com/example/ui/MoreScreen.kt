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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: InventoryViewModel,
    onLogoutClick: () -> Unit
) {
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val ledgerAccounts by viewModel.allLedgerAccounts.collectAsStateWithLifecycle()
    val transactions by viewModel.allLedgerTransactions.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val foreignPurchases by viewModel.allForeignPurchases.collectAsStateWithLifecycle()
    val emiAgreements by viewModel.allEmiAgreements.collectAsStateWithLifecycle()
    val auditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allStockMovements by viewModel.allStockMovements.collectAsStateWithLifecycle()

    var activeSubScreen by remember { mutableStateOf<String?>(null) }

    if (activeSubScreen != null) {
        // Render detailed ERP sub-module
        Box(modifier = Modifier.fillMaxSize()) {
            when (activeSubScreen) {
                "CUSTOMERS" -> CustomersSubScreen(viewModel, allCustomers, onBack = { activeSubScreen = null })
                "INVOICES" -> InvoicesSubScreen(viewModel, allInvoices, allCustomers, onBack = { activeSubScreen = null })
                "PAYMENTS" -> PaymentsSubScreen(viewModel, allPayments, allCustomers, onBack = { activeSubScreen = null })
                "LEDGER" -> LedgerSubScreen(ledgerAccounts, transactions, onBack = { activeSubScreen = null })
                "PRODUCTS_STOCKS" -> ProductsStocksSubScreen(viewModel, allProducts, allStockMovements, suppliers, onBack = { activeSubScreen = null })
                "SUPPLIERS" -> SuppliersSubScreen(viewModel, suppliers, onBack = { activeSubScreen = null })
                "FOREIGN_PURCHASE" -> ForeignPurchaseSubScreen(viewModel, foreignPurchases, suppliers, onBack = { activeSubScreen = null })
                "AGREEMENTS" -> AgreementsSubScreen(viewModel, emiAgreements, allCustomers, onBack = { activeSubScreen = null })
                "AUDIT" -> AuditSubScreen(auditLogs, onBack = { activeSubScreen = null })
                "DIRECTORY" -> DirectorySubScreen(viewModel, users, currentSession, onBack = { activeSubScreen = null })
                "REPORTS" -> ReportsSubScreen(allCustomers, ledgerAccounts, transactions, onBack = { activeSubScreen = null })
                "ATTENDANCE" -> AttendanceSubScreen(viewModel, onBack = { activeSubScreen = null })
                "MESSAGING" -> MessagingSubScreen(viewModel, users, onBack = { activeSubScreen = null })
                "ANNOUNCEMENTS" -> AnnouncementsSubScreen(viewModel, onBack = { activeSubScreen = null })
                "SUPPORT" -> SupportScreen(viewModel, onBack = { activeSubScreen = null })
                "INDUSTRIAL_OPS_DASHBOARD" -> IndustrialOperationsDashboard(viewModel, onClose = { activeSubScreen = null })
                "DRAFT_CENTER" -> DraftCenterScreen(viewModel, onClose = { activeSubScreen = null })
            }
        }
        return
    }

    // Root list of ERP modules
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("COLORJET ERP SYSTEM", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentSession?.fullName?.take(2)?.uppercase() ?: "US",
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentSession?.fullName ?: "Active Operator", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 15.sp)
                            Text("DEPARTMENT: ${currentSession?.role ?: "STAFF"}", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onLogoutClick) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                        }
                    }
                }
            }

            // ERP suite title
            item {
                Text(
                    text = "ENTERPRISE WORKFLOW SUITE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Display & Factory Visibility Settings Card
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val currentThemeMode by ThemePreferences.themeMode.collectAsStateWithLifecycle()
                val isDark = LocalIsDarkMode.current

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                        .testTag("factory_theme_settings_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDark) Color(0xFF1E3A8A) else PrimaryBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = "Theme Mode",
                                        tint = if (isDark) Color(0xFFFFD54F) else PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Factory Visibility & Theme",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isDark) "Active: High-Contrast Dark" else "Active: Standard Light",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Theme Mode Selection Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(AppThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                                Triple(AppThemeMode.DARK, "Dark Mode", Icons.Default.DarkMode),
                                Triple(AppThemeMode.SYSTEM, "System", Icons.Default.BrightnessAuto)
                            ).forEach { (mode, label, icon) ->
                                val isSelected = currentThemeMode == mode
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            ThemePreferences.setThemeMode(context, mode)
                                        }
                                        .testTag("theme_mode_${label.lowercase()}"),
                                    color = if (isSelected) PrimaryBlue else if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else if (isDark) Color(0xFFCBD5E1) else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) Color.White else if (isDark) Color(0xFFCBD5E1) else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Grid Layout of Corporate Modules
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Customers",
                            subtitle = "Statements",
                            icon = Icons.Default.People,
                            color = PrimaryBlue,
                            onClick = { activeSubScreen = "CUSTOMERS" }
                        )
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Sales & Invoices",
                            subtitle = "Post Ledger",
                            icon = Icons.Default.Receipt,
                            color = PrimaryBlue,
                            onClick = { activeSubScreen = "INVOICES" }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Receive Payment",
                            subtitle = "Receipt Vouchers",
                            icon = Icons.Default.Payment,
                            color = SuccessGreen,
                            onClick = { activeSubScreen = "PAYMENTS" }
                        )
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Accounts Ledger",
                            subtitle = "Double Entry Log",
                            icon = Icons.Default.AccountBalance,
                            color = Color(0xFF7C3AED),
                            onClick = { activeSubScreen = "LEDGER" }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Products & Stocks",
                            subtitle = "Warehouse CRUD",
                            icon = Icons.Default.Inventory2,
                            color = Color(0xFF059669),
                            onClick = { activeSubScreen = "PRODUCTS_STOCKS" }
                        )
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Suppliers",
                            subtitle = "Vendors Database",
                            icon = Icons.Default.Business,
                            color = AccentOrange,
                            onClick = { activeSubScreen = "SUPPLIERS" }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "LC & Landed Cost",
                            subtitle = "Duty Allocation",
                            icon = Icons.Default.LocalShipping,
                            color = Color(0xFF0F766E),
                            onClick = { activeSubScreen = "FOREIGN_PURCHASE" }
                        )
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Agreement (EMI)",
                            subtitle = "Legal Bangla/Eng",
                            icon = Icons.Default.Gavel,
                            color = Color(0xFFBE123C),
                            onClick = { activeSubScreen = "AGREEMENTS" }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Reports & Print",
                            subtitle = "Professional PDFs",
                            icon = Icons.Default.Assessment,
                            color = Color(0xFF0369A1),
                            onClick = { activeSubScreen = "REPORTS" }
                        )
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Compliance Log",
                            subtitle = "Audit Trail",
                            icon = Icons.Default.Security,
                            color = Color.DarkGray,
                            onClick = { activeSubScreen = "AUDIT" }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "GPS Attendance",
                            subtitle = "Check In & Out Log",
                            icon = Icons.Default.MyLocation,
                            color = Color(0xFF0EA5E9),
                            onClick = { activeSubScreen = "ATTENDANCE" }
                        )
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Internal Messaging",
                            subtitle = "Peer to Peer Chat",
                            icon = Icons.Default.Chat,
                            color = Color(0xFF10B981),
                            onClick = { activeSubScreen = "MESSAGING" }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "AI Support Centre",
                            subtitle = "Multi-Agent Helpdesk",
                            icon = Icons.Default.SupportAgent,
                            color = PrimaryBlue,
                            onClick = { activeSubScreen = "SUPPORT" }
                        )
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Announcement Hub",
                            subtitle = "Push Alerts Simulation",
                            icon = Icons.Default.NotificationsActive,
                            color = Color(0xFFF59E0B),
                            onClick = { activeSubScreen = "ANNOUNCEMENTS" }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (currentSession?.role == "OWNER" || currentSession?.role == "ADMIN") {
                            ErpModuleTile(
                                modifier = Modifier.weight(1f),
                                title = "Directory & ID Cards",
                                subtitle = "Verified Accounts",
                                icon = Icons.Default.ManageAccounts,
                                color = Color(0xFFE11D48),
                                onClick = { activeSubScreen = "DIRECTORY" }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "Industrial Operations",
                            subtitle = "KPIs & Telemetry",
                            icon = Icons.Default.PrecisionManufacturing,
                            color = PrimaryBlue,
                            onClick = { activeSubScreen = "INDUSTRIAL_OPS_DASHBOARD" }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ErpModuleTile(
                            modifier = Modifier.weight(1f),
                            title = "AI Draft Center",
                            subtitle = "Pending Review & Published",
                            icon = Icons.Default.FactCheck,
                            color = Color(0xFF8B5CF6),
                            onClick = { activeSubScreen = "DRAFT_CENTER" }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // COLORJET Bangladesh Official Footer Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .shadow(1.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "COLORJET Bangladesh",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "Quality • Commitment • Service",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentOrange,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Address",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "44 Purana Paltan, Dhaka-1000, Bangladesh",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Hotline",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hotline: +8809677610610",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp",
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WhatsApp: +8801954100710",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Email: info@colorjet.com.bd",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Website",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Website: https://www.colorjetbd.com",
                                fontSize = 11.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Importer, Distributor & Industrial Digital Printing Solution Provider",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErpModuleTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

// ======================== SUBSIDIARY SCREENS ========================

// 1. CUSTOMERS SUB-SCREEN WITH EXTENSIVE PROFILE, HISTORY, AND EDITING
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersSubScreen(viewModel: InventoryViewModel, customers: List<Customer>, onBack: () -> Unit) {
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val tickets by viewModel.allTickets.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCustomerForDetail by remember { mutableStateOf<Customer?>(null) }
    var showEditDialog by remember { mutableStateOf<Customer?>(null) }
    var showCreateLoginDialogForCustomer by remember { mutableStateOf<Customer?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf("All") } // All, Lead, Dealer, Corporate, Individual
    var selectedFilterStatus by remember { mutableStateOf("All") } // All, Active, Inactive

    // Register Form States
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var customerType by remember { mutableStateOf("Individual") }
    var altPhone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var contactPersonName by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var taxId by remember { mutableStateOf("") }
    var openingBalanceStr by remember { mutableStateOf("") }
    var creditLimitStr by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Active") }

    // Dialog Dropdowns
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    // Alerts
    var alertError by remember { mutableStateOf<String?>(null) }
    var alertSuccess by remember { mutableStateOf<String?>(null) }

    val filteredCustomers = customers.filter { c ->
        val matchesSearch = c.name.contains(searchQuery, ignoreCase = true) ||
                c.company.contains(searchQuery, ignoreCase = true) ||
                c.phone.contains(searchQuery, ignoreCase = true) ||
                c.email.contains(searchQuery, ignoreCase = true) ||
                c.customerCode.contains(searchQuery, ignoreCase = true) ||
                c.district.contains(searchQuery, ignoreCase = true) ||
                c.area.contains(searchQuery, ignoreCase = true) ||
                c.contactPersonName.contains(searchQuery, ignoreCase = true)

        val matchesType = selectedFilterType == "All" || c.customerType.equals(selectedFilterType, ignoreCase = true)
        val matchesStatus = selectedFilterStatus == "All" || c.status.equals(selectedFilterStatus, ignoreCase = true)

        matchesSearch && matchesType && matchesStatus
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CUSTOMERS DIRECTORY", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            // Search and Filters Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, company, code, area...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    // Filter Category Row 1 (Type)
                    Text("CUSTOMER TYPE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Lead", "Dealer", "Corporate", "Individual").forEach { t ->
                            val isSelected = selectedFilterType == t
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                    .clickable { selectedFilterType = t }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(t, color = if (isSelected) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Filter Category Row 2 (Status)
                    Text("REGISTRATION STATUS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Active", "Inactive").forEach { s ->
                            val isSelected = selectedFilterStatus == s
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                    .clickable { selectedFilterStatus = s }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(s, color = if (isSelected) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Client List Area
            if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No customers found matching filters", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers) { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, RoundedCornerShape(12.dp))
                                .clickable { selectedCustomerForDetail = c },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(c.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(c.customerCode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PrimaryBlue.copy(alpha = 0.08f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(c.customerType, color = PrimaryBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "৳${String.format("%,.0f", c.balance)}",
                                        fontWeight = FontWeight.Black,
                                        color = if (c.balance > 0) Color.Red else SuccessGreen,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Company: ${c.company}", fontSize = 12.sp, color = TextPrimary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Phone: ${c.phone}", fontSize = 11.sp, color = TextSecondary)
                                    Text("Area: ${c.area}, ${c.district}", fontSize = 11.sp, color = TextSecondary)
                                }

                                HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Terms: ${c.paymentTerms.ifBlank { "Net cash" }}", fontSize = 11.sp, color = TextSecondary)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (c.status == "Active") SuccessGreen.copy(alpha = 0.12f) else Color.Red.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(c.status, color = if (c.status == "Active") SuccessGreen else Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- ADD CUSTOMER DIALOG ---
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Register New Enterprise Client", fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (alertError != null) {
                            Text(alertError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company / Brand Name *") }, modifier = Modifier.fillMaxWidth())

                        // Type Dropdown
                        Box {
                            OutlinedTextField(
                                value = customerType,
                                onValueChange = {},
                                label = { Text("Customer Category") },
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { typeDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = typeDropdownExpanded, onDismissRequest = { typeDropdownExpanded = false }) {
                                listOf("Individual", "Dealer", "Corporate", "Lead").forEach { typeOption ->
                                    DropdownMenuItem(
                                        text = { Text(typeOption) },
                                        onClick = {
                                            customerType = typeOption
                                            typeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile Number *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = altPhone, onValueChange = { altPhone = it }, label = { Text("Alternative Mobile") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp Link/Number") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Full Business Address") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area / Thana") }, modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = contactPersonName, onValueChange = { contactPersonName = it }, label = { Text("Contact Person Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("Contact Designation") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = taxId, onValueChange = { taxId = it }, label = { Text("NID / VAT BIN / Trade License") }, modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = openingBalanceStr, onValueChange = { openingBalanceStr = it }, label = { Text("Opening Due Balance (BDT)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = creditLimitStr, onValueChange = { creditLimitStr = it }, label = { Text("Credit Limit Amount (BDT)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = paymentTerms, onValueChange = { paymentTerms = it }, label = { Text("Payment Terms (e.g., Net 30)") }, modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Internal Administrative Notes") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isBlank() || company.isBlank() || phone.isBlank()) {
                                alertError = "Please fill in all mandatory (*) fields"
                                return@Button
                            }
                            viewModel.addCustomer(
                                name = name.trim(),
                                company = company.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                address = address.trim(),
                                customerType = customerType,
                                altPhone = altPhone.trim(),
                                whatsapp = whatsapp.trim(),
                                district = district.trim(),
                                area = area.trim(),
                                contactPersonName = contactPersonName.trim(),
                                designation = designation.trim(),
                                taxId = taxId.trim(),
                                openingBalance = openingBalanceStr.toDoubleOrNull() ?: 0.0,
                                creditLimit = creditLimitStr.toDoubleOrNull() ?: 0.0,
                                paymentTerms = paymentTerms.trim(),
                                notes = notes.trim(),
                                status = "Active",
                                onResult = { success, msg ->
                                    if (success) {
                                        showAddDialog = false
                                        alertError = null
                                        // Reset fields
                                        name = ""; company = ""; phone = ""; email = ""; address = ""
                                        altPhone = ""; whatsapp = ""; district = ""; area = ""
                                        contactPersonName = ""; designation = ""; taxId = ""
                                        openingBalanceStr = ""; creditLimitStr = ""; paymentTerms = ""; notes = ""
                                    } else {
                                        alertError = msg
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("PROVISION")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        alertError = null
                    }) { Text("CANCEL") }
                }
            )
        }

        // --- CUSTOMER DETAIL SHEET / DIALOG WITH PORTFOLIO HISTORY ---
        if (selectedCustomerForDetail != null) {
            val c = selectedCustomerForDetail!!
            val customerInvoices = invoices.filter { it.customerId == c.id }
            val customerPayments = payments.filter { it.customerId == c.id }
            val customerTickets = tickets.filter { it.customerId == c.id }

            var selectedTab by remember { mutableStateOf(0) } // 0: Profile, 1: Ledgers & History, 2: Tickets

            AlertDialog(
                onDismissRequest = { selectedCustomerForDetail = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(c.name, fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 16.sp)
                            Text("Code: ${c.customerCode}", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = {
                            showEditDialog = c
                            selectedCustomerForDetail = null
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = PrimaryBlue)
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.heightIn(max = 480.dp)) {
                        // Tabs selector
                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Statements", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Tickets (${customerTickets.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (selectedTab == 0) {
                                // Profile Details
                                ProfileDetailRow("Company Name", c.company)
                                ProfileDetailRow("Customer Category", c.customerType)
                                ProfileDetailRow("Status", c.status)
                                ProfileDetailRow("Primary Phone", c.phone)
                                if (c.altPhone.isNotEmpty()) ProfileDetailRow("Alt Phone", c.altPhone)
                                if (c.whatsapp.isNotEmpty()) ProfileDetailRow("WhatsApp", c.whatsapp)
                                if (c.email.isNotEmpty()) ProfileDetailRow("Email", c.email)
                                ProfileDetailRow("Full Address", c.address)
                                ProfileDetailRow("District / Area", "${c.district} / ${c.area}")
                                if (c.contactPersonName.isNotEmpty()) {
                                    ProfileDetailRow("Contact Person", c.contactPersonName)
                                    ProfileDetailRow("Designation", c.designation)
                                }
                                if (c.taxId.isNotEmpty()) ProfileDetailRow("Tax/BIN/NID", c.taxId)
                                ProfileDetailRow("Credit Limit", "৳${String.format("%,.1f", c.creditLimit)}")
                                ProfileDetailRow("Payment Terms", c.paymentTerms.ifBlank { "Immediate Payment" })
                                if (c.notes.isNotEmpty()) ProfileDetailRow("Internal Notes", c.notes)

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = BackgroundGray)
                                Spacer(modifier = Modifier.height(10.dp))

                                val associatedUser = allUsers.find { it.role == "CUSTOMER" && it.customerId == c.id }
                                if (associatedUser != null) {
                                    Text("Customer Portal Access Active", fontWeight = FontWeight.Black, fontSize = 12.sp, color = SuccessGreen)
                                    ProfileDetailRow("Login Username", associatedUser.username)
                                    ProfileDetailRow("Designated Email", associatedUser.email.ifBlank { "N/A" })
                                    ProfileDetailRow("Designated Phone", associatedUser.phone.ifBlank { "N/A" })
                                    ProfileDetailRow("Login Status", associatedUser.status)
                                } else {
                                    Text("Customer Portal Access Inactive", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                    Button(
                                        onClick = {
                                            showCreateLoginDialogForCustomer = c
                                            selectedCustomerForDetail = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("btn_trigger_create_login")
                                    ) {
                                        Icon(Icons.Default.VpnKey, contentDescription = "Enable Portal", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Create Customer Portal Login", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (selectedTab == 1) {
                                // Ledgers and Invoicing History
                                Text("Invoices Issued", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                                if (customerInvoices.isEmpty()) {
                                    Text("No invoices generated yet.", fontSize = 11.sp, color = TextSecondary)
                                } else {
                                    customerInvoices.forEach { inv ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                                Text(sdf.format(Date(inv.createdAt)), fontSize = 9.sp, color = Color.Gray)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("৳${String.format("%,.0f", inv.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text("Due: ৳${String.format("%,.0f", inv.balance)}", fontSize = 10.sp, color = if (inv.balance > 0) Color.Red else SuccessGreen)
                                            }
                                        }
                                        HorizontalDivider(color = BackgroundGray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Payments Received", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                                if (customerPayments.isEmpty()) {
                                    Text("No payments credited yet.", fontSize = 11.sp, color = TextSecondary)
                                } else {
                                    customerPayments.forEach { pay ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(pay.paymentNumber, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SuccessGreen)
                                                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                                Text(sdf.format(Date(pay.paymentDate)), fontSize = 9.sp, color = Color.Gray)
                                            }
                                            Text("৳${String.format("%,.0f", pay.amount)}", fontWeight = FontWeight.Black, fontSize = 11.sp, color = SuccessGreen)
                                        }
                                        HorizontalDivider(color = BackgroundGray)
                                    }
                                }
                            } else {
                                // Service Tickets
                                Text("Service Tickets History", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                                if (customerTickets.isEmpty()) {
                                    Text("No complaints or tickets posted.", fontSize = 11.sp, color = TextSecondary)
                                } else {
                                    customerTickets.forEach { tk ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = BackgroundGray.copy(alpha = 0.5f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(tk.ticketNumber, fontWeight = FontWeight.Black, fontSize = 11.sp, color = PrimaryBlue)
                                                    Text(tk.status, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = if (tk.status == "RESOLVED") SuccessGreen else AccentOrange)
                                                }
                                                Text("Device: ${tk.deviceModel} (S/N: ${tk.serialNumber})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("Issue: ${tk.issueDescription}", fontSize = 11.sp, color = TextSecondary)
                                                Text("Assigned: ${tk.assignedEngineerName ?: "Unassigned"}", fontSize = 10.sp, color = TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedCustomerForDetail = null }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                        Text("CLOSE")
                    }
                }
            )
        }

        // --- EDIT CUSTOMER DIALOG ---
        if (showEditDialog != null) {
            val original = showEditDialog!!
            var editName by remember { mutableStateOf(original.name) }
            var editCompany by remember { mutableStateOf(original.company) }
            var editPhone by remember { mutableStateOf(original.phone) }
            var editEmail by remember { mutableStateOf(original.email) }
            var editAddress by remember { mutableStateOf(original.address) }
            var editType by remember { mutableStateOf(original.customerType) }
            var editAltPhone by remember { mutableStateOf(original.altPhone) }
            var editWhatsapp by remember { mutableStateOf(original.whatsapp) }
            var editDistrict by remember { mutableStateOf(original.district) }
            var editArea by remember { mutableStateOf(original.area) }
            var editContactPersonName by remember { mutableStateOf(original.contactPersonName) }
            var editDesignation by remember { mutableStateOf(original.designation) }
            var editTaxId by remember { mutableStateOf(original.taxId) }
            var editCreditLimitStr by remember { mutableStateOf(original.creditLimit.toString()) }
            var editPaymentTerms by remember { mutableStateOf(original.paymentTerms) }
            var editNotes by remember { mutableStateOf(original.notes) }
            var editStatus by remember { mutableStateOf(original.status) }

            var editTypeExpanded by remember { mutableStateOf(false) }
            var editStatusExpanded by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showEditDialog = null },
                title = { Text("Edit Client Details", fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Customer Name *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editCompany, onValueChange = { editCompany = it }, label = { Text("Company / Brand Name *") }, modifier = Modifier.fillMaxWidth())

                        // Type Dropdown
                        Box {
                            OutlinedTextField(
                                value = editType,
                                onValueChange = {},
                                label = { Text("Customer Category") },
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { editTypeExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = editTypeExpanded, onDismissRequest = { editTypeExpanded = false }) {
                                listOf("Individual", "Dealer", "Corporate", "Lead").forEach { typeOption ->
                                    DropdownMenuItem(
                                        text = { Text(typeOption) },
                                        onClick = {
                                            editType = typeOption
                                            editTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status Dropdown
                        Box {
                            OutlinedTextField(
                                value = editStatus,
                                onValueChange = {},
                                label = { Text("Client Status") },
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { editStatusExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = editStatusExpanded, onDismissRequest = { editStatusExpanded = false }) {
                                listOf("Active", "Inactive").forEach { statusOption ->
                                    DropdownMenuItem(
                                        text = { Text(statusOption) },
                                        onClick = {
                                            editStatus = statusOption
                                            editStatusExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Mobile Number *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editAltPhone, onValueChange = { editAltPhone = it }, label = { Text("Alternative Mobile") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editWhatsapp, onValueChange = { editWhatsapp = it }, label = { Text("WhatsApp Link/Number") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editAddress, onValueChange = { editAddress = it }, label = { Text("Full Business Address") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editDistrict, onValueChange = { editDistrict = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editArea, onValueChange = { editArea = it }, label = { Text("Area / Thana") }, modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = editContactPersonName, onValueChange = { editContactPersonName = it }, label = { Text("Contact Person Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editDesignation, onValueChange = { editDesignation = it }, label = { Text("Contact Designation") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editTaxId, onValueChange = { editTaxId = it }, label = { Text("NID / VAT BIN / Trade License") }, modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = editCreditLimitStr, onValueChange = { editCreditLimitStr = it }, label = { Text("Credit Limit Amount (BDT)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editPaymentTerms, onValueChange = { editPaymentTerms = it }, label = { Text("Payment Terms") }, modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = editNotes, onValueChange = { editNotes = it }, label = { Text("Notes") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editName.isBlank() || editCompany.isBlank() || editPhone.isBlank()) {
                                return@Button
                            }
                            val updated = original.copy(
                                name = editName.trim(),
                                company = editCompany.trim(),
                                phone = editPhone.trim(),
                                email = editEmail.trim(),
                                address = editAddress.trim(),
                                customerType = editType,
                                altPhone = editAltPhone.trim(),
                                whatsapp = editWhatsapp.trim(),
                                district = editDistrict.trim(),
                                area = editArea.trim(),
                                contactPersonName = editContactPersonName.trim(),
                                designation = editDesignation.trim(),
                                taxId = editTaxId.trim(),
                                creditLimit = editCreditLimitStr.toDoubleOrNull() ?: original.creditLimit,
                                paymentTerms = editPaymentTerms.trim(),
                                notes = editNotes.trim(),
                                status = editStatus
                            )
                            viewModel.updateCustomer(updated) { success, _ ->
                                if (success) {
                                    showEditDialog = null
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("SAVE CHANGES")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = null }) { Text("CANCEL") }
                }
            )
        }

        // --- CREATE CUSTOMER LOGIN DIALOG ---
        if (showCreateLoginDialogForCustomer != null) {
            val customer = showCreateLoginDialogForCustomer!!
            var loginUsername by remember { mutableStateOf(customer.customerCode.lowercase()) }
            var loginPasscode by remember { mutableStateOf("1234") }
            var loginFullName by remember { mutableStateOf(customer.contactPersonName.ifBlank { customer.name }) }
            var loginPhone by remember { mutableStateOf(customer.phone) }
            var loginEmail by remember { mutableStateOf(customer.email) }
            
            var errMessage by remember { mutableStateOf<String?>(null) }
            
            AlertDialog(
                onDismissRequest = { showCreateLoginDialogForCustomer = null },
                title = { Text("Create Customer Portal Login", fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Create secure login credentials for customer representative.", fontSize = 11.sp, color = TextSecondary)
                        
                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = { loginUsername = it },
                            label = { Text("Portal Username *") },
                            modifier = Modifier.fillMaxWidth().testTag("admin_input_portal_username"),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        
                        OutlinedTextField(
                            value = loginPasscode,
                            onValueChange = { loginPasscode = it },
                            label = { Text("Temporary PIN Passcode *") },
                            modifier = Modifier.fillMaxWidth().testTag("admin_input_portal_passcode"),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        
                        OutlinedTextField(
                            value = loginFullName,
                            onValueChange = { loginFullName = it },
                            label = { Text("Representative Full Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        
                        OutlinedTextField(
                            value = loginPhone,
                            onValueChange = { loginPhone = it },
                            label = { Text("Verified Phone Contact") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Verified Email Contact") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        
                        errMessage?.let { err ->
                            Text(err, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (loginUsername.isBlank() || loginPasscode.isBlank() || loginFullName.isBlank()) {
                                errMessage = "Please complete all mandatory fields (*)"
                                return@Button
                            }
                            viewModel.createCustomerLogin(
                                customerId = customer.id,
                                username = loginUsername,
                                pinCode = loginPasscode,
                                fullName = loginFullName,
                                phone = loginPhone,
                                email = loginEmail
                            ) { success, msg ->
                                if (success) {
                                    showCreateLoginDialogForCustomer = null
                                } else {
                                    errMessage = msg
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("admin_btn_submit_portal_login")
                    ) {
                        Text("GENERATE LOGIN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateLoginDialogForCustomer = null }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
        Text(value.ifBlank { "N/A" }, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(top = 4.dp))
    }
}

// 2. COMPREHENSIVE SALES SUITE (INVOICES, QUOTATIONS, AND SALES ORDERS)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesSubScreen(
    viewModel: InventoryViewModel,
    invoices: List<Invoice>,
    customers: List<Customer>,
    onBack: () -> Unit
) {
    val quotations by viewModel.allQuotations.collectAsStateWithLifecycle()
    val salesOrders by viewModel.allSalesOrders.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Invoices, 1: Quotations, 2: Sales Orders

    // Dialog flags
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var showQuotationDialog by remember { mutableStateOf(false) }
    var showSalesOrderDialog by remember { mutableStateOf(false) }

    // Detail view flags
    var selectedInvoiceForDetail by remember { mutableStateOf<Invoice?>(null) }
    var selectedQuotationForDetail by remember { mutableStateOf<Quotation?>(null) }
    var selectedSalesOrderForDetail by remember { mutableStateOf<SalesOrder?>(null) }

    // Universal search query
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "SALES INVOICES"
                            1 -> "SALES QUOTATIONS"
                            else -> "SALES ORDERS"
                        },
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            when (selectedTab) {
                                0 -> showInvoiceDialog = true
                                1 -> showQuotationDialog = true
                                2 -> showSalesOrderDialog = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Document", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Invoices", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Quotations", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Sales Orders", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
            }

            // Universal Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by customer, document code...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Content Areas based on Tab
            when (selectedTab) {
                0 -> {
                    // INVOICES LIST
                    val filtered = invoices.filter {
                        it.customerName.contains(searchQuery, ignoreCase = true) ||
                                it.invoiceNumber.contains(searchQuery, ignoreCase = true)
                    }
                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No invoices recorded yet.", color = TextSecondary, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered) { inv ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(1.dp, RoundedCornerShape(12.dp))
                                        .clickable { selectedInvoiceForDetail = inv },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(inv.invoiceNumber, fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 13.sp)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (inv.status == "PAID" || inv.balance <= 0) SuccessGreen.copy(alpha = 0.1f) else AccentOrange.copy(alpha = 0.1f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                val textStatus = if (inv.balance <= 0) "PAID" else inv.status
                                                Text(textStatus, color = if (textStatus == "PAID") SuccessGreen else AccentOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(inv.customerName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Total Amount: ৳${String.format("%,.0f", inv.totalAmount)}", fontSize = 12.sp, color = TextSecondary)
                                            Text("Due: ৳${String.format("%,.0f", inv.balance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (inv.balance > 0) Color.Red else SuccessGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // QUOTATIONS LIST
                    val filtered = quotations.filter {
                        it.customerName.contains(searchQuery, ignoreCase = true) ||
                                it.quotationNumber.contains(searchQuery, ignoreCase = true)
                    }
                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No quotations recorded yet.", color = TextSecondary, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered) { q ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(1.dp, RoundedCornerShape(12.dp))
                                        .clickable { selectedQuotationForDetail = q },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(q.quotationNumber, fontWeight = FontWeight.Black, color = Color(0xFF7C3AED), fontSize = 13.sp)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PrimaryBlue.copy(alpha = 0.1f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(q.status, color = PrimaryBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(q.customerName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                        Text("Total Proposal Amount: ৳${String.format("%,.0f", q.totalAmount)}", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // SALES ORDERS LIST
                    val filtered = salesOrders.filter {
                        it.customerName.contains(searchQuery, ignoreCase = true) ||
                                it.salesOrderNumber.contains(searchQuery, ignoreCase = true)
                    }
                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No active sales orders.", color = TextSecondary, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered) { so ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(1.dp, RoundedCornerShape(12.dp))
                                        .clickable { selectedSalesOrderForDetail = so },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(so.salesOrderNumber, fontWeight = FontWeight.Black, color = SuccessGreen, fontSize = 13.sp)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(SuccessGreen.copy(alpha = 0.1f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(so.status, color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(so.customerName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                        Text("Value BDT: ৳${String.format("%,.0f", so.totalAmount)}", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 1. POST INVOICE FORM (MULTI-ITEM SELECTION) ---
        if (showInvoiceDialog) {
            var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
            val invoiceItems = remember { mutableStateListOf<TransactionItem>() }

            // Sub-item selector states
            var selectedProd by remember { mutableStateOf<Product?>(null) }
            var itemQty by remember { mutableStateOf("1") }
            var itemRate by remember { mutableStateOf("") }
            var itemDiscount by remember { mutableStateOf("0") }

            var discountAmount by remember { mutableStateOf("0") }
            var vatAmount by remember { mutableStateOf("0") }
            var deliveryCharge by remember { mutableStateOf("0") }
            var installationCharge by remember { mutableStateOf("0") }
            var serviceCharge by remember { mutableStateOf("0") }
            var paidAmount by remember { mutableStateOf("0") }
            var paymentMethod by remember { mutableStateOf("CASH") }
            var salesperson by remember { mutableStateOf("") }
            var notes by remember { mutableStateOf("") }

            var custDropdownExp by remember { mutableStateOf(false) }
            var prodDropdownExp by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showInvoiceDialog = false },
                title = { Text("Generate Sales Invoice", fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Customer dropdown
                        Box {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "Select Client *",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { custDropdownExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = custDropdownExp, onDismissRequest = { custDropdownExp = false }) {
                                customers.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.name} (${c.company})") },
                                        onClick = {
                                            selectedCustomer = c
                                            custDropdownExp = false
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))
                        Text("LINE ITEMS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)

                        // Currently added items list
                        if (invoiceItems.isEmpty()) {
                            Text("No items added yet.", fontSize = 11.sp, color = Color.Red)
                        } else {
                            invoiceItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("${item.qty} ${item.unit} @ ৳${item.rate} (Disc: ৳${item.discount})", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Text("৳${String.format("%,.0f", item.total)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { invoiceItems.remove(item) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Add new item sub-form
                        Box(
                            modifier = Modifier
                                .background(BackgroundGray.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Add Line Item:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                Box {
                                    OutlinedTextField(
                                        value = selectedProd?.name ?: "Select Product...",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { IconButton(onClick = { prodDropdownExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    DropdownMenu(expanded = prodDropdownExp, onDismissRequest = { prodDropdownExp = false }) {
                                        products.forEach { p ->
                                            DropdownMenuItem(
                                                text = { Text("${p.name} (Stock: ${p.stockLevel})") },
                                                onClick = {
                                                    selectedProd = p
                                                    itemRate = p.sellingPrice.toString()
                                                    prodDropdownExp = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(value = itemQty, onValueChange = { itemQty = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = itemRate, onValueChange = { itemRate = it }, label = { Text("Rate (BDT)") }, modifier = Modifier.weight(2f))
                                    OutlinedTextField(value = itemDiscount, onValueChange = { itemDiscount = it }, label = { Text("Disc") }, modifier = Modifier.weight(1.5f))
                                }

                                Button(
                                    onClick = {
                                        val p = selectedProd
                                        val qty = itemQty.toIntOrNull() ?: 1
                                        val rate = itemRate.toDoubleOrNull() ?: 0.0
                                        val disc = itemDiscount.toDoubleOrNull() ?: 0.0
                                        if (p != null && qty > 0 && rate >= 0) {
                                            val newItem = TransactionItem(
                                                productId = p.id,
                                                name = p.name,
                                                qty = qty,
                                                unit = p.unit,
                                                rate = rate,
                                                discount = disc
                                            )
                                            invoiceItems.add(newItem)
                                            selectedProd = null
                                            itemQty = "1"
                                            itemRate = ""
                                            itemDiscount = "0"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Add Product", fontSize = 11.sp)
                                }
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))

                        // Charge / Discount details
                        OutlinedTextField(value = discountAmount, onValueChange = { discountAmount = it }, label = { Text("Invoice Discount (BDT)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = vatAmount, onValueChange = { vatAmount = it }, label = { Text("VAT Amount (BDT)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = deliveryCharge, onValueChange = { deliveryCharge = it }, label = { Text("Delivery Charge") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = installationCharge, onValueChange = { installationCharge = it }, label = { Text("Installation Charge") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = serviceCharge, onValueChange = { serviceCharge = it }, label = { Text("Service Charge") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = paidAmount, onValueChange = { paidAmount = it }, label = { Text("Paid Amount BDT (Downpayment)") }, modifier = Modifier.fillMaxWidth())

                        Column {
                            Text("Downpayment Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("CASH", "BANK_TRANSFER").forEach { mode ->
                                    val isSelected = paymentMethod == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                            .clickable { paymentMethod = mode }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(mode, color = if (isSelected) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(value = salesperson, onValueChange = { salesperson = it }, label = { Text("Sales Person Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Invoice Notes") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cust = selectedCustomer
                            if (cust != null && invoiceItems.isNotEmpty()) {
                                viewModel.postInvoice(
                                    customerId = cust.id,
                                    customerName = cust.name,
                                    items = invoiceItems.toList(),
                                    discountAmount = discountAmount.toDoubleOrNull() ?: 0.0,
                                    vatAmount = vatAmount.toDoubleOrNull() ?: 0.0,
                                    deliveryCharge = deliveryCharge.toDoubleOrNull() ?: 0.0,
                                    installationCharge = installationCharge.toDoubleOrNull() ?: 0.0,
                                    serviceCharge = serviceCharge.toDoubleOrNull() ?: 0.0,
                                    paidAmount = paidAmount.toDoubleOrNull() ?: 0.0,
                                    paymentMethod = paymentMethod,
                                    salesperson = salesperson,
                                    notes = notes
                                )
                                showInvoiceDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("POST INVOICE")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInvoiceDialog = false }) { Text("CANCEL") }
                }
            )
        }

        // --- 2. CREATE QUOTATION FORM ---
        if (showQuotationDialog) {
            var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
            val quotationItems = remember { mutableStateListOf<TransactionItem>() }

            var selectedProd by remember { mutableStateOf<Product?>(null) }
            var itemQty by remember { mutableStateOf("1") }
            var itemRate by remember { mutableStateOf("") }
            var itemDiscount by remember { mutableStateOf("0") }

            var discountAmount by remember { mutableStateOf("0") }
            var vatAmount by remember { mutableStateOf("0") }
            var deliveryCharge by remember { mutableStateOf("0") }
            var installationCharge by remember { mutableStateOf("0") }
            var specialTerms by remember { mutableStateOf("") }
            var salesPerson by remember { mutableStateOf("") }

            var custDropdownExp by remember { mutableStateOf(false) }
            var prodDropdownExp by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showQuotationDialog = false },
                title = { Text("Generate Sales Proposal (Quotation)", fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "Select Client *",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { custDropdownExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = custDropdownExp, onDismissRequest = { custDropdownExp = false }) {
                                customers.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.name} (${c.company})") },
                                        onClick = {
                                            selectedCustomer = c
                                            custDropdownExp = false
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))
                        Text("PROPOSED ITEMS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)

                        if (quotationItems.isEmpty()) {
                            Text("No items added yet.", fontSize = 11.sp, color = Color.Red)
                        } else {
                            quotationItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("${item.qty} ${item.unit} @ ৳${item.rate} (Disc: ৳${item.discount})", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Text("৳${String.format("%,.0f", item.total)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { quotationItems.remove(item) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.background(BackgroundGray.copy(alpha = 0.5f)).padding(8.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Add Line Item:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                Box {
                                    OutlinedTextField(
                                        value = selectedProd?.name ?: "Select Product...",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { IconButton(onClick = { prodDropdownExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    DropdownMenu(expanded = prodDropdownExp, onDismissRequest = { prodDropdownExp = false }) {
                                        products.forEach { p ->
                                            DropdownMenuItem(
                                                text = { Text(p.name) },
                                                onClick = {
                                                    selectedProd = p
                                                    itemRate = p.sellingPrice.toString()
                                                    prodDropdownExp = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(value = itemQty, onValueChange = { itemQty = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = itemRate, onValueChange = { itemRate = it }, label = { Text("Rate (BDT)") }, modifier = Modifier.weight(2f))
                                    OutlinedTextField(value = itemDiscount, onValueChange = { itemDiscount = it }, label = { Text("Disc") }, modifier = Modifier.weight(1.5f))
                                }

                                Button(
                                    onClick = {
                                        val p = selectedProd
                                        val qty = itemQty.toIntOrNull() ?: 1
                                        val rate = itemRate.toDoubleOrNull() ?: 0.0
                                        val disc = itemDiscount.toDoubleOrNull() ?: 0.0
                                        if (p != null && qty > 0 && rate >= 0) {
                                            val newItem = TransactionItem(
                                                productId = p.id,
                                                name = p.name,
                                                qty = qty,
                                                unit = p.unit,
                                                rate = rate,
                                                discount = disc
                                            )
                                            quotationItems.add(newItem)
                                            selectedProd = null
                                            itemQty = "1"
                                            itemRate = ""
                                            itemDiscount = "0"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Add Product", fontSize = 11.sp)
                                }
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))

                        OutlinedTextField(value = discountAmount, onValueChange = { discountAmount = it }, label = { Text("Discount (BDT)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = vatAmount, onValueChange = { vatAmount = it }, label = { Text("VAT Amount") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = deliveryCharge, onValueChange = { deliveryCharge = it }, label = { Text("Delivery Charge") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = installationCharge, onValueChange = { installationCharge = it }, label = { Text("Installation Charge") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = salesPerson, onValueChange = { salesPerson = it }, label = { Text("Sales Person Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = specialTerms, onValueChange = { specialTerms = it }, label = { Text("Validity & Special Terms") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cust = selectedCustomer
                            if (cust != null && quotationItems.isNotEmpty()) {
                                viewModel.addQuotation(
                                    customerId = cust.id,
                                    customerName = cust.name,
                                    validityDate = System.currentTimeMillis() + 30L * 24 * 3600 * 1000,
                                    items = quotationItems.toList(),
                                    discountAmount = discountAmount.toDoubleOrNull() ?: 0.0,
                                    vatAmount = vatAmount.toDoubleOrNull() ?: 0.0,
                                    deliveryCharge = deliveryCharge.toDoubleOrNull() ?: 0.0,
                                    installationCharge = installationCharge.toDoubleOrNull() ?: 0.0,
                                    specialTerms = specialTerms,
                                    salesPerson = salesPerson
                                )
                                showQuotationDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("SAVE PROPOSAL")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuotationDialog = false }) { Text("CANCEL") }
                }
            )
        }

        // --- 3. CREATE SALES ORDER FORM ---
        if (showSalesOrderDialog) {
            var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
            val orderItems = remember { mutableStateListOf<TransactionItem>() }

            var selectedProd by remember { mutableStateOf<Product?>(null) }
            var itemQty by remember { mutableStateOf("1") }
            var itemRate by remember { mutableStateOf("") }
            var itemDiscount by remember { mutableStateOf("0") }

            var discountAmount by remember { mutableStateOf("0") }
            var vatAmount by remember { mutableStateOf("0") }
            var deliveryCharge by remember { mutableStateOf("0") }
            var installationCharge by remember { mutableStateOf("0") }
            var salesPerson by remember { mutableStateOf("") }
            var notes by remember { mutableStateOf("") }

            var custDropdownExp by remember { mutableStateOf(false) }
            var prodDropdownExp by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showSalesOrderDialog = false },
                title = { Text("Generate Confirmed Sales Order", fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "Select Client *",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { custDropdownExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = custDropdownExp, onDismissRequest = { custDropdownExp = false }) {
                                customers.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.name} (${c.company})") },
                                        onClick = {
                                            selectedCustomer = c
                                            custDropdownExp = false
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))
                        Text("ORDER ITEMS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)

                        if (orderItems.isEmpty()) {
                            Text("No items added yet.", fontSize = 11.sp, color = Color.Red)
                        } else {
                            orderItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("${item.qty} ${item.unit} @ ৳${item.rate} (Disc: ৳${item.discount})", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Text("৳${String.format("%,.0f", item.total)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { orderItems.remove(item) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.background(BackgroundGray.copy(alpha = 0.5f)).padding(8.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Add Line Item:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                Box {
                                    OutlinedTextField(
                                        value = selectedProd?.name ?: "Select Product...",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { IconButton(onClick = { prodDropdownExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    DropdownMenu(expanded = prodDropdownExp, onDismissRequest = { prodDropdownExp = false }) {
                                        products.forEach { p ->
                                            DropdownMenuItem(
                                                text = { Text(p.name) },
                                                onClick = {
                                                    selectedProd = p
                                                    itemRate = p.sellingPrice.toString()
                                                    prodDropdownExp = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(value = itemQty, onValueChange = { itemQty = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = itemRate, onValueChange = { itemRate = it }, label = { Text("Rate (BDT)") }, modifier = Modifier.weight(2f))
                                    OutlinedTextField(value = itemDiscount, onValueChange = { itemDiscount = it }, label = { Text("Disc") }, modifier = Modifier.weight(1.5f))
                                }

                                Button(
                                    onClick = {
                                        val p = selectedProd
                                        val qty = itemQty.toIntOrNull() ?: 1
                                        val rate = itemRate.toDoubleOrNull() ?: 0.0
                                        val disc = itemDiscount.toDoubleOrNull() ?: 0.0
                                        if (p != null && qty > 0 && rate >= 0) {
                                            val newItem = TransactionItem(
                                                productId = p.id,
                                                name = p.name,
                                                qty = qty,
                                                unit = p.unit,
                                                rate = rate,
                                                discount = disc
                                            )
                                            orderItems.add(newItem)
                                            selectedProd = null
                                            itemQty = "1"
                                            itemRate = ""
                                            itemDiscount = "0"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Add Product", fontSize = 11.sp)
                                }
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))

                        OutlinedTextField(value = discountAmount, onValueChange = { discountAmount = it }, label = { Text("Discount (BDT)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = vatAmount, onValueChange = { vatAmount = it }, label = { Text("VAT (BDT)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = deliveryCharge, onValueChange = { deliveryCharge = it }, label = { Text("Delivery Charge") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = installationCharge, onValueChange = { installationCharge = it }, label = { Text("Installation Charge") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = salesPerson, onValueChange = { salesPerson = it }, label = { Text("Sales Person") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Sales Order Notes / Ref") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cust = selectedCustomer
                            if (cust != null && orderItems.isNotEmpty()) {
                                viewModel.addSalesOrder(
                                    customerId = cust.id,
                                    customerName = cust.name,
                                    items = orderItems.toList(),
                                    discountAmount = discountAmount.toDoubleOrNull() ?: 0.0,
                                    vatAmount = vatAmount.toDoubleOrNull() ?: 0.0,
                                    deliveryCharge = deliveryCharge.toDoubleOrNull() ?: 0.0,
                                    installationCharge = installationCharge.toDoubleOrNull() ?: 0.0,
                                    salesPerson = salesPerson,
                                    notes = notes
                                )
                                showSalesOrderDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("SAVE SALES ORDER")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSalesOrderDialog = false }) { Text("CANCEL") }
                }
            )
        }

        // --- 4. DETAILS DIALOGS WITH ACTIONABLE CONVERSIONS ---
        if (selectedInvoiceForDetail != null) {
            val inv = selectedInvoiceForDetail!!
            val itemsList = TransactionItem.deserializeList(inv.itemsJson)
            AlertDialog(
                onDismissRequest = { selectedInvoiceForDetail = null },
                title = { Text(inv.invoiceNumber, fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Client Name: ${inv.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Invoice Status: ${inv.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (inv.status == "PAID") SuccessGreen else AccentOrange)
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        Text("Date Generated: ${sdf.format(Date(inv.createdAt))}", fontSize = 11.sp, color = Color.Gray)

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))
                        Text("ITEMS SOLD:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)

                        itemsList.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("${item.qty} ${item.unit} x ৳${String.format("%,.0f", item.rate)}", fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount:", fontSize = 11.sp, color = TextSecondary)
                            Text("৳${String.format("%,.0f", inv.discountAmount)}", fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VAT Amount:", fontSize = 11.sp, color = TextSecondary)
                            Text("৳${String.format("%,.0f", inv.vatAmount)}", fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Charges (Deliv/Inst/Serv):", fontSize = 11.sp, color = TextSecondary)
                            val sumCharges = inv.deliveryCharge + inv.installationCharge + inv.serviceCharge
                            Text("৳${String.format("%,.0f", sumCharges)}", fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Invoice Total:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("৳${String.format("%,.0f", inv.totalAmount)}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = PrimaryBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid:", fontSize = 11.sp, color = TextSecondary)
                            Text("৳${String.format("%,.0f", inv.paidAmount)}", fontSize = 11.sp, color = SuccessGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Balance Due:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Red)
                            Text("৳${String.format("%,.0f", inv.balance)}", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Red)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedInvoiceForDetail = null }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                        Text("CLOSE")
                    }
                }
            )
        }

        if (selectedQuotationForDetail != null) {
            val q = selectedQuotationForDetail!!
            val itemsList = TransactionItem.deserializeList(q.itemsJson)
            AlertDialog(
                onDismissRequest = { selectedQuotationForDetail = null },
                title = { Text(q.quotationNumber, fontWeight = FontWeight.Black, color = Color(0xFF7C3AED), fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Proposal Client: ${q.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Status: ${q.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))
                        Text("ITEMS PROPOSED:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)

                        itemsList.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("${item.qty} ${item.unit} x ৳${String.format("%,.0f", item.rate)}", fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Proposal Total:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("৳${String.format("%,.0f", q.totalAmount)}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFF7C3AED))
                        }
                        if (q.specialTerms.isNotEmpty()) {
                            Text("Validity & Terms: ${q.specialTerms}", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.addSalesOrder(
                                    customerId = q.customerId,
                                    customerName = q.customerName,
                                    items = itemsList,
                                    discountAmount = q.discountAmount,
                                    vatAmount = q.vatAmount,
                                    deliveryCharge = q.deliveryCharge,
                                    installationCharge = q.installationCharge,
                                    salesPerson = q.salesPerson,
                                    notes = "Converted from quotation ${q.quotationNumber}"
                                )
                                selectedQuotationForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CONVERT SO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.postInvoice(
                                    customerId = q.customerId,
                                    customerName = q.customerName,
                                    items = itemsList,
                                    discountAmount = q.discountAmount,
                                    vatAmount = q.vatAmount,
                                    deliveryCharge = q.deliveryCharge,
                                    installationCharge = q.installationCharge,
                                    serviceCharge = 0.0,
                                    paidAmount = 0.0,
                                    paymentMethod = "CASH",
                                    salesperson = q.salesPerson,
                                    notes = "Converted from quotation ${q.quotationNumber}"
                                )
                                selectedQuotationForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CONVERT INV", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = { selectedQuotationForDetail = null }) {
                            Text("CLOSE", fontSize = 11.sp)
                        }
                    }
                }
            )
        }

        if (selectedSalesOrderForDetail != null) {
            val so = selectedSalesOrderForDetail!!
            val itemsList = TransactionItem.deserializeList(so.itemsJson)
            AlertDialog(
                onDismissRequest = { selectedSalesOrderForDetail = null },
                title = { Text(so.salesOrderNumber, fontWeight = FontWeight.Black, color = SuccessGreen, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Client: ${so.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Order Status: ${so.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))
                        Text("ORDERED ITEMS:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)

                        itemsList.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("${item.qty} ${item.unit} x ৳${String.format("%,.0f", item.rate)}", fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order Total:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("৳${String.format("%,.0f", so.totalAmount)}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = SuccessGreen)
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.postInvoice(
                                    customerId = so.customerId,
                                    customerName = so.customerName,
                                    items = itemsList,
                                    discountAmount = so.discountAmount,
                                    vatAmount = so.vatAmount,
                                    deliveryCharge = so.deliveryCharge,
                                    installationCharge = so.installationCharge,
                                    serviceCharge = 0.0,
                                    paidAmount = 0.0,
                                    paymentMethod = "CASH",
                                    salesperson = so.salesPerson,
                                    notes = "Converted from Sales Order ${so.salesOrderNumber}"
                                )
                                selectedSalesOrderForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("CONVERT TO INVOICE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = { selectedSalesOrderForDetail = null }, modifier = Modifier.weight(0.8f)) {
                            Text("CLOSE", fontSize = 11.sp)
                        }
                    }
                }
            )
        }
    }
}


// 3. PAYMENTS (RECEIPTS) SUB-SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsSubScreen(
    viewModel: InventoryViewModel,
    payments: List<Payment>,
    customers: List<Customer>,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }

    var expandedDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PAYMENT RECEIPTS", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Receive Payment", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundGray),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(payments) { pay ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(pay.paymentNumber, fontWeight = FontWeight.Black, color = SuccessGreen, fontSize = 13.sp)
                            Text("৳${String.format("%,.0f", pay.amount)}", fontWeight = FontWeight.Black, color = SuccessGreen, fontSize = 15.sp)
                        }
                        Text(pay.customerName, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Method: ${pay.paymentMethod} • Cashier: ${pay.postedBy}", fontSize = 11.sp, color = TextSecondary)
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        Text("Date: ${sdf.format(Date(pay.paymentDate))}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Receive Customer Payment", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                        ExposedDropdownMenuBox(expanded = expandedDropdown, onExpandedChange = { expandedDropdown = !expandedDropdown }) {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "Select Customer *",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                                customers.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.name} (Bal: ৳${c.balance})") },
                                        onClick = {
                                            selectedCustomer = c
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Received Amount (BDT) *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Payment method
                        Column {
                            Text("Payment Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf("CASH", "BANK_TRANSFER", "BKASH").forEach { m ->
                                    val isSelected = paymentMethod == m
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                            .clickable { paymentMethod = m }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(m.replace("_", " "), color = if (isSelected) Color.White else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amtVal = amount.toDoubleOrNull() ?: 0.0
                            if (selectedCustomer != null && amtVal > 0.0) {
                                viewModel.postPayment(
                                    invoiceId = null,
                                    customerId = selectedCustomer!!.id,
                                    customerName = selectedCustomer!!.name,
                                    amount = amtVal,
                                    paymentMethod = paymentMethod
                                )
                                showDialog = false
                                selectedCustomer = null; amount = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("RECEIVE VOUCHER")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("CANCEL") }
                }
            )
        }
    }
}

// 4. ACCOUNTS LEDGER SUB-SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerSubScreen(
    accounts: List<LedgerAccount>,
    transactions: List<LedgerTransaction>,
    onBack: () -> Unit
) {
    var selectedTabState by remember { mutableStateOf(0) } // 0: Balances, 1: Journal Entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DOUBLE-ENTRY LEDGER", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundGray)) {
            TabRow(selectedTabIndex = selectedTabState, containerColor = Color.White, contentColor = PrimaryBlue) {
                Tab(selected = selectedTabState == 0, onClick = { selectedTabState = 0 }, text = { Text("COA Balances", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTabState == 1, onClick = { selectedTabState = 1 }, text = { Text("Journal Entries", fontWeight = FontWeight.Bold) })
            }

            if (selectedTabState == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(accounts) { acc ->
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(acc.accountName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Account Type: ${acc.type}", fontSize = 11.sp, color = TextSecondary)
                                }
                                Text("৳${String.format("%,.1f", acc.balance)}", fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 15.sp)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions) { tx ->
                        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Ref: ${tx.reference}", fontWeight = FontWeight.Black, color = Color.Gray, fontSize = 11.sp)
                                    Text(sdf.format(Date(tx.date)), fontSize = 10.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text("DEBIT: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                                    Text(tx.debitAccountName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Row {
                                    Text("CREDIT: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Red)
                                    Text(tx.creditAccountName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Narration: ${tx.narration}", fontSize = 11.sp, color = TextSecondary)
                                Text("Amount: ৳${String.format("%,.1f", tx.amount)}", fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. SUPPLIERS SUB-SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersSubScreen(viewModel: InventoryViewModel, suppliers: List<Supplier>, onBack: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VENDORS / SUPPLIERS", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Supplier", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundGray),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(suppliers) { s ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(s.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Contact Person: ${s.contactPerson} • Phone: ${s.phone}", fontSize = 12.sp, color = TextSecondary)
                        Text("Origin Country: ${s.country}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add International Vendor", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vendor Name *") })
                        OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Person") })
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
                        OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country of Origin") })
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addSupplier(name.trim(), contact.trim(), phone.trim(), country.trim())
                                showDialog = false
                                name = ""; contact = ""; phone = ""; country = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("ADD VENDOR")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("CANCEL") }
                }
            )
        }
    }
}

// 6. FOREIGN PURCHASE & LANDED COST SUB-SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForeignPurchaseSubScreen(
    viewModel: InventoryViewModel,
    purchases: List<ForeignPurchase>,
    suppliersList: List<Supplier>,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    var lcNumber by remember { mutableStateOf("") }
    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
    var productDesc by remember { mutableStateOf("") }
    var fobUsd by remember { mutableStateOf("") }
    var exchangeRate by remember { mutableStateOf("118.5") }
    var freight by remember { mutableStateOf("") }
    var clearing by remember { mutableStateOf("") }
    var customsDuty by remember { mutableStateOf("") }
    var insurance by remember { mutableStateOf("") }
    var inland by remember { mutableStateOf("") }

    var expandedDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LC & LANDED COST CALCULATOR", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Purchase LC", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundGray),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(purchases) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("LC: ${p.lcNumber}", fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(p.status, color = PrimaryBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(p.productDescription, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Supplier: ${p.supplierName}", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("FOB Value: $${String.format("%,.2f", p.fobValueUsd)}", fontSize = 12.sp, color = TextSecondary)
                            Text("Total Landed BDT:", fontSize = 12.sp, color = TextSecondary)
                        }
                        Text("৳${String.format("%,.0f", p.totalLandedCostBdt)}", fontWeight = FontWeight.Black, color = SuccessGreen, fontSize = 15.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Calculate Landed Cost BDT", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                        OutlinedTextField(value = lcNumber, onValueChange = { lcNumber = it }, label = { Text("LC / TT Reference Number *") })

                        ExposedDropdownMenuBox(expanded = expandedDropdown, onExpandedChange = { expandedDropdown = !expandedDropdown }) {
                            OutlinedTextField(
                                value = selectedSupplier?.name ?: "Select Supplier *",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                                suppliersList.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text("${s.name} (${s.country})") },
                                        onClick = {
                                            selectedSupplier = s
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(value = productDesc, onValueChange = { productDesc = it }, label = { Text("Consumable/Printer Description *") })
                        OutlinedTextField(value = fobUsd, onValueChange = { fobUsd = it }, label = { Text("FOB Invoice Value (USD) *") })
                        OutlinedTextField(value = exchangeRate, onValueChange = { exchangeRate = it }, label = { Text("Exchange Rate (BDT/USD) *") })
                        OutlinedTextField(value = freight, onValueChange = { freight = it }, label = { Text("Freight Charges BDT") })
                        OutlinedTextField(value = clearing, onValueChange = { clearing = it }, label = { Text("Clearing Agent Fees BDT") })
                        OutlinedTextField(value = customsDuty, onValueChange = { customsDuty = it }, label = { Text("Customs Duty Paid BDT") })
                        OutlinedTextField(value = insurance, onValueChange = { insurance = it }, label = { Text("Marine Insurance BDT") })
                        OutlinedTextField(value = inland, onValueChange = { inland = it }, label = { Text("Inland Transport BDT") })
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val fobVal = fobUsd.toDoubleOrNull() ?: 0.0
                            val rateVal = exchangeRate.toDoubleOrNull() ?: 118.5
                            if (lcNumber.isNotBlank() && selectedSupplier != null && fobVal > 0.0) {
                                viewModel.addForeignPurchase(
                                    lcNumber = lcNumber.trim(),
                                    supplierId = selectedSupplier!!.id,
                                    supplierName = selectedSupplier!!.name,
                                    productDescription = productDesc.trim(),
                                    fobValueUsd = fobVal,
                                    exchangeRate = rateVal,
                                    freightChargesBdt = freight.toDoubleOrNull() ?: 0.0,
                                    clearingAgentBdt = clearing.toDoubleOrNull() ?: 0.0,
                                    customsDutyBdt = customsDuty.toDoubleOrNull() ?: 0.0,
                                    insuranceBdt = insurance.toDoubleOrNull() ?: 0.0,
                                    inlandTransportBdt = inland.toDoubleOrNull() ?: 0.0,
                                    status = "COMPLETED"
                                )
                                showDialog = false
                                lcNumber = ""; selectedSupplier = null; productDesc = ""; fobUsd = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("ALLOCATE & POST")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("CANCEL") }
                }
            )
        }
    }
}

// 7. HIRE-PURCHASE AGREEMENT GENERATOR (EMI) SUB-SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgreementsSubScreen(
    viewModel: InventoryViewModel,
    agreements: List<EmiAgreement>,
    customers: List<Customer>,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var previewAgreementText by remember { mutableStateOf<String?>(null) }

    var selectedCust by remember { mutableStateOf<Customer?>(null) }
    var productName by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var downPayment by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("10.0") }
    var emiCount by remember { mutableStateOf("12") }
    var language by remember { mutableStateOf("ENGLISH") } // ENGLISH or BANGLA

    var expandedDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HIRE PURCHASE CONTRACTS", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Contract", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundGray),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(agreements) { ag ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(ag.agreementNumber, fontWeight = FontWeight.Black, color = PrimaryBlue, fontSize = 13.sp)
                            Text("EMI: ${ag.emiCount} Months", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentOrange)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ag.customerName, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Product: ${ag.productName} (S/N: ${ag.serialNumber})", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Cash Price: ৳${String.format("%,.0f", ag.totalAmount)}", fontSize = 12.sp, color = TextSecondary)
                            Text("Monthly EMI: ৳${String.format("%,.0f", ag.emiAmount)} / mo", fontWeight = FontWeight.Black, color = SuccessGreen, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Establish EMI Schedule", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                        ExposedDropdownMenuBox(expanded = expandedDropdown, onExpandedChange = { expandedDropdown = !expandedDropdown }) {
                            OutlinedTextField(
                                value = selectedCust?.name ?: "Select Client *",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                                customers.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.name) },
                                        onClick = {
                                            selectedCust = c
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Digital Printer Model *") })
                        OutlinedTextField(value = serialNumber, onValueChange = { serialNumber = it }, label = { Text("Chassis Serial Number *") })
                        OutlinedTextField(value = totalAmount, onValueChange = { totalAmount = it }, label = { Text("Cash Principal Cost (BDT) *") })
                        OutlinedTextField(value = downPayment, onValueChange = { downPayment = it }, label = { Text("Initial Downpayment (BDT)") })
                        OutlinedTextField(value = interestRate, onValueChange = { interestRate = it }, label = { Text("Yearly Interest Rate (%)") })
                        OutlinedTextField(value = emiCount, onValueChange = { emiCount = it }, label = { Text("EMI Duration (Months)") })

                        // Language Toggle
                        Column {
                            Text("Agreement Language", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("ENGLISH", "BANGLA").forEach { lang ->
                                    val isSelected = language == lang
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                            .clickable { language = lang }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lang, color = if (isSelected) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val totalVal = totalAmount.toDoubleOrNull() ?: 0.0
                            val downVal = downPayment.toDoubleOrNull() ?: 0.0
                            val rateVal = interestRate.toDoubleOrNull() ?: 10.0
                            val countVal = emiCount.toIntOrNull() ?: 12

                            if (selectedCust != null && totalVal > 0.0) {
                                val remaining = totalVal - downVal
                                val emiWithInterest = remaining * (1 + (rateVal / 100.0))
                                val monthlyEmi = emiWithInterest / countVal

                                // Build Contract Preview
                                val preview = if (language == "BANGLA") {
                                    """
                                    ভাড়া-ক্রয় কিস্তি এবং মালিকানা চুক্তিপত্র
                                    চুক্তি নং: AGR-GEN-${System.currentTimeMillis() % 100000}
                                    মালিক পক্ষ: কালারজেট বাংলাদেশ, ঢাকা।
                                    ভাড়া-গ্রহীতা পক্ষ: ${selectedCust!!.name}
                                    
                                    বিবরণী:
                                    কালারজেট প্রিন্টার মডেল: $productName
                                    সিরিয়াল নম্বর: $serialNumber
                                    মোট বাজার মূল্য: ৳${String.format("%,.0f", totalVal)}
                                    প্রদত্ত ডাউনপেমেন্ট: ৳${String.format("%,.0f", downVal)}
                                    মোট পরিশোধযোগ্য কিস্তি: $countVal টি
                                    মাসিক কিস্তির পরিমাণ: ৳${String.format("%,.0f", monthlyEmi)}
                                    
                                    শর্তাবলী:
                                    ১. সমুদয় মূল্য পরিশোধ না করা পর্যন্ত পণ্যের মালিকানা কালারজেট বাংলাদেশের থাকিবে।
                                    ২. ভাড়া-গ্রহীতা কিস্তি পরিশোধে ব্যর্থ হইলে মালিকপক্ষ যন্ত্রটি ফেরত নেওয়ার অধিকার রাখেন।
                                    
                                    স্বাক্ষরকারীগণ কালারজেট বাংলাদেশ এবং ভাড়া-গ্রহীতার পক্ষে চুক্তিটি সম্পাদন করিলেন।
                                    """.trimIndent()
                                } else {
                                    """
                                    HIRE PURCHASE AGREEMENT (EMI CONTRACT)
                                    Ref No: AGR-GEN-${System.currentTimeMillis() % 100000}
                                    Owner: COLORJET Bangladesh, Dhaka.
                                    Hirer: ${selectedCust!!.name}
                                    
                                    Specifications:
                                    ColorJet Printer: $productName
                                    Chassis Serial No: $serialNumber
                                    Total Value: BDT ${String.format("%,.0f", totalVal)}
                                    Initial Downpayment: BDT ${String.format("%,.0f", downVal)}
                                    No. of Installments: $countVal Months
                                    Monthly EMI Amount: BDT ${String.format("%,.0f", monthlyEmi)}
                                    
                                    Terms & Conditions:
                                    1. Title and ownership of the machinery remains with COLORJET Bangladesh until full settlement is cleared.
                                    2. Failure to pay monthly installments authorizes the owner to reclaim the equipment.
                                    
                                    Agreed and signed by both parties.
                                    """.trimIndent()
                                }

                                previewAgreementText = preview
                                showDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("PREVIEW CONTRACT")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("CANCEL") }
                }
            )
        }

        // Show Contract Preview Dialog (allowing full edit / save)
        if (previewAgreementText != null) {
            var editedText by remember { mutableStateOf(previewAgreementText!!) }
            AlertDialog(
                onDismissRequest = { previewAgreementText = null },
                title = { Text("Contract PDF Generation", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
                text = {
                    OutlinedTextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        minLines = 10
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val totalVal = totalAmount.toDoubleOrNull() ?: 0.0
                            val downVal = downPayment.toDoubleOrNull() ?: 0.0
                            val rateVal = interestRate.toDoubleOrNull() ?: 10.0
                            val countVal = emiCount.toIntOrNull() ?: 12
                            val monthlyEmi = ((totalVal - downVal) * (1 + (rateVal / 100.0))) / countVal

                            viewModel.addEmiAgreement(
                                customerId = selectedCust!!.id,
                                customerName = selectedCust!!.name,
                                productName = productName,
                                serialNumber = serialNumber,
                                totalAmount = totalVal,
                                downPayment = downVal,
                                interestRate = rateVal,
                                emiCount = countVal,
                                emiAmount = monthlyEmi,
                                language = language,
                                termsTemplate = editedText
                            )

                            previewAgreementText = null
                            selectedCust = null; productName = ""; serialNumber = ""; totalAmount = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text("SIGN & POST TO ERP")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { previewAgreementText = null }) { Text("EDIT") }
                }
            )
        }
    }
}

// 8. AUDIT LEDGER SUB-SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditSubScreen(logs: List<AuditLog>, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filteredLogs = logs.filter {
        it.details.contains(query, ignoreCase = true) || 
        it.action.contains(query, ignoreCase = true) || 
        it.username.contains(query, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SYSTEM AUDIT COMPLIANCE", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundGray)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search Audit Trails...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color.White, RoundedCornerShape(12.dp))
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLogs) { log ->
                    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(log.action, color = PrimaryBlue, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                                Text(sdf.format(Date(log.timestamp)), fontSize = 10.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(log.details, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Posted by operator: @${log.username}", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// 9. DIRECTORY (USERS MANAGEMENT) SUB-SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectorySubScreen(
    viewModel: InventoryViewModel,
    users: List<User>,
    currentSession: UserSession?,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedUserForIdCard by remember { mutableStateOf<User?>(null) }
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STAFF") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("COMPANY USER DIRECTORY", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Employee", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundGray),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users) { u ->
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
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
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = u.fullName.split(" ").filter { it.isNotEmpty() }.take(2).map { it.first().uppercase() }.joinToString(""),
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(u.fullName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                    if (u.designation.isNotEmpty()) {
                                        Text(u.designation, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(u.role, color = PrimaryBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                if (u.blood.isNotEmpty() && u.blood != "N/A") {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Red.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("🩸 ${u.blood}", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        
                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (u.department.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Business, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Dept: ${u.department}", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                    }
                                }
                                if (u.phone.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(u.phone, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
                                if (u.email.isNotEmpty() && u.email != "N/A") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(u.email, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(u.status, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 12.dp))

                        Button(
                            onClick = { selectedUserForIdCard = u },
                            modifier = Modifier.fillMaxWidth().testTag("verify_id_card_${u.username}"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VERIFY PHYSICAL ID CARD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Register Employee Profile", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username *") })
                        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name *") })
                        OutlinedTextField(value = pinCode, onValueChange = { pinCode = it.filter { char -> char.isDigit() } }, label = { Text("Secret Login PIN *") })
                        
                        // Select Role
                        Column {
                            Text("Enterprise Role", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("STAFF", "ADMIN", "OWNER").forEach { r ->
                                    val isSelected = role == r
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                            .clickable { role = r }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(r, color = if (isSelected) Color.White else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (username.isNotBlank() && fullName.isNotBlank() && pinCode.isNotBlank()) {
                                viewModel.addUser(username.trim().lowercase(), fullName.trim(), pinCode.trim(), role)
                                showDialog = false
                                username = ""; fullName = ""; pinCode = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("PROVISION ACCOUNT")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("CANCEL") }
                }
            )
        }

        selectedUserForIdCard?.let { user ->
            AlertDialog(
                onDismissRequest = { selectedUserForIdCard = null },
                confirmButton = {
                    Button(
                        onClick = { selectedUserForIdCard = null },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("CLOSE VERIFICATION")
                    }
                },
                text = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, PrimaryBlue)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Professional Corporate ID Card Header
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PrimaryBlue)
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "COLORJET BANGLADESH",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "OFFICIAL IDENTIFICATION CARD",
                                    color = AccentOrange,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Avatar Badging
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(BackgroundGray)
                                    .border(3.dp, AccentOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.fullName.split(" ").filter { it.isNotEmpty() }.take(2).map { it.first().uppercase() }.joinToString(""),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Employee Name and Title
                            Text(
                                text = user.fullName.uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = user.designation.ifEmpty { "ERP User Account" }.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange,
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // ID details block
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .background(BackgroundGray, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("EMPLOYEE ID:", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                    Text("CJB-${user.id.toString().padStart(4, '0')}", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Black)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ROLE / DEPT:", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                    Text("${user.role} / ${user.department.ifEmpty { "OPERATIONS" }}", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Black)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("BLOOD GROUP:", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                    Text(user.blood.ifEmpty { "O+" }, fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Black)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("CONTACT NO:", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                    Text(user.phone.ifEmpty { "+880 1954100710" }, fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Barcode Simulation / QR Simulation
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.QrCode2,
                                    contentDescription = "Barcode",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "AUTHORIZED SECURITY VERIFICATION",
                                    fontSize = 8.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

// 10. PROFESSIONAL ERP STATEMENT REPORTS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsSubScreen(
    customers: List<Customer>,
    accounts: List<LedgerAccount>,
    transactions: List<LedgerTransaction>,
    onBack: () -> Unit
) {
    var activeReportType by remember { mutableStateOf("TRIAL_BALANCE") } // "TRIAL_BALANCE" or "CUSTOMER_STATEMENT"
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FINANCIAL AUDIT REPORTS", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundGray)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { activeReportType = "TRIAL_BALANCE" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeReportType == "TRIAL_BALANCE") PrimaryBlue else Color.LightGray.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("COA Balances", color = if (activeReportType == "TRIAL_BALANCE") Color.White else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { activeReportType = "CUSTOMER_STATEMENT" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeReportType == "CUSTOMER_STATEMENT") PrimaryBlue else Color.LightGray.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Client Statements", color = if (activeReportType == "CUSTOMER_STATEMENT") Color.White else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (activeReportType == "CUSTOMER_STATEMENT") {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    ExposedDropdownMenuBox(expanded = expandedDropdown, onExpandedChange = { expandedDropdown = !expandedDropdown }) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: "Select Client Statement...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth().background(Color.White)
                        )
                        ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                            customers.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedCustomer = c
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Print Preview Box
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp).shadow(3.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                    Text("COLORJET BANGLADESH ERP REPORTING SYSTEM", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                    Text("Quality • Commitment • Service", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text("Date generated: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}", fontSize = 9.sp, color = Color.Gray)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black)

                    if (activeReportType == "TRIAL_BALANCE") {
                        Text("TRIAL BALANCE / CHART OF ACCOUNT STATUS", fontWeight = FontWeight.Black, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var totalDebit = 0.0
                        var totalCredit = 0.0

                        accounts.forEach { acc ->
                            val isDebitType = acc.type == "ASSET" || acc.type == "EXPENSE"
                            if (isDebitType) totalDebit += acc.balance else totalCredit += acc.balance

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(acc.accountName, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text(
                                    text = if (isDebitType) "৳${String.format("%,.1f", acc.balance)}" else "",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(100.dp),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = if (!isDebitType) "৳${String.format("%,.1f", acc.balance)}" else "",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(100.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("LEDGER EQUILIBRIUM TOTAL:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("৳${String.format("%,.1f", totalDebit)}", fontWeight = FontWeight.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = SuccessGreen, modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                            Text("৳${String.format("%,.1f", totalCredit)}", fontWeight = FontWeight.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = SuccessGreen, modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                        }
                    } else {
                        if (selectedCustomer == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Please select a customer to view their statement of accounts.", color = TextSecondary, fontSize = 13.sp)
                            }
                        } else {
                            val c = selectedCustomer!!
                            Text("CUSTOMER ACCOUNT STATEMENT", fontWeight = FontWeight.Black, fontSize = 14.sp, color = TextPrimary)
                            Text("Client Name: ${c.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Company: ${c.company} • Address: ${c.address}", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Show customer ledgers
                            Text("Ledger Postings:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            val customerTransactions = transactions.filter { it.narration.contains(c.name, ignoreCase = true) }

                            if (customerTransactions.isEmpty()) {
                                Text("No transactions posted for this customer in general ledger.", fontSize = 12.sp, color = TextSecondary)
                            } else {
                                customerTransactions.forEach { tx ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tx.narration, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            Text("Ref: ${tx.reference}", fontSize = 9.sp, color = Color.Gray)
                                        }
                                        Text("৳${String.format("%,.0f", tx.amount)}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("NET OUTSTANDING STATEMENT BALANCE:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Red)
                                Text("৳${String.format("%,.1f", c.balance)}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 11. PRODUCTS & STOCK MANAGEMENT SUB-SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsStocksSubScreen(
    viewModel: InventoryViewModel,
    products: List<Product>,
    stockMovements: List<StockMovement>,
    suppliers: List<Supplier>,
    onBack: () -> Unit
) {
    var selectedTabState by remember { mutableStateOf(0) } // 0 = Catalog, 1 = Stock Movements, 2 = Warehouses
    val tabsList = listOf("Products Catalog", "Stock Movements", "Warehouses")

    // Search and filters
    var productSearchQuery by remember { mutableStateOf("") }
    var selectedProductCategory by remember { mutableStateOf("All") }
    val productCategories = listOf("All", "Printing machine", "Ink", "Consumable", "Spare parts", "Service charge")

    // Filtered Products
    val filteredProductList = remember(products, productSearchQuery, selectedProductCategory) {
        products.filter { p ->
            val matchesQuery = p.name.contains(productSearchQuery, ignoreCase = true) ||
                    p.sku.contains(productSearchQuery, ignoreCase = true) ||
                    p.model.contains(productSearchQuery, ignoreCase = true) ||
                    p.brand.contains(productSearchQuery, ignoreCase = true)
            val matchesCategory = selectedProductCategory == "All" || p.category == selectedProductCategory
            matchesQuery && matchesCategory
        }
    }

    // Detail & Dialog triggers
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showEditProductDialog by remember { mutableStateOf<Product?>(null) }
    var showAddStockMovementDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products & Stock Ledger", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddStockMovementDialog = true },
                        modifier = Modifier.testTag("add_stock_movement_button")
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = "Post Stock Action", tint = Color.White)
                    }
                    IconButton(
                        onClick = { showAddProductDialog = true },
                        modifier = Modifier.testTag("add_product_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Product", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundGray)
        ) {
            // Tab Selector Row
            TabRow(
                selectedTabIndex = selectedTabState,
                containerColor = Color.White,
                contentColor = PrimaryBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabState]),
                        color = PrimaryBlue
                    )
                }
            ) {
                tabsList.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTabState == index,
                        onClick = { selectedTabState = index },
                        text = { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
            }

            // Main Active Panel
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTabState) {
                    0 -> ProductsCatalogTab(
                        productsList = filteredProductList,
                        searchQuery = productSearchQuery,
                        onSearchChange = { productSearchQuery = it },
                        selectedCategory = selectedProductCategory,
                        onCategorySelect = { selectedProductCategory = it },
                        categories = productCategories,
                        onProductClick = { selectedProductForDetail = it }
                    )
                    1 -> StockMovementsTab(
                        stockMovements = stockMovements,
                        products = products
                    )
                    2 -> WarehousesTab(
                        products = products,
                        stockMovements = stockMovements
                    )
                }
            }
        }
    }

    // --- DIALOGS & OVERLAYS ---

    // 1. PRODUCT DETAIL DIALOG
    if (selectedProductForDetail != null) {
        val p = selectedProductForDetail!!
        val productMovements = remember(stockMovements, p.id) {
            stockMovements.filter { it.productId == p.id }
        }

        AlertDialog(
            onDismissRequest = { selectedProductForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(p.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                        Text("SKU: ${p.sku} • Model: ${p.model}", fontSize = 11.sp, color = TextSecondary)
                    }
                    IconButton(onClick = {
                        showEditProductDialog = p
                        selectedProductForDetail = null
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = PrimaryBlue)
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("FINANCIALS", fontWeight = FontWeight.Black, fontSize = 10.sp, color = AccentOrange, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Purchase Price", fontSize = 10.sp, color = TextSecondary)
                                        Text("৳${String.format("%,.1f", p.purchasePrice)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column {
                                        Text("Landed Cost", fontSize = 10.sp, color = TextSecondary)
                                        Text("৳${String.format("%,.1f", p.landedCost)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                                    }
                                    Column {
                                        Text("Selling Price", fontSize = 10.sp, color = TextSecondary)
                                        Text("৳${String.format("%,.1f", p.sellingPrice)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SuccessGreen)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Dealer Price", fontSize = 10.sp, color = TextSecondary)
                                        Text("৳${String.format("%,.1f", p.dealerPrice)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Column {
                                        Text("Min Sale Price", fontSize = 10.sp, color = TextSecondary)
                                        Text("৳${String.format("%,.1f", p.minSalePrice)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Column {
                                        Text("Warranty", fontSize = 10.sp, color = TextSecondary)
                                        Text(p.warrantyPeriod.ifEmpty { "None" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("CURRENT STOCK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${p.stockLevel} ${p.unit}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = if (p.stockLevel <= p.reorderLevel) Color.Red else SuccessGreen
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = BackgroundGray),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("REORDER LIMIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${p.reorderLevel} ${p.unit}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("PRODUCT SPECIFICATION", fontWeight = FontWeight.Black, fontSize = 10.sp, color = TextSecondary)
                            Text("• Category: ${p.category} / ${p.subCategory.ifEmpty { "General" }}", fontSize = 12.sp)
                            Text("• Brand: ${p.brand.ifEmpty { "COLORJET" }}", fontSize = 12.sp)
                            Text("• Barcode: ${p.barcode.ifEmpty { "No Barcode" }}", fontSize = 12.sp)
                            Text("• Serialization: ${if (p.isSerialized) "YES (Tracked)" else "NO"}", fontSize = 12.sp)
                            if (p.description.isNotEmpty()) {
                                Text("• Description: ${p.description}", fontSize = 12.sp)
                            }
                            if (p.techSpecs.isNotEmpty()) {
                                Text("• Tech Specs: ${p.techSpecs}", fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("STOCK LEDGER ENTRIES (${productMovements.size})", fontWeight = FontWeight.Black, fontSize = 11.sp, color = PrimaryBlue)
                    }

                    if (productMovements.isEmpty()) {
                        item {
                            Text("No stock transactions logged for this product.", fontSize = 12.sp, color = TextSecondary, style = androidx.compose.ui.text.TextStyle(fontStyle = FontStyle.Italic))
                        }
                    } else {
                        items(productMovements) { mov ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = mov.transactionType,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = when (mov.transactionType) {
                                                "Stock-In", "Opening Stock", "Purchase Receive", "Customer Return" -> SuccessGreen
                                                "Stock-Out", "Invoice Sales", "Supplier Return", "Damaged" -> Color.Red
                                                else -> AccentOrange
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(${mov.warehouse})", fontSize = 9.sp, color = TextSecondary)
                                    }
                                    Text(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(mov.timestamp)), fontSize = 10.sp, color = TextSecondary)
                                    if (mov.notes.isNotEmpty()) {
                                        Text(mov.notes, fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (mov.qtyIn > 0) "+${mov.qtyIn}" else "-${mov.qtyOut}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = if (mov.qtyIn > 0) SuccessGreen else Color.Red
                                    )
                                    Text("Bal: ${mov.balance}", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedProductForDetail = null }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
        )
    }

    // 2. ADD PRODUCT DIALOG
    if (showAddProductDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Printing machine") }
        var subCategory by remember { mutableStateOf("") }
        var brand by remember { mutableStateOf("COLORJET") }
        var model by remember { mutableStateOf("") }
        var sku by remember { mutableStateOf("") }
        var barcode by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("pcs") }
        var purchasePrice by remember { mutableStateOf("") }
        var landedCost by remember { mutableStateOf("") }
        var sellingPrice by remember { mutableStateOf("") }
        var dealerPrice by remember { mutableStateOf("") }
        var minSalePrice by remember { mutableStateOf("") }
        var warrantyPeriod by remember { mutableStateOf("") }
        var reorderLevel by remember { mutableStateOf("5") }
        var description by remember { mutableStateOf("") }
        var techSpecs by remember { mutableStateOf("") }
        var isSerialized by remember { mutableStateOf(false) }
        var openingStock by remember { mutableStateOf("0") }

        var feedbackMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Add Corporate Product", fontWeight = FontWeight.Black) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        if (feedbackMessage.isNotEmpty()) {
                            Text(feedbackMessage, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Product Title / Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU / Unique ID Code *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = brand,
                                onValueChange = { brand = it },
                                label = { Text("Brand") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = model,
                                onValueChange = { model = it },
                                label = { Text("Model No.") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Text("Category Selection", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            productCategories.filter { it != "All" }.forEach { cat ->
                                val isSel = category == cat
                                FilterChip(
                                    selected = isSel,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = subCategory,
                                onValueChange = { subCategory = it },
                                label = { Text("Sub-Category") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit (pcs/liter/roll)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = purchasePrice,
                                onValueChange = { purchasePrice = it },
                                label = { Text("Purchase (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = landedCost,
                                onValueChange = { landedCost = it },
                                label = { Text("Landed Cost (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sellingPrice,
                                onValueChange = { sellingPrice = it },
                                label = { Text("Selling (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = dealerPrice,
                                onValueChange = { dealerPrice = it },
                                label = { Text("Dealer (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = minSalePrice,
                                onValueChange = { minSalePrice = it },
                                label = { Text("Min Sale (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = reorderLevel,
                                onValueChange = { reorderLevel = it },
                                label = { Text("Reorder limit") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = openingStock,
                                onValueChange = { openingStock = it },
                                label = { Text("Opening Stock") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = warrantyPeriod,
                                onValueChange = { warrantyPeriod = it },
                                label = { Text("Warranty (e.g. 1 Year)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Short Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = techSpecs,
                            onValueChange = { techSpecs = it },
                            label = { Text("Technical Specifications") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isSerialized, onCheckedChange = { isSerialized = it })
                            Text("Track by individual serial numbers", fontSize = 12.sp)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("CANCEL")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || sku.isBlank()) {
                            feedbackMessage = "Product Name and SKU are mandatory!"
                            return@Button
                        }

                        viewModel.addProduct(
                            name = name,
                            category = category,
                            subCategory = subCategory,
                            brand = brand,
                            model = model,
                            sku = sku,
                            barcode = barcode,
                            unit = unit,
                            purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                            landedCost = landedCost.toDoubleOrNull() ?: 0.0,
                            sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                            dealerPrice = dealerPrice.toDoubleOrNull() ?: 0.0,
                            minSalePrice = minSalePrice.toDoubleOrNull() ?: 0.0,
                            warrantyPeriod = warrantyPeriod,
                            reorderLevel = reorderLevel.toIntOrNull() ?: 5,
                            description = description,
                            techSpecs = techSpecs,
                            isSerialized = isSerialized,
                            openingStock = openingStock.toIntOrNull() ?: 0,
                            onResult = { success, msg ->
                                if (success) {
                                    showAddProductDialog = false
                                } else {
                                    feedbackMessage = msg
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("SAVE PRODUCT")
                }
            }
        )
    }

    // 3. EDIT PRODUCT DIALOG
    if (showEditProductDialog != null) {
        val original = showEditProductDialog!!
        var name by remember { mutableStateOf(original.name) }
        var category by remember { mutableStateOf(original.category) }
        var subCategory by remember { mutableStateOf(original.subCategory) }
        var brand by remember { mutableStateOf(original.brand) }
        var model by remember { mutableStateOf(original.model) }
        var sku by remember { mutableStateOf(original.sku) }
        var barcode by remember { mutableStateOf(original.barcode) }
        var unit by remember { mutableStateOf(original.unit) }
        var purchasePrice by remember { mutableStateOf(original.purchasePrice.toString()) }
        var landedCost by remember { mutableStateOf(original.landedCost.toString()) }
        var sellingPrice by remember { mutableStateOf(original.sellingPrice.toString()) }
        var dealerPrice by remember { mutableStateOf(original.dealerPrice.toString()) }
        var minSalePrice by remember { mutableStateOf(original.minSalePrice.toString()) }
        var warrantyPeriod by remember { mutableStateOf(original.warrantyPeriod) }
        var reorderLevel by remember { mutableStateOf(original.reorderLevel.toString()) }
        var description by remember { mutableStateOf(original.description) }
        var techSpecs by remember { mutableStateOf(original.techSpecs) }
        var status by remember { mutableStateOf(original.status) }
        var isSerialized by remember { mutableStateOf(original.isSerialized) }

        var feedbackMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEditProductDialog = null },
            title = { Text("Edit Product: ${original.name}", fontWeight = FontWeight.Black) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        if (feedbackMessage.isNotEmpty()) {
                            Text(feedbackMessage, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Product Title / Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = brand,
                                onValueChange = { brand = it },
                                label = { Text("Brand") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = model,
                                onValueChange = { model = it },
                                label = { Text("Model No.") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Text("Category", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            productCategories.filter { it != "All" }.forEach { cat ->
                                val isSel = category == cat
                                FilterChip(
                                    selected = isSel,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = subCategory,
                                onValueChange = { subCategory = it },
                                label = { Text("Sub-Category") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = purchasePrice,
                                onValueChange = { purchasePrice = it },
                                label = { Text("Purchase Price (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = landedCost,
                                onValueChange = { landedCost = it },
                                label = { Text("Landed Cost (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sellingPrice,
                                onValueChange = { sellingPrice = it },
                                label = { Text("Selling Price (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = dealerPrice,
                                onValueChange = { dealerPrice = it },
                                label = { Text("Dealer Price (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = minSalePrice,
                                onValueChange = { minSalePrice = it },
                                label = { Text("Min Sale (৳)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = reorderLevel,
                                onValueChange = { reorderLevel = it },
                                label = { Text("Reorder limit") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = { status = it },
                                label = { Text("Status (Active/Inactive)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = warrantyPeriod,
                                onValueChange = { warrantyPeriod = it },
                                label = { Text("Warranty") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = techSpecs,
                            onValueChange = { techSpecs = it },
                            label = { Text("Tech Specs") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProductDialog = null }) {
                    Text("CANCEL")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            feedbackMessage = "Product Name is required!"
                            return@Button
                        }

                        viewModel.updateProduct(
                            original.copy(
                                name = name,
                                category = category,
                                subCategory = subCategory,
                                brand = brand,
                                model = model,
                                unit = unit,
                                purchasePrice = purchasePrice.toDoubleOrNull() ?: original.purchasePrice,
                                landedCost = landedCost.toDoubleOrNull() ?: original.landedCost,
                                sellingPrice = sellingPrice.toDoubleOrNull() ?: original.sellingPrice,
                                dealerPrice = dealerPrice.toDoubleOrNull() ?: original.dealerPrice,
                                minSalePrice = minSalePrice.toDoubleOrNull() ?: original.minSalePrice,
                                warrantyPeriod = warrantyPeriod,
                                reorderLevel = reorderLevel.toIntOrNull() ?: original.reorderLevel,
                                description = description,
                                techSpecs = techSpecs,
                                status = status,
                                isSerialized = isSerialized
                            ),
                            onResult = { success, msg ->
                                if (success) {
                                    showEditProductDialog = null
                                } else {
                                    feedbackMessage = msg
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("SAVE CHANGES")
                }
            }
        )
    }

    // 4. ADD STOCK MOVEMENT (ADJUSTMENT/TRANSACTION) DIALOG
    if (showAddStockMovementDialog) {
        var selectedProduct by remember { mutableStateOf<Product?>(null) }
        var dropdownExpanded by remember { mutableStateOf(false) }

        var transactionType by remember { mutableStateOf("Stock-In") } // Stock-In, Stock-Out, Adjustment, Transfer, Damaged
        val typesList = listOf("Stock-In", "Stock-Out", "Adjustment", "Transfer", "Damaged", "Customer Return", "Supplier Return")

        var qtyString by remember { mutableStateOf("") }
        var unitCostString by remember { mutableStateOf("") }
        var warehouse by remember { mutableStateOf("Main Warehouse") }
        val warehousesList = listOf("Main Warehouse", "Mirpur Depot", "Chittagong Showroom", "Uttara Branch")

        var relatedEntityName by remember { mutableStateOf("") }
        var referenceNumber by remember { mutableStateOf("TX-${System.currentTimeMillis() % 100000}") }
        var serialNumbers by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        var feedbackMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddStockMovementDialog = false },
            title = { Text("Post Corporate Stock Action", fontWeight = FontWeight.Black) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        if (feedbackMessage.isNotEmpty()) {
                            Text(feedbackMessage, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // SEARCHABLE SELECT PRODUCT DROP DOWN
                    item {
                        Text("Target Corporate Product *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = selectedProduct?.let { "${it.sku} - ${it.name}" } ?: "CHOOSE PRODUCT...",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f).height(240.dp)
                            ) {
                                products.forEach { prod ->
                                    DropdownMenuItem(
                                        text = { Text("${prod.sku} | ${prod.name} (${prod.stockLevel} ${prod.unit})", fontSize = 12.sp) },
                                        onClick = {
                                            selectedProduct = prod
                                            unitCostString = prod.landedCost.toString()
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("Transaction Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            typesList.forEach { type ->
                                val isSelected = transactionType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { transactionType = type },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Text("Target Warehouse Location", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            warehousesList.forEach { wh ->
                                val isSelected = warehouse == wh
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { warehouse = wh },
                                    label = { Text(wh, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = qtyString,
                                onValueChange = { qtyString = it },
                                label = { Text("Quantity *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = unitCostString,
                                onValueChange = { unitCostString = it },
                                label = { Text("Unit Cost (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = referenceNumber,
                            onValueChange = { referenceNumber = it },
                            label = { Text("Reference / Voucher ID *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = relatedEntityName,
                            onValueChange = { relatedEntityName = it },
                            label = { Text("Related Party (Supplier/Client)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = serialNumbers,
                            onValueChange = { serialNumbers = it },
                            label = { Text("Serial Numbers (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Audit Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStockMovementDialog = false }) {
                    Text("CANCEL")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prod = selectedProduct
                        if (prod == null) {
                            feedbackMessage = "Please select a target product!"
                            return@Button
                        }
                        val qty = qtyString.toIntOrNull() ?: 0
                        if (qty <= 0) {
                            feedbackMessage = "Quantity must be greater than zero!"
                            return@Button
                        }
                        val cost = unitCostString.toDoubleOrNull() ?: prod.landedCost

                        // Determine qtyIn / qtyOut based on type
                        val isStockIn = transactionType in listOf("Stock-In", "Opening Stock", "Purchase Receive", "Customer Return")
                        val qtyIn = if (isStockIn) qty else 0
                        val qtyOut = if (!isStockIn) qty else 0

                        viewModel.addStockMovement(
                            referenceNumber = referenceNumber,
                            transactionType = transactionType,
                            productId = prod.id,
                            qtyIn = qtyIn,
                            qtyOut = qtyOut,
                            unitCost = cost,
                            warehouse = warehouse,
                            relatedEntityId = null,
                            relatedEntityName = relatedEntityName,
                            notes = notes,
                            serialNumbers = serialNumbers,
                            onResult = { success, msg ->
                                if (success) {
                                    showAddStockMovementDialog = false
                                } else {
                                    feedbackMessage = msg
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("POST TRANSACTION")
                }
            }
        )
    }
}

// --- TABS IMPLEMENTATIONS ---

// Tab 1: CATALOG OF PRODUCTS
@Composable
fun ProductsCatalogTab(
    productsList: List<Product>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    categories: List<String>,
    onProductClick: (Product) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Categories Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search product name, SKU, brand...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth().testTag("product_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BackgroundGray,
                    unfocusedContainerColor = BackgroundGray,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Category horizontally scrollable chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(cat) },
                        label = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Product Cards List
        if (productsList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("No industrial products registered yet.", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productsList) { p ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(p) }
                            .shadow(1.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.name,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "SKU: ${p.sku} • Brand: ${p.brand.ifEmpty { "COLORJET" }}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                // Status Active/Inactive Indicator
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (p.status == "Active") SuccessGreen.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = p.status.uppercase(),
                                        color = if (p.status == "Active") SuccessGreen else Color.Red,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BackgroundGray)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("Selling Price", fontSize = 10.sp, color = TextSecondary)
                                        Text("৳${String.format("%,.0f", p.sellingPrice)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                                    }
                                    Column {
                                        Text("Landed Cost", fontSize = 10.sp, color = TextSecondary)
                                        Text("৳${String.format("%,.0f", p.landedCost)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Stock Balance", fontSize = 10.sp, color = TextSecondary)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${p.stockLevel} ${p.unit}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = if (p.stockLevel <= p.reorderLevel) Color.Red else SuccessGreen
                                        )
                                        if (p.stockLevel <= p.reorderLevel) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Warning, contentDescription = "Low Stock Alert", tint = Color.Red, modifier = Modifier.size(14.dp))
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
}

// Tab 2: STOCK MOVEMENT LEDGER LOG
@Composable
fun StockMovementsTab(
    stockMovements: List<StockMovement>,
    products: List<Product>
) {
    var selectedTypeFilter by remember { mutableStateOf("All") }
    val types = listOf("All", "Opening Stock", "Stock-In", "Stock-Out", "Adjustment", "Transfer", "Purchase Receive", "Invoice Sales", "Customer Return", "Supplier Return", "Damaged")

    val filteredMovements = remember(stockMovements, selectedTypeFilter) {
        if (selectedTypeFilter == "All") stockMovements else stockMovements.filter { it.transactionType == selectedTypeFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontally scrollable Type Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { t ->
                val isSel = selectedTypeFilter == t
                FilterChip(
                    selected = isSel,
                    onClick = { selectedTypeFilter = t },
                    label = { Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (filteredMovements.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No stock ledger postings logged under this category.", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMovements) { mov ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (mov.transactionType) {
                                                    "Stock-In", "Opening Stock", "Purchase Receive", "Customer Return" -> SuccessGreen
                                                    "Stock-Out", "Invoice Sales", "Supplier Return", "Damaged" -> Color.Red
                                                    else -> AccentOrange
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(mov.transactionType, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${mov.warehouse})", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = if (mov.qtyIn > 0) "+${mov.qtyIn}" else "-${mov.qtyOut}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (mov.qtyIn > 0) SuccessGreen else Color.Red
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(mov.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Ref: ${mov.referenceNumber}", fontSize = 10.sp, color = TextSecondary)
                                    if (mov.relatedEntityName.isNotEmpty()) {
                                        Text("Party: ${mov.relatedEntityName}", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Landed cost: ৳${String.format("%,.1f", mov.unitCost)}", fontSize = 10.sp, color = TextSecondary)
                                    Text("By: ${mov.createdBy}", fontSize = 9.sp, color = Color.Gray)
                                }
                            }

                            if (mov.notes.isNotEmpty() || mov.serialNumbers.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BackgroundGray)
                                        .padding(6.dp)
                                ) {
                                    Column {
                                        if (mov.notes.isNotEmpty()) {
                                            Text("Notes: ${mov.notes}", fontSize = 10.sp, color = Color.DarkGray)
                                        }
                                        if (mov.serialNumbers.isNotEmpty()) {
                                            Text("Serials: ${mov.serialNumbers}", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
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
}

// Tab 3: WAREHOUSE WISE BREAKDOWNS
@Composable
fun WarehousesTab(
    products: List<Product>,
    stockMovements: List<StockMovement>
) {
    val warehouses = listOf("Main Warehouse", "Mirpur Depot", "Chittagong Showroom", "Uttara Branch")

    // Dynamic stock calculations per warehouse and product
    val warehouseStatsList = remember(products, stockMovements) {
        warehouses.map { wh ->
            var totalQty = 0
            var totalValue = 0.0
            val itemBreakdown = mutableListOf<Pair<Product, Int>>()

            products.forEach { p ->
                // Traverse movements to compute local warehouse stock level for this product
                var balance = 0
                stockMovements.filter { it.productId == p.id && it.warehouse == wh }.forEach { mov ->
                    balance += mov.qtyIn - mov.qtyOut
                }

                if (balance > 0) {
                    totalQty += balance
                    totalValue += (balance * p.landedCost)
                    itemBreakdown.add(p to balance)
                }
            }

            WarehouseStats(wh, totalQty, totalValue, itemBreakdown)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("WAREHOUSE FINANCIAL POSITION & METRICS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
        }

        items(warehouseStatsList) { stats ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
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
                            Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stats.name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlue.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "৳${String.format("%,.0f", stats.totalValue)}",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Stored Stock items count:", fontSize = 12.sp, color = TextSecondary)
                        Text("${stats.totalQty} units", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BackgroundGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("STOCKED PRODUCTS BREAKDOWN:", fontWeight = FontWeight.Black, fontSize = 10.sp, color = AccentOrange, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (stats.breakdown.isEmpty()) {
                        Text("No stock allocated in this warehouse.", fontSize = 11.sp, color = TextSecondary, style = androidx.compose.ui.text.TextStyle(fontStyle = FontStyle.Italic))
                    } else {
                        stats.breakdown.forEach { (prod, qty) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("SKU: ${prod.sku}", fontSize = 9.sp, color = TextSecondary)
                                }
                                Text("$qty ${prod.unit}", fontWeight = FontWeight.Black, fontSize = 11.sp, color = SuccessGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class WarehouseStats(
    val name: String,
    val totalQty: Int,
    val totalValue: Double,
    val breakdown: List<Pair<Product, Int>>
)


// ==================================================
// 11. GPS ATTENDANCE SUB-SCREEN
// ==================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSubScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val attendanceList by viewModel.allAttendance.collectAsStateWithLifecycle()
    
    val sdfDate = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    val todayStr = remember { sdfDate.format(java.util.Date()) }
    
    // Find active check-in for today (where checkOutTime is null)
    val activeAttendance = attendanceList.find { 
        it.userId == currentSession?.userId && it.date == todayStr && it.checkOutTime == null 
    }
    
    var workSite by remember { mutableStateOf("Head Office (Tejgaon)") }
    var notes by remember { mutableStateOf("") }
    
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(PrimaryBlue, DeepNavy)
                            )
                        )
                ) {
                    TopAppBar(
                        title = { Text("GPS WORK ATTENDANCE", fontWeight = FontWeight.Black, color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
                HorizontalDivider(color = AccentOrange, thickness = 3.dp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success / Alert banner
            snackbarMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, SuccessGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Interactive Attendance Widget
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "SHIFT REGISTRY", 
                        fontWeight = FontWeight.Black, 
                        fontSize = 11.sp, 
                        color = AccentOrange,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (activeAttendance != null) "ACTIVE SESSION" else "NOT CHECKED IN YET",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (activeAttendance != null) SuccessGreen else Color.Red
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BackgroundGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeAttendance != null) {
                        // Display active session and Check Out button
                        Text("You checked in today at:", fontSize = 12.sp, color = TextSecondary)
                        val checkInTimeStr = remember(activeAttendance.checkInTime) {
                            java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(activeAttendance.checkInTime))
                        }
                        Text(
                            checkInTimeStr,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "📍 ${activeAttendance.checkInLocationName} (${activeAttendance.checkInLat}, ${activeAttendance.checkInLng})",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        if (activeAttendance.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Notes: \"${activeAttendance.notes}\"",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                style = androidx.compose.ui.text.TextStyle(fontStyle = FontStyle.Italic)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Check out Button
                        Button(
                            onClick = {
                                val outLat = activeAttendance.checkInLat + 0.0005
                                val outLng = activeAttendance.checkInLng - 0.0003
                                viewModel.checkOut(
                                    attendanceId = activeAttendance.id,
                                    lat = outLat,
                                    lng = outLng,
                                    locationName = activeAttendance.checkInLocationName,
                                    onResult = { ok, res ->
                                        snackbarMessage = res
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("gps_checkout_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CONFIRM GPS CHECK OUT", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // Check in Form
                        Text("Select Work Site Location:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        listOf(
                            "Head Office (Tejgaon)",
                            "Dhaka Service Center",
                            "Factory (Gazipur)",
                            "Client Office (Savar)",
                            "Chittagong Office"
                        ).forEach { site ->
                            val isSelected = workSite == site
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { workSite = site }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { workSite = site })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = site,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PrimaryBlue else TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Activity Notes / Remarks (Required)") },
                            modifier = Modifier.fillMaxWidth().testTag("attendance_notes_input"),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("What are you working on today?") },
                            colors = com.example.ui.theme.colorJetTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (notes.isBlank()) {
                                    snackbarMessage = "Please fill in Activity Notes first!"
                                    return@Button
                                }
                                val lat = when (workSite) {
                                    "Head Office (Tejgaon)" -> 23.7592
                                    "Dhaka Service Center" -> 23.8759
                                    "Factory (Gazipur)" -> 23.9999
                                    "Client Office (Savar)" -> 23.8123
                                    else -> 22.3569
                                }
                                val lng = when (workSite) {
                                    "Head Office (Tejgaon)" -> 90.3995
                                    "Dhaka Service Center" -> 90.3795
                                    "Factory (Gazipur)" -> 90.4203
                                    "Client Office (Savar)" -> 90.2678
                                    else -> 91.7832
                                }
                                viewModel.checkIn(
                                    lat = lat,
                                    lng = lng,
                                    locationName = workSite,
                                    notes = notes.trim(),
                                    onResult = { ok, res ->
                                        snackbarMessage = res
                                        if (ok) notes = ""
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("gps_checkin_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CONFIRM GPS CHECK IN", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Your Attendance Log
            Text("YOUR HISTORIC ATTENDANCE LOG", fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextPrimary)
            
            val userLogs = attendanceList.filter { it.userId == currentSession?.userId }
            if (userLogs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No historic records found. Check-in above!", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            } else {
                userLogs.forEach { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(log.date, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SuccessGreen.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(log.status, color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(log.checkInLocationName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            val sdfTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                            val checkInStr = sdfTime.format(java.util.Date(log.checkInTime))
                            val checkOutStr = if (log.checkOutTime != null) sdfTime.format(java.util.Date(log.checkOutTime)) else "--:--"
                            
                            Text(
                                "Check In: $checkInStr | Check Out: $checkOutStr",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            if (log.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Notes: \"${log.notes}\"", fontSize = 11.sp, color = Color.Gray, style = androidx.compose.ui.text.TextStyle(fontStyle = FontStyle.Italic))
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==================================================
// 12. INTERNAL MESSAGING SUB-SCREEN
// ==================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingSubScreen(
    viewModel: InventoryViewModel,
    users: List<User>,
    onBack: () -> Unit
) {
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    
    var selectedChatUser by remember { mutableStateOf<User?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var msgText by remember { mutableStateOf("") }
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = selectedChatUser?.fullName ?: "INTERNAL TEAM MESSAGING", 
                        fontWeight = FontWeight.Black, 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedChatUser != null) {
                                selectedChatUser = null
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        if (selectedChatUser == null) {
            // Display User List to Start Chat
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(BackgroundGray)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search team members...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("team_chat_search"),
                    shape = RoundedCornerShape(12.dp)
                )

                val activeCurrentId = currentSession?.userId ?: -1
                val filteredUsers = users.filter { 
                    it.id != activeCurrentId && (searchQuery.isEmpty() || it.fullName.contains(searchQuery, ignoreCase = true)) 
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredUsers.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No team members found", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    } else {
                        items(filteredUsers) { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(1.dp, RoundedCornerShape(12.dp))
                                    .clickable { selectedChatUser = user },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Circular Initials
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.fullName.split(" ").filter { it.isNotEmpty() }.take(2).map { it.first().uppercase() }.joinToString(""),
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.fullName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                        Text("${user.role} | ${user.department.ifEmpty { "Operations" }}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    
                                    Icon(
                                        Icons.Default.Comment,
                                        contentDescription = "Message",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Chat Bubble Interface
            val chatUser = selectedChatUser!!
            val conversation = allMessages.filter { msg ->
                val myId = currentSession?.userId ?: -1
                (msg.senderId == myId && msg.receiverId == chatUser.id) ||
                (msg.senderId == chatUser.id && msg.receiverId == myId)
            }
            
            LaunchedEffect(conversation.size) {
                if (conversation.isNotEmpty()) {
                    listState.animateScrollToItem(conversation.size - 1)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(BackgroundGray)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (conversation.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "No messages with ${chatUser.fullName} yet. Type a message below to start coordinating!",
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(conversation) { msg ->
                            val isMe = msg.senderId == (currentSession?.userId ?: -1)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 0.dp,
                                        bottomEnd = if (isMe) 0.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMe) PrimaryBlue else Color.White
                                    ),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            msg.messageText,
                                            color = if (isMe) Color.White else TextPrimary,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp))
                                        Text(
                                            timeStr,
                                            fontSize = 9.sp,
                                            color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = msgText,
                        onValueChange = { msgText = it },
                        placeholder = { Text("Write coordinates...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    
                    IconButton(
                        onClick = {
                            if (msgText.isNotBlank()) {
                                viewModel.sendMessage(
                                    receiverId = chatUser.id,
                                    receiverName = chatUser.fullName,
                                    messageText = msgText,
                                    onResult = { ok, res ->
                                        if (ok) msgText = ""
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                            .testTag("chat_send_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}


// ==================================================
// 13. PUSH ANNOUNCEMENT CENTER SUB-SCREEN
// ==================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsSubScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val announcements by viewModel.allAnnouncements.collectAsStateWithLifecycle()
    
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") } // "HIGH", "NORMAL", "LOW"
    
    var snackMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ANNOUNCEMENT CENTER", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            snackMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, SuccessGreen)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Composer - Only for OWNER and ADMIN
            if (currentSession?.role == "OWNER" || currentSession?.role == "ADMIN") {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "COMPOSE BROADCAST ALERT", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 11.sp, 
                            color = AccentOrange,
                            letterSpacing = 0.5.sp
                        )
                        
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Alert Title") },
                            modifier = Modifier.fillMaxWidth().testTag("announcement_title"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        OutlinedTextField(
                            value = body,
                            onValueChange = { body = it },
                            label = { Text("Message Body") },
                            modifier = Modifier.fillMaxWidth().testTag("announcement_body"),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3
                        )
                        
                        Column {
                            Text("Broadcast Priority", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("LOW", "NORMAL", "HIGH").forEach { p ->
                                    val isSel = priority == p
                                    val chipColor = when (p) {
                                        "HIGH" -> Color.Red
                                        "NORMAL" -> PrimaryBlue
                                        else -> Color.Gray
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) chipColor else Color.LightGray.copy(alpha = 0.3f))
                                            .clickable { priority = p }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            p, 
                                            color = if (isSel) Color.White else TextPrimary, 
                                            fontSize = 11.sp, 
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Button(
                            onClick = {
                                if (title.isBlank() || body.isBlank()) {
                                    snackMessage = "Title and body cannot be empty!"
                                    return@Button
                                }
                                viewModel.sendAnnouncement(
                                    title = title.trim(),
                                    body = body.trim(),
                                    priority = priority,
                                    onResult = { ok, res ->
                                        snackMessage = res
                                        if (ok) {
                                            title = ""
                                            body = ""
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("broadcast_announcement_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BROADCAST SYSTEM ALERT", fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }

            // Announcement Feed List
            Text("BROADCASTS FEED", fontWeight = FontWeight.Black, fontSize = 12.sp, color = TextPrimary)
            
            if (announcements.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No broadcast alerts found in directory.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            } else {
                announcements.forEach { alert ->
                    val colorAccent = when (alert.priority) {
                        "HIGH" -> Color.Red
                        "NORMAL" -> PrimaryBlue
                        else -> Color.Gray
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, colorAccent.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = colorAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        alert.title, 
                                        fontWeight = FontWeight.Black, 
                                        fontSize = 14.sp, 
                                        color = TextPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colorAccent.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(alert.priority, color = colorAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(alert.body, fontSize = 12.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BackgroundGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Broadcasted by: @${alert.senderName}", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                val dateStr = java.text.SimpleDateFormat("hh:mm a, dd MMM", java.util.Locale.getDefault()).format(java.util.Date(alert.timestamp))
                                Text(dateStr, fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

