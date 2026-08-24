-- Phase 1: Establish Users and Roles Database Schema
-- ColorJet ERP Bangladesh

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    fullName VARCHAR(100) NOT NULL,
    pinCode VARCHAR(10) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'STAFF')),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed initial corporate users for testing roles and secure permissions
INSERT OR IGNORE INTO users (id, username, fullName, pinCode, role) VALUES 
(1, 'owner', 'Sohail Rahman', 'admin123', 'OWNER'),
(2, 'admin', 'Nabila Islam', '2222', 'ADMIN'),
(3, 'staff', 'Tanvir Hasan', '3333', 'STAFF'),
(4, 'guest', 'Guest Observer', '4444', 'STAFF');
