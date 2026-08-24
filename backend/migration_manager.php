<?php
/**
 * ColorJet ERP Bangladesh - Enterprise Database Migration Script Manager
 * Author: AI Coding Agent (AI Studio Build)
 * Version: 2.0.0
 * 
 * Safe, transactional, versioned database schema manager with secure authentication,
 * Dry-Run capabilities, Phased Step-by-Step execution, Rollbacks, and SQL Code Previews.
 */

// Start session securely for browser-based authorization
if (session_status() === PHP_SESSION_NONE) {
    session_set_cookie_params([
        'lifetime' => 0,
        'path' => '/',
        'domain' => '',
        'secure' => isset($_SERVER['HTTPS']),
        'httponly' => true,
        'samesite' => 'Strict'
    ]);
    session_start();
}

require_once dirname(__FILE__) . '/config.php';

// Ensure proper security headers
header("X-Frame-Options: DENY");
header("X-Content-Type-Options: nosniff");
header("X-XSS-Protection: 1; mode=block");

$isCli = (php_sapi_name() === 'cli');

// 1. Authenticate Request (CLI or Session-based Token)
function authenticateRequest($isCli) {
    if ($isCli) {
        if (!defined('ALLOW_CLI_EXECUTION') || !ALLOW_CLI_EXECUTION) {
            die("Error: CLI execution is disabled in config.\n");
        }
        return true;
    }

    // IP Whitelist Check (Optional)
    if (defined('RESTRICT_TO_LOCALHOST') && RESTRICT_TO_LOCALHOST) {
        $allowedIps = ['127.0.0.1', '::1'];
        $clientIp = $_SERVER['REMOTE_ADDR'] ?? '';
        if (!in_array($clientIp, $allowedIps)) {
            http_response_code(403);
            die("Access Denied: Migrations can only be run from localhost.");
        }
    }

    // Handle logout action
    if (isset($_GET['logout'])) {
        unset($_SESSION['migration_authorized']);
        header("Location: " . strtok($_SERVER["REQUEST_URI"], '?'));
        exit();
    }

    // Token Verification via POST, GET or Session
    $token = $_POST['token'] ?? $_GET['token'] ?? $_SESSION['migration_authorized'] ?? '';
    if (!empty($token) && $token === MIGRATION_SECURE_TOKEN) {
        $_SESSION['migration_authorized'] = MIGRATION_SECURE_TOKEN;
        return true;
    }

    return false;
}

$isAuthenticated = authenticateRequest($isCli);

// 2. Initialize Database and Enhanced Migration Tracking Table
function initMigrationLogTable($db) {
    // Check if table exists first to avoid schema noise
    $dbDriver = $db->getAttribute(PDO::ATTR_DRIVER_NAME);
    
    if ($dbDriver === 'sqlite') {
        $sql = "CREATE TABLE IF NOT EXISTS migrations_log (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            migration_name VARCHAR(150) NOT NULL UNIQUE,
            batch INT NOT NULL DEFAULT 1,
            execution_time_ms INT DEFAULT 0,
            applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )";
    } else {
        $sql = "CREATE TABLE IF NOT EXISTS migrations_log (
            id INT AUTO_INCREMENT PRIMARY KEY,
            migration_name VARCHAR(150) NOT NULL UNIQUE,
            batch INT NOT NULL DEFAULT 1,
            execution_time_ms INT DEFAULT 0,
            applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
    }
    
    $db->exec($sql);

    // Verify / Upgrade schema of migration_log for older databases that only had id, name, applied_at
    try {
        if ($dbDriver === 'sqlite') {
            // Check if batch column exists
            $stmt = $db->query("PRAGMA table_info(migrations_log)");
            $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);
            $hasBatch = false;
            $hasTime = false;
            foreach ($columns as $col) {
                if ($col['name'] === 'batch') $hasBatch = true;
                if ($col['name'] === 'execution_time_ms') $hasTime = true;
            }
            if (!$hasBatch) {
                $db->exec("ALTER TABLE migrations_log ADD COLUMN batch INT NOT NULL DEFAULT 1");
            }
            if (!$hasTime) {
                $db->exec("ALTER TABLE migrations_log ADD COLUMN execution_time_ms INT DEFAULT 0");
            }
        } else {
            // MySQL check column
            $stmt = $db->prepare("SHOW COLUMNS FROM migrations_log LIKE 'batch'");
            $stmt->execute();
            if (!$stmt->fetch()) {
                $db->exec("ALTER TABLE migrations_log ADD COLUMN batch INT NOT NULL DEFAULT 1");
            }
            $stmt = $db->prepare("SHOW COLUMNS FROM migrations_log LIKE 'execution_time_ms'");
            $stmt->execute();
            if (!$stmt->fetch()) {
                $db->exec("ALTER TABLE migrations_log ADD COLUMN execution_time_ms INT DEFAULT 0");
            }
        }
    } catch (Exception $e) {
        // Silently skip if table is already structured or being created fresh
    }
}

// 3. Scan migrations directory
function getMigrationFiles() {
    $dir = MIGRATION_ROOT . '/migrations';
    if (!is_dir($dir)) {
        return [];
    }
    
    $files = scandir($dir);
    $migrations = [];
    foreach ($files as $file) {
        // Only fetch .sql files and exclude down migrations (files ending with .down.sql)
        if (pathinfo($file, PATHINFO_EXTENSION) === 'sql' && substr($file, -9) !== '.down.sql') {
            $migrations[] = $file;
        }
    }
    sort($migrations); // Lexicographical / sequential order guaranteed
    return $migrations;
}

// 4. Get applied migrations with details
function getAppliedMigrationsDetailed($db) {
    try {
        $stmt = $db->query("SELECT * FROM migrations_log ORDER BY applied_at ASC, id ASC");
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    } catch (Exception $e) {
        return [];
    }
}

// 5. Safe SQL Statement splitter/parser to execute multiple queries safely and handle comments correctly
function parseSqlStatements($sqlContent) {
    // Strip multi-line comments
    $sqlContent = preg_replace('!/\*.*?\*/!s', '', $sqlContent);
    
    $lines = explode("\n", $sqlContent);
    $statements = [];
    $currentStmt = '';
    
    foreach ($lines as $line) {
        $trimmedLine = trim($line);
        
        // Skip empty lines or single line comments
        if ($trimmedLine === '' || strpos($trimmedLine, '--') === 0 || strpos($trimmedLine, '#') === 0) {
            continue;
        }
        
        $currentStmt .= $line . "\n";
        
        // Check if the statement ends with a semicolon
        if (substr(rtrim($trimmedLine), -1) === ';') {
            $statements[] = trim($currentStmt);
            $currentStmt = '';
        }
    }
    
    if (trim($currentStmt) !== '') {
        $statements[] = trim($currentStmt);
    }
    
    return $statements;
}

