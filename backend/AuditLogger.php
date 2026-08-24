<?php
/**
 * ColorJet ERP Bangladesh - Centralized Audit Logging System
 * Intercepts, inspects, and logs database write operations on critical financial and stock tables.
 */

class AuditLogger {
    private static $targetTables = [
        'financial_transactions',
        'supply_items',
        'vouchers',
        'voucher_lines',
        'accounting_postings',
        'accounting_posting_lines',
        'customer_ledgers',
        'supplier_ledgers',
        'product_stock',
        'stock_movements',
        'products',
        'users',
        'user_sessions'
    ];

    private static $userIdOverride = null;
    private static $isLogging = false;

    /**
     * Override or manually set the user ID for auditing purposes (e.g. in CLI or API requests)
     */
    public static function setUserId($userId) {
        self::$userIdOverride = $userId;
    }

    /**
     * Resolve the current user ID from session or system context
     */
    public static function getCurrentUserId() {
        if (self::$userIdOverride !== null) {
            return self::$userIdOverride;
        }

        // Try to fetch from active session
        if (session_status() === PHP_SESSION_ACTIVE || session_status() === PHP_SESSION_NONE) {
            // Safe check if we can access session
            if (isset($_SESSION) && is_array($_SESSION)) {
                if (isset($_SESSION['userId'])) {
                    return $_SESSION['userId'];
                }
                if (isset($_SESSION['username'])) {
                    return $_SESSION['username'];
                }
                if (isset($_SESSION['migration_authorized'])) {
                    return 'owner_token';
                }
            }
        }

        // Fallback to CLI or system
        if (php_sapi_name() === 'cli') {
            return 'cli_system';
        }

        return 'system';
    }

    /**
     * Parse query to extract the action and table name if it's a write operation
     */
    public static function parseQuery($sql) {
        $sql = trim($sql);
        
        $action = null;
        $tableName = null;
        
        // Match INSERT / REPLACE
        if (preg_match('/^\s*(INSERT\s+INTO|INSERT\s+OR\s+IGNORE\s+INTO|REPLACE\s+INTO)\s+[`"\'\[]?([a-zA-Z0-9_]+)[`"\'\]]?/i', $sql, $matches)) {
            $action = 'INSERT';
            $tableName = $matches[2];
        } 
        // Match UPDATE
        elseif (preg_match('/^\s*UPDATE\s+[`"\'\[]?([a-zA-Z0-9_]+)[`"\'\]]?/i', $sql, $matches)) {
            $action = 'UPDATE';
            $tableName = $matches[1];
        } 
        // Match DELETE
        elseif (preg_match('/^\s*DELETE\s+FROM\s+[`"\'\[]?([a-zA-Z0-9_]+)[`"\'\]]?/i', $sql, $matches)) {
            $action = 'DELETE';
            $tableName = $matches[1];
        }
        
        return [
            'action' => $action,
            'table' => $tableName
        ];
    }

    /**
     * Check if table is a critical financial or stock table
     */
    public static function isTargetTable($tableName) {
        if (!$tableName) {
            return false;
        }
        return in_array(strtolower($tableName), array_map('strtolower', self::$targetTables));
    }

    /**
     * Log a write operation to the audit_logs table securely
     */
    public static function logOperation($pdo, $sql, $params = null) {
        // Prevent recursive logging loops or double-entries
        if (self::$isLogging) {
            return;
        }

        $parsed = self::parseQuery($sql);
        if (!$parsed['action'] || !self::isTargetTable($parsed['table'])) {
            return; // Not a targeted write operation, ignore
        }

        self::$isLogging = true;

        try {
            $userId = self::getCurrentUserId();
            $action = $parsed['action'];
            $tableName = strtolower($parsed['table']);
            
            // Serialize params as payload if available
            $payload = null;
            if (!empty($params)) {
                $payload = json_encode($params, JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
            }

            // Write audit log using standard INSERT query
            $auditSql = "INSERT INTO audit_logs (user_id, action, table_name, query_sql, payload) 
                         VALUES (:user_id, :action, :table_name, :query_sql, :payload)";
            
            $stmt = $pdo->prepare($auditSql);
            $stmt->execute([
                ':user_id' => $userId,
                ':action' => $action,
                ':table_name' => $tableName,
                ':query_sql' => $sql,
                ':payload' => $payload
            ]);
        } catch (Exception $e) {
            // Silently log errors to error_log to ensure auditing never breaks production operations
            error_log("Audit Logger Failure: " . $e->getMessage());
        } finally {
            self::$isLogging = false;
        }
    }
}

/**
 * Custom Auditable PDO wrapper that automatically intercepts database write operations.
 */
class AuditablePDO extends PDO {
    public function __construct($dsn, $username = null, $password = null, $options = []) {
        parent::__construct($dsn, $username, $password, $options);
        $this->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        
        // Register our custom statement class for prepared statement interception
        $this->setAttribute(PDO::ATTR_STATEMENT_CLASS, ['AuditablePDOStatement', [$this]]);
    }

    public function exec($statement): int|false {
        $result = parent::exec($statement);
        if ($result !== false) {
            AuditLogger::logOperation($this, $statement);
        }
        return $result;
    }

    public function query($statement, $mode = PDO::ATTR_DEFAULT_FETCH_MODE, ...$extra_params): PDOStatement|false {
        $result = parent::query($statement, $mode, ...$extra_params);
        if ($result !== false) {
            AuditLogger::logOperation($this, $statement);
        }
        return $result;
    }
}

/**
 * Custom PDOStatement class that intercepts executed prepared statements.
 */
class AuditablePDOStatement extends PDOStatement {
    private $pdo;
    private $boundParams = [];

    protected function __construct($pdo) {
        $this->pdo = $pdo;
    }

    /**
     * Intercept bound values to capture transaction payloads
     */
    public function bindValue($param, $value, $type = PDO::PARAM_STR): bool {
        $this->boundParams[$param] = $value;
        return parent::bindValue($param, $value, $type);
    }

    public function bindParam($param, &$var, $type = PDO::PARAM_STR, $maxLength = 0, $driverOptions = null): bool {
        $this->boundParams[$param] = $var;
        return parent::bindParam($param, $var, $type, $maxLength, $driverOptions);
    }

    /**
     * Intercept statement execution to automatically perform auditable logging
     */
    public function execute($params = null): bool {
        $result = parent::execute($params);
        
        if ($result) {
            $allParams = $this->boundParams;
            if (is_array($params)) {
                foreach ($params as $key => $val) {
                    $allParams[$key] = $val;
                }
            }
            AuditLogger::logOperation($this->pdo, $this->queryString, $allParams);
        }
        
        return $result;
    }
}
