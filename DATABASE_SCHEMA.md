# COLORJET Management Suite - Database Schema

The local SQLite database resides securely on-device, managed via Android **Room Persistence Library** and verified through compile-time type checks.

## Active Database Name: `colorjet_erp_database`

## Table Schemes and Entity Declarations

### 1. `users` Table
Stores login credentials, system roles, and employee records.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `username` (TEXT, Unique)
- `passwordHash` (TEXT)
- `fullName` (TEXT)
- `role` (TEXT - e.g. "OWNER", "ADMIN", "HR", "ACCOUNTS")
- `department` (TEXT)
- `designation` (TEXT)
- `phone` (TEXT)
- `email` (TEXT)
- `shiftId` (TEXT)
- `salary` (REAL)

### 2. `customers` Table
Stores registered industrial printing clients.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `company` (TEXT)
- `name` (TEXT)
- `phone` (TEXT)
- `email` (TEXT)
- `address` (TEXT)
- `district` (TEXT)
- `notes` (TEXT)

### 3. `products` Table
Primary inventory table for heavy printing equipment and consumables.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `name` (TEXT)
- `category` (TEXT)
- `sku` (TEXT, Unique)
- `brand` (TEXT)
- `model` (TEXT)
- `unit` (TEXT)
- `purchasePrice` (REAL)
- `landedCost` (REAL)
- `sellingPrice` (REAL)
- `dealerPrice` (REAL)
- `minSalePrice` (REAL)
- `warrantyPeriod` (TEXT)
- `reorderLevel` (INTEGER)
- `description` (TEXT)
- `techSpecs` (TEXT)
- `status` (TEXT)
- `isSerialized` (INTEGER)
- `stockLevel` (INTEGER)

### 4. `stock_movements` Table
Audit logs tracking inventory changes.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `referenceNumber` (TEXT)
- `transactionType` (TEXT)
- `productId` (INTEGER)
- `productName` (TEXT)
- `qtyIn` (INTEGER)
- `qtyOut` (INTEGER)
- `balance` (INTEGER)
- `unitCost` (REAL)
- `createdBy` (TEXT)
- `notes` (TEXT)
- `serialNumbers` (TEXT)

### 5. `quotations` Table
Tracks customer estimates and potential leads.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `quotationNumber` (TEXT)
- `customerId` (INTEGER)
- `customerName` (TEXT)
- `productName` (TEXT)
- `price` (REAL)
- `validUntil` (TEXT)
- `status` (TEXT)
- `notes` (TEXT)

### 6. `sales_orders` Table
Stores binding client purchase agreements.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `orderNumber` (TEXT)
- `customerId` (INTEGER)
- `customerName` (TEXT)
- `productName` (TEXT)
- `totalAmount` (REAL)
- `orderDate` (TEXT)
- `status` (TEXT)
- `deliveryStatus` (TEXT)

### 7. `invoices` Table
Customer billing logs.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `invoiceNumber` (TEXT)
- `customerId` (INTEGER)
- `customerName` (TEXT)
- `totalAmount` (REAL)
- `paidAmount` (REAL)
- `dueAmount` (REAL)
- `date` (TEXT)
- `status` (TEXT)

### 8. `payments` Table
Records money received, updating accounts cash reserves.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `invoiceNumber` (TEXT)
- `customerId` (INTEGER)
- `customerName` (TEXT)
- `amount` (REAL)
- `paymentDate` (TEXT)
- `paymentMethod` (TEXT)
- `receiptNumber` (TEXT)
- `notes` (TEXT)

### 9. `ledger_transactions` Table
Tracks running financial ledgers for suppliers and customers.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `accountType` (TEXT - e.g. "CUSTOMER", "SUPPLIER")
- `entityId` (INTEGER)
- `entityName` (TEXT)
- `date` (TEXT)
- `reference` (TEXT)
- `particulars` (TEXT)
- `debit` (REAL)
- `credit` (REAL)
- `runningBalance` (REAL)

### 10. `service_tickets` Table
Industrial maintenance tickets.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `ticketNumber` (TEXT)
- `customerId` (INTEGER)
- `customerName` (TEXT)
- `machineName` (TEXT)
- `problemDescription` (TEXT)
- `status` (TEXT)
- `assignedEngineer` (TEXT)
- `createdDate` (TEXT)
- `resolvedDate` (TEXT)
- `diagnosis` (TEXT)
- `partsUsed` (TEXT)
- `customerSignature` (TEXT)

### 11. `warranties` Table
Registers valid warranty periods.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `warrantyNumber` (TEXT)
- `customerId` (INTEGER)
- `customerName` (TEXT)
- `machineModel` (TEXT)
- `machineSerial` (TEXT)
- `startDate` (TEXT)
- `endDate` (TEXT)
- `status` (TEXT)

### 12. `attendance` Table
Locks employee location clock events.
- `id` (INTEGER, Primary Key, Auto-Increment)
- `employeeId` (INTEGER)
- `employeeName` (TEXT)
- `workDate` (TEXT)
- `checkInTime` (TEXT)
- `checkOutTime` (TEXT)
- `checkInLocation` (TEXT)
- `checkOutLocation` (TEXT)
- `status` (TEXT)
