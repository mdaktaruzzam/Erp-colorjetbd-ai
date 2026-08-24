package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

// --- CUSTOM TRANSACTION ITEM HELPER ---
data class TransactionItem(
    val productId: Int,
    val name: String,
    val qty: Int,
    val unit: String,
    val rate: Double,
    val discount: Double = 0.0,
    val total: Double = (qty * rate) - discount
) {
    // Serializes a single item: "productId|name|qty|unit|rate|discount"
    fun serialize(): String {
        return "$productId|${name.replace("|", " ").replace(";", " ")}|$qty|$unit|$rate|$discount"
    }

    companion object {
        fun deserialize(str: String): TransactionItem {
            val parts = str.split("|")
            val productId = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val name = parts.getOrNull(1) ?: "Unknown Item"
            val qty = parts.getOrNull(2)?.toIntOrNull() ?: 1
            val unit = parts.getOrNull(3) ?: "pcs"
            val rate = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0
            val discount = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
            return TransactionItem(productId, name, qty, unit, rate, discount)
        }

        // Serializes a list of items: "item1;item2;item3"
        fun serializeList(items: List<TransactionItem>): String {
            return items.joinToString(";") { it.serialize() }
        }

        // Deserializes a list of items
        fun deserializeList(str: String?): List<TransactionItem> {
            if (str.isNullOrBlank()) return emptyList()
            return str.split(";").filter { it.isNotBlank() }.map { deserialize(it) }
        }
    }
}

// --- CUSTOMERS ---
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val company: String,
    val phone: String,
    val email: String,
    val address: String,
    val balance: Double = 0.0, // outstanding BDT
    // Added fields
    val customerCode: String = "",
    val customerType: String = "Individual", // "Active", "Inactive", "Lead", "Dealer", "Corporate", "Individual"
    val altPhone: String = "",
    val whatsapp: String = "",
    val district: String = "",
    val area: String = "",
    val contactPersonName: String = "",
    val designation: String = "",
    val taxId: String = "", // NID / Trade License / BIN / VAT
    val openingBalance: Double = 0.0,
    val creditLimit: Double = 0.0,
    val paymentTerms: String = "",
    val notes: String = "",
    val photo: String = "",
    val status: String = "Active" // "Active" / "Inactive"
)

// --- PRODUCTS ---
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Printing machine", "Ink", "Consumable", "Spare parts", "Service charge", etc.
    val subCategory: String = "",
    val brand: String = "",
    val model: String = "",
    val sku: String,
    val barcode: String = "",
    val unit: String = "pcs", // pcs, set, meter, liter, kg, roll, box, etc.
    val purchasePrice: Double = 0.0,
    val landedCost: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val dealerPrice: Double = 0.0,
    val minSalePrice: Double = 0.0,
    val warrantyPeriod: String = "",
    val reorderLevel: Int = 5,
    val description: String = "",
    val techSpecs: String = "",
    val photoUrl: String = "",
    val status: String = "Active", // "Active", "Inactive"
    val isSerialized: Boolean = false,
    val stockLevel: Int = 0
)

// --- STOCK MOVEMENTS ---
@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceNumber: String,
    val transactionType: String, // "Opening Stock", "Stock-In", "Stock-Out", "Adjustment", "Transfer", "Purchase Receive", "Invoice Sales", "Customer Return", "Supplier Return", "Damaged"
    val productId: Int,
    val productName: String,
    val qtyIn: Int,
    val qtyOut: Int,
    val balance: Int,
    val unitCost: Double,
    val warehouse: String = "Main Warehouse",
    val relatedEntityId: Int? = null,
    val relatedEntityName: String = "",
    val createdBy: String,
    val notes: String = "",
    val serialNumbers: String = "" // comma-separated for machines
)

// --- QUOTATIONS ---
@Entity(tableName = "quotations")
data class Quotation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quotationNumber: String,
    val customerId: Int,
    val customerName: String,
    val date: Long = System.currentTimeMillis(),
    val validityDate: Long = System.currentTimeMillis() + 7 * 24 * 3600 * 1000L, // 7 days
    val itemsJson: String = "", // transaction items serialized list
    val totalAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val vatAmount: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val installationCharge: Double = 0.0,
    val specialTerms: String = "",
    val salesPerson: String = "",
    val status: String = "Pending" // "Pending", "Converted to Order", "Converted to Invoice", "Cancelled"
)

// --- SALES ORDERS ---
@Entity(tableName = "sales_orders")
data class SalesOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val salesOrderNumber: String,
    val customerId: Int,
    val customerName: String,
    val date: Long = System.currentTimeMillis(),
    val itemsJson: String = "",
    val totalAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val vatAmount: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val installationCharge: Double = 0.0,
    val status: String = "Confirmed", // "Pending", "Confirmed", "Invoiced", "Cancelled"
    val salesPerson: String = "",
    val notes: String = ""
)

