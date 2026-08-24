# COLORJET Management Suite - Form Route Map

This document lists the strict, non-overlapping routing schema for forms inside the application. No generic forms are reused across unrelated business modules, ensuring absolute data integrity.

| Form Name | Route ID | Screen Component | Form ID | Target API / DB Operation |
| :--- | :--- | :--- | :--- | :--- |
| **Add Employee** | `directory` | `DirectorySubScreen` | `frm_employee` | Inserts new employee record in `users` |
| **Add Customer** | `customers` | `CustomersSubScreen` | `frm_customer` | Inserts new customer record in `customers` |
| **Add Machine** | `customers` | `CustomersSubScreen` | `frm_machine` | Binds machinery to customer in `machines` |
| **Add Lead** | `customers` | `CustomersSubScreen` | `frm_lead` | Inserts prospect row into `quotations` |
| **Create Quotation** | `customers` | `CustomersSubScreen` | `frm_quotation` | Saves printable quote in `quotations` |
| **Create Sales Order** | `customers` | `CustomersSubScreen` | `frm_sales_order` | Adds order status to `sales_orders` |
| **Register Delivery** | `customers` | `CustomersSubScreen` | `frm_delivery` | Updates delivery flags in `sales_orders` |
| **Machine Installation**| `customers` | `CustomersSubScreen` | `frm_install` | Configures active status in `machines` |
| **Create Invoice** | `invoices` | `InvoicesSubScreen` | `frm_invoice` | Generates a bill in `invoices` |
| **Receive Payment** | `payments` | `PaymentsSubScreen` | `frm_payment` | Adds payment transaction inside Room DB transaction |
| **Add Supplier** | `suppliers` | `SuppliersSubScreen` | `frm_supplier` | Adds industrial supplier in `suppliers` |
| **Create Purchase Order**| `suppliers` | `SuppliersSubScreen` | `frm_po` | Adds purchase agreement in `suppliers` |
| **Create LC** | `foreign_purchase`| `ForeignPurchaseSubScreen`| `frm_lc` | Adds commercial credit record in `foreign_purchases` |
| **Log TT Payment** | `foreign_purchase`| `ForeignPurchaseSubScreen`| `frm_tt` | Records TT transfer inside `foreign_purchases` |
| **Record Import** | `foreign_purchase`| `ForeignPurchaseSubScreen`| `frm_foreign` | Saves shipping lines context in `foreign_purchases` |
| **Track Shipment** | `foreign_purchase`| `ForeignPurchaseSubScreen`| `frm_shipment` | Tracks transit cargo inside `foreign_purchases` |
| **Landed Cost Calc** | `foreign_purchase`| `ForeignPurchaseSubScreen`| `frm_landed_cost` | Allocates logistics overhead and updates margins |
| **Add Product** | `products` | `ProductsStocksSubScreen` | `frm_product` | Adds stock SKU in `products` |
| **Add Warehouse** | `products` | `ProductsStocksSubScreen` | `frm_warehouse`| Registers storage unit in `supply_items` |
| **Stock In / Out** | `products` | `ProductsStocksSubScreen` | `frm_stock_in_out`| Adjusts balances using `stock_movements` table |
| **Create Ticket** | `service` | `ServiceScreen` | `frm_ticket` | Creates service ticket `service_tickets` |
| **Resolve Ticket** | `service` | `ServiceScreen` | `frm_resolve` | Completes and records parts consumed |
| **Compose Message** | `messages` | `MessagingSubScreen` | `frm_message` | Broadcasts text inside `internal_messages` |
| **Send Push** | `announcements` | `AnnouncementsSubScreen` | `frm_push` | Triggers notification via `push_announcements` |
| **Create Agreement** | `agreements` | `AgreementsSubScreen` | `frm_agreement` | Generates lease contract in `emi_agreements` |
| **Log Expense** | `more` | `MoreScreen` | `frm_expense` | Adds debit to accounts register |
| **Log Income** | `more` | `MoreScreen` | `frm_income` | Adds credit to accounts register |
| **Add User** | `more` | `MoreScreen` | `frm_user` | Inserts system login credential in `users` |
| **Set Permissions** | `more` | `MoreScreen` | `frm_permission` | Modifies active privileges in `users` table |
