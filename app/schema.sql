-- ==============================================================================
-- COLORJET Business Management ERP - Enterprise Relational Database Schema
-- Target Engine: PostgreSQL 15+ / Firebase Data Connect / Cloud SQL compatible
-- Description: Production-ready relational schema with UUID Primary Keys,
--              high-precision DECIMALS, Maker-Checker-Approver support, 
--              and index coverage for industrial scales.
-- Author: Google AI Studio Coding Agent (DeepMind powered)
-- ==============================================================================

-- Enable UUID extension for distributed key generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==============================================================================
-- MODULE 1: BASE SYSTEM, ORGANIZATIONAL HIERARCHY, SECURITY & ACCESS
-- ==============================================================================

CREATE TABLE businesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),
    registration_number VARCHAR(100),
    tax_id VARCHAR(100),
    bin VARCHAR(100), -- Business Identification Number (Bangladesh)
    tin VARCHAR(100), -- Taxpayer Identification Number
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address TEXT,
    phone VARCHAR(50),
    email VARCHAR(100),
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_branch_code_per_business UNIQUE (business_id, code)
);

CREATE TABLE departments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID REFERENCES branches(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dept_code_per_business UNIQUE (business_id, code)
);

CREATE TABLE designations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    grade VARCHAR(50),
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE territories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    region VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_territory_code UNIQUE (business_id, code)
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    pin_code VARCHAR(10), -- 4-digit terminal logins
    phone VARCHAR(50),
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'PENDING')),
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_username_per_business UNIQUE (business_id, username),
    CONSTRAINT uq_email_per_business UNIQUE (business_id, email)
);

CREATE TABLE employee_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    department_id UUID REFERENCES departments(id) ON DELETE SET NULL,
    designation_id UUID REFERENCES designations(id) ON DELETE SET NULL,
    employee_code VARCHAR(100) NOT NULL,
    first_name VARCHAR(150) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    nid VARCHAR(50), -- National ID
    passport_number VARCHAR(50),
    driving_license VARCHAR(50),
    blood_group VARCHAR(10),
    joining_date DATE NOT NULL,
    resign_date DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'RESIGNED', 'TERMINATED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_employee_code UNIQUE (business_id, employee_code)
);

CREATE TABLE employee_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE CASCADE,
    document_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(100) NOT NULL, -- 'NID', 'PASSPORT', 'CV', 'CERTIFICATE'
    file_url TEXT NOT NULL,
    expiry_date DATE,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_bank_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE CASCADE,
    bank_name VARCHAR(255) NOT NULL,
    branch_name VARCHAR(255) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(100) NOT NULL,
    routing_number VARCHAR(50),
    is_primary BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE CASCADE,
    device_model VARCHAR(100),
    os_version VARCHAR(50),
    device_uuid VARCHAR(255) UNIQUE NOT NULL,
    fcm_token TEXT,
    is_authorized BOOLEAN DEFAULT FALSE,
    authorized_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE territories_employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    territory_id UUID NOT NULL REFERENCES territories(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE CASCADE,
    assigned_role VARCHAR(100) DEFAULT 'SALES_REPRESENTATIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_territory_employee UNIQUE (territory_id, employee_id)
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL, -- e.g. 'OWNER', 'ACCOUNTS', 'ENGINEER'
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_role_name_per_business UNIQUE (business_id, name)
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) UNIQUE NOT NULL, -- e.g. 'view_profit', 'confirm_invoice'
    module_name VARCHAR(100) NOT NULL, -- e.g. 'ACCOUNTS', 'SALES'
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    data_scope VARCHAR(50) DEFAULT 'assigned_records' CHECK (data_scope IN (
        'own_records', 'assigned_records', 'team_records', 'department_records', 'branch_records', 'business_records', 'all_company_records'
    )),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id)
);

CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE user_permission_overrides (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    allow_deny BOOLEAN NOT NULL DEFAULT TRUE, -- TRUE = explicit allow, FALSE = explicit deny
    data_scope VARCHAR(50) DEFAULT 'assigned_records',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_permission_override UNIQUE (user_id, permission_id)
);

-- Organization context access tables
CREATE TABLE user_business_access (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, business_id)
);

CREATE TABLE user_branch_access (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, branch_id)
);

CREATE TABLE user_department_access (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    department_id UUID NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, department_id)
);

-- Device Registry & Security Tokens
CREATE TABLE api_devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_fingerprint VARCHAR(255) UNIQUE NOT NULL,
    device_name VARCHAR(150),
    os_name VARCHAR(100),
    app_version VARCHAR(50),
    is_trusted BOOLEAN DEFAULT FALSE,
    trusted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE api_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id UUID REFERENCES api_devices(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    api_token_id UUID REFERENCES api_tokens(id) ON DELETE SET NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    login_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP WITH TIME ZONE
);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Audits & Request Action Logs
CREATE TABLE login_audit (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    status VARCHAR(50) NOT NULL, -- 'SUCCESS', 'FAILED_PASSWORD', 'BLOCKED', 'PIN_FAILED'
    failure_reason TEXT,
    attempted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE activity_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    module VARCHAR(100) NOT NULL, -- 'SALES', 'STOCK', 'HR'
    action VARCHAR(100) NOT NULL, -- 'create', 'approve', 'delete'
    entity_id VARCHAR(100),       -- ID of the entity operated on
    entity_type VARCHAR(100),     -- e.g. 'invoice', 'purchase_order'
    payload TEXT,                 -- raw metadata changes
    ip_address VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    branch_id UUID REFERENCES branches(id) ON DELETE CASCADE,
    table_name VARCHAR(100) NOT NULL,
    record_id UUID NOT NULL,
    action_type VARCHAR(20) CHECK (action_type IN ('INSERT', 'UPDATE', 'DELETE')),
    old_value JSONB,
    new_value JSONB,
    performed_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    performed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MAKER-CHECKER-APPROVER SYSTEM WORKFLOW
-- ==============================================================================

CREATE TABLE approval_workflows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    module VARCHAR(100) NOT NULL, -- 'INVOICE', 'STOCK_ADJUSTMENT', 'PAYROLL', 'LANDED_COST'
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE approval_steps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_id UUID NOT NULL REFERENCES approval_workflows(id) ON DELETE CASCADE,
    step_number INT NOT NULL, -- 1 = CHECKER, 2 = APPROVER, 3 = MAIN APPROVER
    required_role_id UUID REFERENCES roles(id) ON DELETE SET NULL,
    assigned_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE approval_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    branch_id UUID REFERENCES branches(id) ON DELETE CASCADE,
    workflow_id UUID REFERENCES approval_workflows(id) ON DELETE SET NULL,
    entity_type VARCHAR(100) NOT NULL, -- 'invoice', 'goods_receipt', 'salary_structure'
    entity_id UUID NOT NULL,
    maker_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    current_step_number INT DEFAULT 1,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CHECKED', 'APPROVED', 'REJECTED')),
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE approval_action_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    approval_request_id UUID NOT NULL REFERENCES approval_requests(id) ON DELETE CASCADE,
    performed_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    action_type VARCHAR(50) NOT NULL, -- 'CHECKED', 'APPROVED', 'REJECTED'
    remarks TEXT,
    performed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE entity_status_histories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    remarks TEXT,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Period locks for financial periods
CREATE TABLE period_locks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    lock_date DATE NOT NULL, -- No transactions allowed on or before this date
    status VARCHAR(50) DEFAULT 'LOCKED' CHECK (status IN ('LOCKED', 'UNLOCKED')),
    locked_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE period_lock_exceptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    period_lock_id UUID NOT NULL REFERENCES period_locks(id) ON DELETE CASCADE,
    allowed_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    module VARCHAR(100) NOT NULL, -- e.g. 'SALES', 'ACCOUNTS'
    expiry_time TIMESTAMP WITH TIME ZONE NOT NULL,
    granted_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 2: COLORJET THEME, BRANDING & WIDGET CONFIGURATIONS
-- ==============================================================================

CREATE TABLE company_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL UNIQUE REFERENCES businesses(id) ON DELETE CASCADE,
    display_name VARCHAR(255) NOT NULL,
    slogan VARCHAR(255),
    corporate_headquarters TEXT,
    website VARCHAR(255),
    support_email VARCHAR(100),
    support_phone VARCHAR(50),
    custom_domain VARCHAR(255)
);

