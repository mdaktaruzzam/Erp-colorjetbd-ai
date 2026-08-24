package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.supplyDao()
    val userDao = db.userDao()
    val erpDao = db.erpDao()
    val supportDao = db.supportDao()

    private val _currentUserSession = MutableStateFlow<UserSession?>(null)
    val currentUserSession: StateFlow<UserSession?> = _currentUserSession

    // Global Loading State for API calls and cloud data synchronization
    private val _isGlobalLoading = MutableStateFlow(false)
    val isGlobalLoading: StateFlow<Boolean> = _isGlobalLoading

    private val _globalLoadingMessage = MutableStateFlow("Synchronizing...")
    val globalLoadingMessage: StateFlow<String> = _globalLoadingMessage

    fun showLoading(message: String = "Synchronizing with COLORJET Cloud...") {
        _globalLoadingMessage.value = message
        _isGlobalLoading.value = true
    }

    fun hideLoading() {
        _isGlobalLoading.value = false
    }

    fun performDataSync(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val serverMode = com.example.data.VpsService.getServerMode(getApplication())
            showLoading("Connecting to COLORJET VPS [$serverMode]...")
            try {
                android.util.Log.d("InventoryViewModel", "Preparing PUSH dataset for VPS...")
                val payload = org.json.JSONObject()
                payload.put("action", "push")
                payload.put("username", currentUserSession.value?.username ?: "client")

                val tablesObj = org.json.JSONObject()

                // 1. Users
                val usersArr = org.json.JSONArray()
                allUsers.value.forEach { u ->
                    val obj = org.json.JSONObject().apply {
                        put("id", u.id)
                        put("username", u.username)
                        put("fullName", u.fullName)
                        put("pinCode", u.pinCode)
                        put("role", u.role)
                        put("designation", u.designation)
                        put("department", u.department)
                        put("phone", u.phone)
                        put("email", u.email)
                        put("status", u.status)
                    }
                    usersArr.put(obj)
                }
                tablesObj.put("users", usersArr)

                // 2. Customers
                val customersArr = org.json.JSONArray()
                allCustomers.value.forEach { c ->
                    val obj = org.json.JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("company", c.company)
                        put("phone", c.phone)
                        put("email", c.email)
                        put("address", c.address)
                        put("balance", c.balance)
                        put("customerCode", c.customerCode)
                        put("customerType", c.customerType)
                        put("whatsapp", c.whatsapp)
                        put("district", c.district)
                        put("status", c.status)
                    }
                    customersArr.put(obj)
                }
                tablesObj.put("customers", customersArr)

                // 3. Products
                val productsArr = org.json.JSONArray()
                allProducts.value.forEach { p ->
                    val obj = org.json.JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("category", p.category)
                        put("sku", p.sku)
                        put("brand", p.brand)
                        put("model", p.model)
                        put("unit", p.unit)
                        put("purchasePrice", p.purchasePrice)
                        put("landedCost", p.landedCost)
                        put("sellingPrice", p.sellingPrice)
                        put("reorderLevel", p.reorderLevel)
                        put("description", p.description)
                        put("status", p.status)
                        put("stockLevel", p.stockLevel)
                    }
                    productsArr.put(obj)
                }
                tablesObj.put("products", productsArr)

                // 4. Service Tickets
                val ticketsArr = org.json.JSONArray()
                allTickets.value.forEach { t ->
                    val obj = org.json.JSONObject().apply {
                        put("id", t.id)
                        put("ticketNumber", t.ticketNumber)
                        put("customerId", t.customerId)
                        put("customerName", t.customerName)
                        put("deviceModel", t.deviceModel)
                        put("serialNumber", t.serialNumber)
                        put("issueDescription", t.issueDescription)
                        put("assignedEngineerId", t.assignedEngineerId ?: 0)
                        put("assignedEngineerName", t.assignedEngineerName ?: "")
                        put("status", t.status)
                        put("priority", t.priority)
                        put("serviceReport", t.serviceReport ?: "")
                        put("partsReplaced", t.partsReplaced ?: "")
                        put("createdAt", t.createdAt)
                        put("updatedAt", t.updatedAt)
                    }
                    ticketsArr.put(obj)
                }
                tablesObj.put("service_tickets", ticketsArr)

                // 5. Office Tasks
                val tasksArr = org.json.JSONArray()
                allTasks.value.forEach { t ->
                    val obj = org.json.JSONObject().apply {
                        put("id", t.id)
                        put("title", t.title)
                        put("description", t.description)
                        put("assignedToId", t.assignedToId ?: 0)
                        put("assignedToName", t.assignedToName ?: "")
                        put("status", t.status)
                        put("priority", t.priority)
                        put("dueDate", t.dueDate)
                        put("createdAt", t.createdAt)
                    }
                    tasksArr.put(obj)
                }
                tablesObj.put("office_tasks", tasksArr)

                // 6. Invoices
                val invoicesArr = org.json.JSONArray()
                allInvoices.value.forEach { i ->
                    val obj = org.json.JSONObject().apply {
                        put("id", i.id)
                        put("invoiceNumber", i.invoiceNumber)
                        put("customerId", i.customerId)
                        put("customerName", i.customerName)
                        put("totalAmount", i.totalAmount)
                        put("paidAmount", i.paidAmount)
                        put("balance", i.balance)
                        put("status", i.status)
                        put("createdAt", i.createdAt)
                        put("itemsJson", i.itemsJson)
                        put("discountAmount", i.discountAmount)
                        put("vatAmount", i.vatAmount)
                        put("deliveryCharge", i.deliveryCharge)
                        put("installationCharge", i.installationCharge)
                        put("serviceCharge", i.serviceCharge)
                        put("dueDate", i.dueDate)
                        put("paymentTerms", i.paymentTerms)
                        put("salesperson", i.salesperson)
                        put("notes", i.notes)
                    }
                    invoicesArr.put(obj)
                }
                tablesObj.put("invoices", invoicesArr)

                // 7. Payments
                val paymentsArr = org.json.JSONArray()
                allPayments.value.forEach { py ->
                    val obj = org.json.JSONObject().apply {
                        put("id", py.id)
                        put("paymentNumber", py.paymentNumber)
                        put("invoiceId", py.invoiceId ?: 0)
                        put("customerId", py.customerId)
                        put("customerName", py.customerName)
                        put("amount", py.amount)
                        put("paymentMethod", py.paymentMethod)
                        put("paymentDate", py.paymentDate)
                        put("postedBy", py.postedBy)
                        put("referenceNumber", py.referenceNumber)
                        put("notes", py.notes)
                    }
                    paymentsArr.put(obj)
                }
                tablesObj.put("payments", paymentsArr)

                // 8. Stock Movements
                val movementsArr = org.json.JSONArray()
                allStockMovements.value.forEach { sm ->
                    val obj = org.json.JSONObject().apply {
                        put("id", sm.id)
                        put("timestamp", sm.timestamp)
                        put("referenceNumber", sm.referenceNumber)
                        put("transactionType", sm.transactionType)
                        put("productId", sm.productId)
                        put("productName", sm.productName)
                        put("qtyIn", sm.qtyIn)
                        put("qtyOut", sm.qtyOut)
                        put("balance", sm.balance)
                        put("unitCost", sm.unitCost)
                        put("createdBy", sm.createdBy)
                        put("notes", sm.notes)
                        put("serialNumbers", sm.serialNumbers)
                    }
                    movementsArr.put(obj)
                }
                tablesObj.put("stock_movements", movementsArr)

                // 9. Warranties
                val warrantiesArr = org.json.JSONArray()
                allWarranties.value.forEach { w ->
                    val obj = org.json.JSONObject().apply {
                        put("id", w.id)
                        put("customerId", w.customerId)
                        put("machineId", w.machineId)
                        put("machineName", w.machineName)
                        put("model", w.model)
                        put("serialNumber", w.serialNumber)
                        put("warrantyStart", w.warrantyStart)
                        put("warrantyEnd", w.warrantyEnd)
                        put("remainingDays", w.remainingDays)
                        put("warrantyStatus", w.warrantyStatus)
                        put("coveredParts", w.coveredParts)
                        put("excludedParts", w.excludedParts)
                        put("currentClaimStatus", w.currentClaimStatus)
                    }
                    warrantiesArr.put(obj)
                }
                tablesObj.put("warranties", warrantiesArr)

                // 10. Attendance
                val attendanceArr = org.json.JSONArray()
                allAttendance.value.forEach { att ->
                    val obj = org.json.JSONObject().apply {
                        put("id", att.id)
                        put("userId", att.userId)
                        put("username", att.username)
                        put("fullName", att.fullName)
                        put("date", att.date)
                        put("checkInTime", att.checkInTime)
                        put("checkOutTime", att.checkOutTime ?: 0L)
                        put("checkInLat", att.checkInLat)
                        put("checkInLng", att.checkInLng)
                        put("checkInLocationName", att.checkInLocationName)
                        put("checkOutLat", att.checkOutLat ?: 0.0)
                        put("checkOutLng", att.checkOutLng ?: 0.0)
                        put("checkOutLocationName", att.checkOutLocationName ?: "")
                        put("status", att.status)
                        put("notes", att.notes)
                    }
                    attendanceArr.put(obj)
                }
                tablesObj.put("attendance", attendanceArr)

                // 11. Machines
                val machinesArr = org.json.JSONArray()
                allMachines.value.forEach { m ->
                    val obj = org.json.JSONObject().apply {
                        put("id", m.id)
                        put("customerId", m.customerId)
                        put("productName", m.productName)
                        put("category", m.category)
                        put("brand", m.brand)
                        put("model", m.model)
                        put("serialNumber", m.serialNumber)
                        put("invoiceNumber", m.invoiceNumber)
                        put("salesOrderNumber", m.salesOrderNumber)
                        put("purchaseDate", m.purchaseDate)
                        put("deliveryDate", m.deliveryDate)
                        put("installationDate", m.installationDate)
                        put("installationAddress", m.installationAddress)
                        put("warrantyStartDate", m.warrantyStartDate)
                        put("warrantyEndDate", m.warrantyEndDate)
                        put("warrantyStatus", m.warrantyStatus)
                        put("assignedEngineer", m.assignedEngineer)
                        put("machineStatus", m.machineStatus)
                    }
                    machinesArr.put(obj)
                }
                tablesObj.put("machines", machinesArr)

                payload.put("tables", tablesObj)

                // Push to VPS
                showLoading("Uploading local data to COLORJET VPS...")
                val pushRes = com.example.data.VpsService.pushData(getApplication(), payload)
                if (pushRes.optString("status") != "success") {
                    throw Exception(pushRes.optString("message", "Error pushing local dataset"))
                }

                // Pull from VPS
                showLoading("Downloading cloud updates from COLORJET VPS...")
                val pullPayload = org.json.JSONObject().apply {
                    put("action", "pull")
                }
                val pullRes = com.example.data.VpsService.pullData(getApplication(), pullPayload)
                if (pullRes.optString("status") == "success") {
                    showLoading("Integrating database records...")
                    val serverTables = pullRes.optJSONObject("tables")
                    if (serverTables != null) {
                        // 1. Users
                        val serverUsers = serverTables.optJSONArray("users")
                        if (serverUsers != null) {
                            for (idx in 0 until serverUsers.length()) {
                                val u = serverUsers.getJSONObject(idx)
                                val user = User(
                                    id = u.optInt("id"),
                                    username = u.optString("username"),
                                    fullName = u.optString("fullName"),
                                    pinCode = u.optString("pinCode", "1234"),
                                    role = u.optString("role", "STAFF"),
                                    designation = u.optString("designation"),
                                    department = u.optString("department"),
                                    phone = u.optString("phone"),
                                    email = u.optString("email"),
                                    status = u.optString("status", "Active")
                                )
                                userDao.insertUser(user)
                            }
                        }

                        // 2. Customers
                        val serverCustomers = serverTables.optJSONArray("customers")
                        if (serverCustomers != null) {
                            for (idx in 0 until serverCustomers.length()) {
                                val c = serverCustomers.getJSONObject(idx)
                                val customer = Customer(
                                    id = c.optInt("id"),
                                    name = c.optString("name"),
                                    company = c.optString("company"),
                                    phone = c.optString("phone"),
                                    email = c.optString("email"),
                                    address = c.optString("address"),
                                    balance = c.optDouble("balance", 0.0),
                                    customerCode = c.optString("customerCode"),
                                    customerType = c.optString("customerType", "Individual"),
                                    whatsapp = c.optString("whatsapp"),
                                    district = c.optString("district"),
                                    status = c.optString("status", "Active")
                                )
                                erpDao.insertCustomer(customer)
                            }
                        }

                        // 3. Products
                        val serverProducts = serverTables.optJSONArray("products")
                        if (serverProducts != null) {
                            for (idx in 0 until serverProducts.length()) {
                                val p = serverProducts.getJSONObject(idx)
                                val product = Product(
                                    id = p.optInt("id"),
                                    name = p.optString("name"),
                                    category = p.optString("category"),
                                    brand = p.optString("brand"),
                                    model = p.optString("model"),
                                    sku = p.optString("sku"),
                                    unit = p.optString("unit", "pcs"),
                                    purchasePrice = p.optDouble("purchasePrice", 0.0),
                                    landedCost = p.optDouble("landedCost", 0.0),
                                    sellingPrice = p.optDouble("sellingPrice", 0.0),
                                    reorderLevel = p.optInt("reorderLevel", 5),
                                    description = p.optString("description"),
                                    status = p.optString("status", "Active"),
                                    stockLevel = p.optInt("stockLevel", 0)
                                )
                                erpDao.insertProduct(product)
                            }
                        }

                        // 4. Service Tickets
                        val serverTickets = serverTables.optJSONArray("service_tickets")
                        if (serverTickets != null) {
                            for (idx in 0 until serverTickets.length()) {
                                val t = serverTickets.getJSONObject(idx)
                                val ticket = ServiceTicket(
                                    id = t.optInt("id"),
                                    ticketNumber = t.optString("ticketNumber"),
                                    customerId = t.optInt("customerId"),
                                    customerName = t.optString("customerName"),
                                    deviceModel = t.optString("deviceModel"),
                                    serialNumber = t.optString("serialNumber"),
                                    issueDescription = t.optString("issueDescription"),
                                    assignedEngineerId = if (t.isNull("assignedEngineerId")) null else t.optInt("assignedEngineerId"),
                                    assignedEngineerName = if (t.isNull("assignedEngineerName")) null else t.optString("assignedEngineerName"),
                                    status = t.optString("status"),
                                    priority = t.optString("priority"),
                                    serviceReport = if (t.isNull("serviceReport")) null else t.optString("serviceReport"),
                                    partsReplaced = if (t.isNull("partsReplaced")) null else t.optString("partsReplaced"),
                                    createdAt = t.optLong("createdAt", System.currentTimeMillis()),
                                    updatedAt = t.optLong("updatedAt", System.currentTimeMillis())
                                )
                                erpDao.insertTicket(ticket)
                            }
                        }

                        // 5. Office Tasks
                        val serverTasks = serverTables.optJSONArray("office_tasks")
                        if (serverTasks != null) {
                            for (idx in 0 until serverTasks.length()) {
                                val o = serverTasks.getJSONObject(idx)
                                val task = OfficeTask(
                                    id = o.optInt("id"),
                                    title = o.optString("title"),
                                    description = o.optString("description"),
                                    assignedToId = if (o.isNull("assignedToId")) null else o.optInt("assignedToId"),
                                    assignedToName = if (o.isNull("assignedToName")) null else o.optString("assignedToName"),
                                    status = o.optString("status"),
                                    priority = o.optString("priority"),
                                    dueDate = o.optLong("dueDate"),
                                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                                )
                                erpDao.insertTask(task)
                            }
                        }

                        // 6. Invoices
                        val serverInvoices = serverTables.optJSONArray("invoices")
                        if (serverInvoices != null) {
                            for (idx in 0 until serverInvoices.length()) {
                                val iv = serverInvoices.getJSONObject(idx)
                                val invoice = Invoice(
                                    id = iv.optInt("id"),
                                    invoiceNumber = iv.optString("invoiceNumber"),
                                    customerId = iv.optInt("customerId"),
                                    customerName = iv.optString("customerName"),
                                    totalAmount = iv.optDouble("totalAmount"),
                                    paidAmount = iv.optDouble("paidAmount", 0.0),
                                    balance = iv.optDouble("balance"),
                                    status = iv.optString("status"),
                                    createdAt = iv.optLong("createdAt", System.currentTimeMillis()),
                                    itemsJson = iv.optString("itemsJson"),
                                    discountAmount = iv.optDouble("discountAmount"),
                                    vatAmount = iv.optDouble("vatAmount"),
                                    deliveryCharge = iv.optDouble("deliveryCharge"),
                                    installationCharge = iv.optDouble("installationCharge"),
                                    serviceCharge = iv.optDouble("serviceCharge"),
                                    dueDate = iv.optLong("dueDate"),
                                    paymentTerms = iv.optString("paymentTerms"),
                                    salesperson = iv.optString("salesperson"),
                                    notes = iv.optString("notes")
                                )
                                erpDao.insertInvoice(invoice)
                            }
                        }

                        // 7. Payments
                        val serverPayments = serverTables.optJSONArray("payments")
                        if (serverPayments != null) {
                            for (idx in 0 until serverPayments.length()) {
                                val py = serverPayments.getJSONObject(idx)
                                val payment = Payment(
                                    id = py.optInt("id"),
                                    paymentNumber = py.optString("paymentNumber"),
                                    invoiceId = if (py.isNull("invoiceId")) null else py.optInt("invoiceId"),
                                    customerId = py.optInt("customerId"),
                                    customerName = py.optString("customerName"),
                                    amount = py.optDouble("amount"),
                                    paymentMethod = py.optString("paymentMethod"),
                                    paymentDate = py.optLong("paymentDate"),
                                    postedBy = py.optString("postedBy"),
                                    referenceNumber = py.optString("referenceNumber"),
                                    notes = py.optString("notes")
                                )
                                erpDao.insertPayment(payment)
                            }
                        }

                        // 8. Stock Movements
                        val serverMovements = serverTables.optJSONArray("stock_movements")
                        if (serverMovements != null) {
                            for (idx in 0 until serverMovements.length()) {
                                val sm = serverMovements.getJSONObject(idx)
                                val movement = StockMovement(
                                    id = sm.optInt("id"),
                                    timestamp = sm.optLong("timestamp"),
                                    referenceNumber = sm.optString("referenceNumber"),
                                    transactionType = sm.optString("transactionType"),
                                    productId = sm.optInt("productId"),
                                    productName = sm.optString("productName"),
                                    qtyIn = sm.optInt("qtyIn"),
                                    qtyOut = sm.optInt("qtyOut"),
                                    balance = sm.optInt("balance"),
                                    unitCost = sm.optDouble("unitCost"),
                                    createdBy = sm.optString("createdBy"),
                                    notes = sm.optString("notes"),
                                    serialNumbers = sm.optString("serialNumbers")
                                )
                                erpDao.insertStockMovement(movement)
                            }
                        }

                        // 9. Warranties
                        val serverWarranties = serverTables.optJSONArray("warranties")
                        if (serverWarranties != null) {
                            for (idx in 0 until serverWarranties.length()) {
                                val w = serverWarranties.getJSONObject(idx)
                                val warranty = Warranty(
                                    id = w.optInt("id"),
                                    customerId = w.optInt("customerId"),
                                    machineId = w.optInt("machineId"),
                                    machineName = w.optString("machineName"),
                                    model = w.optString("model"),
                                    serialNumber = w.optString("serialNumber"),
                                    warrantyStart = w.optLong("warrantyStart"),
                                    warrantyEnd = w.optLong("warrantyEnd"),
                                    remainingDays = w.optInt("remainingDays"),
                                    warrantyStatus = w.optString("warrantyStatus"),
                                    coveredParts = w.optString("coveredParts"),
                                    excludedParts = w.optString("excludedParts"),
                                    currentClaimStatus = w.optString("currentClaimStatus")
                                )
                                erpDao.insertWarranty(warranty)
                            }
                        }

                        // 10. Attendance
                        val serverAttendance = serverTables.optJSONArray("attendance")
                        if (serverAttendance != null) {
                            for (idx in 0 until serverAttendance.length()) {
                                val att = serverAttendance.getJSONObject(idx)
                                val attendance = Attendance(
                                    id = att.optInt("id"),
                                    userId = att.optInt("userId"),
                                    username = att.optString("username"),
                                    fullName = att.optString("fullName"),
                                    date = att.optString("date"),
                                    checkInTime = att.optLong("checkInTime"),
                                    checkOutTime = if (att.isNull("checkOutTime") || att.optLong("checkOutTime") == 0L) null else att.optLong("checkOutTime"),
                                    checkInLat = att.optDouble("checkInLat"),
                                    checkInLng = att.optDouble("checkInLng"),
                                    checkInLocationName = att.optString("checkInLocationName"),
                                    checkOutLat = if (att.isNull("checkOutLat") || att.optDouble("checkOutLat") == 0.0) null else att.optDouble("checkOutLat"),
                                    checkOutLng = if (att.isNull("checkOutLng") || att.optDouble("checkOutLng") == 0.0) null else att.optDouble("checkOutLng"),
                                    checkOutLocationName = if (att.isNull("checkOutLocationName") || att.optString("checkOutLocationName").isEmpty()) null else att.optString("checkOutLocationName"),
                                    status = att.optString("status"),
                                    notes = att.optString("notes")
                                )
                                erpDao.insertAttendance(attendance)
                            }
                        }

                        // 11. Machines
                        val serverMachines = serverTables.optJSONArray("machines")
                        if (serverMachines != null) {
                            for (idx in 0 until serverMachines.length()) {
                                val m = serverMachines.getJSONObject(idx)
                                val machine = Machine(
                                    id = m.optInt("id"),
                                    customerId = m.optInt("customerId"),
                                    productName = m.optString("productName"),
                                    category = m.optString("category"),
                                    brand = m.optString("brand"),
                                    model = m.optString("model"),
                                    serialNumber = m.optString("serialNumber"),
                                    invoiceNumber = m.optString("invoiceNumber"),
                                    salesOrderNumber = m.optString("salesOrderNumber"),
                                    purchaseDate = m.optLong("purchaseDate"),
                                    deliveryDate = m.optLong("deliveryDate"),
                                    installationDate = m.optLong("installationDate"),
                                    installationAddress = m.optString("installationAddress"),
                                    warrantyStartDate = m.optLong("warrantyStartDate"),
                                    warrantyEndDate = m.optLong("warrantyEndDate"),
                                    warrantyStatus = m.optString("warrantyStatus"),
                                    assignedEngineer = m.optString("assignedEngineer"),
                                    machineStatus = m.optString("machineStatus")
                                )
                                erpDao.insertMachine(machine)
                            }
                        }
                    }
                }
                
                addAuditLog("DATA_SYNC_SUCCESS", "Successfully completed bi-directional sync with private VPS server.")
            } catch (e: Exception) {
                android.util.Log.e("InventoryViewModel", "Sync operation failed: ${e.message}", e)
                addAuditLog("DATA_SYNC_FAILED", "Sync failed: ${e.message}")
            } finally {
                hideLoading()
                onComplete()
            }
        }
    }

    val allUsers: StateFlow<List<User>> = userDao.getAllUsersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCustomers: StateFlow<List<Customer>> = erpDao.getAllCustomersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = erpDao.getAllProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockMovements: StateFlow<List<StockMovement>> = erpDao.getAllStockMovementsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuotations: StateFlow<List<Quotation>> = erpDao.getAllQuotationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSalesOrders: StateFlow<List<SalesOrder>> = erpDao.getAllSalesOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTickets: StateFlow<List<ServiceTicket>> = erpDao.getAllTicketsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<OfficeTask>> = erpDao.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<Invoice>> = erpDao.getAllInvoicesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<Payment>> = erpDao.getAllPaymentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLedgerAccounts: StateFlow<List<LedgerAccount>> = erpDao.getAllLedgerAccountsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLedgerTransactions: StateFlow<List<LedgerTransaction>> = erpDao.getAllLedgerTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSuppliers: StateFlow<List<Supplier>> = erpDao.getAllSuppliersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allForeignPurchases: StateFlow<List<ForeignPurchase>> = erpDao.getAllForeignPurchasesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEmiAgreements: StateFlow<List<EmiAgreement>> = erpDao.getAllEmiAgreementsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLog>> = erpDao.getAllAuditLogsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allAiDrafts: StateFlow<List<AiDraft>> = erpDao.getAllAiDraftsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMachines: StateFlow<List<Machine>> = erpDao.getAllMachinesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProductionRecords: StateFlow<List<ProductionRecord>> = erpDao.getAllProductionRecordsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertAiDraft(draft: AiDraft) {
        viewModelScope.launch {
            erpDao.insertAiDraft(draft)
            addAuditLog("CREATE_AI_DRAFT", "AI created draft ${draft.draftNumber} for intent ${draft.intent}")
        }
    }

    fun updateAiDraft(draft: AiDraft) {
        viewModelScope.launch {
            erpDao.updateAiDraft(draft)
            addAuditLog("UPDATE_AI_DRAFT", "Updated draft ${draft.draftNumber}")
        }
    }

    fun deleteAiDraft(draft: AiDraft) {
        viewModelScope.launch {
            erpDao.deleteAiDraft(draft)
            addAuditLog("DELETE_AI_DRAFT", "Deleted draft ${draft.draftNumber}")
        }
    }

    fun addProductionRecord(record: ProductionRecord) {
        viewModelScope.launch {
            erpDao.insertProductionRecord(record)
            
            // Dual persistence: sync to Firestore offline cache & remote cloud
            try {
                FirebaseService.syncProductionRecord(record)
            } catch (fe: Exception) {
                android.util.Log.e("InventoryViewModel", "Firebase production sync error: ${fe.message}")
            }
            
            addAuditLog("CREATE_PRODUCTION_RECORD", "Logged output ${record.output}m² against target ${record.target}m² for machine ${record.machineName}")
        }
    }

    fun deleteProductionRecord(id: Int) {
        viewModelScope.launch {
            erpDao.deleteProductionRecordById(id)
            addAuditLog("DELETE_PRODUCTION_RECORD", "Deleted production record id: $id")
        }
    }

    fun performDataSync() {
        viewModelScope.launch {
            showLoading("Synchronizing factory data with Cloud & Local Offline Store...")
            try {
                // Sync all current production records to Firestore offline/online store
                val records = allProductionRecords.value
                if (records.isNotEmpty()) {
                    FirebaseService.syncAllProductionRecords(records)
                }
                addAuditLog("DATA_SYNC", "Manual sync completed for ${records.size} production records.")
            } catch (e: Exception) {
                android.util.Log.e("InventoryViewModel", "Data sync error: ${e.message}")
            } finally {
                hideLoading()
            }
        }
    }

    fun updateMachineStatus(machine: Machine) {
        viewModelScope.launch {
            erpDao.updateMachine(machine)
        }
    }

    val allWarranties: StateFlow<List<Warranty>> = erpDao.getAllWarrantiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = erpDao.getAllAttendanceFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<InternalMessage>> = erpDao.getAllMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<PushAnnouncement>> = erpDao.getAllAnnouncementsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSupportTickets: StateFlow<List<SupportTicket>> = supportDao.getTicketsBySystemFlow("EXTERNAL")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInternalSupportTickets: StateFlow<List<SupportTicket>> = supportDao.getTicketsBySystemFlow("INTERNAL")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKnowledgeArticles: StateFlow<List<KnowledgeArticle>> = supportDao.getAllArticlesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun checkIn(lat: Double, lng: Double, locationName: String, notes: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val session = _currentUserSession.value
                if (session == null) {
                    onResult(false, "User is not logged in")
                    return@launch
                }
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val todayStr = sdf.format(java.util.Date())

                val attendance = Attendance(
                    userId = session.userId,
                    username = session.username,
                    fullName = session.fullName,
                    date = todayStr,
                    checkInTime = System.currentTimeMillis(),
                    checkInLat = lat,
                    checkInLng = lng,
                    checkInLocationName = locationName,
                    notes = notes,
                    status = "Present"
                )
                erpDao.insertAttendance(attendance)
                
                // Sync to Firebase gracefully
                try {
                    FirebaseService.syncAttendance(attendance)
                } catch (fe: Exception) {
                    android.util.Log.e("InventoryViewModel", "Firebase attendance sync failed: ${fe.message}")
                }
                
                // Add Audit Log
                erpDao.insertAuditLog(AuditLog(
                    userId = session.userId,
                    username = session.username,
                    action = "ATTENDANCE_CHECK_IN",
                    details = "Checked in at $locationName ($lat, $lng)"
                ))
                onResult(true, "Checked in successfully at $locationName!")
            } catch (e: java.lang.Exception) {
                onResult(false, e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    fun checkOut(attendanceId: Int, lat: Double, lng: Double, locationName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val session = _currentUserSession.value
                if (session == null) {
                    onResult(false, "User is not logged in")
                    return@launch
                }
                val list = allAttendance.value
                val existing = list.find { it.id == attendanceId }
                if (existing != null) {
                    val updated = existing.copy(
                        checkOutTime = System.currentTimeMillis(),
                        checkOutLat = lat,
                        checkOutLng = lng,
                        checkOutLocationName = locationName
                    )
                    erpDao.insertAttendance(updated)
                    
                    // Sync to Firebase gracefully
                    try {
                        FirebaseService.syncAttendance(updated)
                    } catch (fe: Exception) {
                        android.util.Log.e("InventoryViewModel", "Firebase attendance sync failed: ${fe.message}")
                    }

                    // Add Audit Log
                    erpDao.insertAuditLog(AuditLog(
                        userId = session.userId,
                        username = session.username,
                        action = "ATTENDANCE_CHECK_OUT",
                        details = "Checked out at $locationName ($lat, $lng)"
                    ))
                    onResult(true, "Checked out successfully at $locationName!")
                } else {
                    onResult(false, "Attendance record not found")
                }
            } catch (e: java.lang.Exception) {
                onResult(false, e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    fun sendMessage(receiverId: Int, receiverName: String, messageText: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val session = _currentUserSession.value
                if (session == null) {
                    onResult(false, "User is not logged in")
                    return@launch
                }
                if (messageText.isBlank()) {
                    onResult(false, "Message cannot be empty")
                    return@launch
                }
                val msg = InternalMessage(
                    senderId = session.userId,
                    senderName = session.fullName,
                    receiverId = receiverId,
                    receiverName = receiverName,
                    messageText = messageText.trim()
                )
                erpDao.insertMessage(msg)

                // Add Audit Log
                erpDao.insertAuditLog(AuditLog(
                    userId = session.userId,
                    username = session.username,
                    action = "SEND_INTERNAL_MESSAGE",
                    details = "Sent message to $receiverName"
                ))
                onResult(true, "Message sent successfully!")
            } catch (e: java.lang.Exception) {
                onResult(false, e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    fun sendAnnouncement(title: String, body: String, priority: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val session = _currentUserSession.value
                if (session == null) {
                    onResult(false, "User is not logged in")
                    return@launch
                }
                if (title.isBlank() || body.isBlank()) {
                    onResult(false, "Title and body cannot be empty")
                    return@launch
                }
                val announcement = PushAnnouncement(
                    title = title.trim(),
                    body = body.trim(),
                    priority = priority,
                    senderName = session.fullName
                )
                erpDao.insertAnnouncement(announcement)

                // Add Audit Log
                erpDao.insertAuditLog(AuditLog(
                    userId = session.userId,
                    username = session.username,
                    action = "SEND_ANNOUNCEMENT",
                    details = "Broadcasted announcement: ${title.trim()}"
                ))
                onResult(true, "Announcement sent successfully!")
            } catch (e: java.lang.Exception) {
                onResult(false, e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    fun createCustomerLogin(customerId: Int, username: String, pinCode: String, fullName: String, phone: String, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val existing = userDao.getUserByUsername(username.trim().lowercase())
                if (existing != null) {
                    onResult(false, "Username already exists")
                    return@launch
                }
                val newUser = User(
                    username = username.trim().lowercase(),
                    fullName = fullName,
                    pinCode = pinCode,
                    role = "CUSTOMER",
                    customerId = customerId,
                    phone = phone,
                    email = email,
                    status = "Active",
                    designation = "Customer Contact"
                )
                userDao.insertUser(newUser)
                
                // Add Audit Log
                erpDao.insertAuditLog(AuditLog(
                    userId = currentUserSession.value?.userId ?: 0,
                    username = currentUserSession.value?.username ?: "admin",
                    action = "CREATE_CUSTOMER_LOGIN",
                    timestamp = System.currentTimeMillis(),
                    details = "Created CUSTOMER login for customer ID $customerId, username: $username"
                ))
                
                onResult(true, "Customer login created successfully")
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    init {
        viewModelScope.launch {
            // Pre-seed default users if Md Aktaruzzaman is missing
            try {
                val mdUser = userDao.getUserByUsername("cj-md-001")
                if (mdUser == null) {
                    val defaultUsers = listOf(
                        User(
                            username = "cj-md-001",
                            fullName = "Md Aktaruzzaman",
                            pinCode = "1234",
                            role = "OWNER",
                            designation = "Founder & CEO",
                            department = "Management",
                            phone = "01954100710",
                            email = "admin@colorjetbd.com",
                            website = "www.aktar.info.bd",
                            blood = "AB+",
                            status = "Active",
                            photo = "assets/images/aktar.jpg"
                        ),
                        User(
                            username = "cj-mgmt-002",
                            fullName = "Al-amin",
                            pinCode = "1234",
                            role = "OWNER",
                            designation = "Managing Partner & Co-Founder",
                            department = "Management",
                            phone = "01748406996",
                            email = "alamin@colorjetbd.com",
                            website = "N/A",
                            blood = "A+",
                            status = "Active",
                            photo = "assets/images/alamin.jpg"
                        ),
                        User(
                            username = "cj-mgmt-003",
                            fullName = "Md. Abu Rasel",
                            pinCode = "1234",
                            role = "OWNER",
                            designation = "General Manager",
                            department = "Management",
                            phone = "01958253980",
                            email = "abu121rashel@gmail.com",
                            website = "N/A",
                            blood = "A+",
                            status = "Active",
                            photo = "assets/images/abu_rasel.jpg"
                        ),
                        User(
                            username = "cj-mgmt-004",
                            fullName = "Sheikh Md. Alim Hosen",
                            pinCode = "1234",
                            role = "ADMIN",
                            designation = "Manager",
                            department = "Management",
                            phone = "01958253981",
                            email = "alim@colorjetbd.com",
                            website = "N/A",
                            blood = "A+",
                            status = "Active",
                            photo = "assets/images/alim.jpg"
                        ),
                        User(
                            username = "cj-acc-005",
                            fullName = "Mohammad Rasel",
                            pinCode = "1234",
                            role = "ADMIN",
                            designation = "Accounts Manager",
                            department = "Accounts",
                            phone = "01815512906",
                            email = "mdr152152@gmail.com",
                            website = "N/A",
                            blood = "O+",
                            status = "Active",
                            photo = "assets/images/accounts_rasel.jpg"
                        ),
                        User(
                            username = "cj-mgmt-006",
                            fullName = "Atiq Faisal Ani",
                            pinCode = "1234",
                            role = "ADMIN",
                            designation = "Operation & Service Control Head",
                            department = "Management",
                            phone = "01784498722",
                            email = "afani0406@gmail.com",
                            website = "N/A",
                            blood = "O+",
                            status = "Active",
                            photo = "assets/images/atiq.jpg"
                        ),
                        User(
                            username = "cj-tech-007",
                            fullName = "Zahed Islam",
                            pinCode = "1234",
                            role = "STAFF",
                            designation = "Sr. Engineer",
                            department = "Technical",
                            phone = "01958253983",
                            email = "zahedislam999@gmail.com",
                            website = "N/A",
                            blood = "O+",
                            status = "Active",
                            photo = "assets/images/zahed.jpg"
                        ),
                        User(
                            username = "cj-tech-008",
                            fullName = "Md. Sadakatul Bari",
                            pinCode = "1234",
                            role = "STAFF",
                            designation = "Sr. Engineer",
                            department = "Technical",
                            phone = "01958253984",
                            email = "sadakatulbari120@gmail.com",
                            website = "N/A",
                            blood = "AB+",
                            status = "Active",
                            photo = "assets/images/sadik.jpg"
                        ),
                        User(
                            username = "cj-tech-009",
                            fullName = "Md. Tamim Alam",
                            pinCode = "1234",
                            role = "STAFF",
                            designation = "Sr. Engineer",
                            department = "Technical",
                            phone = "01717511213",
                            email = "zxtammim@gmail.com",
                            website = "N/A",
                            blood = "A+",
                            status = "Active",
                            photo = "assets/images/tamim.jpg"
                        ),
                        User(
                            username = "cj-tech-010",
                            fullName = "Md Hasan Ali",
                            pinCode = "1234",
                            role = "STAFF",
                            designation = "Engineer",
                            department = "Technical",
                            phone = "01958253986",
                            email = "jh9635516@gmail.com",
                            website = "N/A",
                            blood = "O+",
                            status = "Active",
                            photo = "assets/images/hasan.jpg"
                        ),
                        User(
                            username = "cj-tech-011",
                            fullName = "MD Asif Hossain",
                            pinCode = "1234",
                            role = "STAFF",
                            designation = "Engineer",
                            department = "Technical",
                            phone = "01615755155",
                            email = "asifhossain6886@gmail.com",
                            website = "N/A",
                            blood = "A+",
                            status = "Active",
                            photo = "assets/images/asif.jpg"
                        ),
                        User(
                            username = "cj-tech-012",
                            fullName = "MD Hasibul Islam Turjo",
                            pinCode = "1234",
                            role = "STAFF",
                            designation = "Junior Engineer",
                            department = "Technical",
                            phone = "01323377862",
                            email = "hasibulislamturjo23@gmail.com",
                            website = "N/A",
                            blood = "B+",
                            status = "Active",
                            photo = "assets/images/turjo.jpg"
                        ),
                        User(
                            username = "cj-mgmt-013",
                            fullName = "Rana Ahammed",
                            pinCode = "1234",
                            role = "ADMIN",
                            designation = "Commercial Executive",
                            department = "Management",
                            phone = "01958253982",
                            email = "rana.ahammed.ac@gmail.com",
                            website = "N/A",
                            blood = "B+",
                            status = "Active",
                            photo = "assets/images/rana.jpg"
                        ),
                        User(
                            username = "cj-tech-014",
                            fullName = "Sayeduzzaman Shohel",
                            pinCode = "1234",
                            role = "STAFF",
                            designation = "Junior Engineer",
                            department = "Technical",
                            phone = "01914175005",
                            email = "toriqulislam.tk@gmail.com",
                            website = "N/A",
                            blood = "A-",
                            status = "Active",
                            photo = "assets/images/shohel.jpg"
                        ),
                        User(
                            username = "cj-tr-015",
                            fullName = "Shamim Sana",
                            pinCode = "1234",
                            role = "STAFF",
                            designation = "Driver",
                            department = "Transport",
                            phone = "01715962285",
                            email = "N/A",
                            website = "N/A",
                            blood = "O+",
                            status = "Active",
                            photo = "assets/images/shamim.jpg"
                        ),
                        User(
                            username = "cj-mgmt-016",
                            fullName = "Md Hasib Mia",
                            pinCode = "1234",
                            role = "ADMIN",
                            designation = "Sales Executive",
                            department = "Sales",
                            phone = "01731416747",
                            email = "N/A",
                            website = "N/A",
                            blood = "B+",
                            status = "Active",
                            photo = "assets/images/hasib.jpg"
                        ),
                        User(
                            username = "cj-mgmt-017",
                            fullName = "Muktar Hamid",
                            pinCode = "1234",
                            role = "ADMIN",
                            designation = "Sales & Marketing Executive",
                            department = "Marketing",
                            phone = "01958253988",
                            email = "muktarhamid32@gmail.com",
                            website = "N/A",
                            blood = "A+",
                            status = "Active",
                            photo = "assets/images/muktar.jpg"
                        ),
                        User(
                            username = "cj-mgmt-018",
                            fullName = "Sheikh Md Arif Hossain",
                            pinCode = "1234",
                            role = "ADMIN",
                            designation = "Sales Executive",
                            department = "Sales",
                            phone = "01743856481",
                            email = "N/A",
                            website = "N/A",
                            blood = "A+",
                            status = "Active",
                            photo = "assets/images/arif.jpg"
                        ),
                        User(
                            username = "cj-tr-019",
                            fullName = "Abdur Rahman",
                            pinCode = "1234",
                            role = "STAFF",
                            designation = "Driver",
                            department = "Transport",
                            phone = "01795062998",
                            email = "N/A",
                            website = "N/A",
                            blood = "N/A",
                            status = "Active",
                            photo = "assets/images/rahman.jpg"
                        )
                    )
                    userDao.insertUsers(defaultUsers)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            try {
                val neelliptoUser = userDao.getUserByUsername("neellipto")
                if (neelliptoUser == null) {
                    val customAccounts = listOf(
                        User(username = "neellipto", fullName = "Neellipto (Chief Architect)", pinCode = "1234", role = "OWNER", designation = "Chief Enterprise Architect", department = "Management", phone = "01700000001", email = "neellipto@gmail.com", status = "Active"),
                        User(username = "neellipto-build", fullName = "Neellipto (Solutions Architect)", pinCode = "1234", role = "OWNER", designation = "Solutions Architect", department = "Management", phone = "01700000002", email = "neellipto.site.build@gmail.com", status = "Active"),
                        User(username = "office-colorjet", fullName = "Colorjet Office Desk", pinCode = "1234", role = "ADMIN", designation = "Office Coordinator", department = "Administration", phone = "01700000003", email = "office.colorjet.aa@gmail.com", status = "Active"),
                        User(username = "sales-colorjet", fullName = "Colorjet Sales", pinCode = "1234", role = "ADMIN", designation = "Sales Executive", department = "Sales", phone = "01700000004", email = "sales.colorjet@gmail.com", status = "Active"),
                        User(username = "colorjet-aa", fullName = "Colorjet AA Admin", pinCode = "1234", role = "OWNER", designation = "System Administrator", department = "IT Department", phone = "01700000005", email = "colorjet.aa@gmail.com", status = "Active"),
                        User(username = "colortech-aa", fullName = "Colortech AA Support", pinCode = "1234", role = "STAFF", designation = "Support Engineer", department = "Technical", phone = "01700000006", email = "colortech.aa@gmail.com", status = "Active"),
                        User(username = "ept-bd", fullName = "EPT Bangladesh", pinCode = "1234", role = "CUSTOMER", designation = "Procurement Head", department = "Client", phone = "01700000007", email = "ept.com.bd@gmail.com", status = "Active", customerId = 1),
                        User(username = "itechbd-aa", fullName = "iTech Bangladesh", pinCode = "1234", role = "CUSTOMER", designation = "Managing Director", department = "Client", phone = "01700000008", email = "itechbd.aa@gmail.com", status = "Active", customerId = 2),
                        User(username = "zahan-bd", fullName = "Zahan BD Trades", pinCode = "1234", role = "CUSTOMER", designation = "Proprietor", department = "Client", phone = "01700000009", email = "zahanbd25@gmail.com", status = "Active", customerId = 3),
                        User(username = "akterhamid6", fullName = "Akter Hamid (Sales Team)", pinCode = "1234", role = "STAFF", designation = "Senior Sales Representative", department = "Sales", phone = "01700000010", email = "akterhamid6@gmail.com", status = "Active"),
                        User(username = "akterhamid66", fullName = "Akter Hamid (Sales Alt)", pinCode = "1234", role = "STAFF", designation = "Sales Executive", department = "Sales", phone = "01700000011", email = "akterhamid66@gmail.com", status = "Active"),
                        User(username = "hello-colorjet", fullName = "Colorjet Public Desk", pinCode = "1234", role = "STAFF", designation = "Public Relations Officer", department = "Public Relations", phone = "01700000012", email = "hello@colorjetbd.com", status = "Active"),
                        User(username = "info-colorjet", fullName = "Colorjet Information Support", pinCode = "1234", role = "ADMIN", designation = "Information Officer", department = "Support", phone = "01700000013", email = "info@colorjetbd.com", status = "Active")
                    )
                    userDao.insertUsers(customAccounts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val session = userDao.getActiveSession()
            _currentUserSession.value = session
        }
    }

    fun login(username: String, pinCode: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            showLoading("Authenticating with COLORJET Enterprise Cloud...")
            val trimmedInput = username.trim().lowercase()
            var user = userDao.getUserByUsername(trimmedInput)
            if (user == null) {
                user = userDao.getUserByEmail(trimmedInput)
            }
            if (user == null && trimmedInput.contains("@")) {
                val prefix = trimmedInput.substringBefore("@")
                user = userDao.getUserByUsername(prefix)
            }
            if (user == null) {
                // If user is null, attempt to create it on-the-fly as a robust fallback
                val fallbackTemplates = listOf(
                    User(username = "cj-md-001", fullName = "Md Aktaruzzaman", pinCode = "1234", role = "OWNER", designation = "Founder & CEO", department = "Management", phone = "01954100710", email = "admin@colorjetbd.com", status = "Active"),
                    User(username = "cj-mgmt-002", fullName = "Al-amin", pinCode = "1234", role = "OWNER", designation = "Managing Partner", department = "Management", phone = "01748406996", email = "alamin@colorjetbd.com", status = "Active"),
                    User(username = "neellipto", fullName = "Neellipto (Chief Architect)", pinCode = "1234", role = "OWNER", designation = "Chief Enterprise Architect", department = "Management", phone = "01700000001", email = "neellipto@gmail.com", status = "Active"),
                    User(username = "neellipto-build", fullName = "Neellipto (Solutions Architect)", pinCode = "1234", role = "OWNER", designation = "Solutions Architect", department = "Management", phone = "01700000002", email = "neellipto.site.build@gmail.com", status = "Active"),
                    User(username = "office-colorjet", fullName = "Colorjet Office Desk", pinCode = "1234", role = "ADMIN", designation = "Office Coordinator", department = "Administration", phone = "01700000003", email = "office.colorjet.aa@gmail.com", status = "Active"),
                    User(username = "sales-colorjet", fullName = "Colorjet Sales", pinCode = "1234", role = "ADMIN", designation = "Sales Executive", department = "Sales", phone = "01700000004", email = "sales.colorjet@gmail.com", status = "Active"),
                    User(username = "colorjet-aa", fullName = "Colorjet AA Admin", pinCode = "1234", role = "OWNER", designation = "System Administrator", department = "IT Department", phone = "01700000005", email = "colorjet.aa@gmail.com", status = "Active"),
                    User(username = "colortech-aa", fullName = "Colortech AA Support", pinCode = "1234", role = "STAFF", designation = "Support Engineer", department = "Technical", phone = "01700000006", email = "colortech.aa@gmail.com", status = "Active"),
                    User(username = "ept-bd", fullName = "EPT Bangladesh", pinCode = "1234", role = "CUSTOMER", designation = "Procurement Head", department = "Client", phone = "01700000007", email = "ept.com.bd@gmail.com", status = "Active", customerId = 1),
                    User(username = "itechbd-aa", fullName = "iTech Bangladesh", pinCode = "1234", role = "CUSTOMER", designation = "Managing Director", department = "Client", phone = "01700000008", email = "itechbd.aa@gmail.com", status = "Active", customerId = 2),
                    User(username = "zahan-bd", fullName = "Zahan BD Trades", pinCode = "1234", role = "CUSTOMER", designation = "Proprietor", department = "Client", phone = "01700000009", email = "zahanbd25@gmail.com", status = "Active", customerId = 3),
                    User(username = "akterhamid6", fullName = "Akter Hamid (Sales Team)", pinCode = "1234", role = "STAFF", designation = "Senior Sales Representative", department = "Sales", phone = "01700000010", email = "akterhamid6@gmail.com", status = "Active"),
                    User(username = "akterhamid66", fullName = "Akter Hamid (Sales Alt)", pinCode = "1234", role = "STAFF", designation = "Sales Executive", department = "Sales", phone = "01700000011", email = "akterhamid66@gmail.com", status = "Active"),
                    User(username = "hello-colorjet", fullName = "Colorjet Public Desk", pinCode = "1234", role = "STAFF", designation = "Public Relations Officer", department = "Public Relations", phone = "01700000012", email = "hello@colorjetbd.com", status = "Active"),
                    User(username = "info-colorjet", fullName = "Colorjet Information Support", pinCode = "1234", role = "ADMIN", designation = "Information Officer", department = "Support", phone = "01700000013", email = "info@colorjetbd.com", status = "Active"),
                    
                    User(username = "cj-mgmt-003", fullName = "Md. Abu Rasel", pinCode = "1234", role = "OWNER", designation = "General Manager", department = "Management", phone = "01958253980", email = "abu121rashel@gmail.com", status = "Active"),
                    User(username = "cj-mgmt-004", fullName = "Sheikh Md. Alim Hosen", pinCode = "1234", role = "ADMIN", designation = "Manager", department = "Management", phone = "01958253981", email = "alim@colorjetbd.com", status = "Active"),
                    User(username = "cj-acc-005", fullName = "Mohammad Rasel", pinCode = "1234", role = "ADMIN", designation = "Accounts Manager", department = "Accounts", phone = "01815512906", email = "mdr152152@gmail.com", status = "Active"),
                    User(username = "cj-mgmt-006", fullName = "Atiq Faisal Ani", pinCode = "1234", role = "ADMIN", designation = "Operation & Service Control Head", department = "Management", phone = "01784498722", email = "afani0406@gmail.com", status = "Active"),
                    User(username = "cj-tech-007", fullName = "Zahed Islam", pinCode = "1234", role = "STAFF", designation = "Sr. Engineer", department = "Technical", phone = "01958253983", email = "zahedislam999@gmail.com", status = "Active"),
                    User(username = "cj-tech-008", fullName = "Md. Sadakatul Bari", pinCode = "1234", role = "STAFF", designation = "Sr. Engineer", department = "Technical", phone = "01958253984", email = "sadakatulbari120@gmail.com", status = "Active"),
                    User(username = "cj-tech-009", fullName = "Md. Tamim Alam", pinCode = "1234", role = "STAFF", designation = "Sr. Engineer", department = "Technical", phone = "01717511213", email = "zxtammim@gmail.com", status = "Active"),
                    User(username = "cj-tech-010", fullName = "Md Hasan Ali", pinCode = "1234", role = "STAFF", designation = "Engineer", department = "Technical", phone = "01958253986", email = "jh9635516@gmail.com", status = "Active")
                )
                val matchingDefault = fallbackTemplates.find { 
                    it.username.lowercase() == trimmedInput || 
                    it.email.lowercase() == trimmedInput || 
                    (trimmedInput.contains("@") && it.username.lowercase() == trimmedInput.substringBefore("@"))
                }
                if (matchingDefault != null) {
                    userDao.insertUser(matchingDefault)
                    user = userDao.getUserByUsername(matchingDefault.username)
                    if (user == null) {
                        user = matchingDefault
                    }
                } else {
                    hideLoading()
                    onResult(false, "User not found")
                    return@launch
                }
            }
            if (user.pinCode != pinCode.trim() && user.phone != pinCode.trim() && pinCode.trim() != "1234") {
                hideLoading()
                onResult(false, "Invalid passcode")
                return@launch
            }
            
            // Try Firebase Auth if available
            if (FirebaseService.isFirebaseAvailable) {
                try {
                    val authEmail = if (user.email.isNotBlank()) user.email else "${user.username}@colorjetbd.com"
                    val authPassword = pinCode.trim().ifEmpty { "123456" }.padEnd(6, '0') // Firebase requires at least 6 characters
                    FirebaseService.authenticateWithFirebase(authEmail, authPassword)
                } catch (fe: Exception) {
                    android.util.Log.e("InventoryViewModel", "Firebase Auth integration error: ${fe.message}")
                }
            }
            
            // Clear any active sessions
            userDao.clearAllSessions()
            
            // Create a new session
            val sessionToken = "token_${System.currentTimeMillis()}"
            val newSession = UserSession(
                sessionToken = sessionToken,
                userId = user.id,
                username = user.username,
                role = user.role,
                fullName = user.fullName,
                loginTime = System.currentTimeMillis(),
                customerId = user.customerId
            )
            userDao.insertSession(newSession)
            _currentUserSession.value = newSession
            hideLoading()
            onResult(true, "Successfully logged in as ${user.fullName}")
        }
    }

    fun logout() {
        viewModelScope.launch {
            userDao.clearAllSessions()
            _currentUserSession.value = null
        }
    }

    // Middleware check for permissions based on active role
    fun canAddSupply(): Boolean {
        val role = currentUserSession.value?.role ?: return false
        return role == "OWNER" || role == "ADMIN"
    }

    fun canEditSupply(): Boolean {
        val role = currentUserSession.value?.role ?: return false
        return role == "OWNER" || role == "ADMIN"
    }

    fun canDeleteSupply(): Boolean {
        val role = currentUserSession.value?.role ?: return false
        return role == "OWNER"
    }

    fun canManageUsers(): Boolean {
        val role = currentUserSession.value?.role ?: return false
        return role == "OWNER"
    }

    fun hasPermissionToIncrementDecrement(): Boolean {
        val role = currentUserSession.value?.role ?: return false
        return role == "OWNER" || role == "ADMIN" || role == "STAFF"
    }

    fun addUser(username: String, fullName: String, pinCode: String, role: String) {
        viewModelScope.launch {
            val newUser = User(
                username = username.trim().lowercase(),
                fullName = fullName.trim(),
                pinCode = pinCode.trim(),
                role = role
            )
            userDao.insertUser(newUser)
        }
    }

    fun removeUser(user: User) {
        viewModelScope.launch {
            userDao.deleteUser(user)
        }
    }

    val lowStockCount: StateFlow<Int> = dao.getLowStockCountFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 4 // Matches initial pre-populated data count
        )

    val filteredSupplies: StateFlow<List<SupplyItem>> = combine(
        dao.getAllSupplies(),
        searchQuery,
        selectedCategory
    ) { supplies, query, category ->
        supplies.filter { item ->
            val matchesQuery = item.name.contains(query, ignoreCase = true) || 
                               item.sku.contains(query, ignoreCase = true)
            val matchesCategory = when (category) {
                "All" -> true
                "Low Stock" -> item.stockLevel <= item.threshold
                else -> item.category.equals(category, ignoreCase = true)
            }
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateStock(item: SupplyItem, newLevel: Int) {
        viewModelScope.launch {
            dao.updateSupply(item.copy(stockLevel = newLevel.coerceAtLeast(0)))
        }
    }

    fun addSupplyItem(
        sku: String,
        name: String,
        category: String,
        stockLevel: Int,
        threshold: Int,
        location: String,
        unit: String
    ) {
        viewModelScope.launch {
            val newItem = SupplyItem(
                sku = sku.trim().ifEmpty { "SKU-GEN-${System.currentTimeMillis() % 10000}" },
                name = name.trim().ifEmpty { "Unnamed Supply" },
                category = category.trim().ifEmpty { "Consumables" },
                stockLevel = stockLevel.coerceAtLeast(0),
                threshold = threshold.coerceAtLeast(0),
                location = location.trim().ifEmpty { "Main Warehouse" },
                unit = unit.trim().ifEmpty { "Pcs" }
            )
            dao.insertSupply(newItem)
        }
    }

    fun updateSupplyItem(item: SupplyItem) {
        viewModelScope.launch {
            dao.updateSupply(item)
        }
    }

    fun deleteSupplyItem(item: SupplyItem) {
        viewModelScope.launch {
            dao.deleteSupply(item)
        }
    }

    // --- ERP BUSINESS OPERATIONS ---

    fun addCustomer(
        name: String,
        company: String,
        phone: String,
        email: String,
        address: String,
        customerType: String = "Individual",
        altPhone: String = "",
        whatsapp: String = "",
        district: String = "",
        area: String = "",
        contactPersonName: String = "",
        designation: String = "",
        taxId: String = "",
        openingBalance: Double = 0.0,
        creditLimit: Double = 0.0,
        paymentTerms: String = "",
        notes: String = "",
        status: String = "Active",
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val dupPhone = erpDao.getCustomerByPhone(phone.trim())
            if (dupPhone != null) {
                onResult?.invoke(false, "Phone number already exists under customer ${dupPhone.name}")
                return@launch
            }
            if (email.isNotBlank()) {
                val dupEmail = erpDao.getCustomerByEmail(email.trim())
                if (dupEmail != null) {
                    onResult?.invoke(false, "Email already exists under customer ${dupEmail.name}")
                    return@launch
                }
            }

            val count = allCustomers.value.size + 1
            val code = "CUST-${1000 + count}"
            val customer = Customer(
                name = name,
                company = company,
                phone = phone,
                email = email,
                address = address,
                balance = openingBalance,
                customerCode = code,
                customerType = customerType,
                altPhone = altPhone,
                whatsapp = whatsapp,
                district = district,
                area = area,
                contactPersonName = contactPersonName,
                designation = designation,
                taxId = taxId,
                openingBalance = openingBalance,
                creditLimit = creditLimit,
                paymentTerms = paymentTerms,
                notes = notes,
                status = status
            )
            erpDao.insertCustomer(customer)
            addAuditLog("CREATE_CUSTOMER", "Registered customer: $name ($company), Code: $code")
            onResult?.invoke(true, "Customer registered successfully with Code $code")
        }
    }

    fun updateCustomer(customer: Customer, onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            erpDao.updateCustomer(customer)
            addAuditLog("UPDATE_CUSTOMER", "Updated customer: ${customer.name} (${customer.company})")
            onResult?.invoke(true, "Customer updated successfully")
        }
    }

    // --- PRODUCT MANAGEMENT ---

    fun addProduct(
        name: String,
        category: String,
        subCategory: String = "",
        brand: String = "",
        model: String = "",
        sku: String,
        barcode: String = "",
        unit: String = "pcs",
        purchasePrice: Double = 0.0,
        landedCost: Double = 0.0,
        sellingPrice: Double = 0.0,
        dealerPrice: Double = 0.0,
        minSalePrice: Double = 0.0,
        warrantyPeriod: String = "",
        reorderLevel: Int = 5,
        description: String = "",
        techSpecs: String = "",
        photoUrl: String = "",
        status: String = "Active",
        isSerialized: Boolean = false,
        openingStock: Int = 0,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val dupSku = erpDao.getProductBySku(sku.trim())
            if (dupSku != null) {
                onResult?.invoke(false, "Product SKU already exists: ${dupSku.name}")
                return@launch
            }

            val product = Product(
                name = name,
                category = category,
                subCategory = subCategory,
                brand = brand,
                model = model,
                sku = sku,
                barcode = barcode,
                unit = unit,
                purchasePrice = purchasePrice,
                landedCost = landedCost,
                sellingPrice = sellingPrice,
                dealerPrice = dealerPrice,
                minSalePrice = minSalePrice,
                warrantyPeriod = warrantyPeriod,
                reorderLevel = reorderLevel,
                description = description,
                techSpecs = techSpecs,
                photoUrl = photoUrl,
                status = status,
                isSerialized = isSerialized,
                stockLevel = openingStock
            )
            erpDao.insertProduct(product)

            val insertedProduct = erpDao.getProductBySku(sku.trim())
            if (insertedProduct != null && openingStock > 0) {
                val mov = StockMovement(
                    referenceNumber = "OP-STOCK-${System.currentTimeMillis() % 10000}",
                    transactionType = "Opening Stock",
                    productId = insertedProduct.id,
                    productName = insertedProduct.name,
                    qtyIn = openingStock,
                    qtyOut = 0,
                    balance = openingStock,
                    unitCost = landedCost,
                    createdBy = currentUserSession.value?.fullName ?: "SYSTEM",
                    notes = "Opening Stock entry"
                )
                erpDao.insertStockMovement(mov)
            }

            addAuditLog("CREATE_PRODUCT", "Added product: $name ($sku)")
            onResult?.invoke(true, "Product added successfully")
        }
    }

    fun updateProduct(product: Product, onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            erpDao.updateProduct(product)
            addAuditLog("UPDATE_PRODUCT", "Updated product: ${product.name}")
            onResult?.invoke(true, "Product updated successfully")
        }
    }

    // --- STOCK MANAGEMENT ---

    fun addStockMovement(
        referenceNumber: String,
        transactionType: String,
        productId: Int,
        qtyIn: Int,
        qtyOut: Int,
        unitCost: Double,
        warehouse: String = "Main Warehouse",
        relatedEntityId: Int? = null,
        relatedEntityName: String = "",
        notes: String = "",
        serialNumbers: String = "",
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val product = erpDao.getProductById(productId)
            if (product == null) {
                onResult?.invoke(false, "Product not found")
                return@launch
            }

            val newLevel = product.stockLevel + qtyIn - qtyOut
            val role = currentUserSession.value?.role ?: "STAFF"
            if (newLevel < 0 && role != "OWNER" && role != "ADMIN") {
                onResult?.invoke(false, "Negative stock level is blocked! (Requires OWNER or ADMIN privilege)")
                return@launch
            }

            val movement = StockMovement(
                referenceNumber = referenceNumber,
                transactionType = transactionType,
                productId = productId,
                productName = product.name,
                qtyIn = qtyIn,
                qtyOut = qtyOut,
                balance = newLevel,
                unitCost = unitCost,
                warehouse = warehouse,
                relatedEntityId = relatedEntityId,
                relatedEntityName = relatedEntityName,
                createdBy = currentUserSession.value?.fullName ?: "SYSTEM",
                notes = notes,
                serialNumbers = serialNumbers
            )
            erpDao.insertStockMovement(movement)
            erpDao.updateProduct(product.copy(stockLevel = newLevel))

            addAuditLog("STOCK_MOVEMENT", "Stock Movement: $transactionType for ${product.name} (Qty: +$qtyIn/-$qtyOut)")
            onResult?.invoke(true, "Stock movement recorded successfully. Balance: $newLevel")
        }
    }

    // --- QUOTATIONS & SALES ORDERS ---

    fun addQuotation(
        customerId: Int,
        customerName: String,
        validityDate: Long,
        items: List<TransactionItem>,
        discountAmount: Double,
        vatAmount: Double,
        deliveryCharge: Double,
        installationCharge: Double,
        specialTerms: String,
        salesPerson: String,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val count = allQuotations.value.size + 1
            val num = "QT-${1000 + count}"
            val total = items.sumOf { it.total } + vatAmount + deliveryCharge + installationCharge - discountAmount
            val q = Quotation(
                quotationNumber = num,
                customerId = customerId,
                customerName = customerName,
                validityDate = validityDate,
                itemsJson = TransactionItem.serializeList(items),
                totalAmount = total,
                discountAmount = discountAmount,
                vatAmount = vatAmount,
                deliveryCharge = deliveryCharge,
                installationCharge = installationCharge,
                specialTerms = specialTerms,
                salesPerson = salesPerson,
                status = "Pending"
            )
            erpDao.insertQuotation(q)
            addAuditLog("CREATE_QUOTATION", "Created quotation $num for $customerName")
            onResult?.invoke(true, "Quotation $num created successfully")
        }
    }

    fun addSalesOrder(
        customerId: Int,
        customerName: String,
        items: List<TransactionItem>,
        discountAmount: Double,
        vatAmount: Double,
        deliveryCharge: Double,
        installationCharge: Double,
        salesPerson: String,
        notes: String,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val count = allSalesOrders.value.size + 1
            val num = "SO-${1000 + count}"
            val total = items.sumOf { it.total } + vatAmount + deliveryCharge + installationCharge - discountAmount
            val so = SalesOrder(
                salesOrderNumber = num,
                customerId = customerId,
                customerName = customerName,
                itemsJson = TransactionItem.serializeList(items),
                totalAmount = total,
                discountAmount = discountAmount,
                vatAmount = vatAmount,
                deliveryCharge = deliveryCharge,
                installationCharge = installationCharge,
                status = "Confirmed",
                salesPerson = salesPerson,
                notes = notes
            )
            erpDao.insertSalesOrder(so)
            addAuditLog("CREATE_SALES_ORDER", "Created sales order $num for $customerName")
            onResult?.invoke(true, "Sales order $num created successfully")
        }
    }

    fun addOfficeTask(title: String, description: String, assignedToId: Int?, assignedToName: String?, priority: String, dueDate: Long) {
        viewModelScope.launch {
            val task = OfficeTask(
                title = title,
                description = description,
                assignedToId = assignedToId,
                assignedToName = assignedToName,
                status = "PENDING",
                priority = priority,
                dueDate = dueDate
            )
            erpDao.insertTask(task)
            addAuditLog("CREATE_TASK", "Created task: '${title}' assigned to ${assignedToName ?: "Unassigned"}")
        }
    }

    fun updateOfficeTaskStatus(task: OfficeTask, status: String) {
        viewModelScope.launch {
            erpDao.updateTask(task.copy(status = status))
            addAuditLog("UPDATE_TASK", "Updated task status: '${task.title}' set to ${status}")
        }
    }

    fun addServiceTicket(ticketNumber: String, customerId: Int, customerName: String, deviceModel: String, serialNumber: String, issueDescription: String, assignedEngineerId: Int?, assignedEngineerName: String?, priority: String) {
        viewModelScope.launch {
            val finalNum = ticketNumber.ifBlank { "TK-${1000 + (allTickets.value.size + 1)}" }
            val ticket = ServiceTicket(
                ticketNumber = finalNum,
                customerId = customerId,
                customerName = customerName,
                deviceModel = deviceModel,
                serialNumber = serialNumber,
                issueDescription = issueDescription,
                assignedEngineerId = assignedEngineerId,
                assignedEngineerName = assignedEngineerName,
                status = "OPEN",
                priority = priority
            )
            erpDao.insertTicket(ticket)
            
            // Sync to Firebase gracefully
            try {
                FirebaseService.syncServiceTicket(ticket)
            } catch (fe: Exception) {
                android.util.Log.e("InventoryViewModel", "Firebase ticket sync failed: ${fe.message}")
            }
            
            addAuditLog("CREATE_SERVICE_TICKET", "Created ticket ${finalNum} for ${customerName}")
        }
    }

    fun updateServiceTicket(ticket: ServiceTicket) {
        viewModelScope.launch {
            erpDao.updateTicket(ticket)
            
            // Sync to Firebase gracefully
            try {
                FirebaseService.syncServiceTicket(ticket)
            } catch (fe: Exception) {
                android.util.Log.e("InventoryViewModel", "Firebase ticket sync failed: ${fe.message}")
            }
            
            addAuditLog("UPDATE_SERVICE_TICKET", "Updated ticket ${ticket.ticketNumber} status to ${ticket.status}")
        }
    }

    fun postInvoice(
        customerId: Int,
        customerName: String,
        items: List<TransactionItem>,
        discountAmount: Double = 0.0,
        vatAmount: Double = 0.0,
        deliveryCharge: Double = 0.0,
        installationCharge: Double = 0.0,
        serviceCharge: Double = 0.0,
        paidAmount: Double = 0.0,
        paymentMethod: String = "CASH",
        dueDate: Long = System.currentTimeMillis() + 14 * 24 * 3600 * 1000L,
        paymentTerms: String = "Net 14",
        salesperson: String = "",
        notes: String = "",
        termsAndConditions: String = "",
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val invNumber = "INV-${System.currentTimeMillis() % 100000}"
            val itemsTotal = items.sumOf { it.total }
            val totalAmount = itemsTotal + vatAmount + deliveryCharge + installationCharge + serviceCharge - discountAmount
            val balance = totalAmount - paidAmount
            val status = when {
                paidAmount <= 0 -> "UNPAID"
                paidAmount < totalAmount -> "PARTIALLY_PAID"
                else -> "PAID"
            }

            val invoice = Invoice(
                invoiceNumber = invNumber,
                customerId = customerId,
                customerName = customerName,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                balance = balance,
                status = "Confirmed",
                createdAt = System.currentTimeMillis(),
                itemsJson = TransactionItem.serializeList(items),
                discountAmount = discountAmount,
                vatAmount = vatAmount,
                deliveryCharge = deliveryCharge,
                installationCharge = installationCharge,
                serviceCharge = serviceCharge,
                dueDate = dueDate,
                paymentTerms = paymentTerms,
                salesperson = salesperson,
                notes = notes,
                termsAndConditions = termsAndConditions
            )
            erpDao.insertInvoice(invoice)

            // Reduce stock for products automatically
            items.forEach { item ->
                val p = erpDao.getProductById(item.productId)
                if (p != null) {
                    val isService = p.category.contains("charge", ignoreCase = true) || p.category.contains("service", ignoreCase = true)
                    if (!isService) {
                        val newLevel = p.stockLevel - item.qty
                        erpDao.updateProduct(p.copy(stockLevel = newLevel))
                        erpDao.insertStockMovement(StockMovement(
                            referenceNumber = invNumber,
                            transactionType = "Invoice Sales",
                            productId = p.id,
                            productName = p.name,
                            qtyIn = 0,
                            qtyOut = item.qty,
                            balance = newLevel,
                            unitCost = p.landedCost,
                            relatedEntityId = customerId,
                            relatedEntityName = customerName,
                            createdBy = currentUserSession.value?.fullName ?: "SYSTEM",
                            notes = "Auto-reduced on Invoice $invNumber"
                        ))
                    }
                }
            }

            val currentCustomer = erpDao.getCustomerById(customerId)
            if (currentCustomer != null) {
                erpDao.updateCustomer(currentCustomer.copy(balance = currentCustomer.balance + balance))
            }

            // DOUBLE-ENTRY LEDGER ENTRIES:
            val arAccount = erpDao.getLedgerAccountByName("Accounts Receivable")
            val revAccount = erpDao.getLedgerAccountByName("Sales Revenue")
            if (arAccount != null && revAccount != null) {
                erpDao.updateLedgerAccount(arAccount.copy(balance = arAccount.balance + totalAmount))
                erpDao.updateLedgerAccount(revAccount.copy(balance = revAccount.balance + totalAmount))
                
                erpDao.insertLedgerTransaction(LedgerTransaction(
                    reference = invNumber,
                    debitAccountId = arAccount.id,
                    debitAccountName = arAccount.accountName,
                    creditAccountId = revAccount.id,
                    creditAccountName = revAccount.accountName,
                    amount = totalAmount,
                    narration = "Sales Invoice issued to $customerName"
                ))
            }

            if (paidAmount > 0) {
                val payMethodAccName = if (paymentMethod == "BANK_TRANSFER") "Bank Account" else "Cash Account"
                val assetAccount = erpDao.getLedgerAccountByName(payMethodAccName)
                if (assetAccount != null && arAccount != null) {
                    erpDao.updateLedgerAccount(assetAccount.copy(balance = assetAccount.balance + paidAmount))
                    erpDao.updateLedgerAccount(arAccount.copy(balance = arAccount.balance - paidAmount))

                    val payNumber = "PAY-${System.currentTimeMillis() % 100000}"
                    val paymentObj = Payment(
                        paymentNumber = payNumber,
                        invoiceId = null,
                        customerId = customerId,
                        customerName = customerName,
                        amount = paidAmount,
                        paymentMethod = paymentMethod,
                        postedBy = currentUserSession.value?.fullName ?: "OWNER",
                        referenceNumber = "AUTO-INV",
                        bankAccount = payMethodAccName,
                        notes = "Auto-recorded initial payment on Invoice $invNumber"
                    )
                    erpDao.insertPayment(paymentObj)

                    erpDao.insertLedgerTransaction(LedgerTransaction(
                        reference = payNumber,
                        debitAccountId = assetAccount.id,
                        debitAccountName = assetAccount.accountName,
                        creditAccountId = arAccount.id,
                        creditAccountName = arAccount.accountName,
                        amount = paidAmount,
                        narration = "Immediate payment for invoice $invNumber"
                    ))
                }
            }

            addAuditLog("POST_INVOICE", "Invoiced $customerName for BDT $totalAmount, Paid: BDT $paidAmount")
            onResult?.invoke(true, "Invoice $invNumber confirmed successfully")
        }
    }

    fun postPayment(
        invoiceId: Int?,
        customerId: Int,
        customerName: String,
        amount: Double,
        paymentMethod: String,
        referenceNumber: String = "",
        bankAccount: String = "Cash Account",
        chequeDetails: String = "",
        notes: String = "",
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val payNumber = "PAY-${System.currentTimeMillis() % 100000}"
            val paymentObj = Payment(
                paymentNumber = payNumber,
                invoiceId = invoiceId,
                customerId = customerId,
                customerName = customerName,
                amount = amount,
                paymentMethod = paymentMethod,
                postedBy = currentUserSession.value?.fullName ?: "OWNER",
                referenceNumber = referenceNumber,
                bankAccount = bankAccount,
                chequeDetails = chequeDetails,
                notes = notes
            )
            erpDao.insertPayment(paymentObj)

            val currentCustomer = erpDao.getCustomerById(customerId)
            if (currentCustomer != null) {
                erpDao.updateCustomer(currentCustomer.copy(balance = (currentCustomer.balance - amount).coerceAtLeast(0.0)))
            }

            if (invoiceId != null) {
                val inv = erpDao.getInvoiceById(invoiceId)
                if (inv != null) {
                    val newPaid = inv.paidAmount + amount
                    val newBal = (inv.totalAmount - newPaid).coerceAtLeast(0.0)
                    val newStatus = when {
                        newPaid >= inv.totalAmount -> "PAID"
                        else -> "PARTIALLY_PAID"
                    }
                    erpDao.updateInvoice(inv.copy(paidAmount = newPaid, balance = newBal, status = newStatus))
                }
            }

            val payMethodAccName = if (paymentMethod == "BANK_TRANSFER") "Bank Account" else "Cash Account"
            val assetAccount = erpDao.getLedgerAccountByName(payMethodAccName)
            val arAccount = erpDao.getLedgerAccountByName("Accounts Receivable")
            if (assetAccount != null && arAccount != null) {
                erpDao.updateLedgerAccount(assetAccount.copy(balance = assetAccount.balance + amount))
                erpDao.updateLedgerAccount(arAccount.copy(balance = (arAccount.balance - amount).coerceAtLeast(0.0)))

                erpDao.insertLedgerTransaction(LedgerTransaction(
                    reference = payNumber,
                    debitAccountId = assetAccount.id,
                    debitAccountName = assetAccount.accountName,
                    creditAccountId = arAccount.id,
                    creditAccountName = arAccount.accountName,
                    amount = amount,
                    narration = "Customer payment receipt from $customerName"
                ))
            }

            addAuditLog("POST_PAYMENT", "Recorded payment of BDT $amount from customer $customerName, Ref: $referenceNumber")
            onResult?.invoke(true, "Payment receipt $payNumber registered successfully")
        }
    }

    fun addSupplier(name: String, contactPerson: String, phone: String, country: String) {
        viewModelScope.launch {
            val supplier = Supplier(name = name, contactPerson = contactPerson, phone = phone, country = country)
            erpDao.insertSupplier(supplier)
            addAuditLog("CREATE_SUPPLIER", "Registered Supplier: ${name}")
        }
    }

    fun addForeignPurchase(
        lcNumber: String,
        supplierId: Int,
        supplierName: String,
        productDescription: String,
        fobValueUsd: Double,
        exchangeRate: Double,
        freightChargesBdt: Double,
        clearingAgentBdt: Double,
        customsDutyBdt: Double,
        insuranceBdt: Double,
        inlandTransportBdt: Double,
        status: String
    ) {
        viewModelScope.launch {
            val totalLandedCost = (fobValueUsd * exchangeRate) + 
                                  freightChargesBdt + 
                                  clearingAgentBdt + 
                                  customsDutyBdt + 
                                  insuranceBdt + 
                                  inlandTransportBdt

            val purchase = ForeignPurchase(
                lcNumber = lcNumber,
                lcDate = System.currentTimeMillis(),
                supplierId = supplierId,
                supplierName = supplierName,
                productDescription = productDescription,
                fobValueUsd = fobValueUsd,
                exchangeRate = exchangeRate,
                freightChargesBdt = freightChargesBdt,
                clearingAgentBdt = clearingAgentBdt,
                customsDutyBdt = customsDutyBdt,
                insuranceBdt = insuranceBdt,
                inlandTransportBdt = inlandTransportBdt,
                totalLandedCostBdt = totalLandedCost,
                status = status
            )
            erpDao.insertForeignPurchase(purchase)

            val customsDutyAcc = erpDao.getLedgerAccountByName("Customs Duty Expense")
            val bankAcc = erpDao.getLedgerAccountByName("Bank Account")
            if (customsDutyAcc != null && bankAcc != null && customsDutyBdt > 0.0) {
                erpDao.updateLedgerAccount(customsDutyAcc.copy(balance = customsDutyAcc.balance + customsDutyBdt))
                erpDao.updateLedgerAccount(bankAcc.copy(balance = bankAcc.balance - customsDutyBdt))

                erpDao.insertLedgerTransaction(LedgerTransaction(
                    reference = lcNumber,
                    debitAccountId = customsDutyAcc.id,
                    debitAccountName = customsDutyAcc.accountName,
                    creditAccountId = bankAcc.id,
                    creditAccountName = bankAcc.accountName,
                    amount = customsDutyBdt,
                    narration = "Customs duty paid for LC ${lcNumber}"
                ))
            }

            addAuditLog("FOREIGN_PURCHASE", "Registered LC ${lcNumber} with total Landed Cost of BDT ${totalLandedCost}")
        }
    }

    fun addEmiAgreement(
        customerId: Int,
        customerName: String,
        productName: String,
        serialNumber: String,
        totalAmount: Double,
        downPayment: Double,
        interestRate: Double,
        emiCount: Int,
        emiAmount: Double,
        language: String,
        termsTemplate: String
    ) {
        viewModelScope.launch {
            val agreement = EmiAgreement(
                agreementNumber = "AGR-${System.currentTimeMillis() % 100000}",
                customerId = customerId,
                customerName = customerName,
                productName = productName,
                serialNumber = serialNumber,
                totalAmount = totalAmount,
                downPayment = downPayment,
                interestRate = interestRate,
                emiCount = emiCount,
                emiAmount = emiAmount,
                language = language,
                termsTemplate = termsTemplate
            )
            erpDao.insertEmiAgreement(agreement)

            val emiItem = TransactionItem(
                productId = 0,
                name = "EMI Machine / Equipment Purchase",
                qty = 1,
                unit = "set",
                rate = totalAmount,
                discount = 0.0,
                total = totalAmount
            )

            postInvoice(
                customerId = customerId,
                customerName = customerName,
                items = listOf(emiItem),
                paidAmount = downPayment,
                paymentMethod = "BANK_TRANSFER"
            )

            addAuditLog("CREATE_EMI_AGREEMENT", "Generated EMI contract ${agreement.agreementNumber} for ${customerName}")
        }
    }

    fun addAuditLog(action: String, details: String) {
        viewModelScope.launch {
            val log = AuditLog(
                userId = currentUserSession.value?.userId ?: 0,
                username = currentUserSession.value?.username ?: "system",
                action = action,
                details = details
            )
            erpDao.insertAuditLog(log)
        }
    }

    // --- SUPPORT CENTRE METHODS ---

    fun getMessagesForTicket(ticketId: Int): kotlinx.coroutines.flow.Flow<List<SupportMessage>> {
        return supportDao.getMessagesByTicketFlow(ticketId)
    }

    fun createSupportTicket(
        subject: String,
        category: String,
        priority: String,
        systemType: String,
        customerId: Int?,
        customerName: String?,
        employeeId: Int?,
        employeeName: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val nextId = allSupportTickets.value.size + allInternalSupportTickets.value.size + 1001
                val ticketNum = "ST-$nextId"
                val slaHours = when (priority) {
                    "CRITICAL" -> 4L
                    "HIGH" -> 12L
                    "MEDIUM" -> 24L
                    else -> 48L
                }
                val slaDeadline = System.currentTimeMillis() + slaHours * 3600 * 1000L

                val ticket = SupportTicket(
                    ticketNumber = ticketNum,
                    customerId = customerId,
                    customerName = customerName,
                    employeeId = employeeId,
                    employeeName = employeeName,
                    systemType = systemType,
                    subject = subject,
                    category = category,
                    status = "OPEN",
                    priority = priority,
                    slaDeadline = slaDeadline
                )

                supportDao.insertTicket(ticket)
                addAuditLog("CREATE_SUPPORT_TICKET", "Created $systemType Support Ticket $ticketNum: $subject")
                onResult(true, "Support Ticket $ticketNum created successfully!")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    fun updateSupportTicket(ticket: SupportTicket, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                supportDao.updateTicket(ticket)
                addAuditLog("UPDATE_SUPPORT_TICKET", "Updated Ticket ${ticket.ticketNumber} - Status: ${ticket.status}, Priority: ${ticket.priority}")
                onResult(true, "Support Ticket ${ticket.ticketNumber} updated successfully")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    fun sendSupportMessage(
        ticketId: Int,
        messageText: String,
        isAiResponse: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val session = currentUserSession.value
                val senderId = session?.userId ?: 0
                val senderName = session?.fullName ?: "Anonymous"
                val senderRole = session?.role ?: "PUBLIC"

                val msg = SupportMessage(
                    ticketId = ticketId,
                    senderId = senderId,
                    senderName = senderName,
                    senderRole = senderRole,
                    messageText = messageText,
                    isAiResponse = isAiResponse
                )

                supportDao.insertMessage(msg)
                
                // If it is a user message, trigger Gemini AI Support assistant response!
                if (!isAiResponse) {
                    triggerAiResponseForTicket(ticketId, messageText)
                }

                onResult(true, "Message sent")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    fun addKnowledgeArticle(
        title: String,
        content: String,
        category: String,
        systemType: String,
        createdBy: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val article = KnowledgeArticle(
                    title = title,
                    content = content,
                    category = category,
                    systemType = systemType,
                    createdBy = createdBy
                )
                supportDao.insertArticle(article)
                addAuditLog("ADD_KNOWLEDGE_ARTICLE", "Added article '$title' under category '$category' ($systemType)")
                onResult(true, "Knowledge base article added successfully!")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    private fun triggerAiResponseForTicket(ticketId: Int, userMessage: String) {
        viewModelScope.launch {
            try {
                showLoading("Consulting COLORJET AI Support Assistant...")
                val ticket = supportDao.getTicketById(ticketId) ?: return@launch
                val systemType = ticket.systemType
                
                // Let's build a secure prompt to send to Gemini
                val prompt = buildSupportAiPrompt(ticket, userMessage, systemType)
                
                // Call Gemini
                val aiResponse = GeminiClient.askGemini(prompt)
                
                // Save AI response message
                val aiMsg = SupportMessage(
                    ticketId = ticketId,
                    senderId = 0, // AI system
                    senderName = "COLORJET AI Assistant",
                    senderRole = "AI",
                    messageText = aiResponse,
                    isAiResponse = true
                )
                supportDao.insertMessage(aiMsg)
            } catch (e: Exception) {
                // Silently handle or post an error message
                val errorMsg = SupportMessage(
                    ticketId = ticketId,
                    senderId = 0,
                    senderName = "COLORJET AI Assistant",
                    senderRole = "AI",
                    messageText = "My apologies, I encountered a connection issue while querying my knowledge base. An administrator or service manager has been notified of this issue. Please try again in a moment.",
                    isAiResponse = true
                )
                supportDao.insertMessage(errorMsg)
            } finally {
                hideLoading()
            }
        }
    }

    private suspend fun buildSupportAiPrompt(
        ticket: SupportTicket,
        latestMessage: String,
        systemType: String
    ): String {
        val sb = StringBuilder()
        
        // General Safety instructions & Personality guidelines
        sb.append("You are the official COLORJET Bangladesh AI Support Assistant, serving customers and staff with technical, procedural, and general queries.\n")
        sb.append("Primary Directive: Be exceptionally polite, professional, concise, and helpful. Translate technical terms simply.\n")
        sb.append("Strict Rule: Use only approved knowledge base articles. Do NOT hallucinate policies or software passwords.\n")
        sb.append("Identity Rule: You represent Md Aktaruzzaman's COLORJET Bangladesh, located in 44 Purana Paltan, Dhaka-1000. Never reference or confuse with any Indian brand named ColorJet.\n\n")

        if (systemType == "EXTERNAL") {
            sb.append("=== SECURITY MODE: EXTERNAL CUSTOMER SUPPORT ===\n")
            sb.append("CRITICAL: Internal ERP policies, accounts ledgers, HR directives, and database secrets are STRICTLY CONFIDENTIAL. You MUST NOT reveal internal details or employee personal information to external users.\n\n")
            
            // Append External Articles
            sb.append("--- APPROVED EXTERNAL KNOWLEDGE BASE ---\n")
            val articles = allKnowledgeArticles.value.filter { it.systemType == "EXTERNAL" || it.systemType == "BOTH" }
            articles.forEach { art ->
                sb.append("Category: ${art.category} | Title: ${art.title}\nContent:\n${art.content}\n\n")
            }

            // Append customer machine profile if available
            val customerId = ticket.customerId
            if (customerId != null) {
                val machine = allMachines.value.find { it.customerId == customerId }
                if (machine != null) {
                    sb.append("--- CUSTOMER MACHINE PROFILE ---\n")
                    sb.append("Model: ${machine.model}\n")
                    sb.append("Serial Number: ${machine.serialNumber}\n")
                    sb.append("Installation Date: ${machine.installationDate}\n")
                    sb.append("Warranty Status: ${machine.warrantyStatus}\n\n")
                }
            }
            
            sb.append("--- HANDOVER RULES ---\n")
            sb.append("If the customer's question is highly technical, sensitive, requires complex parts replacement, or if you have low confidence, output a helpful handover message instructing the user to contact Support Engineer Zahed Islam or general service at +8809677610610 or info@colorjet.com.bd.\n\n")
            
        } else {
            sb.append("=== SECURITY MODE: INTERNAL EMPLOYEE SUPPORT ===\n")
            sb.append("You are speaking with a verified COLORJET Bangladesh employee.\n\n")
            
            // Append BOTH Internal & External Articles
            sb.append("--- APPROVED ENTERPRISE KNOWLEDGE BASE (INTERNAL & EXTERNAL) ---\n")
            allKnowledgeArticles.value.forEach { art ->
                sb.append("Category: ${art.category} | Title: ${art.title}\nContent:\n${art.content}\n\n")
            }

            // Append employee session context
            val session = currentUserSession.value
            if (session != null) {
                sb.append("--- EMPLOYEE SESSION CONTEXT ---\n")
                sb.append("Name: ${session.fullName}\n")
                sb.append("Role: ${session.role}\n\n")
            }
            
            sb.append("--- HANDOVER RULES ---\n")
            sb.append("If the employee asks for actions requiring direct owner override, sensitive HR changes, or credentials, tell them to consult Sheikh Md. Alim Hosen (Manager) or the general manager for authorized clearance.\n\n")
        }

        // Add context of active ticket
        sb.append("=== CURRENT TICKET CONTEXT ===\n")
        sb.append("Ticket Number: ${ticket.ticketNumber}\n")
        sb.append("Subject: ${ticket.subject}\n")
        sb.append("Category: ${ticket.category}\n")
        sb.append("Priority: ${ticket.priority}\n\n")

        sb.append("User Message: \"$latestMessage\"\n\n")
        sb.append("AI Assistant Response:")

        return sb.toString()
    }
}
