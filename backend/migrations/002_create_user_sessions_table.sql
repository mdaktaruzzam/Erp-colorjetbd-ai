-- Phase 1: Establish Session-based Authentication Tables
-- ColorJet ERP Bangladesh

CREATE TABLE IF NOT EXISTS user_sessions (
    sessionToken VARCHAR(100) PRIMARY KEY,
    userId INTEGER NOT NULL,
    username VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    fullName VARCHAR(100) NOT NULL,
    loginTime BIGINT NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
);