CREATE TABLE branding_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL UNIQUE REFERENCES businesses(id) ON DELETE CASCADE,
    logo_dark_url TEXT,
    logo_light_url TEXT,
    favicon_url TEXT,
    theme_primary_color VARCHAR(10) DEFAULT '#0D47A1', -- primary_blue
    theme_deep_navy VARCHAR(10) DEFAULT '#182078',
    theme_bright_blue VARCHAR(10) DEFAULT '#0097E8',
    theme_accent_orange VARCHAR(10) DEFAULT '#FF6F00',
    theme_success_green VARCHAR(10) DEFAULT '#22C55E',
    theme_warning_yellow VARCHAR(10) DEFAULT '#F59E0B',
    theme_danger_red VARCHAR(10) DEFAULT '#EF4444',
    theme_analytics_purple VARCHAR(10) DEFAULT '#7C3AED',
    font_family VARCHAR(100) DEFAULT 'Space Grotesk',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE theme_change_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    changed_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    old_values JSONB,
    new_values JSONB,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 3: ACCOUNTING FOUNDATION & LEDGERS
-- ==============================================================================

CREATE TABLE fiscal_years (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL, -- e.g. 'FY2026-2027'
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounting_periods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    fiscal_year_id UUID NOT NULL REFERENCES fiscal_years(id) ON DELETE CASCADE,
    period_name VARCHAR(100) NOT NULL, -- e.g. 'July 2026'
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_locked BOOLEAN DEFAULT FALSE
);

CREATE TABLE chart_of_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    account_code VARCHAR(100) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL CHECK (account_type IN ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE')),
    parent_account_id UUID REFERENCES chart_of_accounts(id) ON DELETE CASCADE,
    is_reconciliation_allowed BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_account_code_per_business UNIQUE (business_id, account_code)
);

CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL, -- 'CASH', 'BANK_TRANSFER', 'CHEQUE', 'BKASH', 'NAGAD'
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cash_bank_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    branch_id UUID REFERENCES branches(id) ON DELETE CASCADE,
    account_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(100),
    bank_name VARCHAR(150),
    swift_code VARCHAR(50),
    routing_number VARCHAR(50),
    gl_account_id UUID REFERENCES chart_of_accounts(id) ON DELETE RESTRICT,
    account_type VARCHAR(50) DEFAULT 'CASH' CHECK (account_type IN ('CASH', 'SAVINGS', 'CURRENT', 'CREDIT_CARD', 'MOBILE_BANKING')),
    opening_balance DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    current_balance DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vouchers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    voucher_number VARCHAR(100) NOT NULL,
    voucher_type VARCHAR(50) NOT NULL CHECK (voucher_type IN ('PAYMENT', 'RECEIPT', 'JOURNAL', 'CONTRA')),
    voucher_date DATE NOT NULL,
    narration TEXT,
    status VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'CHECKED', 'APPROVED', 'REVERSED')),
    maker_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    checker_id UUID REFERENCES users(id) ON DELETE RESTRICT,
    approver_id UUID REFERENCES users(id) ON DELETE RESTRICT,
    reversal_reference_id UUID, -- self reference when cancelled/reversed
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_voucher_number UNIQUE (business_id, voucher_number)
);

CREATE TABLE voucher_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    voucher_id UUID NOT NULL REFERENCES vouchers(id) ON DELETE CASCADE,
    gl_account_id UUID NOT NULL REFERENCES chart_of_accounts(id) ON DELETE RESTRICT,
    debit_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    credit_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    narration TEXT,
    CONSTRAINT chk_balanced_line CHECK (debit_amount >= 0 AND credit_amount >= 0)
);

-- POSTED FINANCIAL SOURCE OF TRUTH (Immutable entries posted after final approval)
CREATE TABLE accounting_postings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    posting_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source_document_id UUID NOT NULL, -- points to invoices, vouchers, etc.
    source_document_type VARCHAR(100) NOT NULL, -- 'sales_invoice', 'supplier_payment', 'salary_slip'
    reference_number VARCHAR(100),
    narration TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounting_posting_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    posting_id UUID NOT NULL REFERENCES accounting_postings(id) ON DELETE CASCADE,
    gl_account_id UUID NOT NULL REFERENCES chart_of_accounts(id) ON DELETE RESTRICT,
    debit DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    credit DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    reference_id UUID, -- link to subledgers (customers, suppliers, employees)
    reference_type VARCHAR(50) CHECK (reference_type IN ('CUSTOMER', 'SUPPLIER', 'EMPLOYEE', 'ASSET', 'NONE'))
);

-- ==============================================================================
-- MODULE 4: SUBLEDGERS
-- ==============================================================================