// --- SERVICE TICKETS ---
@Entity(tableName = "service_tickets")
data class ServiceTicket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticketNumber: String,
    val customerId: Int,
    val customerName: String,
    val deviceModel: String,
    val serialNumber: String,
    val issueDescription: String,
    val assignedEngineerId: Int?,
    val assignedEngineerName: String?,
    val status: String, // "OPEN", "IN_PROGRESS", "RESOLVED", "CANCELLED"
    val priority: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val serviceReport: String? = null,
    val partsReplaced: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// --- OFFICE TASKS ---
@Entity(tableName = "office_tasks")
data class OfficeTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val assignedToId: Int?,
    val assignedToName: String?,
    val status: String, // "PENDING", "IN_PROGRESS", "COMPLETED"
    val priority: String, // "LOW", "MEDIUM", "HIGH"
    val dueDate: Long,
    val createdAt: Long = System.currentTimeMillis()
)

// --- INVOICES (SALES) ---
@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNumber: String,
    val customerId: Int,
    val customerName: String,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val balance: Double,
    val status: String, // "Draft", "Confirmed", "Partially Paid", "Paid", "Cancelled", "Returned"
    val createdAt: Long = System.currentTimeMillis(),
    // Added fields
    val itemsJson: String = "",
    val discountAmount: Double = 0.0,
    val vatAmount: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val installationCharge: Double = 0.0,
    val serviceCharge: Double = 0.0,
    val dueDate: Long = System.currentTimeMillis(),
    val paymentTerms: String = "",
    val salesperson: String = "",
    val notes: String = "",
    val termsAndConditions: String = ""
)

// --- PAYMENTS (RECEIPTS) ---
@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val paymentNumber: String,
    val invoiceId: Int?,
    val customerId: Int,
    val customerName: String,
    val amount: Double,
    val paymentMethod: String, // "CASH", "BANK_TRANSFER", "BKASH", "CHEQUE", "MFS", "CARD"
    val paymentDate: Long = System.currentTimeMillis(),
    val postedBy: String,
    // Added fields
    val referenceNumber: String = "",
    val bankAccount: String = "Cash Account",
    val chequeDetails: String = "",
    val notes: String = "",
    val attachmentUrl: String = ""
)

// --- DOUBLE ENTRY LEDGER ACCOUNTS ---
@Entity(tableName = "ledger_accounts")
data class LedgerAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountName: String, // "Cash Account", "Bank Account", "Accounts Receivable", "Sales Revenue", "Customs Duty Expense"
    val type: String, // "ASSET", "LIABILITY", "EQUITY", "REVENUE", "EXPENSE"
    val balance: Double = 0.0
)

// --- LEDGER TRANSACTIONS ---
@Entity(tableName = "ledger_transactions")
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val reference: String,
    val debitAccountId: Int,
    val debitAccountName: String,
    val creditAccountId: Int,
    val creditAccountName: String,
    val amount: Double,
    val narration: String
)

// --- SUPPLIERS ---
@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val country: String
)

// --- FOREIGN PURCHASES & LANDED COSTS ---
@Entity(tableName = "foreign_purchases")
data class ForeignPurchase(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lcNumber: String,
    val lcDate: Long,
    val supplierId: Int,
    val supplierName: String,
    val productDescription: String,
    val fobValueUsd: Double,
    val exchangeRate: Double, // USD to BDT
    val freightChargesBdt: Double = 0.0,
    val clearingAgentBdt: Double = 0.0,
    val customsDutyBdt: Double = 0.0,
    val insuranceBdt: Double = 0.0,
    val inlandTransportBdt: Double = 0.0,
    val totalLandedCostBdt: Double = 0.0,
    val status: String // "INITIATED", "SHIPPED", "CUSTOMS_CLEARING", "COMPLETED"
)

// --- EMI AGREEMENTS ---
@Entity(tableName = "emi_agreements")
data class EmiAgreement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agreementNumber: String,
    val customerId: Int,
    val customerName: String,
    val productName: String,
    val serialNumber: String,
    val totalAmount: Double,
    val downPayment: Double,
    val interestRate: Double,
    val emiCount: Int,
    val emiAmount: Double,
    val agreementDate: Long = System.currentTimeMillis(),
    val language: String, // "ENGLISH", "BANGLA"
    val termsTemplate: String
)

// --- AUDIT LOGS ---
@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val username: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)

