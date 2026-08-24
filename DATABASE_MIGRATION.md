# COLORJET Management Suite - Database Migration

This document outlines the SQLite Room database migration steps and seeding strategy applied within the codebase.

## SQLite Seeding & Migration Safety

Room checks compile-time schemas. When a schema modification occurs, migrations must be declared to prevent data loss.

### Room Database Configuration (`InventoryData.kt`)

The database is built using `Room.databaseBuilder` inside the application context:

```kotlin
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
        PushAnnouncement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ErpDatabase : RoomDatabase() { ... }
```

### Seeding Logic
On initial database generation:
1. Creates default users (`admin`, `alamin`, `alim`, `user`) with safe password hashes.
2. Seeds default customers (`Motijheel C/A`, `Dhaka Poly & Packaging`).
3. Seeds default products (`ColorJet Vulcan UV Printer` model `Vulcan 6090`).
4. Logs initial audit records.

This allows immediate system access and validation testing across roles.