CREATE TABLE customer_ledgers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL, -- referenced in Module 5
    posting_id UUID REFERENCES accounting_postings(id) ON DELETE SET NULL,
    transaction_date DATE NOT NULL,
    reference_number VARCHAR(100) NOT NULL,
    description TEXT,
    debit_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    credit_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    running_balance DECIMAL(18,4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE supplier_ledgers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    supplier_id UUID NOT NULL, -- referenced in Module 9
    posting_id UUID REFERENCES accounting_postings(id) ON DELETE SET NULL,
    transaction_date DATE NOT NULL,
    reference_number VARCHAR(100) NOT NULL,
    description TEXT,
    debit_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    credit_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    running_balance DECIMAL(18,4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_financial_ledgers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE RESTRICT,
    posting_id UUID REFERENCES accounting_postings(id) ON DELETE SET NULL,
    transaction_date DATE NOT NULL,
    reference_number VARCHAR(100) NOT NULL,
    description TEXT,
    debit_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    credit_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    running_balance DECIMAL(18,4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Reconciliations & Cheques
CREATE TABLE cheques (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    cash_bank_account_id UUID NOT NULL REFERENCES cash_bank_accounts(id) ON DELETE RESTRICT,
    cheque_number VARCHAR(100) NOT NULL,
    cheque_date DATE NOT NULL,
    amount DECIMAL(18,4) NOT NULL,
    payee_recipient_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PRESENTED', 'CLEARED', 'BOUNCED', 'CANCELLED')),
    cleared_at TIMESTAMP WITH TIME ZONE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bank_reconciliations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cash_bank_account_id UUID NOT NULL REFERENCES cash_bank_accounts(id) ON DELETE RESTRICT,
    reconciliation_date DATE NOT NULL,
    bank_statement_balance DECIMAL(18,4) NOT NULL,
    gl_balance DECIMAL(18,4) NOT NULL,
    unreconciled_difference DECIMAL(18,4) NOT NULL,
    completed_by UUID REFERENCES users(id) ON DELETE RESTRICT,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 5: CUSTOMER RELATIONSHIP MANAGEMENT (CRM) & CLIENT PORTAL
-- ==============================================================================

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    customer_code VARCHAR(100) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    company_name VARCHAR(255),
    contact_person VARCHAR(255),
    phone VARCHAR(50) NOT NULL,
    alternate_phone VARCHAR(50),
    whatsapp VARCHAR(50),
    email VARCHAR(255),
    address TEXT NOT NULL,
    district VARCHAR(100), -- regional breakdown
    division VARCHAR(100),
    postal_code VARCHAR(50),
    credit_limit DECIMAL(18,4) DEFAULT 0.0000,
    opening_due DECIMAL(18,4) DEFAULT 0.0000,
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLACKLISTED')),
    salesperson_id UUID REFERENCES employee_profiles(id) ON DELETE SET NULL,
    marketing_person_id UUID REFERENCES employee_profiles(id) ON DELETE SET NULL,
    collection_officer_id UUID REFERENCES employee_profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_customer_code UNIQUE (business_id, customer_code)
);

CREATE TABLE customer_contacts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    designation VARCHAR(150),
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_machines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    brand VARCHAR(100) NOT NULL,       -- e.g. 'COLORJET'
    model_name VARCHAR(150) NOT NULL,  -- e.g. 'UV-6090 Flatbed'
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    printhead_model VARCHAR(100),
    installation_date DATE,
    warranty_expiry_date DATE,
    last_service_date DATE,
    status VARCHAR(50) DEFAULT 'OPERATIONAL' CHECK (status IN ('OPERATIONAL', 'BREAKDOWN', 'UNDER_MAINTENANCE', 'DECOMMISSIONED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_portal_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_feedback (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    service_ticket_id UUID, -- can link with a service ticket
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comments TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_complaints (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    complaint_type VARCHAR(100) NOT NULL, -- 'SERVICE_DELAY', 'BILLING', 'PRODUCT_QUALITY'
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'INVESTIGATING', 'RESOLVED', 'CLOSED')),
    resolution_details TEXT,
    resolved_by UUID REFERENCES users(id),
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Leads & Pipeline
CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    customer_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    product_interest TEXT, -- machine models
    expected_value DECIMAL(18,4),
    pipeline_stage VARCHAR(100) NOT NULL DEFAULT 'New' CHECK (pipeline_stage IN (
        'New', 'Contacted', 'Qualified', 'Requirement Collected', 'Demo Scheduled', 'Demo Completed', 'Quotation Sent', 'Negotiation', 'Won', 'Lost'
    )),
    probability_percent INT DEFAULT 10,
    assigned_employee_id UUID REFERENCES employee_profiles(id) ON DELETE SET NULL,
    source VARCHAR(100), -- 'EXHIBITION', 'WEBSITE', 'COLD_CALL'
    lost_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lead_activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    activity_type VARCHAR(100) NOT NULL, -- 'CALL', 'MEETING', 'DEMO', 'VISIT'
    description TEXT,
    performed_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    performed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE marketing_visits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE RESTRICT,
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    lead_id UUID REFERENCES leads(id) ON DELETE SET NULL,
    visit_date DATE NOT NULL,
    purpose TEXT NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    location_name TEXT,
    outcome_summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 6: PRODUCTS, STOCK & WAREHOUSING
-- ==============================================================================

CREATE TABLE product_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    CONSTRAINT uq_prod_cat_code UNIQUE (business_id, code)
);

CREATE TABLE product_subcategories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id UUID NOT NULL REFERENCES product_categories(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    CONSTRAINT uq_prod_subcat_code UNIQUE (category_id, code)
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    category_id UUID NOT NULL REFERENCES product_categories(id) ON DELETE RESTRICT,
    subcategory_id UUID REFERENCES product_subcategories(id) ON DELETE SET NULL,
    product_code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(100),
    barcode VARCHAR(100),
    product_type VARCHAR(50) NOT NULL CHECK (product_type IN (
        'Machine', 'Ink', 'Consumable', 'Spare Part', 'Accessory', 'Service', 'Installation Charge', 'Freight'
    )),
    is_serial_tracked BOOLEAN DEFAULT FALSE,
    reorder_level DECIMAL(18,4) DEFAULT 1.0000,
    uom VARCHAR(50) DEFAULT 'Pcs', -- Unit of Measure
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_product_code UNIQUE (business_id, product_code)
);

CREATE TABLE product_prices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    currency VARCHAR(10) DEFAULT 'BDT',
    cost_price DECIMAL(18,4) NOT NULL DEFAULT 0.0000, -- Weighted cost price
    retail_price DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    minimum_selling_price DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE warehouses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    location_address TEXT,
    warehouse_type VARCHAR(50) DEFAULT 'Main Warehouse' CHECK (warehouse_type IN (
        'Main Warehouse', 'Spare Parts Store', 'Showroom', 'Service Van', 'Transit Stock', 'Damaged Stock'
    )),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_warehouse_code UNIQUE (business_id, code)
);

-- Dynamic inventory tracking
CREATE TABLE product_stock (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    allocated_quantity DECIMAL(18,4) NOT NULL DEFAULT 0.0000, -- Reserved for sales orders
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_warehouse_product UNIQUE (warehouse_id, product_id)
);

-- STOCK MOVEMENTS (Auditable, transaction-based ledger entries. Never directly overwritten.)
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    movement_type VARCHAR(50) NOT NULL CHECK (movement_type IN (
        'opening_stock', 'local_purchase_receive', 'foreign_purchase_receive', 'sales_invoice_out', 
        'delivery_out', 'service_parts_out', 'customer_return_in', 'supplier_return_out', 
        'warehouse_transfer_out', 'warehouse_transfer_in', 'stock_adjustment_in', 'stock_adjustment_out'
    )),
    quantity DECIMAL(18,4) NOT NULL,
    unit_cost DECIMAL(18,4) NOT NULL,
    source_document_id UUID NOT NULL, -- points to invoices, shipments, transfers, etc.
    source_document_type VARCHAR(100) NOT NULL,
    performed_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    checker_id UUID REFERENCES users(id) ON DELETE RESTRICT,
    approver_id UUID REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE serial_numbers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    serial_code VARCHAR(100) UNIQUE NOT NULL,
    warehouse_id UUID REFERENCES warehouses(id) ON DELETE SET NULL,
    status VARCHAR(50) DEFAULT 'IN_STOCK' CHECK (status IN ('IN_STOCK', 'SOLD', 'SERVICE_REPLACEMENT', 'DAMAGED', 'TRANSIT')),
    associated_customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE serial_movement_histories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    serial_number_id UUID NOT NULL REFERENCES serial_numbers(id) ON DELETE CASCADE,
    source_warehouse_id UUID REFERENCES warehouses(id),
    destination_warehouse_id UUID REFERENCES warehouses(id),
    stock_movement_id UUID REFERENCES stock_movements(id) ON DELETE SET NULL,
    movement_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE stock_transfer_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    source_warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    destination_warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    request_number VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DISPATCHED', 'RECEIVED', 'REJECTED')),
    maker_id UUID NOT NULL REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE stock_transfer_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transfer_request_id UUID NOT NULL REFERENCES stock_transfer_requests(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    requested_quantity DECIMAL(18,4) NOT NULL,
    transferred_quantity DECIMAL(18,4)
);

CREATE TABLE stock_adjustment_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    adjustment_number VARCHAR(100) UNIQUE NOT NULL,
    reason TEXT,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    maker_id UUID NOT NULL REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 7: SALES, INVOICES, DELIVERIES & COLLECTIONS
-- ==============================================================================

CREATE TABLE quotations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    quotation_number VARCHAR(100) NOT NULL,
    valid_until DATE,
    net_total DECIMAL(18,4) NOT NULL,
    tax_total DECIMAL(18,4) NOT NULL,
    grand_total DECIMAL(18,4) NOT NULL,
    status VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'SENT', 'ACCEPTED', 'REJECTED')),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_quotation_number UNIQUE (business_id, quotation_number)
);

