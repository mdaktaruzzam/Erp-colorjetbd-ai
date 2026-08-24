# COLORJET Management Suite - Module Inventory

This document acts as a comprehensive, multi-dimensional operational index for the 75 core modules deployed within the **COLORJET Management Suite** application.

| Module No | Module Name | Role Access | Target Screen | Route ID | Form ID | API / DB Table | Current Status | Test Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :---: |
| 01 | Authentication | ALL | LoginScreen | `login` | `frm_login` | `users`, `user_sessions` | COMPLETE | PASS |
| 02 | Role-Based Dashboard Routing | ALL | MainActivity | `routing` | `N/A` | `users` | COMPLETE | PASS |
| 03 | Owner Executive Dashboard | OWNER | DashboardScreen | `dashboard` | `N/A` | Room / State | COMPLETE | PASS |
| 04 | Administration Dashboard | ADMIN | DashboardScreen | `dashboard` | `N/A` | Room / State | COMPLETE | PASS |
| 05 | Management Dashboard | MANAGER | DashboardScreen | `dashboard` | `N/A` | Room / State | COMPLETE | PASS |
| 06 | Employee Directory | OWNER, ADMIN, HR, MGR | DirectorySubScreen | `directory` | `frm_employee` | `users` | COMPLETE | PASS |
| 07 | Employee Profile | ALL | ProfileSubScreen | `profile` | `N/A` | `users` | COMPLETE | PASS |
| 08 | Employee Verification ID Card | ALL | EmployeeCardSubScreen | `employee_id` | `N/A` | `users` | COMPLETE | PASS |
| 09 | QR Employee Verification | ALL | QREmployeeVerification | `qr_verify` | `N/A` | `users` | COMPLETE | PASS |
| 10 | Attendance | STAFF, ENG, ALL | AttendanceSubScreen | `attendance` | `frm_checkin` | `attendance` | COMPLETE | PASS |
| 11 | Google Maps Location Attendance | STAFF, ENG, ALL | AttendanceSubScreen | `attendance` | `frm_checkin` | `attendance` (coordinates) | COMPLETE | PASS |
| 12 | Shift Management | HR, ADMIN | AttendanceSubScreen | `attendance` | `frm_shift` | `attendance` | COMPLETE | PASS |
| 13 | Leave and Holiday | ALL | AttendanceSubScreen | `attendance` | `frm_leave` | `attendance` | COMPLETE | PASS |
| 14 | Payroll and Salary | HR, OWNER, ADMIN | AttendanceSubScreen | `attendance` | `frm_salary` | `attendance` | COMPLETE | PASS |
| 15 | Internal Messaging | ALL | MessagingSubScreen | `messages` | `frm_message` | `internal_messages` | COMPLETE | PASS |
| 16 | System Notifications | ALL | AnnouncementsSubScreen | `announcements` | `N/A` | `push_announcements` | COMPLETE | PASS |
| 17 | FCM Push Notifications | ALL | AnnouncementsSubScreen | `announcements` | `frm_push` | `push_announcements` | COMPLETE | PASS |
| 18 | Office Task Management | ALL | TasksScreen | `tasks` | `frm_task` | `office_tasks` | COMPLETE | PASS |
| 19 | Customer Management | ALL | CustomersSubScreen | `customers` | `frm_customer` | `customers` | COMPLETE | PASS |
| 20 | Customer Portal | CUSTOMER | CustomerDashboard | `customer_portal` | `N/A` | Isolated Customers | COMPLETE | PASS |
| 21 | Customer Machines | CUSTOMER, ENG, ALL | CustomersSubScreen | `customers` | `frm_machine` | `machines` | COMPLETE | PASS |
| 22 | Leads and Follow-Up | SALES, OWNER, ADMIN | CustomersSubScreen | `customers` | `frm_lead` | `quotations` | COMPLETE | PASS |
| 23 | Quotations | SALES, OWNER, ADMIN | CustomersSubScreen | `customers` | `frm_quotation` | `quotations` | COMPLETE | PASS |
| 24 | Sales Orders | SALES, OWNER, ADMIN | CustomersSubScreen | `customers` | `frm_sales_order` | `sales_orders` | COMPLETE | PASS |
| 25 | Delivery | STORE, LOGISTICS, ALL | CustomersSubScreen | `customers` | `frm_delivery` | `sales_orders` | COMPLETE | PASS |
| 26 | Machine Installation | ENG, SERVICE_MGR | CustomersSubScreen | `customers` | `frm_install` | `machines` | COMPLETE | PASS |
| 27 | Invoices | ACCOUNTS, OWNER, ADMIN | InvoicesSubScreen | `invoices` | `frm_invoice` | `invoices` | COMPLETE | PASS |
| 28 | Payments | ACCOUNTS, OWNER, ADMIN | PaymentsSubScreen | `payments` | `frm_payment` | `payments`, `ledger_transactions` | COMPLETE | PASS |
| 29 | Money Receipts | ACCOUNTS, OWNER, ADMIN | PaymentsSubScreen | `payments` | `frm_receipt` | `payments` | COMPLETE | PASS |
| 30 | Customer Ledger | ACCOUNTS, OWNER, ADMIN | LedgerSubScreen | `ledger` | `N/A` | `ledger_transactions` | COMPLETE | PASS |
| 31 | Supplier Management | STORE, ACCOUNTS, OWNER | SuppliersSubScreen | `suppliers` | `frm_supplier` | `suppliers` | COMPLETE | PASS |
| 32 | Supplier Ledger | ACCOUNTS, OWNER, ADMIN | LedgerSubScreen | `ledger` | `N/A` | `ledger_transactions` | COMPLETE | PASS |
| 33 | Purchase Orders | STORE, OWNER, ADMIN | SuppliersSubScreen | `suppliers` | `frm_po` | `suppliers` | COMPLETE | PASS |
| 34 | LC Management | OWNER, ACCOUNTS, ADMIN | ForeignPurchaseSubScreen | `foreign_purchase` | `frm_lc` | `foreign_purchases` | COMPLETE | PASS |
| 35 | TT Payments | OWNER, ACCOUNTS, ADMIN | ForeignPurchaseSubScreen | `foreign_purchase` | `frm_tt` | `foreign_purchases` | COMPLETE | PASS |
| 36 | Foreign Purchase | OWNER, STORE, ACCOUNTS | ForeignPurchaseSubScreen | `foreign_purchase` | `frm_foreign` | `foreign_purchases` | COMPLETE | PASS |
| 37 | Shipment | STORE, OWNER, ADMIN | ForeignPurchaseSubScreen | `foreign_purchase` | `frm_shipment` | `foreign_purchases` | COMPLETE | PASS |
| 38 | Trucking and Logistics | STORE, OWNER, ADMIN | ForeignPurchaseSubScreen | `foreign_purchase` | `frm_logistics` | `foreign_purchases` | COMPLETE | PASS |
| 39 | Landed Cost | OWNER, STORE, ACCOUNTS | ForeignPurchaseSubScreen | `foreign_purchase` | `frm_landed_cost` | `foreign_purchases` | COMPLETE | PASS |
| 40 | Products | STORE, ALL | ProductsStocksSubScreen | `products` | `frm_product` | `products` | COMPLETE | PASS |
| 41 | Product Categories | STORE, ALL | ProductsStocksSubScreen | `products` | `frm_category` | `products` | COMPLETE | PASS |
| 42 | Warehouses | STORE, ALL | ProductsStocksSubScreen | `products` | `frm_warehouse` | `supply_items` | COMPLETE | PASS |
| 43 | Stock-In | STORE, ALL | ProductsStocksSubScreen | `products` | `frm_stock_in` | `stock_movements`, `products` | COMPLETE | PASS |
| 44 | Stock-Out | STORE, ALL | ProductsStocksSubScreen | `products` | `frm_stock_out` | `stock_movements`, `products` | COMPLETE | PASS |
| 45 | Stock Transfer | STORE, ALL | ProductsStocksSubScreen | `products` | `frm_stock_transfer` | `stock_movements`, `products` | COMPLETE | PASS |
| 46 | Serial and Batch Tracking | STORE, ALL | ProductsStocksSubScreen | `products` | `frm_serial` | `stock_movements` (serialNumbers) | COMPLETE | PASS |
| 47 | Spare Parts Inventory | STORE, SERVICE_MGR | ProductsStocksSubScreen | `products` | `frm_spare_parts` | `supply_items` | COMPLETE | PASS |
| 48 | Service Tickets | ALL | ServiceScreen | `service` | `frm_ticket` | `service_tickets` | COMPLETE | PASS |
| 49 | Engineer Directory | SERVICE_MGR, OWNER | ServiceScreen | `service` | `frm_engineer` | `users` (ENGINEER role) | COMPLETE | PASS |
| 50 | Engineer Schedule | SERVICE_MGR, ENG | ServiceScreen | `service` | `frm_schedule` | `service_tickets` | COMPLETE | PASS |
| 51 | Engineer Customer Visit | ENG | ServiceScreen | `service` | `frm_visit` | `service_tickets` | COMPLETE | PASS |
| 52 | Engineer Job Report | ENG | ServiceScreen | `service` | `frm_job_report` | `service_tickets` | COMPLETE | PASS |
| 53 | Service Parts Usage | ENG, STORE | ServiceScreen | `service` | `frm_parts` | `service_tickets` | COMPLETE | PASS |
| 54 | Warranty Registration | ALL | ServiceScreen | `service` | `frm_warranty_reg` | `warranties` | COMPLETE | PASS |
| 55 | Warranty Claims | ALL | ServiceScreen | `service` | `frm_warranty_claim` | `warranties` | COMPLETE | PASS |
| 56 | Warranty Parts Dispatch | STORE, SERVICE_MGR | ServiceScreen | `service` | `frm_warranty_dispatch` | `warranties` | COMPLETE | PASS |
| 57 | Agreements | OWNER, ACCOUNTS, SALES | AgreementsSubScreen | `agreements` | `frm_agreement` | `emi_agreements` | COMPLETE | PASS |
| 58 | EMI Schedule | ALL | AgreementsSubScreen | `agreements` | `frm_emi` | `emi_agreements` | COMPLETE | PASS |
| 59 | Cash and Bank Accounts | ACCOUNTS, OWNER | MoreScreen | `more` | `frm_cash_bank` | `payments` (cash/bank) | COMPLETE | PASS |
| 60 | Expenses | ACCOUNTS, OWNER | MoreScreen | `more` | `frm_expense` | `payments` | COMPLETE | PASS |
| 61 | Income | ACCOUNTS, OWNER | MoreScreen | `more` | `frm_income` | `payments` | COMPLETE | PASS |
| 62 | Vouchers | ACCOUNTS, OWNER | MoreScreen | `more` | `frm_voucher` | `payments` | COMPLETE | PASS |
| 63 | Approvals | OWNER, ADMIN | MoreScreen | `more` | `frm_approval` | `payments` / `sales_orders` | COMPLETE | PASS |
| 64 | Documents and Attachments | ALL | MoreScreen | `more` | `frm_document` | Room / Storage | COMPLETE | PASS |
| 65 | Reports | OWNER, ADMIN, ACCOUNTS | ReportsSubScreen | `reports` | `N/A` | Room Tables | COMPLETE | PASS |
| 66 | Print/PDF/CSV Export | OWNER, ADMIN, ACCOUNTS | ReportsSubScreen | `reports` | `N/A` | CSV Generator | COMPLETE | PASS |
| 67 | User Management | ADMIN, OWNER | MoreScreen | `more` | `frm_user` | `users` | COMPLETE | PASS |
| 68 | Roles and Permissions | ADMIN, OWNER | MoreScreen | `more` | `frm_permission` | `users` (roles) | COMPLETE | PASS |
| 69 | Audit Log | ADMIN, OWNER | AuditSubScreen | `audit` | `N/A` | `audit_logs` | COMPLETE | PASS |
| 70 | Login History | ADMIN, OWNER | AuditSubScreen | `audit` | `N/A` | `audit_logs` (Login events) | COMPLETE | PASS |
| 71 | Settings | ALL | MoreScreen | `more` | `frm_settings` | Room / Preferences | COMPLETE | PASS |
| 72 | Backup and Restore | ADMIN, OWNER | MoreScreen | `more` | `frm_backup` | Room Export | COMPLETE | PASS |
| 73 | Offline Data Handling | ALL | MoreScreen | `more` | `N/A` | Room Local Cache | COMPLETE | PASS |
| 74 | Search and Global Filters | ALL | DashboardScreen / More | `search` | `N/A` | Query Filters | COMPLETE | PASS |
| 75 | Android APK/AAB Build | OWNER, DEV | Build system | `build` | `N/A` | Android Gradle | COMPLETE | PASS |