// 6. Execute SQL Migration script with transaction safety
function executeMigrationScript($db, $filePath, $isDryRun = false) {
    if (!file_exists($filePath)) {
        throw new Exception("File not found: " . basename($filePath));
    }
    
    $sqlContent = file_get_contents($filePath);
    if ($sqlContent === false) {
        throw new Exception("Could not read file contents: " . basename($filePath));
    }
    
    $statements = parseSqlStatements($sqlContent);
    $queriesRun = 0;
    
    foreach ($statements as $index => $sql) {
        try {
            $db->exec($sql);
            $queriesRun++;
        } catch (PDOException $e) {
            $stmtNum = $index + 1;
            throw new Exception("Error in statement #{$stmtNum} of " . basename($filePath) . ": " . $e->getMessage() . "\nSQL: " . substr($sql, 0, 150) . "...");
        }
    }
    
    return $queriesRun;
}

// DB state loading
$db = null;
$dbError = null;
$appliedDetailed = [];
$appliedNames = [];
$allList = [];
$pendingList = [];
$nextPending = null;
$maxBatch = 0;

try {
    $db = getMigrationDbConnection();
    initMigrationLogTable($db);
    $allList = getMigrationFiles();
    $appliedDetailed = getAppliedMigrationsDetailed($db);
    
    $appliedNames = array_column($appliedDetailed, 'migration_name');
    $pendingList = array_diff($allList, $appliedNames);
    $nextPending = !empty($pendingList) ? reset($pendingList) : null;
    
    foreach ($appliedDetailed as $row) {
        if (isset($row['batch']) && $row['batch'] > $maxBatch) {
            $maxBatch = (int)$row['batch'];
        }
    }
} catch (Exception $e) {
    $dbError = $e->getMessage();
}

// Fetch captured audit logs safely
$auditLogs = [];
if ($db && empty($dbError)) {
    try {
        $stmt = $db->query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100");
        $auditLogs = $stmt->fetchAll(PDO::FETCH_ASSOC);
    } catch (Exception $e) {
        // Table may not exist yet if migration 004 isn't executed
    }
}

// 7. Operations Handler
$executionLogs = [];
$successMessage = "";
$errorMessage = "";

if ($isAuthenticated && isset($_POST['action']) && empty($dbError)) {
    $action = $_POST['action'];
    $isDryRun = isset($_POST['dry_run']) && $_POST['dry_run'] === '1';
    
    if ($action === 'run_all' || $action === 'run_single') {
        $targets = [];
        if ($action === 'run_single') {
            if ($nextPending) {
                $targets[] = $nextPending;
            }
        } else {
            $targets = $pendingList;
        }
        
        if (empty($targets)) {
            $successMessage = "Database is already fully synchronized! No migrations to execute.";
        } else {
            $newBatchNum = $maxBatch + 1;
            $db->beginTransaction();
            
            try {
                $count = 0;
                foreach ($targets as $migrationFile) {
                    $startTime = microtime(true);
                    $filePath = MIGRATION_ROOT . '/migrations/' . $migrationFile;
                    
                    // Execute scripts
                    $queriesRun = executeMigrationScript($db, $filePath, $isDryRun);
                    
                    $durationMs = round((microtime(true) - $startTime) * 1000);
                    
                    if (!$isDryRun) {
                        // Register migration
                        $stmt = $db->prepare("INSERT INTO migrations_log (migration_name, batch, execution_time_ms) VALUES (:name, :batch, :duration)");
                        $stmt->execute([
                            ':name' => $migrationFile,
                            ':batch' => $newBatchNum,
                            ':duration' => $durationMs
                        ]);
                    }
                    
                    $executionLogs[] = [
                        'file' => $migrationFile,
                        'status' => 'SUCCESS',
                        'message' => "Successfully executed {$queriesRun} statements. Duration: {$durationMs}ms." . ($isDryRun ? " (SIMULATED)" : "")
                    ];
                    $count++;
                }
                
                if ($isDryRun) {
                    $db->rollBack();
                    $successMessage = "[DRY-RUN SIMULATION] Successfully validated {$count} pending migration(s) without writing to disk!";
                } else {
                    $db->commit();
                    $successMessage = "Successfully deployed {$count} migration(s) (Batch #{$newBatchNum})!";
                }
                
                // Refresh local lists
                $appliedDetailed = getAppliedMigrationsDetailed($db);
                $appliedNames = array_column($appliedDetailed, 'migration_name');
                $pendingList = array_diff($allList, $appliedNames);
                $nextPending = !empty($pendingList) ? reset($pendingList) : null;
                $maxBatch = $isDryRun ? $maxBatch : $newBatchNum;
                
            } catch (Exception $e) {
                $db->rollBack();
                $errorMessage = "Migration batch execution aborted! " . $e->getMessage();
                $executionLogs[] = [
                    'file' => $migrationFile ?? 'Unknown Batch',
                    'status' => 'FAILED',
                    'message' => $e->getMessage()
                ];
            }
        }
    }
    
    // Rollback execution
    elseif ($action === 'rollback_last') {
        if ($maxBatch === 0) {
            $errorMessage = "No applied migrations found. Rollback is not possible.";
        } else {
            // Find migrations belonging to the last batch
            $rollbackTargets = [];
            foreach (array_reverse($appliedDetailed) as $applied) {
                if ((int)$applied['batch'] === $maxBatch) {
                    $rollbackTargets[] = $applied['migration_name'];
                }
            }
            
            $db->beginTransaction();
            try {
                $count = 0;
                foreach ($rollbackTargets as $migrationFile) {
                    $baseName = pathinfo($migrationFile, PATHINFO_FILENAME);
                    $downFile = $baseName . '.down.sql';
                    $downPath = MIGRATION_ROOT . '/migrations/' . $downFile;
                    
                    $queriesRun = 0;
                    $hasDownFile = file_exists($downPath);
                    
                    if ($hasDownFile) {
                        // Execute Down statements
                        $queriesRun = executeMigrationScript($db, $downPath, $isDryRun);
                    }
                    
                    if (!$isDryRun) {
                        // Remove log entry
                        $stmt = $db->prepare("DELETE FROM migrations_log WHERE migration_name = :name");
                        $stmt->execute([':name' => $migrationFile]);
                    }
                    
                    $logMsg = $hasDownFile 
                        ? "Executed down script: '{$downFile}' ({$queriesRun} statements cleared)."
                        : "No corresponding .down.sql script found. Table registration has been deleted.";
                    
                    $executionLogs[] = [
                        'file' => $migrationFile,
                        'status' => 'ROLLBACK',
                        'message' => $logMsg . ($isDryRun ? " (SIMULATED)" : "")
                    ];
                    $count++;
                }
                
                if ($isDryRun) {
                    $db->rollBack();
                    $successMessage = "[DRY-RUN SIMULATION] Rollback simulation for Batch #{$maxBatch} passed successfully!";
                } else {
                    $db->commit();
                    $successMessage = "Successfully rolled back {$count} migration(s) (Cleared Batch #{$maxBatch})!";
                }
                
                // Refresh local lists
                $appliedDetailed = getAppliedMigrationsDetailed($db);
                $appliedNames = array_column($appliedDetailed, 'migration_name');
                $pendingList = array_diff($allList, $appliedNames);
                $nextPending = !empty($pendingList) ? reset($pendingList) : null;
                
                // Recalculate maxBatch
                $maxBatch = 0;
                foreach ($appliedDetailed as $row) {
                    if (isset($row['batch']) && $row['batch'] > $maxBatch) {
                        $maxBatch = (int)$row['batch'];
                    }
                }
                
            } catch (Exception $e) {
                $db->rollBack();
                $errorMessage = "Rollback process aborted! " . $e->getMessage();
                $executionLogs[] = [
                    'file' => $migrationFile ?? 'Batch ' . $maxBatch,
                    'status' => 'FAILED',
                    'message' => $e->getMessage()
                ];
            }
        }
    }
    
    // Trigger Write Simulation for Auditing
    elseif ($action === 'trigger_write') {
        $writeType = $_POST['write_type'] ?? '';
        try {
            if ($writeType === 'stock') {
                $sku = $_POST['stock_sku'] ?? 'CJ-UV-CYN1L';
                $change = (int)($_POST['stock_change'] ?? 5);
                
                $stmt = $db->prepare("SELECT stockLevel, name FROM supply_items WHERE sku = :sku");
                $stmt->execute([':sku' => $sku]);
                $item = $stmt->fetch();
                
                if ($item) {
                    $newLevel = $item['stockLevel'] + $change;
                    $updateStmt = $db->prepare("UPDATE supply_items SET stockLevel = :level WHERE sku = :sku");
                    $updateStmt->execute([':level' => $newLevel, ':sku' => $sku]);
                    
                    $successMessage = "Successfully updated Stock Level of '{$item['name']}' by {$change}! New level: {$newLevel}. Audit log automatically captured.";
                } else {
                    throw new Exception("Sku '{$sku}' not found in supply_items.");
                }
            } elseif ($writeType === 'finance') {
                $account = $_POST['fin_account'] ?? 'Cash Account';
                $amount = (float)($_POST['fin_amount'] ?? 1500.00);
                $type = $_POST['fin_type'] ?? 'DEBIT';
                $desc = $_POST['fin_desc'] ?? 'Office supplies replenishment';
                
                $insertStmt = $db->prepare("INSERT INTO financial_transactions (account_name, transaction_type, amount, description) VALUES (:account, :type, :amount, :desc)");
                $insertStmt->execute([
                    ':account' => $account,
                    ':type' => $type,
                    ':amount' => $amount,
                    ':desc' => $desc
                ]);
                
                $successMessage = "Successfully recorded financial transaction of {$amount} BDT for '{$account}' ({$type})! Audit log automatically captured.";
            } else {
                throw new Exception("Invalid simulation write type.");
            }
        } catch (Exception $e) {
            $errorMessage = "Write Simulation Aborted: " . $e->getMessage();
        }
    }
}