CREATE TABLE quotation_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quotation_id UUID NOT NULL REFERENCES quotations(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity DECIMAL(18,4) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    discount_amount DECIMAL(18,4) DEFAULT 0.0000,
    line_total DECIMAL(18,4) NOT NULL
);

CREATE TABLE sales_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    quotation_id UUID REFERENCES quotations(id) ON DELETE SET NULL,
    order_number VARCHAR(100) NOT NULL,
    order_date DATE NOT NULL,
    delivery_deadline DATE,
    grand_total DECIMAL(18,4) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CHECKED', 'APPROVED', 'PROCESSING', 'DISPATCHED', 'COMPLETED', 'CANCELLED')),
    maker_id UUID NOT NULL REFERENCES users(id),
    checker_id UUID REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_order_number UNIQUE (business_id, order_number)
);

CREATE TABLE sales_order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sales_order_id UUID NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity DECIMAL(18,4) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    line_total DECIMAL(18,4) NOT NULL
);

-- SALES INVOICE (Irreversible posted system billing document)
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    sales_order_id UUID REFERENCES sales_orders(id) ON DELETE SET NULL,
    invoice_number VARCHAR(100) NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount DECIMAL(18,4) NOT NULL,
    discount_amount DECIMAL(18,4) DEFAULT 0.0000,
    tax_amount DECIMAL(18,4) DEFAULT 0.0000,
    grand_total DECIMAL(18,4) NOT NULL,
    paid_amount DECIMAL(18,4) DEFAULT 0.0000,
    due_amount DECIMAL(18,4) NOT NULL,
    salesperson_snapshot_id UUID REFERENCES employee_profiles(id),
    marketing_snapshot_id UUID REFERENCES employee_profiles(id),
    status VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'CONFIRMED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED', 'REVERSED')),
    maker_id UUID NOT NULL REFERENCES users(id),
    checker_id UUID REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    audit_reference_id UUID, -- References approval_requests or activity logs
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_invoice_number UNIQUE (business_id, invoice_number)
);

CREATE TABLE invoice_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity DECIMAL(18,4) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    line_total DECIMAL(18,4) NOT NULL
);

CREATE TABLE invoice_serials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    invoice_item_id UUID NOT NULL REFERENCES invoice_items(id) ON DELETE CASCADE,
    serial_number_id UUID NOT NULL REFERENCES serial_numbers(id) ON DELETE RESTRICT,
    CONSTRAINT uq_invoice_serial_item UNIQUE (invoice_item_id, serial_number_id)
);

-- Receipts & Money Receipts (MRs)
CREATE TABLE customer_receipts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    receipt_number VARCHAR(100) NOT NULL,
    receipt_date DATE NOT NULL,
    payment_method_id UUID NOT NULL REFERENCES payment_methods(id),
    cash_bank_account_id UUID REFERENCES cash_bank_accounts(id) ON DELETE RESTRICT,
    received_amount DECIMAL(18,4) NOT NULL,
    bank_charge DECIMAL(18,4) DEFAULT 0.0000,
    reference_number VARCHAR(100), -- transaction ref, bkash txnId, etc.
    is_advance BOOLEAN DEFAULT FALSE,
    maker_id UUID NOT NULL REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_receipt_number UNIQUE (business_id, receipt_number)
);

CREATE TABLE payment_allocations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    receipt_id UUID NOT NULL REFERENCES customer_receipts(id) ON DELETE CASCADE,
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    allocated_amount DECIMAL(18,4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Deliveries
CREATE TABLE delivery_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    sales_order_id UUID REFERENCES sales_orders(id),
    invoice_id UUID REFERENCES invoices(id),
    delivery_number VARCHAR(100) NOT NULL,
    delivery_date DATE NOT NULL,
    driver_name VARCHAR(150),
    vehicle_number VARCHAR(100),
    status VARCHAR(50) DEFAULT 'SCHEDULED' CHECK (status IN (
        'SCHEDULED', 'PACKED', 'DISPATCHED', 'ON_THE_WAY', 'DELIVERED', 'FAILED', 'RETURNED'
    )),
    maker_id UUID NOT NULL REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_delivery_number UNIQUE (business_id, delivery_number)
);

