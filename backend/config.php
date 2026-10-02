<?php
/**
 * ColorJet ERP Bangladesh - Production-safe migration configuration
 * Reads environment variables instead of hardcoded secrets.
 */

define('MIGRATION_ROOT', dirname(__FILE__));

function colorjet_env($key, $fallback = '') {
    $value = getenv($key);
    if ($value !== false && $value !== null && trim((string) $value) !== '') {
        return $value;
    }

    if (isset($_ENV[$key]) && trim((string) $_ENV[$key]) !== '') {
        return $_ENV[$key];
    }

    if (isset($_SERVER[$key]) && trim((string) $_SERVER[$key]) !== '') {
        return $_SERVER[$key];
    }

    return $fallback;
}

// Security configuration
define('MIGRATION_SECURE_TOKEN', colorjet_env('COLORJET_API_TOKEN', 'CHANGE_ME_IN_PRODUCTION'));
define('ALLOW_CLI_EXECUTION', filter_var(colorjet_env('ALLOW_CLI_EXECUTION', 'false'), FILTER_VALIDATE_BOOLEAN));
define('RESTRICT_TO_LOCALHOST', filter_var(colorjet_env('RESTRICT_TO_LOCALHOST', 'true'), FILTER_VALIDATE_BOOLEAN));

// Database provider configuration
// Options: 'sqlite' or 'mysql'
define('DB_PROVIDER', strtolower(colorjet_env('DB_PROVIDER', 'sqlite')));

// SQLite specific config
define('SQLITE_DB_PATH', MIGRATION_ROOT . '/database.sqlite');

// MySQL specific config
define('DB_HOST', colorjet_env('DB_HOST', '127.0.0.1'));
define('DB_PORT', colorjet_env('DB_PORT', '3306'));
define('DB_NAME', colorjet_env('DB_NAME', 'colorjet_erp_db'));
define('DB_USER', colorjet_env('DB_USER', 'colorjet_admin'));
define('DB_PASS', colorjet_env('DB_PASS', 'change_this_secure_password'));
define('DB_CHARSET', colorjet_env('DB_CHARSET', 'utf8mb4'));

// Centralized auditing system
require_once MIGRATION_ROOT . '/AuditLogger.php';

/**
 * Establish a PDO database connection based on configured provider
 */
function getMigrationDbConnection() {
    try {
        if (DB_PROVIDER === 'sqlite') {
            $dsn = 'sqlite:' . SQLITE_DB_PATH;
            $pdo = new AuditablePDO($dsn);
            $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
            return $pdo;
        }

        $dsn = 'mysql:host=' . DB_HOST . ';port=' . DB_PORT . ';dbname=' . DB_NAME . ';charset=' . DB_CHARSET;
        $options = [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ];
        return new AuditablePDO($dsn, DB_USER, DB_PASS, $options);
    } catch (PDOException $e) {
        throw new Exception('Database Connection Failed: ' . $e->getMessage());
    }
}
