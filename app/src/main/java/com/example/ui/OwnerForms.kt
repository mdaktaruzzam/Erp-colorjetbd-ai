package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.launch

@Composable
fun OwnerFormOverlay(
    formType: String,
    viewModel: InventoryViewModel,
    onClose: () -> Unit
) {
    if (formType == "PRODUCTION_DASHBOARD") {
        ProductionTrackingDashboard(viewModel = viewModel, onClose = onClose)
        return
    }
    if (formType == "INDUSTRIAL_OPS_DASHBOARD") {
        IndustrialOperationsDashboard(viewModel = viewModel, onClose = onClose)
        return
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Core states
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val salesOrders by viewModel.allSalesOrders.collectAsStateWithLifecycle()
    val tickets by viewModel.allTickets.collectAsStateWithLifecycle()
    val accounts by viewModel.allLedgerAccounts.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    
    val currentUserId = currentSession?.userId ?: 1
    val currentUsername = currentSession?.username ?: "admin"

    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Form Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getFormTitle(formType),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "COLORJET Management Suite • ERP Active Portal",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (submitSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = SuccessGreen, modifier = Modifier.size(48.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("SAVED SUCCESSFULLY", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The transaction has been safely committed to the enterprise ledger database and audit logs have been updated.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("DONE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (errorMessage.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMessage, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Render specific form based on type
                    when (formType) {
                        "ADD_EMPLOYEE" -> EmployeeCreateScreen(
                            viewModel = viewModel,
                            onSubmit = { employee ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.userDao.insertUser(employee)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "ADD_EMPLOYEE",
                                        details = "Registered employee: ${employee.fullName} (${employee.designation})"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "ISSUE_ID_CARD" -> EmployeeIdCardIssueScreen(
                            users = users.filter { it.role != "CUSTOMER" },
                            onSubmit = { cardDetails ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "ISSUE_ID_CARD",
                                        details = "Issued/updated biometric ID card for: $cardDetails"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "MANUAL_ATTENDANCE" -> AttendanceManualEntryScreen(
                            users = users.filter { it.role != "CUSTOMER" },
                            onSubmit = { attendance ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertAttendance(attendance)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "MANUAL_ATTENDANCE",
                                        details = "Logged manual attendance for ${attendance.fullName} on ${attendance.date}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "COMPOSE_MESSAGE" -> MessageComposeScreen(
                            users = users,
                            onSubmit = { msg ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertMessage(msg)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "COMPOSE_MESSAGE",
                                        details = "Sent internal message to user: ${msg.receiverName}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "SEND_PUSH" -> PushNotificationCreateScreen(
                            users = users,
                            onSubmit = { campaignTitle ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertAnnouncement(PushAnnouncement(
                                        title = campaignTitle,
                                        body = "New push broadcast sent to department target",
                                        priority = "HIGH",
                                        senderName = currentUsername
                                    ))
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "SEND_PUSH",
                                        details = "Sent Push Campaign: $campaignTitle"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "ADD_CUSTOMER" -> CustomerCreateScreen(
                            onSubmit = { cust ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertCustomer(cust)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "ADD_CUSTOMER",
                                        details = "Registered Customer Account: ${cust.name}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CREATE_TASK" -> TaskCreateScreen(
                            users = users.filter { it.role != "CUSTOMER" },
                            onSubmit = { task ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertTask(task)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CREATE_TASK",
                                        details = "Created company task: ${task.title}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CREATE_TICKET" -> ServiceTicketCreateScreen(
                            customers = customers,
                            engineers = users.filter { it.department == "Technical" || it.designation.contains("Engineer") },
                            onSubmit = { ticket ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertTicket(ticket)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CREATE_TICKET",
                                        details = "Created Service Ticket ${ticket.ticketNumber} for customer ${ticket.customerName}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "SCHEDULE_ENGINEER" -> EngineerScheduleCreateScreen(
                            engineers = users.filter { it.department == "Technical" || it.designation.contains("Engineer") },
                            customers = customers,
                            onSubmit = { scheduleDetails ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "SCHEDULE_ENGINEER",
                                        details = "Scheduled Engineer service visit: $scheduleDetails"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "REGISTER_WARRANTY" -> WarrantyCreateScreen(
                            customers = customers,
                            onSubmit = { warranty ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertWarranty(warranty)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "REGISTER_WARRANTY",
                                        details = "Registered machine warranty for serial: ${warranty.serialNumber}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "ADD_PRODUCT" -> ProductCreateScreen(
                            onSubmit = { prod ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertProduct(prod)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "ADD_PRODUCT",
                                        details = "Added product: ${prod.name} (SKU: ${prod.sku})"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "STOCK_IN" -> StockInCreateScreen(
                            products = products,
                            suppliers = suppliers,
                            onSubmit = { movement ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertStockMovement(movement)
                                    // Update product stockLevel
                                    val currentProduct = viewModel.erpDao.getProductById(movement.productId)
                                    if (currentProduct != null) {
                                        viewModel.erpDao.updateProduct(currentProduct.copy(stockLevel = currentProduct.stockLevel + movement.qtyIn))
                                    }
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "STOCK_IN",
                                        details = "Received stock-in reference ${movement.referenceNumber} for ${movement.productName}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "STOCK_OUT" -> StockOutCreateScreen(
                            products = products,
                            customers = customers,
                            onSubmit = { movement ->
                                isSubmitting = true
                                scope.launch {
                                    val currentProduct = viewModel.erpDao.getProductById(movement.productId)
                                    if (currentProduct != null && currentProduct.stockLevel >= movement.qtyOut) {
                                        viewModel.erpDao.insertStockMovement(movement)
                                        viewModel.erpDao.updateProduct(currentProduct.copy(stockLevel = currentProduct.stockLevel - movement.qtyOut))
                                        viewModel.erpDao.insertAuditLog(AuditLog(
                                            userId = currentUserId,
                                            username = currentUsername,
                                            action = "STOCK_OUT",
                                            details = "Issued stock-out reference ${movement.referenceNumber} for ${movement.productName}"
                                        ))
                                        isSubmitting = false
                                        submitSuccess = true
                                    } else {
                                        isSubmitting = false
                                        errorMessage = "Insufficient Stock Available!"
                                    }
                                }
                            }
                        )
                        "ADD_SUPPLIER" -> SupplierCreateScreen(
                            onSubmit = { supp ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertSupplier(supp)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "ADD_SUPPLIER",
                                        details = "Registered Supplier Account: ${supp.name}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CREATE_PO" -> PurchaseOrderCreateScreen(
                            suppliers = suppliers,
                            products = products,
                            onSubmit = { poDetails ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CREATE_PO",
                                        details = "Created supplier purchase order: $poDetails"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CREATE_LC" -> LCCreateScreen(
                            suppliers = suppliers,
                            onSubmit = { lc ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertForeignPurchase(lc)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CREATE_LC",
                                        details = "Opened foreign LC Number: ${lc.lcNumber} for supplier ${lc.supplierName}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CREATE_SHIPMENT" -> ShipmentCreateScreen(
                            suppliers = suppliers,
                            onSubmit = { shipmentDetails ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CREATE_SHIPMENT",
                                        details = "Logged cargo shipment progress: $shipmentDetails"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CALCULATE_LANDED_COST" -> LandedCostCreateScreen(
                            onSubmit = { landedCostDetails ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CALCULATE_LANDED_COST",
                                        details = "Calculated consignment unit landed cost: $landedCostDetails"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CREATE_QUOTATION" -> QuotationCreateScreen(
                            customers = customers,
                            products = products,
                            onSubmit = { quotation ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertQuotation(quotation)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CREATE_QUOTATION",
                                        details = "Generated customer quotation: ${quotation.quotationNumber}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CREATE_SALES_ORDER" -> SalesOrderCreateScreen(
                            customers = customers,
                            products = products,
                            onSubmit = { salesOrder ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertSalesOrder(salesOrder)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CREATE_SALES_ORDER",
                                        details = "Created sales order: ${salesOrder.salesOrderNumber}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CREATE_INVOICE" -> InvoiceCreateScreen(
                            customers = customers,
                            onSubmit = { invoice ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertInvoice(invoice)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CREATE_INVOICE",
                                        details = "Issued tax invoice ${invoice.invoiceNumber} for customer ${invoice.customerName}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "RECEIVE_PAYMENT" -> PaymentCreateScreen(
                            customers = customers,
                            invoices = invoices.filter { it.status != "Paid" },
                            onSubmit = { payment ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertPayment(payment)
                                    // Update invoice paid amount & customer balance
                                    payment.invoiceId?.let { invId ->
                                        val invoiceObj = viewModel.erpDao.getInvoiceById(invId)
                                        if (invoiceObj != null) {
                                            val newPaid = invoiceObj.paidAmount + payment.amount
                                            val newBal = (invoiceObj.totalAmount - newPaid).coerceAtLeast(0.0)
                                            val newStatus = if (newBal == 0.0) "Paid" else "Partially Paid"
                                            viewModel.erpDao.updateInvoice(invoiceObj.copy(paidAmount = newPaid, balance = newBal, status = newStatus))
                                        }
                                    }
                                    
                                    val customerObj = viewModel.erpDao.getCustomerById(payment.customerId)
                                    if (customerObj != null) {
                                        val newCustomerBal = (customerObj.balance - payment.amount).coerceAtLeast(0.0)
                                        viewModel.erpDao.updateCustomer(customerObj.copy(balance = newCustomerBal))
                                    }
                                    
                                    // Update Ledger Accounts
                                    val targetAccountName = if (payment.paymentMethod == "CASH") "Cash Account" else "Bank Account"
                                    val accountObj = viewModel.erpDao.getLedgerAccountByName(targetAccountName)
                                    if (accountObj != null) {
                                        viewModel.erpDao.updateLedgerAccount(accountObj.copy(balance = accountObj.balance + payment.amount))
                                    }
                                    
                                    val arAccount = viewModel.erpDao.getLedgerAccountByName("Accounts Receivable")
                                    if (arAccount != null) {
                                        viewModel.erpDao.updateLedgerAccount(arAccount.copy(balance = (arAccount.balance - payment.amount).coerceAtLeast(0.0)))
                                    }
                                    
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "RECEIVE_PAYMENT",
                                        details = "Received payment ${payment.paymentNumber} of ৳${payment.amount} from customer ${payment.customerName}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "CREATE_EXPENSE" -> ExpenseCreateScreen(
                            onSubmit = { expenseDetails ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "CREATE_EXPENSE",
                                        details = "Logged company expense transaction: $expenseDetails"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "GENERATE_AGREEMENT" -> AgreementCreateScreen(
                            customers = customers,
                            onSubmit = { agreement ->
                                isSubmitting = true
                                scope.launch {
                                    viewModel.erpDao.insertEmiAgreement(agreement)
                                    viewModel.erpDao.insertAuditLog(AuditLog(
                                        userId = currentUserId,
                                        username = currentUsername,
                                        action = "GENERATE_AGREEMENT",
                                        details = "Generated Hire-Purchase EMI Agreement: ${agreement.agreementNumber} for ${agreement.customerName}"
                                    ))
                                    isSubmitting = false
                                    submitSuccess = true
                                }
                            }
                        )
                        "VIEW_REPORTS" -> ViewReportsScreen(
                            customers = customers,
                            accounts = accounts,
                            onClose = onClose
                        )
                        else -> {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("This form module is being loaded in active screen registry...", color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getFormTitle(type: String): String {
    return when (type) {
        "ADD_EMPLOYEE" -> "ADD NEW EMPLOYEE"
        "ISSUE_ID_CARD" -> "ISSUE EMPLOYEE ID CARD"
        "MANUAL_ATTENDANCE" -> "MANUAL ATTENDANCE ENTRY"
        "COMPOSE_MESSAGE" -> "COMPOSE INTERNAL MESSAGE"
        "SEND_PUSH" -> "CREATE PUSH BROADCAST"
        "ADD_CUSTOMER" -> "ADD NEW CUSTOMER"
        "CREATE_TASK" -> "CREATE OFFICE TASK"
        "CREATE_TICKET" -> "CREATE SERVICE TICKET"
        "SCHEDULE_ENGINEER" -> "SCHEDULE ENGINEER VISIT"
        "REGISTER_WARRANTY" -> "REGISTER MACHINE WARRANTY"
        "ADD_PRODUCT" -> "ADD NEW PRODUCT"
        "STOCK_IN" -> "STOCK-IN RECEIVED ENTRY"
        "STOCK_OUT" -> "STOCK-OUT ISSUED ENTRY"
        "ADD_SUPPLIER" -> "ADD NEW SUPPLIER"
        "CREATE_PO" -> "CREATE PURCHASE ORDER"
        "CREATE_LC" -> "CREATE FOREIGN LC / PI"
        "CREATE_SHIPMENT" -> "CREATE CARGO SHIPMENT"
        "CALCULATE_LANDED_COST" -> "CALCULATE LANDED COST"
        "CREATE_QUOTATION" -> "CREATE SALES QUOTATION"
        "CREATE_SALES_ORDER" -> "CREATE SALES ORDER"
        "CREATE_INVOICE" -> "CREATE TAX INVOICE"
        "RECEIVE_PAYMENT" -> "RECEIVE CUSTOMER PAYMENT"
        "CREATE_EXPENSE" -> "CREATE EXPENSE ENTRY"
        "GENERATE_AGREEMENT" -> "GENERATE EMI AGREEMENT"
        "VIEW_REPORTS" -> "EXECUTIVE ANALYTICS REPORTS"
        "PRODUCTION_DASHBOARD" -> "PRODUCTION TRACKING DASHBOARD"
        else -> "ERP FORM TRANSACTION"
    }
}

@Composable
fun EmployeeCreateScreen(
    viewModel: InventoryViewModel,
    onSubmit: (User) -> Unit
) {
    var fullNameEng by remember { mutableStateOf("") }
    var fullNameBng by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var addressPres by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Dhaka") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Technical") }
    var designation by remember { mutableStateOf("Sr. Engineer") }
    var employmentType by remember { mutableStateOf("Permanent") }
    var salaryStr by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STAFF") }
    var password by remember { mutableStateOf("1234") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("SECTION A — BASIC INFORMATION", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        
        OutlinedTextField(
            value = fullNameEng,
            onValueChange = { fullNameEng = it },
            label = { Text("Full Name (English) *") },
            modifier = Modifier.fillMaxWidth().testTag("employee_name_field")
        )
        OutlinedTextField(
            value = fullNameBng,
            onValueChange = { fullNameBng = it },
            label = { Text("Full Name (Bangla)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Primary Phone *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.weight(1f)
            )
        }

        Text("SECTION B — EMPLOYMENT & ROLE", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("Department *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = designation,
                onValueChange = { designation = it },
                label = { Text("Designation *") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = salaryStr,
                onValueChange = { salaryStr = it },
                label = { Text("Basic Salary (BDT)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = role,
                onValueChange = { role = it },
                label = { Text("Application Role *") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                if (fullNameEng.isNotBlank() && phone.isNotBlank()) {
                    val nextId = (100..999).random()
                    val userObj = User(
                        username = "cj-emp-$nextId",
                        fullName = fullNameEng,
                        pinCode = password,
                        role = role.uppercase(Locale.getDefault()),
                        designation = designation,
                        department = department,
                        phone = phone,
                        email = email,
                        status = "Active"
                    )
                    onSubmit(userObj)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("employee_submit_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("SAVE EMPLOYEE RECORDS", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun EmployeeIdCardIssueScreen(
    users: List<User>,
    onSubmit: (String) -> Unit
) {
    var selectedUserIndex by remember { mutableStateOf(0) }
    var cardExpiryStr by remember { mutableStateOf("2028-12-31") }
    var bloodGroup by remember { mutableStateOf("B+") }
    var remarks by remember { mutableStateOf("Issued new Biometric RF standard card") }

    if (users.isEmpty()) {
        Text("No active employees found to issue cards.")
        return
    }

    val employee = users[selectedUserIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SELECT EMPLOYEE TARGET", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedUserIndex = (selectedUserIndex + 1) % users.size
                }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(employee.fullName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${employee.designation} • Dept: ${employee.department}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = cardExpiryStr,
            onValueChange = { cardExpiryStr = it },
            label = { Text("Card Validity Expiry Date *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = bloodGroup,
            onValueChange = { bloodGroup = it },
            label = { Text("Employee Blood Group *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Internal Serial & Remarks") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text("ID CARD BIOMETRIC PREVIEW (FRONT & BACK)", fontWeight = FontWeight.Black, fontSize = 11.sp, color = TextSecondary)
        
        // CR80 card front mock layout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("COLORJET BANGLADESH", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("Quality • Commitment • Service", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(54.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(employee.fullName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text(employee.designation, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("ID: ${employee.username.uppercase()}", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("BLOOD: $bloodGroup • EXPIRY: $cardExpiryStr", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                    }
                }
            }
        }

        Button(
            onClick = {
                onSubmit("${employee.fullName} (Blood: $bloodGroup, Exp: $cardExpiryStr)")
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("AUTHORIZE & GENERATE PRINTABLE CR80 CARD", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttendanceManualEntryScreen(
    users: List<User>,
    onSubmit: (Attendance) -> Unit
) {
    var selectedUserIndex by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("Present") }
    var checkInTimeStr by remember { mutableStateOf("09:00 AM") }
    var checkOutTimeStr by remember { mutableStateOf("06:00 PM") }
    var locationName by remember { mutableStateOf("COLORJET Corporate Office") }
    var notes by remember { mutableStateOf("Manual entry posted by Owner Authorization") }

    if (users.isEmpty()) {
        Text("No active employees found to log attendance.")
        return
    }

    val employee = users[selectedUserIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SELECT TARGET STAFF", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedUserIndex = (selectedUserIndex + 1) % users.size
                }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonPin, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(employee.fullName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Designation: ${employee.designation}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = status,
            onValueChange = { status = it },
            label = { Text("Attendance Status (Present / Late / Absent / Leave) *") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = checkInTimeStr,
                onValueChange = { checkInTimeStr = it },
                label = { Text("Check-In Time *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = checkOutTimeStr,
                onValueChange = { checkOutTimeStr = it },
                label = { Text("Check-Out Time") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = locationName,
            onValueChange = { locationName = it },
            label = { Text("Assigned GPS Site Location *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Compliance Authorization Note *") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdfDate.format(Date())
                val att = Attendance(
                    userId = employee.id,
                    username = employee.username,
                    fullName = employee.fullName,
                    date = todayStr,
                    checkInTime = System.currentTimeMillis() - 8 * 3600 * 1000L,
                    checkOutTime = System.currentTimeMillis(),
                    checkInLat = 23.7509,
                    checkInLng = 90.3934,
                    checkInLocationName = locationName,
                    checkOutLat = 23.7509,
                    checkOutLng = 90.3934,
                    checkOutLocationName = locationName,
                    status = status,
                    notes = notes
                )
                onSubmit(att)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("POST MANUAL ATTENDANCE TRANSACTION", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MessageComposeScreen(
    users: List<User>,
    onSubmit: (InternalMessage) -> Unit
) {
    var selectedUserIndex by remember { mutableStateOf(0) }
    var priority by remember { mutableStateOf("Urgent") }
    var msgText by remember { mutableStateOf("") }

    if (users.isEmpty()) {
        Text("No active database users found to message.")
        return
    }

    val recipient = users[selectedUserIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("RECIPIENT", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedUserIndex = (selectedUserIndex + 1) % users.size
                }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Send, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(recipient.fullName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Role: ${recipient.role} • ${recipient.email}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = priority,
            onValueChange = { priority = it },
            label = { Text("Priority Level (Normal / Important / Urgent)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = msgText,
            onValueChange = { msgText = it },
            label = { Text("Message Body Text *") },
            modifier = Modifier.fillMaxWidth().height(140.dp)
        )

        Button(
            onClick = {
                if (msgText.isNotBlank()) {
                    val msg = InternalMessage(
                        senderId = 1,
                        senderName = "Md Aktaruzzaman",
                        receiverId = recipient.id,
                        receiverName = recipient.fullName,
                        messageText = "[$priority] $msgText",
                        timestamp = System.currentTimeMillis()
                    )
                    onSubmit(msg)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("COMPOSE & BROADCAST", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PushNotificationCreateScreen(
    users: List<User>,
    onSubmit: (String) -> Unit
) {
    var campaignName by remember { mutableStateOf("Eid Mubarak Promo Broadcast") }
    var title by remember { mutableStateOf("Eid Al-Adha Splendid Offer!") }
    var body by remember { mutableStateOf("Get flat 15% discount on all ColorJet UV Curable original inks from July 15-20! Order via suite now.") }
    var targetGroup by remember { mutableStateOf("All Active Customers") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = campaignName,
            onValueChange = { campaignName = it },
            label = { Text("Campaign Name *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = targetGroup,
            onValueChange = { targetGroup = it },
            label = { Text("Audience Target Group (All / Selected Customers / Staff / Roles)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Push Notification Banner Title *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Push Notification Body Content *") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text("FCM BROADCAST LIVE SIMULATION METRIC SUMMARY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AccentOrange)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f).background(BackgroundGray, RoundedCornerShape(8.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AUDIENCE", fontSize = 10.sp, color = TextSecondary)
                    Text("142 devices", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                }
            }
            Box(modifier = Modifier.weight(1f).background(BackgroundGray, RoundedCornerShape(8.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DELIVERABILITY", fontSize = 10.sp, color = TextSecondary)
                    Text("99.4% guaranteed", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                }
            }
        }

        Button(
            onClick = {
                if (campaignName.isNotBlank() && title.isNotBlank()) {
                    onSubmit(campaignName)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("DISPATCH LIVE PUSH NOTIFICATIONS", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CustomerCreateScreen(
    onSubmit: (Customer) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var customerType by remember { mutableStateOf("Dealer") }
    var creditLimitStr by remember { mutableStateOf("500000") }
    var openingBalanceStr by remember { mutableStateOf("0") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Customer Representative Name *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Company Name *") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Business Address *") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = customerType,
                onValueChange = { customerType = it },
                label = { Text("Customer Type *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = creditLimitStr,
                onValueChange = { creditLimitStr = it },
                label = { Text("Credit Limit (BDT)") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                if (name.isNotBlank() && company.isNotBlank() && phone.isNotBlank()) {
                    val customer = Customer(
                        name = name,
                        company = company,
                        phone = phone,
                        email = email,
                        address = address,
                        customerType = customerType,
                        creditLimit = creditLimitStr.toDoubleOrNull() ?: 0.0,
                        openingBalance = openingBalanceStr.toDoubleOrNull() ?: 0.0,
                        balance = openingBalanceStr.toDoubleOrNull() ?: 0.0,
                        status = "Active"
                    )
                    onSubmit(customer)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("COMMIT CUSTOMER ACCOUNT", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TaskCreateScreen(
    users: List<User>,
    onSubmit: (OfficeTask) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedUserIndex by remember { mutableStateOf(0) }
    var priority by remember { mutableStateOf("HIGH") }
    var dueDaysStr by remember { mutableStateOf("2") }

    if (users.isEmpty()) {
        Text("No active staff found to assign tasks.")
        return
    }

    val employee = users[selectedUserIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Heading Title *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Detailed Instructions") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )
        
        Text("CHOOSE ASSIGNEE", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedUserIndex = (selectedUserIndex + 1) % users.size
                }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(employee.fullName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Designation: ${employee.designation}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = priority,
                onValueChange = { priority = it },
                label = { Text("Priority Level *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = dueDaysStr,
                onValueChange = { dueDaysStr = it },
                label = { Text("Due In Days *") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    val days = dueDaysStr.toIntOrNull() ?: 1
                    val task = OfficeTask(
                        title = title,
                        description = description,
                        assignedToId = employee.id,
                        assignedToName = employee.fullName,
                        status = "PENDING",
                        priority = priority.uppercase(),
                        dueDate = System.currentTimeMillis() + (days * 24 * 3600 * 1000L)
                    )
                    onSubmit(task)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("DISPATCH TASK ASSIGNMENT", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ServiceTicketCreateScreen(
    customers: List<Customer>,
    engineers: List<User>,
    onSubmit: (ServiceTicket) -> Unit
) {
    var selectedCustIndex by remember { mutableStateOf(0) }
    var selectedEngIndex by remember { mutableStateOf(0) }
    var modelName by remember { mutableStateOf("ColorJet Vulcan 6090") }
    var serialNum by remember { mutableStateOf("VLC-6090-4491") }
    var problemDesc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("CRITICAL") }

    if (customers.isEmpty()) {
        Text("No customer database found to log tickets.")
        return
    }

    val customer = customers[selectedCustIndex]
    val engineer = engineers.getOrNull(selectedEngIndex)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SELECT CUSTOMER TARGET", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedCustIndex = (selectedCustIndex + 1) % customers.size
                }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.company, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Contact: ${customer.name} • ${customer.phone}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = modelName,
                onValueChange = { modelName = it },
                label = { Text("Device Model *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = serialNum,
                onValueChange = { serialNum = it },
                label = { Text("Serial Number *") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = problemDesc,
            onValueChange = { problemDesc = it },
            label = { Text("Technical Fault Problem Description *") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )

        if (engineers.isNotEmpty()) {
            Text("ASSIGN TECHNICAL ENGINEER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundGray, RoundedCornerShape(12.dp))
                    .clickable {
                        selectedEngIndex = (selectedEngIndex + 1) % engineers.size
                    }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(engineer?.fullName ?: "N/A", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Role: ${engineer?.designation}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        Button(
            onClick = {
                if (problemDesc.isNotBlank() && modelName.isNotBlank() && serialNum.isNotBlank()) {
                    val ticket = ServiceTicket(
                        ticketNumber = "CJ-SRV-${(1000..9999).random()}",
                        customerId = customer.id,
                        customerName = customer.name,
                        deviceModel = modelName,
                        serialNumber = serialNum,
                        issueDescription = problemDesc,
                        assignedEngineerId = engineer?.id,
                        assignedEngineerName = engineer?.fullName,
                        status = "OPEN",
                        priority = priority
                    )
                    onSubmit(ticket)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GENERATE TECHNICAL SERVICE TICKET", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EngineerScheduleCreateScreen(
    engineers: List<User>,
    customers: List<Customer>,
    onSubmit: (String) -> Unit
) {
    var selectedEngIndex by remember { mutableStateOf(0) }
    var selectedCustIndex by remember { mutableStateOf(0) }
    var scheduleType by remember { mutableStateOf("Customer Visit / Maintenance") }
    var visitDateStr by remember { mutableStateOf("2026-07-20") }
    var remarks by remember { mutableStateOf("Emergency inspection for ink supply system") }

    if (engineers.isEmpty() || customers.isEmpty()) {
        Text("No active engineers or customers found.")
        return
    }

    val engineer = engineers[selectedEngIndex]
    val customer = customers[selectedCustIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("CHOOSE ENGINEER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedEngIndex = (selectedEngIndex + 1) % engineers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Engineering, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(engineer.fullName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(engineer.designation, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Text("SELECT CUSTOMER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedCustIndex = (selectedCustIndex + 1) % customers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.company, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(customer.address, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = scheduleType,
            onValueChange = { scheduleType = it },
            label = { Text("Schedule Work Type *") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = visitDateStr,
                onValueChange = { visitDateStr = it },
                label = { Text("Visit Date *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = "10:00 AM",
                onValueChange = { },
                label = { Text("Expect Start Time") },
                modifier = Modifier.weight(1f),
                enabled = false
            )
        }
        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Engineering Scope of Work / Instructions") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )

        Button(
            onClick = {
                onSubmit("${engineer.fullName} visit to ${customer.company} scheduled on $visitDateStr")
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("SAVE & CONFIRM SCHEDULE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WarrantyCreateScreen(
    customers: List<Customer>,
    onSubmit: (Warranty) -> Unit
) {
    var selectedCustIndex by remember { mutableStateOf(0) }
    var machineName by remember { mutableStateOf("ColorJet Vulcan 6090 UV Flatbed") }
    var serialNumber by remember { mutableStateOf("CJ-VULC-2025-098") }
    var warrantyDurationMonths by remember { mutableStateOf("12") }
    var coveredParts by remember { mutableStateOf("Mainboard, Headboard, UV Lamp, Servo Motors") }

    if (customers.isEmpty()) {
        Text("No active customers found to register warranties.")
        return
    }

    val customer = customers[selectedCustIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("CHOOSE ASSIGNED CUSTOMER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedCustIndex = (selectedCustIndex + 1) % customers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(customer.company, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = machineName,
            onValueChange = { machineName = it },
            label = { Text("Product / Machine Name *") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = serialNumber,
                onValueChange = { serialNumber = it },
                label = { Text("Biometric Serial Number *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = warrantyDurationMonths,
                onValueChange = { warrantyDurationMonths = it },
                label = { Text("Duration (Months) *") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = coveredParts,
            onValueChange = { coveredParts = it },
            label = { Text("Covered Electronic Parts under Warranty") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )

        Button(
            onClick = {
                if (machineName.isNotBlank() && serialNumber.isNotBlank()) {
                    val months = warrantyDurationMonths.toIntOrNull() ?: 12
                    val warranty = Warranty(
                        customerId = customer.id,
                        machineId = 1,
                        machineName = machineName,
                        model = "Vulcan-Series",
                        serialNumber = serialNumber,
                        warrantyStart = System.currentTimeMillis(),
                        warrantyEnd = System.currentTimeMillis() + (months * 30 * 24 * 3600 * 1000L),
                        remainingDays = months * 30,
                        warrantyStatus = "Active",
                        coveredParts = coveredParts,
                        excludedParts = "Damper, Ink Tubes, Printheads",
                        currentClaimStatus = "Active"
                    )
                    onSubmit(warranty)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("REGISTER COMPREHENSIVE WARRANTY CERTIFICATE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProductCreateScreen(
    onSubmit: (Product) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Printing machine") }
    var sku by remember { mutableStateOf("") }
    var purchasePriceStr by remember { mutableStateOf("") }
    var sellingPriceStr by remember { mutableStateOf("") }
    var stockLevelStr by remember { mutableStateOf("1") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product / Part Name *") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("Unique SKU *") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = purchasePriceStr,
                onValueChange = { purchasePriceStr = it },
                label = { Text("Purchase cost (BDT) *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = sellingPriceStr,
                onValueChange = { sellingPriceStr = it },
                label = { Text("Selling retail price *") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = stockLevelStr,
            onValueChange = { stockLevelStr = it },
            label = { Text("Initial Stock Level") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (name.isNotBlank() && sku.isNotBlank() && purchasePriceStr.isNotBlank()) {
                    val p = Product(
                        name = name,
                        category = category,
                        sku = sku,
                        purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 0.0,
                        sellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0,
                        stockLevel = stockLevelStr.toIntOrNull() ?: 1,
                        status = "Active"
                    )
                    onSubmit(p)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("REGISTER PRODUCT IN DIRECTORY", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StockInCreateScreen(
    products: List<Product>,
    suppliers: List<Supplier>,
    onSubmit: (StockMovement) -> Unit
) {
    var selectedProdIndex by remember { mutableStateOf(0) }
    var selectedSuppIndex by remember { mutableStateOf(0) }
    var qtyStr by remember { mutableStateOf("10") }
    var remarks by remember { mutableStateOf("Warehouse stock replenishment consignment") }

    if (products.isEmpty()) {
        Text("No registered product catalog found.")
        return
    }

    val product = products[selectedProdIndex]
    val supplier = suppliers.getOrNull(selectedSuppIndex)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SELECT PRODUCT", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedProdIndex = (selectedProdIndex + 1) % products.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Inventory, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(product.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Current stock: ${product.stockLevel} units • SKU: ${product.sku}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        if (suppliers.isNotEmpty()) {
            Text("SELECT SUPPLIER SOURCE", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundGray, RoundedCornerShape(12.dp))
                    .clickable {
                        selectedSuppIndex = (selectedSuppIndex + 1) % suppliers.size
                    }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(supplier?.name ?: "N/A", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Country: ${supplier?.country}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        OutlinedTextField(
            value = qtyStr,
            onValueChange = { qtyStr = it },
            label = { Text("Stock-In Quantity (Incoming) *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Consignment References & Notes") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )

        Button(
            onClick = {
                val qty = qtyStr.toIntOrNull() ?: 1
                val m = StockMovement(
                    referenceNumber = "STK-IN-${(1000..9999).random()}",
                    transactionType = "Stock-In",
                    productId = product.id,
                    productName = product.name,
                    qtyIn = qty,
                    qtyOut = 0,
                    balance = product.stockLevel + qty,
                    unitCost = product.purchasePrice,
                    createdBy = "admin",
                    notes = remarks
                )
                onSubmit(m)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("APPROVE STOCK-IN RECEIVED", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StockOutCreateScreen(
    products: List<Product>,
    customers: List<Customer>,
    onSubmit: (StockMovement) -> Unit
) {
    var selectedProdIndex by remember { mutableStateOf(0) }
    var selectedCustIndex by remember { mutableStateOf(0) }
    var qtyStr by remember { mutableStateOf("1") }
    var remarks by remember { mutableStateOf("Invoiced order fulfillment transfer") }

    if (products.isEmpty() || customers.isEmpty()) {
        Text("No catalog or customers available.")
        return
    }

    val product = products[selectedProdIndex]
    val customer = customers[selectedCustIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SELECT PRODUCT TO OUT", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedProdIndex = (selectedProdIndex + 1) % products.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Launch, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(product.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Stock level available: ${product.stockLevel} units • SKU: ${product.sku}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Text("SELECT TARGET CUSTOMER RECIPIENT", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedCustIndex = (selectedCustIndex + 1) % customers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.company, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(customer.address, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = qtyStr,
            onValueChange = { qtyStr = it },
            label = { Text("Stock-Out Quantity (Deducted) *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Authorized Release Notes") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )

        Button(
            onClick = {
                val qty = qtyStr.toIntOrNull() ?: 1
                val m = StockMovement(
                    referenceNumber = "STK-OUT-${(1000..9999).random()}",
                    transactionType = "Stock-Out",
                    productId = product.id,
                    productName = product.name,
                    qtyIn = 0,
                    qtyOut = qty,
                    balance = (product.stockLevel - qty).coerceAtLeast(0),
                    unitCost = product.sellingPrice,
                    createdBy = "admin",
                    notes = remarks
                )
                onSubmit(m)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("APPROVE STOCK RELEASE DISPATCH", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SupplierCreateScreen(
    onSubmit: (Supplier) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("China") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Supplier Organization Name *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contactPerson,
            onValueChange = { contactPerson = it },
            label = { Text("Key Representative Contact Person *") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Corporate Phone *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Origin Country *") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    val s = Supplier(
                        name = name,
                        contactPerson = contactPerson,
                        phone = phone,
                        country = country
                    )
                    onSubmit(s)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("REGISTER SUPPLY CHAIN PARTNER", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PurchaseOrderCreateScreen(
    suppliers: List<Supplier>,
    products: List<Product>,
    onSubmit: (String) -> Unit
) {
    var selectedSuppIndex by remember { mutableStateOf(0) }
    var selectedProdIndex by remember { mutableStateOf(0) }
    var orderQtyStr by remember { mutableStateOf("5") }
    var paymentTerms by remember { mutableStateOf("LC at Sight 100%") }

    if (suppliers.isEmpty() || products.isEmpty()) {
        Text("Prerequisite suppliers or catalog products not seeded yet.")
        return
    }

    val supplier = suppliers[selectedSuppIndex]
    val product = products[selectedProdIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SELECT TARGET SUPPLIER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedSuppIndex = (selectedSuppIndex + 1) % suppliers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(supplier.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Origin: ${supplier.country}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Text("SELECT PRODUCT DIRECTORY CATALOG", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedProdIndex = (selectedProdIndex + 1) % products.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(product.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Catalog Purchase Cost: ৳${product.purchasePrice}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = orderQtyStr,
            onValueChange = { orderQtyStr = it },
            label = { Text("Order Import Quantity *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = paymentTerms,
            onValueChange = { paymentTerms = it },
            label = { Text("Import Purchase Payment Terms *") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSubmit("PO to ${supplier.name} for ${orderQtyStr} units of ${product.name} created")
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GENERATE OFFICIAL PURCHASE ORDER", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LCCreateScreen(
    suppliers: List<Supplier>,
    onSubmit: (ForeignPurchase) -> Unit
) {
    var lcNumber by remember { mutableStateOf("LC-2026-981223") }
    var lcValueUsdStr by remember { mutableStateOf("45000") }
    var exchangeRateStr by remember { mutableStateOf("117.8") }
    var productDesc by remember { mutableStateOf("Original ColorJet High-Density Industrial Spare Parts & Inks") }
    var selectedSuppIndex by remember { mutableStateOf(0) }

    if (suppliers.isEmpty()) {
        Text("No registered suppliers found to establish LC.")
        return
    }

    val supplier = suppliers[selectedSuppIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = lcNumber,
            onValueChange = { lcNumber = it },
            label = { Text("Foreign LC Letter of Credit Serial *") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("SELECT BENEFICIARY SUPPLIER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedSuppIndex = (selectedSuppIndex + 1) % suppliers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(supplier.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Country: ${supplier.country}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = lcValueUsdStr,
                onValueChange = { lcValueUsdStr = it },
                label = { Text("PI FOB Value (USD) *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = exchangeRateStr,
                onValueChange = { exchangeRateStr = it },
                label = { Text("Current Ex-Rate (BDT) *") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = productDesc,
            onValueChange = { productDesc = it },
            label = { Text("Import Product Description Summary") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (lcNumber.isNotBlank() && lcValueUsdStr.isNotBlank()) {
                    val usd = lcValueUsdStr.toDoubleOrNull() ?: 0.0
                    val rate = exchangeRateStr.toDoubleOrNull() ?: 117.8
                    val purchase = ForeignPurchase(
                        lcNumber = lcNumber,
                        lcDate = System.currentTimeMillis(),
                        supplierId = supplier.id,
                        supplierName = supplier.name,
                        productDescription = productDesc,
                        fobValueUsd = usd,
                        exchangeRate = rate,
                        freightChargesBdt = usd * 0.05 * rate,
                        totalLandedCostBdt = usd * rate * 1.15, // estimated landed cost
                        status = "INITIATED"
                    )
                    onSubmit(purchase)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("OPEN COMPLIANT LETTER OF CREDIT", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShipmentCreateScreen(
    suppliers: List<Supplier>,
    onSubmit: (String) -> Unit
) {
    var blNumber by remember { mutableStateOf("BL-MAERSK-9812739") }
    var containerNum by remember { mutableStateOf("MSKU-829123-0") }
    var vesselName by remember { mutableStateOf("Maersk Honam V.108") }
    var trackingStatus by remember { mutableStateOf("In Transit (Singapore Port)") }
    var selectedSuppIndex by remember { mutableStateOf(0) }

    val supplier = suppliers.getOrNull(selectedSuppIndex)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = blNumber,
            onValueChange = { blNumber = it },
            label = { Text("Bill of Lading / AWB Number *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = containerNum,
            onValueChange = { containerNum = it },
            label = { Text("Cargo Container Serial ID *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = vesselName,
            onValueChange = { vesselName = it },
            label = { Text("Vessel Carrier Fleet Name *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = trackingStatus,
            onValueChange = { trackingStatus = it },
            label = { Text("Real-Time Tracking Status Location") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSubmit("Cargo consignment $containerNum under Bill $blNumber registered: $trackingStatus")
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("LOG IN-TRANSIT CARGO PROGRESS", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LandedCostCreateScreen(
    onSubmit: (String) -> Unit
) {
    var lcRefNumber by remember { mutableStateOf("LC-2026-981223") }
    var purchaseValueBdt by remember { mutableStateOf("5,301,000") }
    var customsDutyBdt by remember { mutableStateOf("1,240,000") }
    var clearingAgentBdt by remember { mutableStateOf("45,000") }
    var laborAndTransportBdt by remember { mutableStateOf("15,000") }
    var insuranceBdt by remember { mutableStateOf("25,000") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = lcRefNumber,
            onValueChange = { lcRefNumber = it },
            label = { Text("Letter of Credit Reference Serial *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = purchaseValueBdt,
            onValueChange = { purchaseValueBdt = it },
            label = { Text("Raw FOB Invoice Purchase Value (BDT) *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = customsDutyBdt,
            onValueChange = { customsDutyBdt = it },
            label = { Text("Customs Duty & VAT Tariffs Paid (Chittagong Port) *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = clearingAgentBdt,
            onValueChange = { clearingAgentBdt = it },
            label = { Text("C&F Clearing Agent Service Fees *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = insuranceBdt,
            onValueChange = { insuranceBdt = it },
            label = { Text("Marine Insurance & Bank Charges BDT *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = laborAndTransportBdt,
            onValueChange = { laborAndTransportBdt = it },
            label = { Text("Inland Transport & Port Labor Cost *") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text("Suggested Landed Cost Calculation Output", fontWeight = FontWeight.Black, fontSize = 11.sp, color = PrimaryBlue)
        
        Card(
            colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Accumulated Capital Expenses", fontSize = 12.sp, color = TextSecondary)
                    Text("৳6,626,000 BDT", fontWeight = FontWeight.Black, color = TextPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("True Unit Landed Cost Multiplier factor", fontSize = 12.sp, color = TextSecondary)
                    Text("1.25x (markup multiplier)", fontWeight = FontWeight.Black, color = SuccessGreen)
                }
            }
        }

        Button(
            onClick = {
                onSubmit("Landed Cost established for $lcRefNumber: Cumulative Cost 6,626,000 BDT")
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("APPROVE & ALLOCATE UNIT LANDED COSTS", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuotationCreateScreen(
    customers: List<Customer>,
    products: List<Product>,
    onSubmit: (Quotation) -> Unit
) {
    var selectedCustIndex by remember { mutableStateOf(0) }
    var selectedProdIndex by remember { mutableStateOf(0) }
    var discountStr by remember { mutableStateOf("15000") }
    var terms by remember { mutableStateOf("Valid 30 days, 50% advance bank transfer, remaining on delivery.") }

    if (customers.isEmpty() || products.isEmpty()) {
        Text("Prerequisite records missing.")
        return
    }

    val customer = customers[selectedCustIndex]
    val product = products[selectedProdIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("CHOOSE QUOTATION CUSTOMER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedCustIndex = (selectedCustIndex + 1) % customers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Contacts, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.company, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(customer.name, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Text("CHOOSE QUOTED MACHINE", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedProdIndex = (selectedProdIndex + 1) % products.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(product.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Standard Retail: ৳${product.sellingPrice}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = discountStr,
            onValueChange = { discountStr = it },
            label = { Text("Special Sales Discount (BDT)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = terms,
            onValueChange = { terms = it },
            label = { Text("Quotation Legal Terms & Conditions") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )

        Button(
            onClick = {
                val disc = discountStr.toDoubleOrNull() ?: 0.0
                val item = TransactionItem(product.id, product.name, 1, product.unit, product.sellingPrice, disc)
                val q = Quotation(
                    quotationNumber = "Q-2026-${(1000..9999).random()}",
                    customerId = customer.id,
                    customerName = customer.company,
                    itemsJson = TransactionItem.serializeList(listOf(item)),
                    totalAmount = product.sellingPrice - disc,
                    discountAmount = disc,
                    specialTerms = terms,
                    salesPerson = "admin",
                    status = "Pending"
                )
                onSubmit(q)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GENERATE OFFICIAL SALES QUOTATION", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SalesOrderCreateScreen(
    customers: List<Customer>,
    products: List<Product>,
    onSubmit: (SalesOrder) -> Unit
) {
    var selectedCustIndex by remember { mutableStateOf(0) }
    var selectedProdIndex by remember { mutableStateOf(0) }
    var orderQtyStr by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("Confirmed via customer email dispatch order.") }

    if (customers.isEmpty() || products.isEmpty()) {
        Text("Prerequisite records missing.")
        return
    }

    val customer = customers[selectedCustIndex]
    val product = products[selectedProdIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SELECT ORDER CUSTOMER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedCustIndex = (selectedCustIndex + 1) % customers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.company, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Representative: ${customer.name}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Text("SELECT ORDER PRODUCT", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedProdIndex = (selectedProdIndex + 1) % products.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(product.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Fulfillment rate: ৳${product.sellingPrice} / unit", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = orderQtyStr,
            onValueChange = { orderQtyStr = it },
            label = { Text("Order Quantity *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Fulfillment Schedule Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val qty = orderQtyStr.toIntOrNull() ?: 1
                val total = product.sellingPrice * qty
                val item = TransactionItem(product.id, product.name, qty, product.unit, product.sellingPrice)
                val order = SalesOrder(
                    salesOrderNumber = "SO-2026-${(1000..9999).random()}",
                    customerId = customer.id,
                    customerName = customer.company,
                    itemsJson = TransactionItem.serializeList(listOf(item)),
                    totalAmount = total,
                    salesPerson = "admin",
                    notes = notes
                )
                onSubmit(order)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GENERATE APPROVED SALES ORDER", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InvoiceCreateScreen(
    customers: List<Customer>,
    onSubmit: (Invoice) -> Unit
) {
    var invoiceNumber by remember { mutableStateOf("INV-2026-004") }
    var selectedCustIndex by remember { mutableStateOf(0) }
    var totalAmountStr by remember { mutableStateOf("1200000") }
    var terms by remember { mutableStateOf("Net 30 standard commercial contract.") }

    if (customers.isEmpty()) {
        Text("No active customer database accounts.")
        return
    }

    val customer = customers[selectedCustIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = invoiceNumber,
            onValueChange = { invoiceNumber = it },
            label = { Text("Tax Invoice Number *") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("SELECT BILLING CLIENT", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedCustIndex = (selectedCustIndex + 1) % customers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.company, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Client Address: ${customer.address}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = totalAmountStr,
            onValueChange = { totalAmountStr = it },
            label = { Text("Taxable Invoice Total (BDT) *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = terms,
            onValueChange = { terms = it },
            label = { Text("Invoicing Terms & Credit Expiry Period") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (invoiceNumber.isNotBlank() && totalAmountStr.isNotBlank()) {
                    val total = totalAmountStr.toDoubleOrNull() ?: 0.0
                    val inv = Invoice(
                        invoiceNumber = invoiceNumber,
                        customerId = customer.id,
                        customerName = customer.company,
                        totalAmount = total,
                        paidAmount = 0.0,
                        balance = total,
                        status = "Draft",
                        paymentTerms = terms
                    )
                    onSubmit(inv)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GENERATE APPROVED TAX INVOICE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PaymentCreateScreen(
    customers: List<Customer>,
    invoices: List<Invoice>,
    onSubmit: (Payment) -> Unit
) {
    var paymentNum by remember { mutableStateOf("PAY-2026-881") }
    var selectedCustIndex by remember { mutableStateOf(0) }
    var selectedInvoiceIndex by remember { mutableStateOf(0) }
    var method by remember { mutableStateOf("BANK_TRANSFER") }
    var amountStr by remember { mutableStateOf("100000") }
    var chequeDetails by remember { mutableStateOf("City Bank Chq-981223") }

    if (customers.isEmpty()) {
        Text("No active customer ledger accounts.")
        return
    }

    val customer = customers[selectedCustIndex]
    val matchingInvoices = invoices.filter { it.customerId == customer.id }
    val invoice = matchingInvoices.getOrNull(selectedInvoiceIndex)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = paymentNum,
            onValueChange = { paymentNum = it },
            label = { Text("Corporate Money Receipt / Payment Number *") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("SELECT CUSTOMER PAYER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedCustIndex = (selectedCustIndex + 1) % customers.size
                    selectedInvoiceIndex = 0
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.company, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Total Outstandings: ৳${customer.balance}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        if (matchingInvoices.isNotEmpty()) {
            Text("SELECT OUTSTANDING TAX INVOICE", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundGray, RoundedCornerShape(12.dp))
                    .clickable {
                        selectedInvoiceIndex = (selectedInvoiceIndex + 1) % matchingInvoices.size
                    }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FeaturedPlayList, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(invoice?.invoiceNumber ?: "N/A", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Invoice Balance Remaining: ৳${invoice?.balance ?: 0.0}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = method,
                onValueChange = { method = it },
                label = { Text("Payment Method *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Collected Amount (BDT) *") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = chequeDetails,
            onValueChange = { chequeDetails = it },
            label = { Text("Cheque / Bank Reference details") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val amt = amountStr.toDoubleOrNull() ?: 0.0
                if (amt > 0.0) {
                    val pay = Payment(
                        paymentNumber = paymentNum,
                        invoiceId = invoice?.id,
                        customerId = customer.id,
                        customerName = customer.company,
                        amount = amt,
                        paymentMethod = method,
                        paymentDate = System.currentTimeMillis(),
                        postedBy = "admin",
                        chequeDetails = chequeDetails
                    )
                    onSubmit(pay)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("RECEIVE PAYMENT & POST LEDGER", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpenseCreateScreen(
    onSubmit: (String) -> Unit
) {
    var expenseCategory by remember { mutableStateOf("Spare Parts Import Shipping") }
    var amountStr by remember { mutableStateOf("32000") }
    var accountUsed by remember { mutableStateOf("Bank Account") }
    var narration by remember { mutableStateOf("Port handling transport & clearing expenses") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = expenseCategory,
            onValueChange = { expenseCategory = it },
            label = { Text("Expense Category *") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Debit Expense Amount *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = accountUsed,
                onValueChange = { accountUsed = it },
                label = { Text("Asset Account used *") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = narration,
            onValueChange = { narration = it },
            label = { Text("Detailed Narration & Vouchers") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )

        Button(
            onClick = {
                if (expenseCategory.isNotBlank() && amountStr.isNotBlank()) {
                    onSubmit("Expense: $expenseCategory, Amount: ৳$amountStr via $accountUsed")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("LOG APPROVED CORPORATE EXPENSE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AgreementCreateScreen(
    customers: List<Customer>,
    onSubmit: (EmiAgreement) -> Unit
) {
    var selectedCustIndex by remember { mutableStateOf(0) }
    var machineName by remember { mutableStateOf("ColorJet Vulcan 6090 Flatbed UV") }
    var serialNum by remember { mutableStateOf("VLC-6090-4491") }
    var cashPriceStr by remember { mutableStateOf("1200000") }
    var downPaymentStr by remember { mutableStateOf("400000") }
    var emiMonthsStr by remember { mutableStateOf("12") }

    if (customers.isEmpty()) {
        Text("No active customers.")
        return
    }

    val customer = customers[selectedCustIndex]

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SELECT HIRE-PURCHASE CUSTOMER", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundGray, RoundedCornerShape(12.dp))
                .clickable {
                    selectedCustIndex = (selectedCustIndex + 1) % customers.size
                }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.company, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Payer representative: ${customer.name}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        OutlinedTextField(
            value = machineName,
            onValueChange = { machineName = it },
            label = { Text("Biometric Machine Model Name *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = serialNum,
            onValueChange = { serialNum = it },
            label = { Text("Machine Registered Serial *") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = cashPriceStr,
                onValueChange = { cashPriceStr = it },
                label = { Text("Total Capital Cash Price *") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = downPaymentStr,
                onValueChange = { downPaymentStr = it },
                label = { Text("Down Payment received *") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = emiMonthsStr,
            onValueChange = { emiMonthsStr = it },
            label = { Text("Hire Contract Period (Months) *") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (machineName.isNotBlank() && serialNum.isNotBlank()) {
                    val cashPrice = cashPriceStr.toDoubleOrNull() ?: 0.0
                    val down = downPaymentStr.toDoubleOrNull() ?: 0.0
                    val emiMonths = emiMonthsStr.toIntOrNull() ?: 12
                    val remAmt = (cashPrice - down).coerceAtLeast(0.0)
                    val installment = remAmt / emiMonths
                    val agreement = EmiAgreement(
                        agreementNumber = "AGR-2026-${(100..999).random()}",
                        customerId = customer.id,
                        customerName = customer.company,
                        productName = machineName,
                        serialNumber = serialNum,
                        totalAmount = cashPrice,
                        downPayment = down,
                        interestRate = 0.0,
                        emiCount = emiMonths,
                        emiAmount = installment,
                        language = "ENGLISH",
                        termsTemplate = "Standard ColorJet Hire-Purchase Agreement Terms"
                    )
                    onSubmit(agreement)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("GENERATE LEASE AGREEMENT &EMI TIMELINE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ViewReportsScreen(
    customers: List<Customer>,
    accounts: List<LedgerAccount>,
    onClose: () -> Unit
) {
    var activeReportTab by remember { mutableStateOf("Sales") }
    val tabs = listOf("Sales", "Collection", "Customer Due", "Cash/Bank")
    val context = androidx.compose.ui.platform.LocalContext.current
    var showExportSuccess by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { t ->
                val isSelected = activeReportTab == t
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) PrimaryBlue else BackgroundGray)
                        .clickable { activeReportTab = t }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(t.uppercase(), color = if (isSelected) Color.White else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Button(
            onClick = {
                val title = "ColorJet Enterprise $activeReportTab Report"
                val subtitle = "Official Business Summary & Ledger Statements"
                val summary = listOf(
                    PdfReportGenerator.SummaryItem("Report Category", activeReportTab),
                    PdfReportGenerator.SummaryItem("Generated By", "Management ERP User"),
                    PdfReportGenerator.SummaryItem("Total Records", when(activeReportTab) {
                        "Sales" -> "3 Items"
                        "Collection" -> "3 Receipts"
                        "Customer Due" -> "${customers.size} Customers"
                        else -> "${accounts.size} Accounts"
                    })
                )
                val headers = listOf("Reference / Item", "Details / Subtitle", "Amount / Balance (BDT)")
                val rows = when(activeReportTab) {
                    "Sales" -> listOf(
                        PdfReportGenerator.TableRowItem(listOf("ColorJet Vulcan 6090 UV", "3 Pcs Sold", "৳3,600,000")),
                        PdfReportGenerator.TableRowItem(listOf("Cyan UV Original Ink", "120 Liters", "৳780,000")),
                        PdfReportGenerator.TableRowItem(listOf("Printhead Epson i3200-A1", "14 Pcs Installed", "৳1,190,000"))
                    )
                    "Collection" -> listOf(
                        PdfReportGenerator.TableRowItem(listOf("PAY-1001", "Bank Transfer (Digiprint)", "৳200,000")),
                        PdfReportGenerator.TableRowItem(listOf("PAY-1002", "bKash Merchant (Dhaka Poly)", "৳85,000")),
                        PdfReportGenerator.TableRowItem(listOf("PAY-1003", "Bank Transfer (Premium Prints)", "৳1,480,000"))
                    )
                    "Customer Due" -> customers.map { c ->
                        PdfReportGenerator.TableRowItem(listOf(c.company, c.name, "৳${String.format("%,.0f", c.balance)}"))
                    }
                    else -> accounts.filter { it.type == "ASSET" }.map { a ->
                        PdfReportGenerator.TableRowItem(listOf(a.accountName, a.type, "৳${String.format("%,.0f", a.balance)}"))
                    }
                }

                val pdfFile = PdfReportGenerator.generateReportPdf(context, title, subtitle, summary, headers, rows)
                if (pdfFile != null) {
                    PdfReportGenerator.sharePdfReport(context, pdfFile)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("EXPORT $activeReportTab REPORT TO PDF", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        when (activeReportTab) {
            "Sales" -> {
                Text("Quarterly Consolidated Sales Summary Report", fontWeight = FontWeight.Bold, color = TextPrimary)
                Box(modifier = Modifier.fillMaxWidth().background(BackgroundGray, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReportRow("1. ColorJet Vulcan 6090 UV Machine", "3 Pcs Sold", "৳3,600,000")
                        ReportRow("2. Premium Cyan UV Original Ink", "120 Liters", "৳780,000")
                        ReportRow("3. Printhead Epson i3200-A1", "14 Pcs Installed", "৳1,190,000")
                        HorizontalDivider(color = Color.LightGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Gross Billing sales", fontWeight = FontWeight.Black)
                            Text("৳5,570,000 BDT", fontWeight = FontWeight.Black, color = PrimaryBlue)
                        }
                    }
                }
            }
            "Collection" -> {
                Text("Consolidated Collection & Money Receipt Logs", fontWeight = FontWeight.Bold, color = TextPrimary)
                Box(modifier = Modifier.fillMaxWidth().background(BackgroundGray, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReportRow("PAY-1001 • Bank Transfer (Digiprint)", "July 11, 2026", "৳200,000")
                        ReportRow("PAY-1002 • bKash Merchant (Dhaka Poly)", "July 09, 2026", "৳85,000")
                        ReportRow("PAY-1003 • Bank Transfer (Premium Prints)", "July 08, 2026", "৳1,480,000")
                        HorizontalDivider(color = Color.LightGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Realized Cash Collections", fontWeight = FontWeight.Black)
                            Text("৳1,765,000 BDT", fontWeight = FontWeight.Black, color = SuccessGreen)
                        }
                    }
                }
            }
            "Customer Due" -> {
                Text("Consolidated Customer Outstanding Due Balances", fontWeight = FontWeight.Bold, color = TextPrimary)
                Box(modifier = Modifier.fillMaxWidth().background(BackgroundGray, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        customers.forEach { c ->
                            ReportRow(c.company, "Outstandings Balance Due", "৳${String.format("%,.0f", c.balance)}")
                        }
                        HorizontalDivider(color = Color.LightGray)
                        val totalDue = customers.sumOf { it.balance }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Enterprise Receivables Due", fontWeight = FontWeight.Black)
                            Text("৳${String.format("%,.0f", totalDue)} BDT", fontWeight = FontWeight.Black, color = Color.Red)
                        }
                    }
                }
            }
            "Cash/Bank" -> {
                Text("Asset Ledgers Liquidity Balance Accounts", fontWeight = FontWeight.Bold, color = TextPrimary)
                Box(modifier = Modifier.fillMaxWidth().background(BackgroundGray, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accounts.filter { it.type == "ASSET" }.forEach { acc ->
                            ReportRow(acc.accountName, "Ledger Account type: ${acc.type}", "৳${String.format("%,.0f", acc.balance)}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportRow(label: String, subtitle: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
            Text(subtitle, fontSize = 10.sp, color = TextSecondary)
        }
        Text(value, fontWeight = FontWeight.Black, fontSize = 13.sp, color = TextPrimary)
    }
}