CREATE TABLE delivery_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_order_id UUID NOT NULL REFERENCES delivery_orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    shipped_quantity DECIMAL(18,4) NOT NULL,
    serial_number_id UUID REFERENCES serial_numbers(id) ON DELETE SET NULL
);

CREATE TABLE delivery_proofs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_order_id UUID NOT NULL REFERENCES delivery_orders(id) ON DELETE CASCADE,
    proof_image_url TEXT,
    recipient_signature_url TEXT,
    delivered_latitude DECIMAL(10,8),
    delivered_longitude DECIMAL(11,8),
    notes TEXT,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 8: SUPPLIERS, PURCHASES & LOCAL PROCUREMENT
-- ==============================================================================

CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    supplier_code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    supplier_type VARCHAR(50) DEFAULT 'Local Supplier' CHECK (supplier_type IN (
        'Local Supplier', 'Foreign Supplier', 'Manufacturer', 'Freight Forwarder', 'Shipping Agent', 'CNF Agent'
    )),
    opening_payable DECIMAL(18,4) DEFAULT 0.0000,
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_supplier_code UNIQUE (business_id, supplier_code)
);

CREATE TABLE purchase_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE RESTRICT,
    po_number VARCHAR(100) NOT NULL,
    po_date DATE NOT NULL,
    grand_total DECIMAL(18,4) NOT NULL,
    status VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'CHECKED', 'APPROVED', 'SENT', 'COMPLETED', 'CANCELLED')),
    maker_id UUID NOT NULL REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_po_number UNIQUE (business_id, po_number)
);

CREATE TABLE purchase_order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity DECIMAL(18,4) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    line_total DECIMAL(18,4) NOT NULL
);

-- Goods Receipt Note (GRN) triggers Stock Increase
CREATE TABLE goods_receipts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE RESTRICT,
    purchase_order_id UUID REFERENCES purchase_orders(id) ON DELETE SET NULL,
    grn_number VARCHAR(100) NOT NULL,
    receive_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'CANCELLED')),
    maker_id UUID NOT NULL REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_grn_number UNIQUE (business_id, grn_number)
);

CREATE TABLE goods_receipt_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    goods_receipt_id UUID NOT NULL REFERENCES goods_receipts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    ordered_quantity DECIMAL(18,4) NOT NULL,
    received_quantity DECIMAL(18,4) NOT NULL,
    unit_cost_price DECIMAL(18,4) NOT NULL
);

-- ==============================================================================
-- MODULE 9: SERVICE TICKETS, WARRANTY, AMC & FIELD OPERATIONS
-- ==============================================================================

CREATE TABLE engineer_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL UNIQUE REFERENCES employee_profiles(id) ON DELETE CASCADE,
    specialization VARCHAR(255), -- 'UV printers', 'Eco-Solvent', 'DTF'
    skills_grade VARCHAR(50) DEFAULT 'Junior' CHECK (skills_grade IN ('Junior', 'Senior', 'Expert')),
    is_available BOOLEAN DEFAULT TRUE,
    current_latitude DECIMAL(10,8),
    current_longitude DECIMAL(11,8),
    gps_updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE service_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    customer_machine_id UUID NOT NULL REFERENCES customer_machines(id) ON DELETE RESTRICT,
    ticket_number VARCHAR(100) NOT NULL,
    ticket_type VARCHAR(50) NOT NULL CHECK (ticket_type IN (
        'Warranty', 'Paid Service', 'AMC', 'Installation', 'Training', 'Inspection', 'Remote Support'
    )),
    issue_description TEXT NOT NULL,
    reported_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    priority VARCHAR(50) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status VARCHAR(50) DEFAULT 'OPEN' CHECK (status IN (
        'OPEN', 'ASSIGNED', 'ACCEPTED', 'ON_THE_WAY', 'AT_CUSTOMER_SITE', 'IN_PROGRESS', 'WAITING_PARTS', 'COMPLETED', 'CANCELLED'
    )),
    assigned_engineer_id UUID REFERENCES engineer_profiles(id) ON DELETE SET NULL,
    service_charge DECIMAL(18,4) DEFAULT 0.0000,
    service_report TEXT, -- Filled by engineer on completion
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_ticket_number UNIQUE (business_id, ticket_number)
);

CREATE TABLE service_ticket_attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_ticket_id UUID NOT NULL REFERENCES service_tickets(id) ON DELETE CASCADE,
    file_url TEXT NOT NULL,
    file_type VARCHAR(100), -- 'IMAGE_BREAKDOWN', 'ERROR_LOG'
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_ticket_parts_used (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_ticket_id UUID NOT NULL REFERENCES service_tickets(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT, -- Spare part
    quantity DECIMAL(18,4) NOT NULL DEFAULT 1.0000,
    unit_price DECIMAL(18,4) NOT NULL DEFAULT 0.0000, -- Charge to customer if not covered under warranty/AMC
    is_covered_under_warranty BOOLEAN DEFAULT FALSE,
    stock_movement_id UUID REFERENCES stock_movements(id) ON DELETE SET NULL
);

CREATE TABLE engineer_checkins (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    engineer_id UUID NOT NULL REFERENCES engineer_profiles(id) ON DELETE CASCADE,
    service_ticket_id UUID REFERENCES service_tickets(id) ON DELETE CASCADE,
    checkin_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    checkout_time TIMESTAMP WITH TIME ZONE,
    checkin_latitude DECIMAL(10,8),
    checkin_longitude DECIMAL(11,8),
    checkout_latitude DECIMAL(10,8),
    checkout_longitude DECIMAL(11,8),
    checkin_address TEXT,
    checkout_address TEXT
);

CREATE TABLE service_customer_signatures (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_ticket_id UUID NOT NULL UNIQUE REFERENCES service_tickets(id) ON DELETE CASCADE,
    customer_name VARCHAR(255) NOT NULL,
    signature_image_url TEXT NOT NULL,
    signed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 10: FOREIGN PURCHASES, LETTERS OF CREDIT (LC) & LANDED COST
-- ==============================================================================

CREATE TABLE pi_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE RESTRICT, -- Foreign Supplier
    pi_number VARCHAR(100) NOT NULL,
    pi_date DATE NOT NULL,
    pi_value_foreign DECIMAL(18,4) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    status VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'APPROVED', 'SUBMITTED_TO_BANK')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pi_number UNIQUE (business_id, pi_number)
);

CREATE TABLE pi_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pi_record_id UUID NOT NULL REFERENCES pi_records(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity DECIMAL(18,4) NOT NULL,
    unit_price_foreign DECIMAL(18,4) NOT NULL,
    line_total_foreign DECIMAL(18,4) NOT NULL
);

