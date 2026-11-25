-- ============================================================================
-- INTERNET BILLING MANAGER 2 (IBM 2) - POSTGRESQL PRODUCTION DATABASE SCHEMA
-- ============================================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. USERS & OPERATORS (Authentication)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'admin', -- 'admin', 'operator', 'collector'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. ISP SETTINGS (Single record ID = 1)
CREATE TABLE IF NOT EXISTS isp_settings (
    id INT PRIMARY KEY DEFAULT 1,
    company_name VARCHAR(255) NOT NULL DEFAULT 'Internet Service Provider',
    tagline VARCHAR(255) NOT NULL DEFAULT 'Fast • Stable • Connected',
    phone VARCHAR(50) NOT NULL DEFAULT '',
    email VARCHAR(100) NOT NULL DEFAULT '',
    address TEXT NOT NULL DEFAULT '',
    currency_symbol VARCHAR(10) NOT NULL DEFAULT '৳',
    network_status VARCHAR(50) NOT NULL DEFAULT 'Operational',
    logo_uri TEXT NOT NULL DEFAULT '',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Initialize default ISP settings row
INSERT INTO isp_settings (id, company_name, tagline, currency_symbol)
VALUES (1, 'Internet Service Provider', 'Fast • Stable • Connected', '৳')
ON CONFLICT (id) DO NOTHING;

-- 3. INTERNET PACKAGES
CREATE TABLE IF NOT EXISTS packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    speed_mbps INT NOT NULL DEFAULT 10,
    monthly_price NUMERIC(12, 2) NOT NULL DEFAULT 500.00,
    description TEXT NOT NULL DEFAULT 'High-speed optical fiber connection',
    color_hex VARCHAR(20) NOT NULL DEFAULT '#00E5FF',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. CUSTOMERS
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    pppoe_username VARCHAR(100) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    package_id BIGINT REFERENCES packages(id) ON DELETE SET NULL,
    package_name VARCHAR(100) NOT NULL DEFAULT '',
    speed_mbps INT NOT NULL DEFAULT 0,
    monthly_bill NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    due_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    bill_due_day INT NOT NULL DEFAULT 10,
    status VARCHAR(50) NOT NULL DEFAULT 'Active', -- 'Active', 'Suspended', 'Inactive'
    connection_type VARCHAR(50) NOT NULL DEFAULT 'Fiber',
    address TEXT NOT NULL DEFAULT '',
    static_ip VARCHAR(50) NOT NULL DEFAULT '',
    notes TEXT NOT NULL DEFAULT '',
    created_date_millis BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Customers
CREATE INDEX IF NOT EXISTS idx_customers_status ON customers(status);
CREATE INDEX IF NOT EXISTS idx_customers_code ON customers(customer_code);
CREATE INDEX IF NOT EXISTS idx_customers_pppoe ON customers(pppoe_username);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);

-- 5. BILL ENTRIES
CREATE TABLE IF NOT EXISTS bill_entries (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    customer_name VARCHAR(255) NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    month_year VARCHAR(20) NOT NULL, -- e.g. '2026-11'
    bill_amount NUMERIC(12, 2) NOT NULL,
    paid_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    due_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    bill_date_millis BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    notes TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Bills
CREATE INDEX IF NOT EXISTS idx_bills_customer_id ON bill_entries(customer_id);
CREATE INDEX IF NOT EXISTS idx_bills_month_year ON bill_entries(month_year);
CREATE INDEX IF NOT EXISTS idx_bills_is_paid ON bill_entries(is_paid);
CREATE INDEX IF NOT EXISTS idx_bills_date_millis ON bill_entries(bill_date_millis);

-- 6. PAYMENT TRANSACTIONS
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    customer_name VARCHAR(255) NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    discount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'Cash', -- 'Cash', 'bKash', 'Nagad', 'Bank'
    date_millis BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    month_year VARCHAR(20) NOT NULL,
    note TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Payments
CREATE INDEX IF NOT EXISTS idx_payments_customer_id ON payments(customer_id);
CREATE INDEX IF NOT EXISTS idx_payments_month_year ON payments(month_year);
CREATE INDEX IF NOT EXISTS idx_payments_date_millis ON payments(date_millis);

-- 7. EXPENSES
CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'Other',
    amount NUMERIC(12, 2) NOT NULL,
    date_millis BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    month_year VARCHAR(20) NOT NULL,
    note TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_expenses_month_year ON expenses(month_year);
CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category);

-- 8. MIKROTIK ROUTEROS CONFIGURATION
CREATE TABLE IF NOT EXISTS mikrotik_configs (
    id INT PRIMARY KEY DEFAULT 1,
    host VARCHAR(255) NOT NULL DEFAULT '192.168.88.1',
    port INT NOT NULL DEFAULT 8728,
    username VARCHAR(100) NOT NULL DEFAULT 'admin',
    password VARCHAR(255) NOT NULL DEFAULT '',
    use_ssl BOOLEAN NOT NULL DEFAULT FALSE,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_sync_time BIGINT DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO mikrotik_configs (id, host, port, username)
VALUES (1, '192.168.88.1', 8728, 'admin')
ON CONFLICT (id) DO NOTHING;

-- 9. SYNC EVENTS (For Android Mobile App Two-Way Sync)
CREATE TABLE IF NOT EXISTS sync_events (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL, -- 'customer', 'bill', 'payment', 'expense', 'package'
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL, -- 'INSERT', 'UPDATE', 'DELETE'
    client_id VARCHAR(100) NOT NULL DEFAULT 'server',
    payload JSONB NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_sync_events_ts ON sync_events(timestamp);
