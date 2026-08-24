package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "supply_items")
data class SupplyItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sku: String,
    val name: String,
    val category: String,
    val stockLevel: Int,
    val threshold: Int,
    val location: String = "Main Warehouse",
    val unit: String = "Pcs"
)

@Dao
interface SupplyDao {
    @Query("SELECT * FROM supply_items ORDER BY name ASC")
    fun getAllSupplies(): Flow<List<SupplyItem>>

    @Query("SELECT * FROM supply_items WHERE stockLevel <= threshold ORDER BY name ASC")
    fun getLowStockSupplies(): Flow<List<SupplyItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupply(item: SupplyItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplies(items: List<SupplyItem>)

    @Update
    suspend fun updateSupply(item: SupplyItem)

    @Delete
    suspend fun deleteSupply(item: SupplyItem)

    @Query("SELECT COUNT(*) FROM supply_items WHERE stockLevel <= threshold")
    fun getLowStockCountFlow(): Flow<Int>
}

@Database(
    entities = [
        SupplyItem::class, 
        User::class, 
        UserSession::class,
        Customer::class,
        Product::class,
        StockMovement::class,
        Quotation::class,
        SalesOrder::class,
        ServiceTicket::class,
        OfficeTask::class,
        Invoice::class,
        Payment::class,
        LedgerAccount::class,
        LedgerTransaction::class,
        Supplier::class,
        ForeignPurchase::class,
        EmiAgreement::class,
        AuditLog::class,
        Machine::class,
        Warranty::class,
        Attendance::class,
        InternalMessage::class,
        PushAnnouncement::class,
        SupportTicket::class,
        SupportMessage::class,
        KnowledgeArticle::class,
        AiSupportSession::class,
        ProductionRecord::class,
        AiDraft::class
    ], 
    version = 10, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun supplyDao(): SupplyDao
    abstract fun userDao(): UserDao
    abstract fun erpDao(): ErpDao
    abstract fun supportDao(): SupportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "colorjet_erp_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val fullName: String,
    val pinCode: String,
    val role: String, // "OWNER", "ADMIN", "STAFF"
    val designation: String = "",
    val department: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val blood: String = "",
    val status: String = "Active",
    val photo: String = "",
    val customerId: Int? = null
)

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey val sessionToken: String,
    val userId: Int,
    val username: String,
    val role: String,
    val fullName: String,
    val loginTime: Long,
    val customerId: Int? = null
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Delete
    suspend fun deleteUser(user: User)

    // Sessions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSession)

    @Query("SELECT * FROM user_sessions ORDER BY loginTime DESC LIMIT 1")
    suspend fun getActiveSession(): UserSession?

    @Query("DELETE FROM user_sessions")
    suspend fun clearAllSessions()
}