CREATE TABLE lc_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    pi_record_id UUID NOT NULL REFERENCES pi_records(id) ON DELETE RESTRICT,
    lc_number VARCHAR(100) UNIQUE NOT NULL,
    opening_bank VARCHAR(150) NOT NULL,
    lc_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    lc_value_foreign DECIMAL(18,4) NOT NULL,
    exchange_rate DECIMAL(12,6) NOT NULL, -- BDT per Foreign Currency
    margin_deposited_percent DECIMAL(5,2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tt_payments ( -- Telegraphic Transfer Payments (Direct wire)
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    supplier_id UUID NOT NULL REFERENCES suppliers(id) ON DELETE RESTRICT,
    pi_record_id UUID REFERENCES pi_records(id),
    tt_number VARCHAR(100) NOT NULL,
    tt_date DATE NOT NULL,
    tt_amount_foreign DECIMAL(18,4) NOT NULL,
    exchange_rate DECIMAL(12,6) NOT NULL,
    tt_amount_bdt DECIMAL(18,4) NOT NULL,
    bank_charge_bdt DECIMAL(18,4) DEFAULT 0.0000,
    cash_bank_account_id UUID REFERENCES cash_bank_accounts(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    lc_record_id UUID REFERENCES lc_records(id) ON DELETE SET NULL,
    bl_number VARCHAR(100) UNIQUE NOT NULL, -- Bill of Lading
    shipping_line VARCHAR(150),
    container_number VARCHAR(100),
    port_of_loading VARCHAR(150),
    port_of_discharge VARCHAR(150),
    eta DATE, -- Estimated Arrival
    actual_arrival_date DATE,
    status VARCHAR(50) DEFAULT 'IN_TRANSIT' CHECK (status IN ('IN_TRANSIT', 'PORT_ARRIVED', 'CUSTOMS_CLEARANCE', 'RELEASED', 'DELIVERED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shipment_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    shipped_quantity DECIMAL(18,4) NOT NULL
);

CREATE TABLE shipment_clearances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shipment_id UUID NOT NULL UNIQUE REFERENCES shipments(id) ON DELETE CASCADE,
    cnf_agent_supplier_id UUID NOT NULL REFERENCES suppliers(id), -- CNF supplier account
    bill_of_entry_number VARCHAR(100) NOT NULL,
    clearance_date DATE NOT NULL,
    customs_assessed_value DECIMAL(18,4) NOT NULL, -- Assessed Value for Duty
    duty_paid DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    ait_paid DECIMAL(18,4) NOT NULL DEFAULT 0.0000, -- Advance Income Tax
    vat_paid DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    other_taxes_paid DECIMAL(18,4) DEFAULT 0.0000,
    cnf_bill_amount DECIMAL(18,4) NOT NULL, -- CNF Agent direct charges
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- LANDED COST RECONCILIATION ENGINE
-- Combines ALL acquisition expenses to calculate final item-level cost price
CREATE TABLE landed_cost_summaries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    shipment_id UUID NOT NULL UNIQUE REFERENCES shipments(id) ON DELETE RESTRICT,
    invoice_cost_bdt DECIMAL(18,4) NOT NULL, -- Supplier FOB / CIF Price
    total_customs_costs DECIMAL(18,4) NOT NULL, -- Duties, Taxes, AIT
    total_logistics_costs DECIMAL(18,4) NOT NULL, -- CNF, Freight, Local Transport
    total_bank_charges DECIMAL(18,4) NOT NULL, -- LC commissions, SWIFT, Margins
    grand_total_landed_cost DECIMAL(18,4) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'POSTED')),
    maker_id UUID NOT NULL REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE landed_cost_allocations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    landed_cost_summary_id UUID NOT NULL REFERENCES landed_cost_summaries(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    shipped_quantity DECIMAL(18,4) NOT NULL,
    pi_unit_price_foreign DECIMAL(18,4) NOT NULL,
    pi_total_cost_bdt DECIMAL(18,4) NOT NULL,
    allocated_additional_cost DECIMAL(18,4) NOT NULL, -- Weighted sum of other costs
    final_landed_unit_cost DECIMAL(18,4) NOT NULL, -- Base Cost + Allocated costs
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('UTC', NOW())
);

-- ==============================================================================
-- MODULE 11: AGREEMENTS & EMI SCHEDULES
-- ==============================================================================

CREATE TABLE agreements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT, -- e.g. Colorjet UV machine
    serial_number_id UUID UNIQUE REFERENCES serial_numbers(id) ON DELETE RESTRICT,
    agreement_number VARCHAR(100) NOT NULL,
    agreement_date DATE NOT NULL,
    cash_price DECIMAL(18,4) NOT NULL,
    total_credit_price DECIMAL(18,4) NOT NULL, -- Downpayment + Sum of all EMIs
    downpayment_paid DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    emi_balance_amount DECIMAL(18,4) NOT NULL, -- Due amount to be divided into EMIs
    total_installments INT NOT NULL DEFAULT 12,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'DEFAULTED', 'CLOSED')),
    maker_id UUID NOT NULL REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_agreement_number UNIQUE (business_id, agreement_number)
);

CREATE TABLE emi_schedules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agreement_id UUID NOT NULL REFERENCES agreements(id) ON DELETE CASCADE,
    installment_number INT NOT NULL,
    due_date DATE NOT NULL,
    installment_amount DECIMAL(18,4) NOT NULL,
    principal_component DECIMAL(18,4) NOT NULL,
    interest_component DECIMAL(18,4) DEFAULT 0.0000,
    paid_amount DECIMAL(18,4) DEFAULT 0.0000,
    payment_status VARCHAR(50) DEFAULT 'UNPAID' CHECK (status IN ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'OVERDUE')),
    last_payment_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_installment_per_agreement UNIQUE (agreement_id, installment_number)
);

CREATE TABLE emi_collections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    emi_schedule_id UUID NOT NULL REFERENCES emi_schedules(id) ON DELETE RESTRICT,
    receipt_id UUID NOT NULL REFERENCES customer_receipts(id) ON DELETE RESTRICT,
    collected_amount DECIMAL(18,4) NOT NULL,
    collected_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 12: ASSET MANAGEMENT (Fixed Assets, Capitalized Spares & Tools)
-- ==============================================================================

CREATE TABLE asset_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    depreciation_method VARCHAR(50) DEFAULT 'STRAIGHT_LINE' CHECK (depreciation_method IN ('STRAIGHT_LINE', 'REDUCING_BALANCE')),
    default_depreciation_rate_percent DECIMAL(5,2) DEFAULT 10.00
);

