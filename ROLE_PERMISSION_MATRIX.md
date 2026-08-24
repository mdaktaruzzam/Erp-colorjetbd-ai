# COLORJET Management Suite - Role Permission Matrix

This matrix outlines the strict, granular access rules configured for different staff roles and customer portals.

| Role ID | Role Name | Dashboards Accessible | Module Visibility Rights | Database Writes | Configuration / Setup |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **OWNER** | Owner (Md Aktaruzzaman)| Executive Dashboard | All Modules | Unlimited | Full Access |
| **ADMIN** | System Administrator | Administration Dashboard| All Modules | Unlimited (except Owner only)| Full Access |
| **MANAGER** | General Manager | Management Dashboard | All except Financial Reserves | Approved Modules | View Only |
| **HR** | Human Resources Manager | Staff Dashboard | Personnel, Shifts, Leave, Salaries| HR database writes | View Only |
| **ACCOUNTS** | Financial Accountant | Accounts Dashboard | Ledgers, Invoices, Cash/Bank, Expenses | Post payments / receipts | No |
| **SALES** | Sales & Marketing Exec | Sales Dashboard | Customer, Leads, Quotations, Orders | Create quotes & orders | No |
| **SERVICE_MGR**| Service Manager | Service Dashboard | Tickets, Schedules, Warranty Claims | Edit tickets & assignments | No |
| **ENGINEER** | Service Engineer | Engineer Dashboard | Service Tickets, Visits, Parts | Job Reports / Signatures | No |
| **STORE** | Warehouse Storekeeper | Inventory Dashboard | Products, Warehouses, Stock In/Out | Register movements | No |
| **OFFICE_STAFF**| Corporate Office Staff | Staff Dashboard | Tasks, Directories, Messaging | Complete tasks | No |
| **CUSTOMER** | Client Representative | Customer Portal | Isolated own invoices, machines & tickets| Request support tickets | No |

## Permission Rules Checked in Composable Render Blocks

Access restrictions are checked inline using:
- `userRole` comparison.
- Column-level data isolation (e.g. customer ID check in queries).
- Inline composable condition locks preventing unauthorized role views.