class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        super.onCreate(db)
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val database = AppDatabase.getDatabase(context)
            val supplyDao = database.supplyDao()
            val userDao = database.userDao()
            val erpDao = database.erpDao()
            val supportDao = database.supportDao()

            // Seed Supplies
            val initialSupplies = listOf(
                SupplyItem(sku = "CJ-UV-CYN1L", name = "UV-6090 Cyan Ink 1L", category = "Inks", stockLevel = 15, threshold = 10, location = "Shelf A-1"),
                SupplyItem(sku = "CJ-UV-MAG1L", name = "UV-6090 Magenta Ink 1L", category = "Inks", stockLevel = 8, threshold = 10, location = "Shelf A-1"),
                SupplyItem(sku = "CJ-UV-YEL1L", name = "UV-6090 Yellow Ink 1L", category = "Inks", stockLevel = 12, threshold = 10, location = "Shelf A-2"),
                SupplyItem(sku = "CJ-UV-BLK1L", name = "UV-6090 Black Ink 1L", category = "Inks", stockLevel = 6, threshold = 10, location = "Shelf A-2"),
                SupplyItem(sku = "CJ-PH-I3200", name = "Eco-Solvent Printhead i3200", category = "Spare Parts", stockLevel = 3, threshold = 5, location = "Cabinet B-2"),
                SupplyItem(sku = "CJ-DTF-POW1", name = "DTF Hot Melt Powder 1kg", category = "DTF Supplies", stockLevel = 25, threshold = 15, location = "Shelf C-1"),
                SupplyItem(sku = "CJ-CS-ULTR", name = "Cleaning Solution Ultra 1L", category = "Consumables", stockLevel = 5, threshold = 8, location = "Shelf C-3"),
                SupplyItem(sku = "CJ-DMP-DX5", name = "Damper DX5 Single Line", category = "Spare Parts", stockLevel = 50, threshold = 20, location = "Drawer D-4"),
                SupplyItem(sku = "CJ-SQ-100", name = "Squeegee Blade 100mm", category = "Spare Parts", stockLevel = 18, threshold = 10, location = "Drawer D-1")
            )
            supplyDao.insertSupplies(initialSupplies)

            // Seed Users
            val initialUsers = listOf(
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
                ),
                User(
                    username = "neellipto",
                    fullName = "Neellipto (Chief Architect)",
                    pinCode = "1234",
                    role = "OWNER",
                    designation = "Chief Enterprise Architect",
                    department = "Management",
                    phone = "01700000001",
                    email = "neellipto@gmail.com",
                    status = "Active"
                ),
                User(
                    username = "neellipto-build",
                    fullName = "Neellipto (Solutions Architect)",
                    pinCode = "1234",
                    role = "OWNER",
                    designation = "Solutions Architect",
                    department = "Management",
                    phone = "01700000002",
                    email = "neellipto.site.build@gmail.com",
                    status = "Active"
                ),
                User(
                    username = "office-colorjet",
                    fullName = "Colorjet Office Desk",
                    pinCode = "1234",
                    role = "ADMIN",
                    designation = "Office Coordinator",
                    department = "Administration",
                    phone = "01700000003",
                    email = "office.colorjet.aa@gmail.com",
                    status = "Active"
                ),
                User(
                    username = "sales-colorjet",
                    fullName = "Colorjet Sales",
                    pinCode = "1234",
                    role = "ADMIN",
                    designation = "Sales Executive",
                    department = "Sales",
                    phone = "01700000004",
                    email = "sales.colorjet@gmail.com",
                    status = "Active"
                ),
                User(
                    username = "colorjet-aa",
                    fullName = "Colorjet AA Admin",
                    pinCode = "1234",
                    role = "OWNER",
                    designation = "System Administrator",
                    department = "IT Department",
                    phone = "01700000005",
                    email = "colorjet.aa@gmail.com",
                    status = "Active"
                ),
                User(
                    username = "colortech-aa",
                    fullName = "Colortech AA Support",
                    pinCode = "1234",
                    role = "STAFF",
                    designation = "Support Engineer",
                    department = "Technical",
                    phone = "01700000006",
                    email = "colortech.aa@gmail.com",
                    status = "Active"
                ),
                User(
                    username = "ept-bd",
                    fullName = "EPT Bangladesh",
                    pinCode = "1234",
                    role = "CUSTOMER",
                    designation = "Procurement Head",
                    department = "Client",
                    phone = "01700000007",
                    email = "ept.com.bd@gmail.com",
                    status = "Active",
                    customerId = 1
                ),
                User(
                    username = "itechbd-aa",
                    fullName = "iTech Bangladesh",
                    pinCode = "1234",
                    role = "CUSTOMER",
                    designation = "Managing Director",
                    department = "Client",
                    phone = "01700000008",
                    email = "itechbd.aa@gmail.com",
                    status = "Active",
                    customerId = 2
                ),
                User(
                    username = "zahan-bd",
                    fullName = "Zahan BD Trades",
                    pinCode = "1234",
                    role = "CUSTOMER",
                    designation = "Proprietor",
                    department = "Client",
                    phone = "01700000009",
                    email = "zahanbd25@gmail.com",
                    status = "Active",
                    customerId = 3
                ),
                User(
                    username = "akterhamid6",
                    fullName = "Akter Hamid (Sales Team)",
                    pinCode = "1234",
                    role = "STAFF",
                    designation = "Senior Sales Representative",
                    department = "Sales",
                    phone = "01700000010",
                    email = "akterhamid6@gmail.com",
                    status = "Active"
                ),
                User(
                    username = "akterhamid66",
                    fullName = "Akter Hamid (Sales Alt)",
                    pinCode = "1234",
                    role = "STAFF",
                    designation = "Sales Executive",
                    department = "Sales",
                    phone = "01700000011",
                    email = "akterhamid66@gmail.com",
                    status = "Active"
                ),
                User(
                    username = "hello-colorjet",
                    fullName = "Colorjet Public Desk",
                    pinCode = "1234",
                    role = "STAFF",
                    designation = "Public Relations Officer",
                    department = "Public Relations",
                    phone = "01700000012",
                    email = "hello@colorjetbd.com",
                    status = "Active"
                ),
                User(
                    username = "info-colorjet",
                    fullName = "Colorjet Information Support",
                    pinCode = "1234",
                    role = "ADMIN",
                    designation = "Information Officer",
                    department = "Support",
                    phone = "01700000013",
                    email = "info@colorjetbd.com",
                    status = "Active"
                )
            )
            userDao.insertUsers(initialUsers)

            // Seed Customer logins
            val customerUsers = listOf(
                User(
                    username = "customer-001",
                    fullName = "Sohail Rahman (Digiprint)",
                    pinCode = "1234",
                    role = "CUSTOMER",
                    designation = "Director",
                    department = "External Customer",
                    phone = "+8801711223344",
                    email = "info@digiprint.com",
                    status = "Active",
                    customerId = 1
                ),
                User(
                    username = "customer-002",
                    fullName = "Zamil Hossain (Dhaka Poly)",
                    pinCode = "1234",
                    role = "CUSTOMER",
                    designation = "Procurement Head",
                    department = "External Customer",
                    phone = "+8801819998877",
                    email = "orders@dhakapoly.com",
                    status = "Active",
                    customerId = 2
                )
            )
            userDao.insertUsers(customerUsers)

            // Seed Customers
            val c1 = Customer(
                id = 1, 
                name = "Digiprint Solutions Ltd.", 
                company = "Digiprint Bangladesh", 
                phone = "+8801711223344", 
                email = "info@digiprint.com", 
                address = "Motijheel C/A, Dhaka", 
                balance = 450000.0,
                customerCode = "CUST-1001",
                customerType = "Dealer",
                district = "Dhaka",
                area = "Motijheel",
                contactPersonName = "Sohail Rahman",
                designation = "Director",
                taxId = "NID-9182391231",
                openingBalance = 0.0,
                creditLimit = 500000.0,
                paymentTerms = "Net 30",
                notes = "Long-time printing supplier dealer in Dhaka region.",
                status = "Active"
            )
            val c2 = Customer(
                id = 2, 
                name = "Dhaka Poly & Packaging", 
                company = "Dhaka Poly", 
                phone = "+8801819998877", 
                email = "orders@dhakapoly.com", 
                address = "Tejgaon I/A, Dhaka", 
                balance = 0.0,
                customerCode = "CUST-1002",
                customerType = "Corporate",
                district = "Dhaka",
                area = "Tejgaon",
                contactPersonName = "Zamil Hossain",
                designation = "Procurement Head",
                taxId = "VAT-BIN-110291",
                openingBalance = 0.0,
                creditLimit = 1000000.0,
                paymentTerms = "Net 15",
                notes = "Major industrial packaging company.",
                status = "Active"
            )
            val c3 = Customer(
                id = 3,
                name = "Premium Prints BD",
                company = "Premium Prints",
                phone = "+8801911556677",
                email = "premiumprints@gmail.com",
                address = "Chittagong Port Area",
                balance = 0.0,
                customerCode = "CUST-1003",
                customerType = "Lead",
                district = "Chittagong",
                area = "Halishahar",
                contactPersonName = "Nazmul Huda",
                designation = "Owner",
                notes = "Potential lead interested in Vulcan UV printer.",
                status = "Active"
            )
            erpDao.insertCustomer(c1)
            erpDao.insertCustomer(c2)
            erpDao.insertCustomer(c3)

            // Seed Products
            val p1 = Product(id = 1, name = "ColorJet Vulcan UV Printer", category = "Printing machine", sku = "CJ-VULC-6090", brand = "ColorJet", model = "Vulcan 6090", unit = "pcs", purchasePrice = 900000.0, landedCost = 950000.0, sellingPrice = 1200000.0, dealerPrice = 1100000.0, minSalePrice = 1050000.0, warrantyPeriod = "1 Year", reorderLevel = 1, description = "High-speed industrial flatbed UV printing machine.", techSpecs = "Printhead: Epson i3200; Bed size: 60x90cm; Ink: UV Curable", status = "Active", isSerialized = true, stockLevel = 3)
            val p2 = Product(id = 2, name = "Premium Cyan UV Ink 1L", category = "Ink", sku = "CJ-INK-CYAN", brand = "ColorJet", model = "Premium UV Cyan", unit = "liter", purchasePrice = 4000.0, landedCost = 4500.0, sellingPrice = 6500.0, dealerPrice = 5800.0, minSalePrice = 5500.0, warrantyPeriod = "N/A", reorderLevel = 10, description = "Cyan UV curable ink optimized for industrial printheads.", status = "Active", isSerialized = false, stockLevel = 50)
            val p3 = Product(id = 3, name = "Premium Magenta UV Ink 1L", category = "Ink", sku = "CJ-INK-MAG", brand = "ColorJet", model = "Premium UV Magenta", unit = "liter", purchasePrice = 4000.0, landedCost = 4500.0, sellingPrice = 6500.0, dealerPrice = 5800.0, minSalePrice = 5500.0, warrantyPeriod = "N/A", reorderLevel = 10, description = "Magenta UV curable ink optimized for industrial printheads.", status = "Active", isSerialized = false, stockLevel = 50)
            val p4 = Product(id = 4, name = "Printhead i3200-A1", category = "Spare parts", sku = "CJ-HD-I3200", brand = "Epson", model = "i3200-A1", unit = "pcs", purchasePrice = 60000.0, landedCost = 65000.0, sellingPrice = 85000.0, dealerPrice = 78000.0, minSalePrice = 75000.0, warrantyPeriod = "6 Months", reorderLevel = 2, description = "Genuine Epson micro-piezo industrial printhead.", status = "Active", isSerialized = false, stockLevel = 8)
            val p5 = Product(id = 5, name = "Standard Installation Charge", category = "Installation charge", sku = "CJ-SRV-INST", brand = "ColorJet", model = "Services", unit = "job", purchasePrice = 0.0, landedCost = 0.0, sellingPrice = 15000.0, dealerPrice = 15000.0, minSalePrice = 10000.0, warrantyPeriod = "N/A", reorderLevel = 0, description = "Standard machine installation and technician travel fee.", status = "Active", isSerialized = false, stockLevel = 0)
            erpDao.insertProduct(p1)
            erpDao.insertProduct(p2)
            erpDao.insertProduct(p3)
            erpDao.insertProduct(p4)
            erpDao.insertProduct(p5)

            // Seed Stock Movements
            val m1 = StockMovement(id = 1, referenceNumber = "OP-STOCK-001", transactionType = "Opening Stock", productId = 1, productName = "ColorJet Vulcan UV Printer", qtyIn = 3, qtyOut = 0, balance = 3, unitCost = 950000.0, createdBy = "admin", notes = "Opening inventory seeding", serialNumbers = "VLC-6090-4491, VLC-6090-4492, VLC-6090-4493")
            val m2 = StockMovement(id = 2, referenceNumber = "OP-STOCK-002", transactionType = "Opening Stock", productId = 2, productName = "Premium Cyan UV Ink 1L", qtyIn = 50, qtyOut = 0, balance = 50, unitCost = 4500.0, createdBy = "admin", notes = "Opening inventory seeding")
            val m3 = StockMovement(id = 3, referenceNumber = "OP-STOCK-003", transactionType = "Opening Stock", productId = 3, productName = "Premium Magenta UV Ink 1L", qtyIn = 50, qtyOut = 0, balance = 50, unitCost = 4500.0, createdBy = "admin", notes = "Opening inventory seeding")
            val m4 = StockMovement(id = 4, referenceNumber = "OP-STOCK-004", transactionType = "Opening Stock", productId = 4, productName = "Printhead i3200-A1", qtyIn = 8, qtyOut = 0, balance = 8, unitCost = 65000.0, createdBy = "admin", notes = "Opening inventory seeding")
            erpDao.insertStockMovement(m1)
            erpDao.insertStockMovement(m2)
            erpDao.insertStockMovement(m3)
            erpDao.insertStockMovement(m4)

            // Seed Suppliers
            val s1 = Supplier(id = 1, name = "Global Industrial Digital Tech Co.", contactPerson = "Chen Wei", phone = "+8613912345678", country = "China")
            val s2 = Supplier(id = 2, name = "Epson Asia Singapore", contactPerson = "Kenji Sato", phone = "+6561234567", country = "Singapore")
            erpDao.insertSuppliers(listOf(s1, s2))

            // Seed Ledger Accounts
            val ledgerAccs = listOf(
                LedgerAccount(id = 1, accountName = "Cash Account", type = "ASSET", balance = 85000.0),
                LedgerAccount(id = 2, accountName = "Bank Account", type = "ASSET", balance = 1250000.0),
                LedgerAccount(id = 3, accountName = "Accounts Receivable", type = "ASSET", balance = 450000.0),
                LedgerAccount(id = 4, accountName = "Sales Revenue", type = "REVENUE", balance = 14800000.0),
                LedgerAccount(id = 5, accountName = "Customs Duty Expense", type = "EXPENSE", balance = 320000.0),
                LedgerAccount(id = 6, accountName = "Service Revenue", type = "REVENUE", balance = 125000.0)
            )
            erpDao.insertLedgerAccounts(ledgerAccs)

            // Seed Office Tasks
            val t1 = OfficeTask(
                title = "Deliver Cyan Ink 10L to Digiprint",
                description = "Customer has urgent printing deadline. Deliver from main store, confirm with storekeeper first.",
                assignedToId = 3,
                assignedToName = "Tanvir Hasan",
                status = "PENDING",
                priority = "HIGH",
                dueDate = System.currentTimeMillis() + 86400000 // 1 day from now
            )
            val t2 = OfficeTask(
                title = "Prepare quarterly sales audit logs",
                description = "Review overall accounts ledger and payments posted by staff this quarter.",
                assignedToId = 2,
                assignedToName = "Nabila Islam",
                status = "IN_PROGRESS",
                priority = "MEDIUM",
                dueDate = System.currentTimeMillis() + 172800000 // 2 days from now
            )
            erpDao.insertTask(t1)
            erpDao.insertTask(t2)

            // Seed Service Tickets
            val tk1 = ServiceTicket(
                ticketNumber = "TK-1001",
                customerId = 1,
                customerName = "Digiprint Solutions Ltd.",
                deviceModel = "COLORJET VULCAN UV",
                serialNumber = "VLC-6090-4491",
                issueDescription = "Cyan ink pump not engaging, printhead temperature error on sub-board.",
                assignedEngineerId = 3,
                assignedEngineerName = "Tanvir Hasan",
                status = "OPEN",
                priority = "HIGH",
                createdAt = System.currentTimeMillis() - 72000000,
                updatedAt = System.currentTimeMillis()
            )
            erpDao.insertTicket(tk1)

            // Seed Invoice & Payments to align with Customer balance & Revenue
            val inv = Invoice(
                id = 1,
                invoiceNumber = "INV-2026-001",
                customerId = 1,
                customerName = "Digiprint Solutions Ltd.",
                totalAmount = 650000.0,
                paidAmount = 200000.0,
                balance = 450000.0,
                status = "PARTIALLY_PAID",
                createdAt = System.currentTimeMillis() - 172800000
            )
            erpDao.insertInvoice(inv)

            val pay = Payment(
                paymentNumber = "PAY-1001",
                invoiceId = 1,
                customerId = 1,
                customerName = "Digiprint Solutions Ltd.",
                amount = 200000.0,
                paymentMethod = "BANK_TRANSFER",
                paymentDate = System.currentTimeMillis() - 86400000,
                postedBy = "Nabila Islam"
            )
            erpDao.insertPayment(pay)

            val initialMachines = listOf(
                Machine(
                    customerId = 1,
                    productName = "ColorJet Vulcan Prime UV Printer",
                    category = "Printing machine",
                    brand = "ColorJet",
                    model = "Vulcan Prime UV-6090",
                    serialNumber = "CJ-VULC-2025-098",
                    invoiceNumber = "INV-2025-001",
                    salesOrderNumber = "SO-2025-001",
                    purchaseDate = System.currentTimeMillis() - 120 * 24 * 3600 * 1000L, // 4 months ago
                    deliveryDate = System.currentTimeMillis() - 118 * 24 * 3600 * 1000L,
                    installationDate = System.currentTimeMillis() - 115 * 24 * 3600 * 1000L,
                    installationAddress = "Motijheel C/A, Dhaka",
                    warrantyStartDate = System.currentTimeMillis() - 115 * 24 * 3600 * 1000L,
                    warrantyEndDate = System.currentTimeMillis() + 250 * 24 * 3600 * 1000L, // 1 year warranty
                    warrantyStatus = "Active",
                    assignedEngineer = "Zahed Islam",
                    lastServiceDate = System.currentTimeMillis() - 15 * 24 * 3600 * 1000L,
                    nextMaintenanceDate = System.currentTimeMillis() + 75 * 24 * 3600 * 1000L,
                    machineStatus = "Active"
                ),
                Machine(
                    customerId = 1,
                    productName = "ColorJet Eco-Solvent Plotter",
                    category = "Printing machine",
                    brand = "ColorJet",
                    model = "Eco-Solvent i3200",
                    serialNumber = "CJ-ECO-2024-342",
                    invoiceNumber = "INV-2024-089",
                    salesOrderNumber = "SO-2024-089",
                    purchaseDate = System.currentTimeMillis() - 400 * 24 * 3600 * 1000L, // Over a year ago
                    deliveryDate = System.currentTimeMillis() - 398 * 24 * 3600 * 1000L,
                    installationDate = System.currentTimeMillis() - 395 * 24 * 3600 * 1000L,
                    installationAddress = "Motijheel C/A, Dhaka",
                    warrantyStartDate = System.currentTimeMillis() - 395 * 24 * 3600 * 1000L,
                    warrantyEndDate = System.currentTimeMillis() - 30 * 24 * 3600 * 1000L, // Expired 1 month ago
                    warrantyStatus = "Expired",
                    assignedEngineer = "Md. Sadakatul Bari",
                    lastServiceDate = System.currentTimeMillis() - 45 * 24 * 3600 * 1000L,
                    nextMaintenanceDate = System.currentTimeMillis() + 45 * 24 * 3600 * 1000L,
                    machineStatus = "Active"
                )
            )
            erpDao.insertMachines(initialMachines)

            val initialWarranties = listOf(
                Warranty(
                    customerId = 1,
                    machineId = 1,
                    machineName = "ColorJet Vulcan Prime UV Printer",
                    model = "Vulcan Prime UV-6090",
                    serialNumber = "CJ-VULC-2025-098",
                    warrantyStart = System.currentTimeMillis() - 115 * 24 * 3600 * 1000L,
                    warrantyEnd = System.currentTimeMillis() + 250 * 24 * 3600 * 1000L,
                    remainingDays = 250,
                    warrantyStatus = "Active",
                    coveredParts = "Mainboard, Headboard, UV Lamp Driver, Servo Motors",
                    excludedParts = "Printheads, Damper, Ink Supply Tubes, Consumables",
                    claimHistory = "None",
                    currentClaimStatus = "Active",
                    engineerRemarks = "Machine is performing perfectly. UV level is stable."
                ),
                Warranty(
                    customerId = 1,
                    machineId = 2,
                    machineName = "ColorJet Eco-Solvent Plotter",
                    model = "Eco-Solvent i3200",
                    serialNumber = "CJ-ECO-2024-342",
                    warrantyStart = System.currentTimeMillis() - 395 * 24 * 3600 * 1000L,
                    warrantyEnd = System.currentTimeMillis() - 30 * 24 * 3600 * 1000L,
                    remainingDays = 0,
                    warrantyStatus = "Expired",
                    coveredParts = "Mainboard, Servo Driver",
                    excludedParts = "Printhead i3200, Cap-top, Damper",
                    claimHistory = "1 Claim on Oct 2024 (Resolved)",
                    currentClaimStatus = "Expired",
                    engineerRemarks = "Out of standard warranty. AMC package recommended."
                )
            )
            erpDao.insertWarranties(initialWarranties)
            
            // Seed Production Records
            val initialProduction = listOf(
                ProductionRecord(date = "2026-07-14", machineId = 1, machineName = "ColorJet Vulcan Prime UV Printer", output = 180f, target = 150f, operatorName = "Mominul Islam", notes = "Achieved 120% target, high density vinyl print run"),
                ProductionRecord(date = "2026-07-13", machineId = 1, machineName = "ColorJet Vulcan Prime UV Printer", output = 145f, target = 150f, operatorName = "Mominul Islam", notes = "Slight delay due to printhead cleaning"),
                ProductionRecord(date = "2026-07-12", machineId = 1, machineName = "ColorJet Vulcan Prime UV Printer", output = 160f, target = 150f, operatorName = "Siddikur Rahman", notes = "Smooth run, regular PVC banner"),
                ProductionRecord(date = "2026-07-11", machineId = 1, machineName = "ColorJet Vulcan Prime UV Printer", output = 135f, target = 150f, operatorName = "Siddikur Rahman", notes = "Standby mode for maintenance check"),
                ProductionRecord(date = "2026-07-10", machineId = 1, machineName = "ColorJet Vulcan Prime UV Printer", output = 155f, target = 150f, operatorName = "Mominul Islam", notes = "Target met, fine art printing"),
                ProductionRecord(date = "2026-07-09", machineId = 1, machineName = "ColorJet Vulcan Prime UV Printer", output = 170f, target = 150f, operatorName = "Mominul Islam", notes = "Rush order from Digiprint completed"),
                ProductionRecord(date = "2026-07-08", machineId = 1, machineName = "ColorJet Vulcan Prime UV Printer", output = 140f, target = 150f, operatorName = "Siddikur Rahman", notes = "Regular schedule"),
                
                ProductionRecord(date = "2026-07-14", machineId = 2, machineName = "ColorJet Eco-Solvent Plotter", output = 240f, target = 200f, operatorName = "Ariful Islam", notes = "High volume print run on backlit film"),
                ProductionRecord(date = "2026-07-13", machineId = 2, machineName = "ColorJet Eco-Solvent Plotter", output = 210f, target = 200f, operatorName = "Ariful Islam", notes = "Target achieved, canvas job"),
                ProductionRecord(date = "2026-07-12", machineId = 2, machineName = "ColorJet Eco-Solvent Plotter", output = 195f, target = 200f, operatorName = "Masud Rana", notes = "Nearly met, slight media jamming"),
                ProductionRecord(date = "2026-07-11", machineId = 2, machineName = "ColorJet Eco-Solvent Plotter", output = 205f, target = 200f, operatorName = "Masud Rana", notes = "Excellent speed setup"),
                ProductionRecord(date = "2026-07-10", machineId = 2, machineName = "ColorJet Eco-Solvent Plotter", output = 180f, target = 200f, operatorName = "Ariful Islam", notes = "Roll change delay"),
                ProductionRecord(date = "2026-07-09", machineId = 2, machineName = "ColorJet Eco-Solvent Plotter", output = 220f, target = 200f, operatorName = "Ariful Islam", notes = "Continuous production schedule"),
                ProductionRecord(date = "2026-07-08", machineId = 2, machineName = "ColorJet Eco-Solvent Plotter", output = 190f, target = 200f, operatorName = "Masud Rana", notes = "Regular run")
            )
            erpDao.insertProductionRecords(initialProduction)

            // Seed Support Knowledge Base Articles
            val initialArticles = listOf(
                KnowledgeArticle(
                    title = "ColorJet Vulcan 6090 UV Flatbed Troubleshooting Guide",
                    content = "1. Carriage Limit switch error: Clear any dust or debris from optical sensor on left carriage axis.\n2. Inconsistent UV lamp intensity: Ensure chiller liquid level is optimal and fan operates without noise.\n3. Head temperature high: Verify printer room ambient temperature remains between 20-25°C.",
                    category = "Troubleshooting",
                    systemType = "EXTERNAL",
                    createdBy = "Zahed Islam"
                ),
                KnowledgeArticle(
                    title = "Proper Maintenance Checklist for Epson i3200 Printheads",
                    content = "Daily checklist:\n1. Perform head clean cycle before printing.\n2. Inspect cap-top for dry ink buildup.\n3. Wipe printhead edges with specialized cleaning swabs and cleaning solution.\nNote: NEVER apply metal tools or abrasive cloth directly onto printhead nozzles.",
                    category = "Product Manual",
                    systemType = "BOTH",
                    createdBy = "Zahed Islam"
                ),
                KnowledgeArticle(
                    title = "Internal ERP Policy: Accounts & Invoicing Rules",
                    content = "A. All credit agreements must include down payment of at least 30% unless approved by Owner (Md Aktaruzzaman).\nB. Invoices must be registered on the same day as dispatch.\nC. Ledger transactions must correspond to actual bank deposit slip reference codes.",
                    category = "HR Rules",
                    systemType = "INTERNAL",
                    createdBy = "Md Aktaruzzaman"
                ),
                KnowledgeArticle(
                    title = "Technical Team SLA Rules & Response Escalation Protocol",
                    content = "CRITICAL priority support tickets must receive initial engineer update within 2 hours. HIGH priority within 4 hours. If unresolved after 12 hours, ticket must be escalated to Atiq Faisal Ani (Operation & Service Control Head) or general management.",
                    category = "Troubleshooting",
                    systemType = "INTERNAL",
                    createdBy = "Atiq Faisal Ani"
                )
            )
            supportDao.insertArticles(initialArticles)

            // Seed initial Support Tickets
            val ticketId1 = supportDao.insertTicket(SupportTicket(
                ticketNumber = "ST-1001",
                customerId = 1,
                customerName = "Digiprint Solutions Ltd.",
                employeeId = null,
                employeeName = null,
                systemType = "EXTERNAL",
                subject = "Vulcan 6090 printhead temperature error on startup",
                category = "HARDWARE",
                status = "OPEN",
                priority = "HIGH",
                createdAt = System.currentTimeMillis() - 2 * 3600 * 1000L, // 2 hours ago
                slaDeadline = System.currentTimeMillis() + 22 * 3600 * 1000L, // 24 hours total
                assignedToId = 7, // Zahed Islam
                assignedToName = "Zahed Islam"
            ))

            val ticketId2 = supportDao.insertTicket(SupportTicket(
                ticketNumber = "ST-1002",
                customerId = null,
                customerName = null,
                employeeId = 7, // Zahed Islam
                employeeName = "Zahed Islam",
                systemType = "INTERNAL",
                subject = "Attendance Location Verification Override",
                category = "HR",
                status = "OPEN",
                priority = "MEDIUM",
                createdAt = System.currentTimeMillis() - 4 * 3600 * 1000L, // 4 hours ago
                slaDeadline = System.currentTimeMillis() + 44 * 3600 * 1000L, // 48 hours total
                assignedToId = 4, // Sheikh Md. Alim Hosen
                assignedToName = "Sheikh Md. Alim Hosen"
            ))

            // Seed initial Support Messages for ST-1001
            supportDao.insertMessage(SupportMessage(
                ticketId = ticketId1.toInt(),
                senderId = 1, // Customer login (Sohail Rahman)
                senderName = "Sohail Rahman",
                senderRole = "CUSTOMER",
                messageText = "Hello support team, our Vulcan flatbed is throwing an Epson printhead temperature limit warning immediately on booting up the control software. We have a heavy packaging delivery tonight. Please assist!"
            ))

            supportDao.insertMessage(SupportMessage(
                ticketId = ticketId1.toInt(),
                senderId = 0, // AI Assistant
                senderName = "COLORJET AI Assistant",
                senderRole = "AI",
                messageText = "Hello Sohail, I have checked your machine profile (ColorJet Vulcan Prime UV Printer, Model: Vulcan Prime UV-6090, Serial: CJ-VULC-2025-098). Based on our Knowledge Base, a startup head temperature error is typically caused by: 1. Ambient room temperature exceeding 25°C. 2. Printhead sub-board connector ribbon cable loose. Please verify the flatbed room AC is set below 24°C and let me know. I have also assigned this ticket to Sr. Engineer Zahed Islam as a HIGH priority ticket.",
                isAiResponse = true
            ))

            val audit = AuditLog(
                userId = 2,
                username = "admin",
                action = "SEED_DATA_INITIALIZED",
                timestamp = System.currentTimeMillis(),
                details = "Enterprise relational schema tables initialized and pre-seeded successfully."
            )
            erpDao.insertAuditLog(audit)
        }
    }
}