CREATE TABLE fixed_assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    category_id UUID NOT NULL REFERENCES asset_categories(id) ON DELETE RESTRICT,
    asset_tag VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    purchase_date DATE NOT NULL,
    purchase_cost DECIMAL(18,4) NOT NULL,
    capitalized_additional_cost DECIMAL(18,4) DEFAULT 0.0000,
    current_book_value DECIMAL(18,4) NOT NULL,
    residual_value DECIMAL(18,4) DEFAULT 0.0000,
    useful_life_years INT NOT NULL,
    custodian_employee_id UUID REFERENCES employee_profiles(id) ON DELETE SET NULL,
    source_document_id UUID, -- LC, invoice or expense reference
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'UNDER_REPAIR', 'DEPRECIATED', 'DISPOSED', 'LOST')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_asset_tag UNIQUE (business_id, asset_tag)
);

CREATE TABLE asset_depreciation_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    asset_id UUID NOT NULL REFERENCES fixed_assets(id) ON DELETE CASCADE,
    depreciation_period_end DATE NOT NULL,
    depreciation_amount DECIMAL(18,4) NOT NULL,
    accumulated_depreciation_after DECIMAL(18,4) NOT NULL,
    accounting_posting_id UUID REFERENCES accounting_postings(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 13: HR, LEAVES, SHIFTS, LOANS & ENTERPRISE PAYROLL
-- ==============================================================================

CREATE TABLE employee_salary_structures (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL UNIQUE REFERENCES employee_profiles(id) ON DELETE CASCADE,
    basic_salary DECIMAL(18,4) NOT NULL,
    house_allowance DECIMAL(18,4) DEFAULT 0.0000,
    medical_allowance DECIMAL(18,4) DEFAULT 0.0000,
    transport_allowance DECIMAL(18,4) DEFAULT 0.0000,
    mobile_allowance DECIMAL(18,4) DEFAULT 0.0000,
    other_allowances DECIMAL(18,4) DEFAULT 0.0000,
    provident_fund_employee DECIMAL(18,4) DEFAULT 0.0000,
    income_tax_deduction DECIMAL(18,4) DEFAULT 0.0000,
    maker_id UUID REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED')),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE CASCADE,
    work_date DATE NOT NULL,
    check_in TIMESTAMP WITH TIME ZONE,
    check_out TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'PRESENT' CHECK (status IN (
        'PRESENT', 'ABSENT', 'LATE', 'HALF_DAY', 'LEAVE', 'HOLIDAY', 'WEEKEND', 'FIELD_DUTY'
    )),
    latitude_check_in DECIMAL(10,8),
    longitude_check_in DECIMAL(11,8),
    latitude_check_out DECIMAL(10,8),
    longitude_check_out DECIMAL(11,8),
    device_uuid VARCHAR(255),
    verified_by_biometric BOOLEAN DEFAULT FALSE,
    CONSTRAINT uq_employee_attendance_date UNIQUE (employee_id, work_date)
);

CREATE TABLE leave_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL, -- 'CASUAL', 'SICK', 'ANNUAL', 'MATERNITY'
    yearly_allocation INT NOT NULL DEFAULT 14
);

CREATE TABLE leave_balances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE CASCADE,
    leave_type_id UUID NOT NULL REFERENCES leave_types(id) ON DELETE CASCADE,
    calendar_year INT NOT NULL,
    allocated_days INT NOT NULL,
    used_days INT DEFAULT 0,
    remaining_days INT NOT NULL,
    CONSTRAINT uq_employee_leave_year UNIQUE (employee_id, leave_type_id, calendar_year)
);

CREATE TABLE leave_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE CASCADE,
    leave_type_id UUID NOT NULL REFERENCES leave_types(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    reason TEXT,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    approved_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Employee Loans & Advances
CREATE TABLE employee_loans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE CASCADE,
    loan_amount DECIMAL(18,4) NOT NULL,
    monthly_recovery_amount DECIMAL(18,4) NOT NULL,
    total_months INT NOT NULL,
    remaining_balance DECIMAL(18,4) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'SUSPENDED')),
    maker_id UUID REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Commissions Engine
CREATE TABLE employee_commissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE RESTRICT,
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    commission_rule_meta JSONB, -- stores base percentage, margins criteria
    sales_amount_achieved DECIMAL(18,4) NOT NULL,
    commission_earned DECIMAL(18,4) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED_PAYABLE', 'PAID')),
    payroll_run_id UUID, -- linked during salary disbursement
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Monthly Payroll Runs
CREATE TABLE payroll_runs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    month_year VARCHAR(50) NOT NULL, -- e.g. '07-2026'
    run_date DATE NOT NULL,
    total_payout DECIMAL(18,4) NOT NULL,
    status VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'CHECKED', 'APPROVED', 'DISBURSED')),
    maker_id UUID NOT NULL REFERENCES users(id),
    approver_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payroll_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payroll_run_id UUID NOT NULL REFERENCES payroll_runs(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employee_profiles(id) ON DELETE RESTRICT,
    gross_earnings DECIMAL(18,4) NOT NULL,
    loan_deduction DECIMAL(18,4) DEFAULT 0.0000,
    advance_recovery_deduction DECIMAL(18,4) DEFAULT 0.0000,
    unpaid_leaves_deduction DECIMAL(18,4) DEFAULT 0.0000,
    other_deductions DECIMAL(18,4) DEFAULT 0.0000,
    commission_earnings DECIMAL(18,4) DEFAULT 0.0000,
    net_payable DECIMAL(18,4) NOT NULL,
    salary_slip_url TEXT
);

-- ==============================================================================
-- MODULE 14: VAT, TAX & COMPLIANCE (Bangladesh context specific NBR rules)
-- ==============================================================================

