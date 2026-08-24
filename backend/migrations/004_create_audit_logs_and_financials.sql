-- Phase 1: Establish Financial Transactions & Audit Logging Databases
-- ColorJet ERP Bangladesh

CREATE TABLE IF NOT EXISTS audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    query_sql TEXT NOT NULL,
    payload TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS financial_transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_name VARCHAR(150) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEBIT', 'CREDIT')),
    amount DECIMAL(18, 4) NOT NULL DEFAULT 0.0000,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed initial financial transactions
INSERT OR IGNORE INTO financial_transactions (id, account_name, transaction_type, amount, description) VALUES
(1, 'Cash Account', 'DEBIT', 50000.0000, 'Initial capital injection'),
(2, 'Raw Materials Expense', 'CREDIT', 12000.0000, 'Purchase of UV Cyan Inks');