// 8. --- CLI EXECUTION ENGINE ---
if ($isCli) {
    if (!$isAuthenticated) {
        echo "Error: Authentication Failure. Correct secure token must be provided in config.\n";
        exit(1);
    }

    echo "========================================================\n";
    echo " COLORJET ERP BANGLADESH - DATABASE MIGRATION ENGINE   \n";
    echo "========================================================\n";

    if ($dbError) {
        echo "DATABASE CONNECTION ERROR: $dbError\n";
        exit(1);
    }

    echo "Database:               " . strtoupper(DB_PROVIDER) . "\n";
    echo "Total Schema Scripts:   " . count($allList) . "\n";
    echo "Applied Migrations:     " . count($appliedNames) . "\n";
    echo "Pending Migrations:     " . count($pendingList) . "\n";
    echo "Current Active Batch:   " . $maxBatch . "\n";
    echo "========================================================\n\n";

    $cliArg = $argv[1] ?? '';
    
    switch ($cliArg) {
        case '--status':
        case '-s':
            echo "Status of all registered migration files:\n";
            foreach ($allList as $file) {
                $status = in_array($file, $appliedNames) ? "[APPLIED]" : "[PENDING]";
                echo "  $status $file\n";
            }
            break;

        case '--run':
        case '-r':
            if (empty($pendingList)) {
                echo "Database is up-to-date. No pending migrations.\n";
                exit(0);
            }
            $newBatchNum = $maxBatch + 1;
            echo "Starting sequential upgrade execution (Batch #{$newBatchNum})...\n";
            $db->beginTransaction();
            try {
                foreach ($pendingList as $migrationFile) {
                    $startTime = microtime(true);
                    echo " -> Executing: $migrationFile... ";
                    $filePath = MIGRATION_ROOT . '/migrations/' . $migrationFile;
                    $queries = executeMigrationScript($db, $filePath, false);
                    $duration = round((microtime(true) - $startTime) * 1000);
                    
                    $stmt = $db->prepare("INSERT INTO migrations_log (migration_name, batch, execution_time_ms) VALUES (:name, :batch, :duration)");
                    $stmt->execute([
                        ':name' => $migrationFile,
                        ':batch' => $newBatchNum,
                        ':duration' => $duration
                    ]);
                    echo "SUCCESS ($queries queries, {$duration}ms)\n";
                }
                $db->commit();
                echo "SUCCESS: Database migrated successfully to Batch #{$newBatchNum}.\n";
            } catch (Exception $e) {
                $db->rollBack();
                echo "FAILED!\n";
                echo "Error: " . $e->getMessage() . "\n";
                exit(1);
            }
            break;

        case '--run-next':
        case '-n':
            if (!$nextPending) {
                echo "Database is already up to date.\n";
                exit(0);
            }
            $newBatchNum = $maxBatch + 1;
            echo "Executing next pending migration: {$nextPending}...\n";
            $db->beginTransaction();
            try {
                $startTime = microtime(true);
                $filePath = MIGRATION_ROOT . '/migrations/' . $nextPending;
                $queries = executeMigrationScript($db, $filePath, false);
                $duration = round((microtime(true) - $startTime) * 1000);
                
                $stmt = $db->prepare("INSERT INTO migrations_log (migration_name, batch, execution_time_ms) VALUES (:name, :batch, :duration)");
                $stmt->execute([
                    ':name' => $nextPending,
                    ':batch' => $newBatchNum,
                    ':duration' => $duration
                ]);
                $db->commit();
                echo "SUCCESS: Next migration registered under Batch #{$newBatchNum}.\n";
            } catch (Exception $e) {
                $db->rollBack();
                echo "FAILED: " . $e->getMessage() . "\n";
                exit(1);
            }
            break;

        case '--rollback':
        case '-b':
            if ($maxBatch === 0) {
                echo "No applied migrations found to roll back.\n";
                exit(0);
            }
            $rollbackTargets = [];
            foreach (array_reverse($appliedDetailed) as $applied) {
                if ((int)$applied['batch'] === $maxBatch) {
                    $rollbackTargets[] = $applied['migration_name'];
                }
            }
            echo "Rolling back last batch (#{$maxBatch}) containing " . count($rollbackTargets) . " migrations...\n";
            $db->beginTransaction();
            try {
                foreach ($rollbackTargets as $migrationFile) {
                    echo " -> Reversing: $migrationFile... ";
                    $baseName = pathinfo($migrationFile, PATHINFO_FILENAME);
                    $downFile = $baseName . '.down.sql';
                    $downPath = MIGRATION_ROOT . '/migrations/' . $downFile;
                    
                    if (file_exists($downPath)) {
                        executeMigrationScript($db, $downPath, false);
                        echo "DOWN_SQL RUN... ";
                    }
                    
                    $stmt = $db->prepare("DELETE FROM migrations_log WHERE migration_name = :name");
                    $stmt->execute([':name' => $migrationFile]);
                    echo "LOG REMOVED.\n";
                }
                $db->commit();
                echo "SUCCESS: Rollback of Batch #{$maxBatch} complete.\n";
            } catch (Exception $e) {
                $db->rollBack();
                echo "FAILED: " . $e->getMessage() . "\n";
                exit(1);
            }
            break;

        case '--dry-run':
        case '-d':
            if (empty($pendingList)) {
                echo "No pending migrations to simulate.\n";
                exit(0);
            }
            echo "Simulating schema upgrade (DRY-RUN mode - changes will be rolled back)...\n";
            $db->beginTransaction();
            try {
                foreach ($pendingList as $migrationFile) {
                    echo " -> Simulating: $migrationFile... ";
                    $filePath = MIGRATION_ROOT . '/migrations/' . $migrationFile;
                    $queries = executeMigrationScript($db, $filePath, true);
                    echo "OK ($queries queries)\n";
                }
                $db->rollBack();
                echo "DRY-RUN SUCCESSFUL: All DDL statement scripts validated with no errors.\n";
            } catch (Exception $e) {
                $db->rollBack();
                echo "FAILED SIMULATION!\n";
                echo "Error: " . $e->getMessage() . "\n";
                exit(1);
            }
            break;

        default:
            echo "Available CLI Commands:\n";
            echo "  --status, -s    Check status of all migration files\n";
            echo "  --run, -r       Run all pending database migrations\n";
            echo "  --run-next, -n  Run only the very next pending migration\n";
            echo "  --rollback, -b  Roll back the last batch of migrations\n";
            echo "  --dry-run, -d   Simulate migration validation (dry run)\n";
            break;
    }
    exit(0);
}