// --- AI DRAFTS ---
@Entity(tableName = "ai_drafts")
data class AiDraft(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val draftNumber: String,
    val command: String,
    val intent: String,
    val module: String,
    val actionType: String,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastEditedBy: String = "",
    val lastEditedAt: Long = System.currentTimeMillis(),
    val approvalStatus: String = "Draft", // "Draft", "Waiting for Approval", "Approved", "Rejected", "Cancelled"
    val publishStatus: String = "Unpublished", // "Unpublished", "Published", "Failed"
    val payloadJson: String = "",
    val warnings: String = "",
    val isHighRisk: Boolean = false
)

// --- DAO INTERFACE ---
@Dao
interface ErpDao {
    // AI Drafts
    @Query("SELECT * FROM ai_drafts ORDER BY createdAt DESC")
    fun getAllAiDraftsFlow(): Flow<List<AiDraft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiDraft(draft: AiDraft)

    @Update
    suspend fun updateAiDraft(draft: AiDraft)

    @Delete
    suspend fun deleteAiDraft(draft: AiDraft)

    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Int): Customer?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): Customer?

    @Query("SELECT * FROM customers WHERE email = :email LIMIT 1")
    suspend fun getCustomerByEmail(email: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // Products
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    // Stock Movements
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllStockMovementsFlow(): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getStockMovementsByProductFlow(productId: Int): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovement)

    // Quotations
    @Query("SELECT * FROM quotations ORDER BY date DESC")
    fun getAllQuotationsFlow(): Flow<List<Quotation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: Quotation)

    @Update
    suspend fun updateQuotation(quotation: Quotation)

    // Sales Orders
    @Query("SELECT * FROM sales_orders ORDER BY date DESC")
    fun getAllSalesOrdersFlow(): Flow<List<SalesOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesOrder(salesOrder: SalesOrder)

    @Update
    suspend fun updateSalesOrder(salesOrder: SalesOrder)

    // Service Tickets
    @Query("SELECT * FROM service_tickets ORDER BY createdAt DESC")
    fun getAllTicketsFlow(): Flow<List<ServiceTicket>>

    @Query("SELECT * FROM service_tickets WHERE assignedEngineerId = :engineerId ORDER BY createdAt DESC")
    fun getTicketsByEngineerFlow(engineerId: Int): Flow<List<ServiceTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: ServiceTicket)

    @Update
    suspend fun updateTicket(ticket: ServiceTicket)

    @Delete
    suspend fun deleteTicket(ticket: ServiceTicket)

    // Office Tasks
    @Query("SELECT * FROM office_tasks ORDER BY dueDate ASC")
    fun getAllTasksFlow(): Flow<List<OfficeTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: OfficeTask)

    @Update
    suspend fun updateTask(task: OfficeTask)

    @Delete
    suspend fun deleteTask(task: OfficeTask)

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoicesFlow(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Int): Invoice?

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getInvoiceByNumber(invoiceNumber: String): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice)

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    // Payments
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPaymentsFlow(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE paymentNumber = :num LIMIT 1")
    suspend fun getPaymentByNumber(num: String): Payment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    // Ledger Accounts
    @Query("SELECT * FROM ledger_accounts ORDER BY accountName ASC")
    fun getAllLedgerAccountsFlow(): Flow<List<LedgerAccount>>

    @Query("SELECT * FROM ledger_accounts WHERE id = :id LIMIT 1")
    suspend fun getLedgerAccountById(id: Int): LedgerAccount?

    @Query("SELECT * FROM ledger_accounts WHERE accountName = :name LIMIT 1")
    suspend fun getLedgerAccountByName(name: String): LedgerAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerAccount(account: LedgerAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerAccounts(accounts: List<LedgerAccount>)

    @Update
    suspend fun updateLedgerAccount(account: LedgerAccount)

    // Ledger Transactions
    @Query("SELECT * FROM ledger_transactions ORDER BY date DESC")
    fun getAllLedgerTransactionsFlow(): Flow<List<LedgerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerTransaction(transaction: LedgerTransaction)

    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliersFlow(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<Supplier>)

    // Foreign Purchases
    @Query("SELECT * FROM foreign_purchases ORDER BY lcDate DESC")
    fun getAllForeignPurchasesFlow(): Flow<List<ForeignPurchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForeignPurchase(purchase: ForeignPurchase)

    @Update
    suspend fun updateForeignPurchase(purchase: ForeignPurchase)

    // EMI Agreements
    @Query("SELECT * FROM emi_agreements ORDER BY agreementDate DESC")
    fun getAllEmiAgreementsFlow(): Flow<List<EmiAgreement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmiAgreement(agreement: EmiAgreement)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    // Machines
    @Query("SELECT * FROM machines ORDER BY purchaseDate DESC")
    fun getAllMachinesFlow(): Flow<List<Machine>>

    @Query("SELECT * FROM machines WHERE customerId = :customerId ORDER BY purchaseDate DESC")
    fun getMachinesByCustomerFlow(customerId: Int): Flow<List<Machine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachine(machine: Machine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachines(machines: List<Machine>)

    @Update
    suspend fun updateMachine(machine: Machine)

    // Warranties
    @Query("SELECT * FROM warranties ORDER BY warrantyEnd ASC")
    fun getAllWarrantiesFlow(): Flow<List<Warranty>>

    @Query("SELECT * FROM warranties WHERE customerId = :customerId ORDER BY warrantyEnd ASC")
    fun getWarrantiesByCustomerFlow(customerId: Int): Flow<List<Warranty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarranty(warranty: Warranty)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarranties(warranties: List<Warranty>)

    @Update
    suspend fun updateWarranty(warranty: Warranty)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY checkInTime DESC")
    fun getAllAttendanceFlow(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE userId = :userId ORDER BY checkInTime DESC")
    fun getAttendanceByUserFlow(userId: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    // Internal Messages
    @Query("SELECT * FROM internal_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<InternalMessage>>

    @Query("SELECT * FROM internal_messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    fun getConversationFlow(userId1: Int, userId2: Int): Flow<List<InternalMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: InternalMessage)

    // Push Announcements
    @Query("SELECT * FROM push_announcements ORDER BY timestamp DESC")
    fun getAllAnnouncementsFlow(): Flow<List<PushAnnouncement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: PushAnnouncement)

    // Production Records
    @Query("SELECT * FROM production_records ORDER BY date DESC, id DESC")
    fun getAllProductionRecordsFlow(): Flow<List<ProductionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionRecord(record: ProductionRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionRecords(records: List<ProductionRecord>)

    @Query("DELETE FROM production_records WHERE id = :id")
    suspend fun deleteProductionRecordById(id: Int)
}

// --- MACHINES ---
@Entity(tableName = "machines")
data class Machine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val productName: String,
    val category: String,
    val brand: String,
    val model: String,
    val serialNumber: String,
    val invoiceNumber: String,
    val salesOrderNumber: String,
    val purchaseDate: Long,
    val deliveryDate: Long,
    val installationDate: Long,
    val installationAddress: String,
    val warrantyStartDate: Long,
    val warrantyEndDate: Long,
    val warrantyStatus: String, // "Active", "Expired"
    val assignedEngineer: String = "Zahed Islam",
    val lastServiceDate: Long = 0L,
    val nextMaintenanceDate: Long = 0L,
    val machineStatus: String = "Active", // "Active", "Under Installation", "Under Service", "Warranty Claim", "Inactive", "Transferred"
    val machineImage: String = "",
    val warrantyCertificate: String = "",
    val installationReport: String = "",
    val relatedDocuments: String = ""
)

// --- WARRANTIES ---
@Entity(tableName = "warranties")
data class Warranty(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val machineId: Int,
    val machineName: String,
    val model: String,
    val serialNumber: String,
    val warrantyStart: Long,
    val warrantyEnd: Long,
    val remainingDays: Int = 365,
    val warrantyStatus: String, // "Active", "Expired"
    val coveredParts: String = "Mainboard, Headboard, Motors",
    val excludedParts: String = "Printheads, Consumables",
    val claimHistory: String = "",
    val currentClaimStatus: String = "Active", // "Active", "Expired", "Claim Submitted", "Under Review", "Engineer Assigned", "Parts Required", "Sent to Supplier", "Under Repair", "Replacement Approved", "Resolved", "Rejected"
    val engineerRemarks: String = "",
    val uploadedDocuments: String = "",
    val warrantyCertificate: String = ""
)

// --- ATTENDANCE ---
@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val username: String,
    val fullName: String,
    val date: String, // "YYYY-MM-DD"
    val checkInTime: Long,
    val checkOutTime: Long? = null,
    val checkInLat: Double,
    val checkInLng: Double,
    val checkInLocationName: String,
    val checkOutLat: Double? = null,
    val checkOutLng: Double? = null,
    val checkOutLocationName: String? = null,
    val status: String = "Present", // "Present", "Late", "Absent"
    val notes: String = ""
)

// --- INTERNAL MESSAGES ---
@Entity(tableName = "internal_messages")
data class InternalMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: Int,
    val senderName: String,
    val receiverId: Int,
    val receiverName: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// --- PUSH ANNOUNCEMENTS ---
@Entity(tableName = "push_announcements")
data class PushAnnouncement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val priority: String, // "HIGH", "NORMAL", "LOW"
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- PRODUCTION RECORDS ---
@Entity(tableName = "production_records")
data class ProductionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "YYYY-MM-DD"
    val machineId: Int,
    val machineName: String,
    val output: Float,
    val target: Float,
    val operatorName: String = "Mominul Islam",
    val notes: String = ""
)
