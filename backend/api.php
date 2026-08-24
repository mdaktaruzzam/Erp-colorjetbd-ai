<?php
/**
 * COLORJET Bangladesh - Enterprise Business Management Suite
 * Production-ready VPS REST API Endpoint
 * Handles bi-directional synchronization and remote data persistence
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-ColorJet-Token');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once dirname(__FILE__) . '/config.php';

// Auth verification
$headers = getallheaders();
$providedToken = isset($headers['X-ColorJet-Token']) ? $headers['X-ColorJet-Token'] : (isset($_GET['token']) ? $_GET['token'] : '');

if ($providedToken !== MIGRATION_SECURE_TOKEN) {
    http_response_code(401);
    echo json_encode([
        'status' => 'error',
        'message' => 'Unauthorized. Invalid security token.'
    ]);
    exit();
}

try {
    $db = getMigrationDbConnection();
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Database connection failed: ' . $e->getMessage()
    ]);
    exit();
}

// Get raw request payload
$jsonInput = file_get_contents('php://input');
$requestData = json_decode($jsonInput, true);

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $action = isset($_GET['action']) ? $_GET['action'] : 'pull';
} else {
    $action = isset($requestData['action']) ? $requestData['action'] : 'pull';
}

switch ($action) {
    case 'test':
        echo json_encode([
            'status' => 'success',
            'message' => 'COLORJET VPS REST API connected successfully!',
            'database_provider' => DB_PROVIDER,
            'timestamp' => time()
        ]);
        break;

    case 'push':
        if (!isset($requestData['tables']) || !is_array($requestData['tables'])) {
            http_response_code(400);
            echo json_encode(['status' => 'error', 'message' => 'Missing "tables" dataset.']);
            exit();
        }

        $results = [];
        $db->beginTransaction();
        try {
            foreach ($requestData['tables'] as $tableName => $records) {
                if (!is_array($records)) continue;
                
                // Whitelist tables for security
                $validTables = [
                    'users', 'customers', 'products', 'stock_movements', 'quotations', 
                    'sales_orders', 'invoices', 'payments', 'ledger_transactions', 
                    'service_tickets', 'warranties', 'attendance', 'office_tasks', 'machines',
                    'internal_messages', 'push_announcements', 'production_records', 'ai_drafts'
                ];
                if (!in_array($tableName, $validTables)) {
                    continue;
                }

                $insertedCount = 0;
                foreach ($records as $record) {
                    if (!is_array($record) || empty($record)) continue;

                    // Extract columns and prepare parameters
                    $columns = array_keys($record);
                    
                    // For safety, let's clean column names
                    $escapedColumns = array_map(function($col) { return "`" . preg_replace('/[^a-zA-Z0-9_]/', '', $col) . "`"; }, $columns);
                    $placeholders = array_map(function($col) { return ":" . preg_replace('/[^a-zA-Z0-9_]/', '', $col); }, $columns);
                    
                    // Database-agnostic upsert: delete-then-insert is incredibly clean and prevents constraint conflicts
                    if (isset($record['id'])) {
                        $delStmt = $db->prepare("DELETE FROM `{$tableName}` WHERE `id` = :id");
                        $delStmt->execute(['id' => $record['id']]);
                    }

                    $sql = "INSERT INTO `{$tableName}` (" . implode(', ', $escapedColumns) . ") VALUES (" . implode(', ', $placeholders) . ")";
                    $stmt = $db->prepare($sql);
                    
                    // Bind parameters with clean keys
                    $bindParams = [];
                    foreach ($record as $key => $val) {
                        $cleanKey = preg_replace('/[^a-zA-Z0-9_]/', '', $key);
                        // Convert booleans/nulls cleanly for DB
                        if (is_bool($val)) {
                            $bindParams[$cleanKey] = $val ? 1 : 0;
                        } else {
                            $bindParams[$cleanKey] = $val;
                        }
                    }

                    $stmt->execute($bindParams);
                    $insertedCount++;
                }
                $results[$tableName] = [
                    'status' => 'success',
                    'synced_count' => $insertedCount
                ];
            }
            $db->commit();
            
            // Log sync activity
            $syncUser = isset($requestData['username']) ? $requestData['username'] : 'sync_client';
            $db->query("INSERT INTO `audit_logs` (user_id, username, action, details, timestamp) VALUES (0, '{$syncUser}', 'CLOUDSYNC_PUSH', 'Synchronized client-side tables to VPS.', " . (time() * 1000) . ")");

            echo json_encode([
                'status' => 'success',
                'message' => 'Data pushed successfully to COLORJET Enterprise VPS.',
                'sync_summary' => $results
            ]);
        } catch (Exception $e) {
            $db->rollBack();
            http_response_code(500);
            echo json_encode([
                'status' => 'error',
                'message' => 'Sync push failed: ' . $e->getMessage()
            ]);
        }
        break;

    case 'pull':
        $tablesToPull = isset($requestData['tables']) ? $requestData['tables'] : [
            'users', 'customers', 'products', 'stock_movements', 'quotations', 
            'sales_orders', 'invoices', 'payments', 'ledger_transactions', 
            'service_tickets', 'warranties', 'attendance', 'office_tasks', 'machines',
            'internal_messages', 'push_announcements', 'production_records', 'ai_drafts'
        ];
        
        $responsePayload = [];
        try {
            foreach ($tablesToPull as $tableName) {
                // Security whitelisting
                $validTables = [
                    'users', 'customers', 'products', 'stock_movements', 'quotations', 
                    'sales_orders', 'invoices', 'payments', 'ledger_transactions', 
                    'service_tickets', 'warranties', 'attendance', 'office_tasks', 'machines',
                    'internal_messages', 'push_announcements', 'production_records', 'ai_drafts'
                ];
                if (!in_array($tableName, $validTables)) continue;

                $stmt = $db->query("SELECT * FROM `{$tableName}`");
                $responsePayload[$tableName] = $stmt->fetchAll(PDO::FETCH_ASSOC);
            }

            echo json_encode([
                'status' => 'success',
                'message' => 'Pulled updates from VPS successfully.',
                'timestamp' => time(),
                'tables' => $responsePayload
            ]);
        } catch (Exception $e) {
            http_response_code(500);
            echo json_encode([
                'status' => 'error',
                'message' => 'Sync pull failed: ' . $e->getMessage()
            ]);
        }
        break;

    default:
        http_response_code(400);
        echo json_encode([
            'status' => 'error',
            'message' => 'Invalid action parameter.'
        ]);
        break;
}