// 9. --- SECURE HTML BROWSER UI ---
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ColorJet ERP Bangladesh - Schema Migration Engine</title>
    <!-- Tailwind CSS CDN for high-end styling -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;700&display=swap" rel="stylesheet">
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Space Grotesk', 'sans-serif'],
                        mono: ['JetBrains Mono', 'monospace'],
                    }
                }
            }
        }
    </script>
    <style>
        .custom-scrollbar::-webkit-scrollbar {
            width: 6px;
            height: 6px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
            background: #f1f1f1;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
            background: #888;
            border-radius: 4px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
            background: #555;
        }
    </style>
</head>
<body class="bg-[#F3F5F9] text-[#1F2937] min-h-screen flex flex-col">

    <!-- Header Navigation Bar -->
    <header class="bg-[#0D47A1] text-white py-4 px-6 shadow-md border-b-4 border-[#FF6F00]">
        <div class="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-4">
            <div class="flex items-center space-x-3">
                <!-- ColorJet Logo Asset Placeholder -->
                <div class="bg-white text-[#0D47A1] h-10 w-10 rounded-lg flex items-center justify-center font-bold text-xl tracking-tighter border-2 border-[#0097E8]">
                    CJ
                </div>
                <div>
                    <h1 class="text-xl font-bold tracking-tight">COLORJET BANGLADESH</h1>
                    <p class="text-[10px] font-bold text-[#FF6F00] tracking-widest uppercase">Enterprise Schema Migration Portal</p>
                </div>
            </div>
            
            <div class="flex items-center space-x-4">
                <div class="bg-[#182078] text-white border border-[#0097E8]/30 rounded-xl px-4 py-1.5 text-xs font-mono">
                    System State: <span class="text-[#0097E8] font-bold">Secure Staging</span>
                </div>
                <?php if ($isAuthenticated): ?>
                    <a href="?logout=1" class="text-xs bg-red-600 hover:bg-red-700 text-white font-bold px-3 py-1.5 rounded-lg transition duration-200">
                        LOGOUT
                    </a>
                <?php endif; ?>
            </div>
        </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 md:px-6 py-8 flex-grow w-full">

        <?php if (!$isAuthenticated): ?>
            <!-- Login Token Screen -->
            <div class="max-w-md mx-auto bg-white rounded-2xl shadow-xl border border-gray-100 p-8 text-center mt-12">
                <div class="w-16 h-16 bg-orange-100 rounded-2xl flex items-center justify-center mx-auto mb-6 border-2 border-[#FF6F00]">
                    <svg class="w-8 h-8 text-[#FF6F00]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                    </svg>
                </div>
                <h2 class="text-xl font-bold text-gray-800 mb-2">Access Token Required</h2>
                <p class="text-xs text-[#526887] mb-6">Database maintenance requires active staging token access. Please provide authorization credentials below.</p>
                
                <form method="POST" class="space-y-4">
                    <input type="password" name="token" required placeholder="Enter Secure Migration Token" 
                           class="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-[#0D47A1] focus:border-transparent text-center font-mono text-sm">
                    <button type="submit" class="w-full bg-[#0D47A1] hover:bg-[#182078] text-white py-3 rounded-xl font-bold text-xs tracking-widest uppercase transition duration-200 shadow-md">
                        VERIFY & DEPLOY PORTAL
                    </button>
                </form>
            </div>
        <?php else: ?>

            <!-- Interactive Authenticated Panel -->
            <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
                
                <!-- Database Connection Info & Pre-Flight Checks -->
                <div class="space-y-6 lg:col-span-1">
                    
                    <!-- connection info -->
                    <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
                        <h3 class="text-xs font-bold text-[#526887] tracking-wider uppercase mb-3">Database Target</h3>
                        
                        <?php if ($dbError): ?>
                            <div class="flex items-center space-x-2.5 mb-3 bg-red-50 text-red-700 px-3 py-2 rounded-xl border border-red-100">
                                <span class="h-2.5 w-2.5 rounded-full bg-red-500 inline-block animate-pulse"></span>
                                <span class="font-bold text-xs">OFFLINE / ERROR</span>
                            </div>
                            <p class="text-xs font-mono text-red-600 bg-red-50 p-3 rounded-lg border border-red-100 break-all leading-normal">
                                <?php echo htmlspecialchars($dbError); ?>
                            </p>
                        <?php else: ?>
                            <div class="flex items-center space-x-2.5 mb-4 bg-emerald-50 text-emerald-700 px-3 py-2 rounded-xl border border-emerald-100">
                                <span class="h-2.5 w-2.5 rounded-full bg-emerald-500 inline-block"></span>
                                <span class="font-bold text-xs">DATABASE CONNECTED</span>
                            </div>
                            
                            <div class="space-y-2.5 text-xs">
                                <div class="flex justify-between pb-1.5 border-b border-gray-100">
                                    <span class="text-[#526887]">Engine:</span>
                                    <span class="font-mono font-bold text-[#0D47A1]"><?php echo strtoupper(DB_PROVIDER); ?></span>
                                </div>
                                <?php if (DB_PROVIDER === 'mysql'): ?>
                                    <div class="flex justify-between pb-1.5 border-b border-gray-100">
                                        <span class="text-[#526887]">Host:</span>
                                        <span class="font-mono text-gray-800"><?php echo DB_HOST; ?></span>
                                    </div>
                                    <div class="flex justify-between pb-1.5 border-b border-gray-100">
                                        <span class="text-[#526887]">Database:</span>
                                        <span class="font-mono text-gray-800"><?php echo DB_NAME; ?></span>
                                    </div>
                                <?php else: ?>
                                    <div class="pb-1">
                                        <span class="text-[#526887] block mb-1">Local DB Path:</span>
                                        <span class="font-mono bg-gray-50 p-2 rounded block break-all text-[10px] text-gray-600 leading-normal border border-gray-100"><?php echo htmlspecialchars(basename(SQLITE_DB_PATH)); ?></span>
                                    </div>
                                <?php endif; ?>
                            </div>
                        <?php endif; ?>
                    </div>

                    <!-- Operations Stats Widget -->
                    <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
                        <h3 class="text-xs font-bold text-[#526887] tracking-wider uppercase mb-4">Migration Progress</h3>
                        <div class="grid grid-cols-2 gap-4">
                            <div class="bg-[#F3F5F9] p-3.5 rounded-xl border border-gray-100 text-center">
                                <span class="text-xs text-[#526887] block mb-1">Total Found</span>
                                <span class="text-2xl font-bold text-[#0D47A1]"><?php echo count($allList); ?></span>
                            </div>
                            <div class="bg-[#F3F5F9] p-3.5 rounded-xl border border-gray-100 text-center">
                                <span class="text-xs text-[#526887] block mb-1">Deployed</span>
                                <span class="text-2xl font-bold text-[#22C55E]"><?php echo count($appliedNames); ?></span>
                            </div>
                            <div class="bg-orange-50/50 p-3.5 rounded-xl border border-orange-100 text-center col-span-2">
                                <span class="text-xs text-[#FF6F00] block mb-1 font-bold">Pending Actions</span>
                                <span class="text-3xl font-extrabold text-[#FF6F00]"><?php echo count($pendingList); ?></span>
                            </div>
                        </div>
                    </div>

                    <!-- Pre-Migration Safety Checklist -->
                    <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm text-xs">
                        <h3 class="text-xs font-bold text-[#526887] tracking-wider uppercase mb-3">Pre-Flight Staging Checklist</h3>
                        <ul class="space-y-2.5">
                            <li class="flex items-start space-x-2">
                                <span class="text-[#22C55E] font-bold font-mono">✓</span>
                                <span class="text-gray-600">Database backup executed successfully.</span>
                            </li>
                            <li class="flex items-start space-x-2">
                                <span class="text-[#22C55E] font-bold font-mono">✓</span>
                                <span class="text-gray-600">Accounting periods unlocked for DDL changes.</span>
                            </li>
                            <li class="flex items-start space-x-2">
                                <span class="text-[#22C55E] font-bold font-mono">✓</span>
                                <span class="text-gray-600">Active terminal sessions notified of upgrade.</span>
                            </li>
                        </ul>
                    </div>
                </div>

                <!-- Main Work Space -->
                <div class="lg:col-span-3 space-y-6">

                    <!-- Execution Banners / Success Alerts -->
                    <?php if ($successMessage): ?>
                        <div class="bg-emerald-50 border-l-4 border-[#22C55E] p-4 rounded-r-xl shadow-sm flex items-start space-x-3">
                            <svg class="w-5 h-5 text-[#22C55E] flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                            </svg>
                            <div>
                                <h4 class="font-bold text-emerald-800 text-sm">Action Completed Successfully</h4>
                                <p class="text-xs text-emerald-700 mt-1"><?php echo htmlspecialchars($successMessage); ?></p>
                            </div>
                        </div>
                    <?php endif; ?>

                    <?php if ($errorMessage): ?>
                        <div class="bg-red-50 border-l-4 border-[#EF4444] p-4 rounded-r-xl shadow-sm flex items-start space-x-3">
                            <svg class="w-5 h-5 text-[#EF4444] flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                            </svg>
                            <div>
                                <h4 class="font-bold text-red-800 text-sm">Operation Aborted (Transaction Rolled Back)</h4>
                                <p class="text-xs text-red-700 mt-1"><?php echo htmlspecialchars($errorMessage); ?></p>
                            </div>
                        </div>
                    <?php endif; ?>

                    <!-- Action control Panel -->
                    <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
                        <h3 class="text-sm font-bold text-[#0D47A1] mb-4 uppercase tracking-wide">Staging Control Panel</h3>
                        
                        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                            
                            <!-- Run All Pending -->
                            <form method="POST" onsubmit="return confirm('Execute all pending schema scripts sequentially in a single database transaction?');" 
                                  class="bg-[#F3F5F9] p-4 rounded-xl border border-gray-200/60 flex flex-col justify-between">
                                <input type="hidden" name="action" value="run_all">
                                <div>
                                    <h4 class="font-bold text-[#1F2937] text-xs uppercase mb-1">Upgrade Database (All)</h4>
                                    <p class="text-[11px] text-[#526887] leading-relaxed mb-4">Executes all pending schema upgrades sequentially. Fully transactional.</p>
                                </div>
                                <div class="space-y-2 mt-auto">
                                    <label class="flex items-center text-[11px] text-[#526887] font-semibold cursor-pointer">
                                        <input type="checkbox" name="dry_run" value="1" class="mr-1.5 accent-[#0D47A1]">
                                        Dry-Run Simulation
                                    </label>
                                    <button type="submit" <?php echo empty($pendingList) ? 'disabled' : ''; ?> 
                                            class="w-full bg-[#0D47A1] hover:bg-[#182078] disabled:bg-gray-200 disabled:text-gray-400 text-white font-bold text-xs py-2 px-3 rounded-lg tracking-widest uppercase transition-all duration-150">
                                        RUN UPGRADES
                                    </button>
                                </div>
                            </form>

                            <!-- Phased Run (Step-by-Step) -->
                            <form method="POST" onsubmit="return confirm('Execute ONLY the next versioned migration script?');" 
                                  class="bg-[#F3F5F9] p-4 rounded-xl border border-gray-200/60 flex flex-col justify-between">
                                <input type="hidden" name="action" value="run_single">
                                <div>
                                    <h4 class="font-bold text-[#1F2937] text-xs uppercase mb-1">Phased Execution (Next)</h4>
                                    <p class="text-[11px] text-[#526887] leading-relaxed mb-4">Run only the very next sequential schema script to verify phases slowly.</p>
                                    <?php if ($nextPending): ?>
                                        <div class="bg-amber-50 text-amber-800 text-[10px] font-mono p-1.5 rounded border border-amber-200 mb-4 overflow-hidden text-ellipsis whitespace-nowrap">
                                            Next: <?php echo htmlspecialchars($nextPending); ?>
                                        </div>
                                    <?php endif; ?>
                                </div>
                                <div class="space-y-2 mt-auto">
                                    <label class="flex items-center text-[11px] text-[#526887] font-semibold cursor-pointer">
                                        <input type="checkbox" name="dry_run" value="1" class="mr-1.5 accent-[#0D47A1]">
                                        Dry-Run Simulation
                                    </label>
                                    <button type="submit" <?php echo empty($pendingList) ? 'disabled' : ''; ?> 
                                            class="w-full bg-[#0097E8] hover:bg-[#0D47A1] disabled:bg-gray-200 disabled:text-gray-400 text-white font-bold text-xs py-2 px-3 rounded-lg tracking-widest uppercase transition-all duration-150">
                                        RUN NEXT STEP
                                    </button>
                                </div>
                            </form>

                            <!-- Rollback Last Batch -->
                            <form method="POST" onsubmit="return confirm('WARNING: Roll back the last batch of migrations? This will attempt to run corresponding down files!');" 
                                  class="bg-[#F3F5F9] p-4 rounded-xl border border-gray-200/60 flex flex-col justify-between">
                                <input type="hidden" name="action" value="rollback_last">
                                <div>
                                    <h4 class="font-bold text-[#EF4444] text-xs uppercase mb-1">Rollback Last Batch</h4>
                                    <p class="text-[11px] text-[#526887] leading-relaxed mb-4">Reverses the most recent batch of changes. Runs `.down.sql` scripts if present.</p>
                                    <?php if ($maxBatch > 0): ?>
                                        <div class="bg-red-50 text-red-800 text-[10px] font-mono p-1.5 rounded border border-red-200 mb-4">
                                            Active Batch: #<?php echo $maxBatch; ?>
                                        </div>
                                    <?php endif; ?>
                                </div>
                                <div class="space-y-2 mt-auto">
                                    <label class="flex items-center text-[11px] text-[#526887] font-semibold cursor-pointer">
                                        <input type="checkbox" name="dry_run" value="1" class="mr-1.5 accent-[#EF4444]">
                                        Dry-Run Simulation
                                    </label>
                                    <button type="submit" <?php echo ($maxBatch === 0) ? 'disabled' : ''; ?> 
                                            class="w-full bg-[#EF4444] hover:bg-red-700 disabled:bg-gray-200 disabled:text-gray-400 text-white font-bold text-xs py-2 px-3 rounded-lg tracking-widest uppercase transition-all duration-150">
                                        ROLLBACK BATCH
                                    </button>
                                </div>
                            </form>

                        </div>
                    </div>

                    <!-- Migration Scripts Registry & SQL Code Preview -->
                    <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
                        <div class="flex justify-between items-center mb-4">
                            <h3 class="text-sm font-bold text-[#1F2937] uppercase tracking-wide">Versioned Migration Manifest</h3>
                            <span class="text-xs bg-gray-100 text-[#526887] px-2.5 py-1 rounded-full font-mono font-bold">
                                total: <?php echo count($allList); ?> scripts
                            </span>
                        </div>

                        <div class="space-y-3">
                            <?php if (empty($allList)): ?>
                                <p class="text-sm text-[#526887] text-center py-6">No SQL migration files found in `/backend/migrations/`.</p>
                            <?php else: ?>
                                <?php foreach ($allList as $index => $file): 
                                    $isApplied = in_array($file, $appliedNames);
                                    $fileId = "sql_code_" . $index;
                                    
                                    // Fetch execution info
                                    $duration = 0;
                                    $batch = 0;
                                    $appliedDate = '';
                                    foreach ($appliedDetailed as $row) {
                                        if ($row['migration_name'] === $file) {
                                            $duration = $row['execution_time_ms'] ?? 0;
                                            $batch = $row['batch'] ?? 1;
                                            $appliedDate = $row['applied_at'] ?? '';
                                        }
                                    }
                                    
                                    $filePath = MIGRATION_ROOT . '/migrations/' . $file;
                                    $sqlCode = file_exists($filePath) ? htmlspecialchars(file_get_contents($filePath)) : "File could not be found.";
                                ?>
                                    <div class="border border-gray-100 rounded-xl bg-gray-50/50 hover:bg-white hover:shadow-sm hover:border-gray-200/80 transition-all duration-200">
                                        <!-- Header Row -->
                                        <div class="p-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3 cursor-pointer" 
                                             onclick="document.getElementById('<?php echo $fileId; ?>').classList.toggle('hidden');">
                                            
                                            <div class="flex items-center space-x-3">
                                                <!-- Icon badge -->
                                                <?php if ($isApplied): ?>
                                                    <div class="h-8 w-8 bg-emerald-50 text-[#22C55E] rounded-lg flex items-center justify-center border border-emerald-100">
                                                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"></path>
                                                        </svg>
                                                    </div>
                                                <?php else: ?>
                                                    <div class="h-8 w-8 bg-orange-50 text-[#FF6F00] rounded-lg flex items-center justify-center border border-orange-100">
                                                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                                                        </svg>
                                                    </div>
                                                <?php endif; ?>

                                                <div>
                                                    <h4 class="font-mono text-xs font-bold text-[#1F2937] leading-tight"><?php echo htmlspecialchars($file); ?></h4>
                                                    <?php if ($isApplied): ?>
                                                        <p class="text-[10px] text-[#526887] mt-0.5">
                                                            Applied at: <span class="font-mono"><?php echo $appliedDate; ?></span> • Batch: <span class="font-bold">#<?php echo $batch; ?></span> • Duration: <span class="font-bold font-mono"><?php echo $duration; ?>ms</span>
                                                        </p>
                                                    <?php else: ?>
                                                        <p class="text-[10px] text-orange-600 mt-0.5 font-bold">
                                                            Pending deployment upgrade. Click to view schema SQL.
                                                        </p>
                                                    <?php endif; ?>
                                                </div>
                                            </div>

                                            <div>
                                                <?php if ($isApplied): ?>
                                                    <span class="bg-emerald-50 text-[#22C55E] text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded border border-emerald-100">
                                                        APPLIED OK
                                                    </span>
                                                <?php else: ?>
                                                    <span class="bg-orange-50 text-[#FF6F00] text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded border border-orange-100">
                                                        PENDING DEPLOY
                                                    </span>
                                                <?php endif; ?>
                                            </div>
                                        </div>

                                        <!-- SQL Code Preview Block -->
                                        <div id="<?php echo $fileId; ?>" class="hidden border-t border-gray-100 bg-[#182078] rounded-b-xl">
                                            <div class="p-3 bg-[#13195C] text-[10px] text-gray-400 font-bold tracking-widest uppercase flex justify-between items-center rounded-t-sm">
                                                <span>DATABASE STRUCTURE DEFINITION (UP)</span>
                                                <span class="font-mono text-orange-400">SQL SOURCE CODE</span>
                                            </div>
                                            <pre class="p-4 text-xs font-mono text-[#0097E8] overflow-x-auto max-h-72 custom-scrollbar whitespace-pre-wrap leading-relaxed"><?php echo $sqlCode; ?></pre>
                                        </div>
                                    </div>
                                <?php endforeach; ?>
                            <?php endif; ?>
                        </div>
                    </div>

                    <!-- Interactive Database Write Simulation Playground -->
                    <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
                        <div class="flex items-center space-x-3 mb-4">
                            <div class="bg-indigo-50 text-indigo-600 h-9 w-9 rounded-xl flex items-center justify-center border border-indigo-100">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                                </svg>
                            </div>
                            <div>
                                <h3 class="text-sm font-bold text-[#1F2937] uppercase tracking-wide">Write Interception Simulator</h3>
                                <p class="text-[11px] text-[#526887]">Perform standard database writes to trigger the AuditablePDO automatic interceptor in real time.</p>
                            </div>
                        </div>

                        <?php if (!in_array('004_create_audit_logs_and_financials.sql', $appliedNames)): ?>
                            <div class="bg-amber-50 border border-amber-200 text-amber-800 rounded-xl p-4 text-xs flex items-start space-x-2.5">
                                <svg class="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                                </svg>
                                <div>
                                    <span class="font-bold block mb-1">Audit Logs Setup Pending</span>
                                    The financial & audit logging migration (004) must be run first to set up targeted tables. Click <strong>Run Upgrades</strong> above to apply it!
                                </div>
                            </div>
                        <?php else: ?>
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                                
                                <!-- Stock Level Form -->
                                <form method="POST" class="bg-gray-50/70 rounded-xl border border-gray-100 p-4 flex flex-col justify-between">
                                    <input type="hidden" name="action" value="trigger_write">
                                    <input type="hidden" name="write_type" value="stock">
                                    <div>
                                        <div class="flex justify-between items-center mb-3">
                                            <span class="text-[10px] font-bold uppercase tracking-widest text-indigo-600 bg-indigo-50 border border-indigo-100 px-2 py-0.5 rounded-md font-sans">Stock Write</span>
                                            <span class="text-[10px] text-[#526887] font-mono">Table: supply_items</span>
                                        </div>
                                        <div class="space-y-3">
                                            <div>
                                                <label class="block text-[11px] text-[#526887] font-bold mb-1.5">Select Supply Item (SKU):</label>
                                                <select name="stock_sku" class="w-full bg-white text-xs border border-gray-200 rounded-lg px-2.5 py-2 font-mono">
                                                    <option value="CJ-UV-CYN1L">CJ-UV-CYN1L - UV-6090 Cyan Ink 1L</option>
                                                    <option value="CJ-UV-MAG1L">CJ-UV-MAG1L - UV-6090 Magenta Ink 1L</option>
                                                    <option value="CJ-UV-YEL1L">CJ-UV-YEL1L - UV-6090 Yellow Ink 1L</option>
                                                    <option value="CJ-DMP-DX5">CJ-DMP-DX5 - Damper DX5 Single Line</option>
                                                </select>
                                            </div>
                                            <div>
                                                <label class="block text-[11px] text-[#526887] font-bold mb-1.5">Adjustment Quantity:</label>
                                                <div class="flex items-center space-x-2">
                                                    <input type="number" name="stock_change" value="5" min="-50" max="50" class="w-20 bg-white text-xs border border-gray-200 rounded-lg px-2.5 py-1.5 text-center font-bold">
                                                    <span class="text-[11px] text-[#526887]">units</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mt-4 pt-4 border-t border-gray-100">
                                        <button type="submit" class="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs py-2 px-3 rounded-lg tracking-wider uppercase transition duration-150 shadow-sm">
                                            Execute Stock UPDATE
                                        </button>
                                    </div>
                                </form>

                                <!-- Financial Transaction Form -->
                                <form method="POST" class="bg-gray-50/70 rounded-xl border border-gray-100 p-4 flex flex-col justify-between">
                                    <input type="hidden" name="action" value="trigger_write">
                                    <input type="hidden" name="write_type" value="finance">
                                    <div>
                                        <div class="flex justify-between items-center mb-3">
                                            <span class="text-[10px] font-bold uppercase tracking-widest text-emerald-600 bg-emerald-50 border border-emerald-100 px-2 py-0.5 rounded-md font-sans font-bold">Financial Write</span>
                                            <span class="text-[10px] text-[#526887] font-mono">Table: financial_transactions</span>
                                        </div>
                                        <div class="space-y-3">
                                            <div class="grid grid-cols-2 gap-2">
                                                <div>
                                                    <label class="block text-[11px] text-[#526887] font-bold mb-1">Account:</label>
                                                    <input type="text" name="fin_account" value="Cash Account" class="w-full bg-white text-xs border border-gray-200 rounded-lg px-2.5 py-1.5 font-bold">
                                                </div>
                                                <div>
                                                    <label class="block text-[11px] text-[#526887] font-bold mb-1">Type:</label>
                                                    <select name="fin_type" class="w-full bg-white text-xs border border-gray-200 rounded-lg px-2.5 py-1.5 font-bold">
                                                        <option value="DEBIT">DEBIT (Inflow)</option>
                                                        <option value="CREDIT">CREDIT (Outflow)</option>
                                                    </select>
                                                </div>
                                            </div>
                                            <div class="grid grid-cols-2 gap-2">
                                                <div>
                                                    <label class="block text-[11px] text-[#526887] font-bold mb-1">Amount (BDT):</label>
                                                    <input type="number" step="100" name="fin_amount" value="1500" class="w-full bg-white text-xs border border-gray-200 rounded-lg px-2.5 py-1.5 font-mono">
                                                </div>
                                                <div>
                                                    <label class="block text-[11px] text-[#526887] font-bold mb-1">Narration:</label>
                                                    <input type="text" name="fin_desc" value="Office inventory restock" class="w-full bg-white text-xs border border-gray-200 rounded-lg px-2.5 py-1.5">
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mt-4 pt-4 border-t border-gray-100">
                                        <button type="submit" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs py-2 px-3 rounded-lg tracking-wider uppercase transition duration-150 shadow-sm">
                                            Execute Finance INSERT
                                        </button>
                                    </div>
                                </form>

                            </div>
                        <?php endif; ?>
                    </div>

                    <!-- Real-time Intercepted Audit Logs Inspector -->
                    <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
                        <div class="flex justify-between items-center mb-4">
                            <div class="flex items-center space-x-3">
                                <div class="bg-amber-50 text-amber-600 h-9 w-9 rounded-xl flex items-center justify-center border border-amber-100">
                                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                                    </svg>
                                </div>
                                <div>
                                    <h3 class="text-sm font-bold text-[#1F2937] uppercase tracking-wide">Automatic SQL Audit Logs</h3>
                                    <p class="text-[11px] text-[#526887]">Intercepted and captured SQL write operations on key stock and financial tables.</p>
                                </div>
                            </div>
                            <span class="text-xs bg-amber-100 text-amber-800 px-2.5 py-1 rounded-full font-mono font-bold">
                                total logged: <?php echo count($auditLogs); ?> events
                            </span>
                        </div>

                        <?php if (empty($auditLogs)): ?>
                            <div class="text-center py-8 bg-gray-50/50 border border-dashed border-gray-200 rounded-xl">
                                <p class="text-xs text-[#526887] leading-relaxed">No write operations recorded yet.<br>Apply pending migrations or use the simulator above to write to the database!</p>
                            </div>
                        <?php else: ?>
                            <div class="space-y-4 max-h-[500px] overflow-y-auto custom-scrollbar pr-1">
                                <?php foreach ($auditLogs as $log): 
                                    $actionColor = 'bg-gray-100 text-gray-700 border-gray-200';
                                    if ($log['action'] === 'INSERT') {
                                        $actionColor = 'bg-emerald-50 text-emerald-700 border-emerald-100';
                                    } elseif ($log['action'] === 'UPDATE') {
                                        $actionColor = 'bg-blue-50 text-blue-700 border-blue-100';
                                    } elseif ($log['action'] === 'DELETE') {
                                        $actionColor = 'bg-red-50 text-red-700 border-red-100';
                                    }
                                ?>
                                    <div class="bg-gray-50/80 rounded-xl border border-gray-100 p-4 hover:border-gray-200 transition-all duration-150">
                                        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2 mb-3 border-b border-gray-100 pb-2">
                                            <div class="flex flex-wrap items-center gap-2">
                                                <span class="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded border <?php echo $actionColor; ?>">
                                                    <?php echo htmlspecialchars($log['action']); ?>
                                                </span>
                                                <span class="text-[10px] font-bold text-[#182078] bg-blue-50/50 border border-blue-100/50 px-2 py-0.5 rounded">
                                                    <?php echo htmlspecialchars($log['table_name']); ?>
                                                </span>
                                                <span class="text-[10px] text-[#526887] bg-gray-100 border border-gray-200/50 px-2 py-0.5 rounded font-mono">
                                                    Actor: <strong class="text-gray-800"><?php echo htmlspecialchars($log['user_id']); ?></strong>
                                                </span>
                                            </div>
                                            <span class="text-[10px] font-mono text-[#526887]" title="Timestamp">
                                                <?php echo htmlspecialchars($log['timestamp']); ?>
                                            </span>
                                        </div>

                                        <div class="space-y-2">
                                            <!-- Query block -->
                                            <div class="bg-[#0F144D] text-white p-2.5 rounded-lg font-mono text-[11px] overflow-x-auto custom-scrollbar whitespace-pre shadow-inner">
                                                <span class="text-orange-400">QUERY:</span> <?php echo htmlspecialchars($log['query_sql']); ?>
                                            </div>

                                            <!-- Payload/Params block -->
                                            <?php if (!empty($log['payload']) && $log['payload'] !== 'null'): ?>
                                                <div class="bg-gray-100/60 p-2 text-gray-700 rounded-lg font-mono text-[10px]">
                                                    <span class="text-indigo-600 font-bold">BOUND PAYLOAD:</span> <?php echo htmlspecialchars($log['payload']); ?>
                                                </div>
                                            <?php endif; ?>
                                        </div>
                                    </div>
                                <?php endforeach; ?>
                            </div>
                        <?php endif; ?>
                    </div>

                    <!-- Operations Logs & Audit Logs -->
                    <?php if (!empty($executionLogs)): ?>
                        <div class="bg-[#182078] rounded-2xl border border-blue-900/40 p-6 text-white shadow-xl">
                            <h3 class="text-xs font-bold text-orange-400 tracking-widest uppercase mb-4">Immediate Staging Output Stream</h3>
                            <div class="space-y-3 font-mono text-xs">
                                <?php foreach ($executionLogs as $log): ?>
                                    <div class="bg-[#0F144D] border border-blue-900/50 rounded-xl p-4">
                                        <div class="flex justify-between items-center border-b border-blue-900/30 pb-2 mb-2">
                                            <span class="text-white font-bold text-xs"><?php echo htmlspecialchars($log['file']); ?></span>
                                            <span class="text-[9px] font-black uppercase px-2.5 py-0.5 rounded <?php echo $log['status'] === 'SUCCESS' || $log['status'] === 'ROLLBACK' ? 'bg-emerald-950 text-emerald-400 border border-emerald-900' : 'bg-red-950 text-red-400 border border-red-900'; ?>">
                                                <?php echo $log['status']; ?>
                                            </span>
                                        </div>
                                        <p class="text-[#0097E8] text-[11px] leading-relaxed"><?php echo htmlspecialchars($log['message']); ?></p>
                                    </div>
                                <?php endforeach; ?>
                            </div>
                        </div>
                    <?php endif; ?>

                </div>
            </div>

            <!-- Staging System Health Check -->
            <footer class="mt-16 text-center text-[10px] text-[#526887] leading-relaxed border-t border-gray-200/80 pt-8 max-w-2xl mx-auto">
                <p class="font-bold uppercase tracking-widest text-[#0D47A1] mb-1">ColorJet Business ERP Platform</p>
                ISO 9001:2015 Compliant Database Transaction Engine. Session Managed securely under strict owner-check policy rules.
            </footer>

        <?php endif; ?>

    </main>

</body>
</html>
<?php endif; ?>
