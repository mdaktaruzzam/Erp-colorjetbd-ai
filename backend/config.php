<?php
/**
 * ColorJet ERP Bangladesh - Phase 1 Migration Config
 * Secure Configuration for Database Migrations
 */

// Define application root
define('MIGRATION_ROOT', dirname(__FILE__));

// Security Configuration
$defaultToken = 'REPLACE_WITH_SECURE_TOKEN';
$envToken = getenv('COLORJET_API_TOKEN');
define('MIGRATION_SECURE_TOKEN', $envToken !== false && $envToken !== '' ? $envToken : $defaultToken);
define('ALLOW_CLI_EXECUTION', filter_var(getenv('ALLOW_CLI_EXECUTION') ?: 'true', FILTER_VALIDATE_BOOLEAN));
define('RESTRICT_TO_LOCALHOST', filter_var(getenv('RESTRICT_TO_LOCALHOST') ?: 'false', FILTER_VALIDATE_BOOLEAN));

// Database Provider Configuration
// Options: 'sqlite' or 'mysql'
define('DB_PROVIDER', getenv('DB_PROVIDER') ?: 'sqlite');

// SQLite Specific Configuration
define('SQLITE_DB_PATH', MIGRATION_ROOT . '/database.sqlite');

// MySQL Specific Configuration
define('DB_HOST', getenv('DB_HOST') ?: '127.0.0.1');
define('DB_PORT', getenv('DB_PORT') ?: '3306');
define('DB_NAME', getenv('DB_NAME') ?: 'colorjet_erp_db');
define('DB_USER', getenv('DB_USER') ?: 'colorjet_admin');
define('DB_PASS', getenv('DB_PASS') ?: 'change_this_secure_password');
define('DB_CHARSET', getenv('DB_CHARSET') ?: 'utf8mb4');

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
