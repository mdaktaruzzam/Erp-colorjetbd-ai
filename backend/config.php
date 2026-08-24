<?php
/**
 * ColorJet ERP Bangladesh - Phase 1 Migration Config
 * Secure Configuration for Database Migrations
 */

// Define application root
define('MIGRATION_ROOT', dirname(__FILE__));

// Security Configuration
define('MIGRATION_SECURE_TOKEN', 'CJ_ERP_BD_MIGRATE_2026_SECURE_KEY'); // Secure token for browser-based execution
define('ALLOW_CLI_EXECUTION', true); // Allow terminal execution without token
define('RESTRICT_TO_LOCALHOST', false); // Restrict to localhost requests for enhanced production security

// Database Provider Configuration
// Options: 'sqlite' or 'mysql'
define('DB_PROVIDER', 'sqlite'); 

// SQLite Specific Configuration
define('SQLITE_DB_PATH', MIGRATION_ROOT . '/database.sqlite');

// MySQL Specific Configuration
define('DB_HOST', '127.0.0.1');
define('DB_PORT', '3306');
define('DB_NAME', 'colorjet_erp_db');
define('DB_USER', 'colorjet_admin');
define('DB_PASS', 'secure_erp_password_2026');
define('DB_CHARSET', 'utf8mb4');

// Centralized Auditing System
require_once MIGRATION_ROOT . '/AuditLogger.php';

/**
 * Establish a PDO database connection based on configured provider
 */
function getMigrationDbConnection() {
    try {
        if (DB_PROVIDER === 'sqlite') {
            $dsn = "sqlite:" . SQLITE_DB_PATH;
            $pdo = new AuditablePDO($dsn);
            $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
            return $pdo;
        } else {
            $dsn = "mysql:host=" . DB_HOST . ";port=" . DB_PORT . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
            $options = [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ];
            return new AuditablePDO($dsn, DB_USER, DB_PASS, $options);
        }
    } catch (PDOException $e) {
        throw new Exception("Database Connection Failed: " . $e->getMessage());
    }
}