CREATE TABLE tax_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL, -- e.g. 'Standard 15% VAT Bangladesh', 'Import VAT'
    tax_type VARCHAR(100) NOT NULL, -- 'VAT', 'AIT', 'TDS', 'CUSTOMS_DUTY'
    rate_percent DECIMAL(5,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE transaction_tax_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE RESTRICT,
    tax_profile_id UUID NOT NULL REFERENCES tax_profiles(id) ON DELETE RESTRICT,
    source_document_id UUID NOT NULL, -- sales invoice, purchase bill, customs clearance
    source_document_type VARCHAR(100) NOT NULL,
    taxable_base_amount_bdt DECIMAL(18,4) NOT NULL,
    tax_amount_bdt DECIMAL(18,4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE compliance_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    document_title VARCHAR(255) NOT NULL, -- 'Trade License', 'BIN Certificate', 'TIN Certificate', 'IRC', 'ERC'
    document_number VARCHAR(100) NOT NULL,
    issue_date DATE,
    expiry_date DATE NOT NULL,
    scanned_file_url TEXT,
    alert_days_before INT DEFAULT 30,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 15: OFFICE TASKS, NOTIFICATIONS & INTERNAL COLLABORATION
-- ==============================================================================

CREATE TABLE office_tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) DEFAULT 'Other' CHECK (category IN (
        'Customer Follow-up', 'Sales Follow-up', 'Collection Follow-up', 'Bank Submission', 'Cheque Deposit',
        'LC Deposit', 'EMI Follow-up', 'VAT/Tax', 'License Renewal', 'Meeting', 'Other'
    )),
    priority VARCHAR(50) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status VARCHAR(50) DEFAULT 'NEW' CHECK (status IN ('NEW', 'IN_PROGRESS', 'WAITING', 'COMPLETED', 'OVERDUE')),
    due_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE task_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL REFERENCES office_tasks(id) ON DELETE CASCADE,
    assigned_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    notification_type VARCHAR(100) DEFAULT 'SYSTEM', -- 'APPROVAL_REQUEST', 'SLA_BREACH', 'COMPLIANCE'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- MODULE 16: OFFLINE SYNC, BACKUPS & INTEGRATIONS
-- ==============================================================================

CREATE TABLE sync_queue (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_fingerprint VARCHAR(255) NOT NULL,
    operation_type VARCHAR(50) NOT NULL, -- 'INSERT', 'UPDATE'
    table_name VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    queued_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED', 'CONFLICT'))
);

CREATE TABLE backup_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    backup_file_name VARCHAR(255) NOT NULL,
    backup_size_bytes BIGINT NOT NULL,
    storage_provider VARCHAR(100) DEFAULT 'GOOGLE_CLOUD_STORAGE',
    is_verified BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'SUCCESS' CHECK (status IN ('SUCCESS', 'FAILED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ODOO INTERACTIVE READ-ONLY CACHE REPLICATOR
CREATE TABLE odoo_readonly_cache (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    odoo_model VARCHAR(100) NOT NULL, -- e.g. 'res.partner', 'product.template'
    odoo_record_id INT NOT NULL,
    cached_payload JSONB NOT NULL,
    cached_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_odoo_record UNIQUE (odoo_model, odoo_record_id)
);

-- Document sequence control to prevent gaps in auto numbers
CREATE TABLE document_number_sequences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    prefix VARCHAR(50) NOT NULL, -- 'INV', 'PO', 'GRN', 'TKT'
    current_value INT NOT NULL DEFAULT 1,
    padding INT DEFAULT 6,
    year_prefix VARCHAR(4) NOT NULL, -- '2026'
    CONSTRAINT uq_sequence_pattern UNIQUE (business_id, prefix, year_prefix)
);

-- ==============================================================================
-- MODULE 17: ENTERPRISE PERFORMANCE MONITORING, KPI LAYERS & SNAPSHOTS
-- ==============================================================================

CREATE TABLE kpi_definitions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    kpi_code VARCHAR(100) UNIQUE NOT NULL, -- e.g., 'KPI_TODAY_SALES', 'KPI_LOW_STOCK'
    kpi_name VARCHAR(255) NOT NULL,
    module VARCHAR(100) NOT NULL, -- 'SALES', 'SERVICE', 'ACCOUNTS', 'IMPORT'
    description TEXT,
    refresh_frequency_seconds INT DEFAULT 3600,
    card_type VARCHAR(50) DEFAULT 'number', -- 'number', 'currency', 'percentage', 'trend'
    chart_type VARCHAR(50), -- 'line', 'bar', 'pie', 'gauge'
    display_color VARCHAR(10) DEFAULT '#0D47A1',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE kpi_snapshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    branch_id UUID REFERENCES branches(id) ON DELETE CASCADE,
    kpi_definition_id UUID NOT NULL REFERENCES kpi_definitions(id) ON DELETE CASCADE,
    cached_value_numeric DECIMAL(18,4),
    cached_value_text TEXT,
    trend_direction INT DEFAULT 0, -- -1 = down, 0 = stable, 1 = up
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE kpi_threshold_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    kpi_definition_id UUID NOT NULL REFERENCES kpi_definitions(id) ON DELETE CASCADE,
    warning_threshold DECIMAL(18,4),
    critical_threshold DECIMAL(18,4),
    alert_recipient_role_id UUID REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE dashboard_attention_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON ZONE_ID_PLACEHOLDER CASCADE,
    attention_type VARCHAR(100) NOT NULL, -- 'COMPLIANCE_EXPIRING', 'BACKUP_FAILED', 'LOW_STOCK'
    message TEXT NOT NULL,
    action_route VARCHAR(255), -- Deep-link path to take action
    is_resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- IMPORTANT CORE INDEXES FOR HIGH-VELOCITY QUERY RETRIEVAL
-- ==============================================================================

-- Security & Audit
CREATE INDEX idx_audit_logs_record ON audit_logs(table_name, record_id);
CREATE INDEX idx_activity_logs_user ON activity_logs(user_id, created_at);

-- Accounting Postings & Ledger Indexes
CREATE INDEX idx_accounting_posting_lines_gl ON accounting_posting_lines(gl_account_id);
CREATE INDEX idx_accounting_postings_doc ON accounting_postings(source_document_id, source_document_type);
CREATE INDEX idx_customer_ledgers_date ON customer_ledgers(customer_id, transaction_date);
CREATE INDEX idx_supplier_ledgers_date ON supplier_ledgers(supplier_id, transaction_date);

-- Stock Movements & Serial tracking
CREATE INDEX idx_stock_movements_prod_wh ON stock_movements(product_id, warehouse_id);
CREATE INDEX idx_serial_numbers_wh ON serial_numbers(product_id, warehouse_id, status);

-- Service Operations
CREATE INDEX idx_service_tickets_eng ON service_tickets(assigned_engineer_id, status);
CREATE INDEX idx_engineer_checkins_tkt ON engineer_checkins(service_ticket_id, checkin_time);

-- Sales & Import Pipeline
CREATE INDEX idx_invoices_customer ON invoices(customer_id, due_date, status);
CREATE INDEX idx_sales_orders_customer ON sales_orders(customer_id, status);
CREATE INDEX idx_shipments_lc ON shipments(lc_record_id, status);
CREATE INDEX idx_landed_cost_summary ON landed_cost_summaries(shipment_id, status);

-- Agreements & EMIs
CREATE INDEX idx_emi_schedules_due ON emi_schedules(agreement_id, due_date, payment_status);

-- HR, Attendance & Payroll
CREATE INDEX idx_attendance_records_emp ON attendance_records(employee_id, work_date);
CREATE INDEX idx_payroll_items_run ON payroll_items(payroll_run_id, employee_id);

-- KPI Snapshots Index
CREATE INDEX idx_kpi_snapshots_lookup ON kpi_snapshots(business_id, kpi_definition_id, calculated_at DESC);
