# COLORJET Management Suite - API Route Map

This document tracks how local data services and transaction handlers bridge client interactions directly to the secure Room Engine.

| Service/API Endpoint | Functionality | Request Structure | DB Operation | Role Restricted |
| :--- | :--- | :--- | :--- | :--- |
| `login(username, pass)` | Authenticates user credentials | `username`, `password` | `UserDao.getUserByUsername` | All Roles |
| `getDashboardMetrics()` | Computes financial & service statistics | State triggers | Multi-table aggregations | OWNER, ADMIN, MGR |
| `getEmployees()` | Pulls list of active staff | State query | `UserDao.getAllUsers` | OWNER, ADMIN, HR, MGR |
| `checkIn(empId, loc)` | Logs GPS coordinate check-ins | `empId`, `latitude`, `longitude` | Inserts record into `attendance` | All Staff |
| `checkOut(empId, loc)`| Updates shift departure logs | `empId`, `latitude`, `longitude` | Updates record in `attendance` | All Staff |
| `getCustomers()` | Fetches customers | State query | `ErpDao.getAllCustomers` | All Roles |
| `postInvoice(inv)` | Registers customer invoices | Invoice details | Inserts record into `invoices` | ACCOUNTS, OWNER |
| `postPayment(pay)` | Inserts payment transactions | Payment receipt details | Room Transaction (Invoices / Ledgers) | ACCOUNTS, OWNER |
| `createTicket(tkt)` | Logs printing equipment faults | Ticket details | Inserts record into `service_tickets` | All Roles |
| `resolveTicket(tkt)` | Closes resolved tickets | Diagnostics & parts used | Updates `service_tickets` / `products` | ENGINEER, SERVICE_MGR |
| `getMessages()` | Pulls private and broadcast text boards| State query | `ErpDao.getAllMessages` | All Roles |
| `postMessage(msg)` | Sends messaging communications | Message body & recipients | Inserts record into `internal_messages` | All Roles |

## Secure Database Transactions

Critical actions (e.g., `postPayment`) execute inside `RoomDatabase.withTransaction {}` blocks to prevent split states or unbalanced books.
