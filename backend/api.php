<?php
/**
 * COLORJET Bangladesh ERP - Production-safe API
 * Strict CORS, token validation, and request sanitization
 */

header('Content-Type: application/json; charset=utf-8');

$allowedOrigins = [
    'https://colorjetbd.com',
    'https://www.colorjetbd.com',
    'https://api.colorjetbd.com',
];

$origin = $_SERVER['HTTP_ORIGIN'] ?? '';
if (in_array($origin, $allowedOrigins, true)) {
    header('Access-Control-Allow-Origin: ' . $origin);
}
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-ColorJet-Token, Authorization');
header('Access-Control-Max-Age: 3600');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once dirname(__FILE__) . '/config.php';

function sanitizeAction($action) {
    $allowedActions = ['test', 'push', 'pull'];
    $normalized = is_string($action) ? strtolower(trim($action)) : 'pull';
    return in_array($normalized, $allowedActions, true) ? $normalized : 'pull';
}

function getAllowedTables() {
    return [
        'users', 'customers', 'products', 'stock_movements', 'quotations',
        'sales_orders', 'invoices', 'payments', 'ledger_transactions',
        'service_tickets', 'warranties', 'attendance', 'office_tasks', 'machines',
        'internal_messages', 'push_announcements', 'production_records', 'ai_drafts'
    ];
}

$headers = getallheaders();
$providedToken = $headers['X-ColorJet-Token'] ?? (
    isset($headers['Authorization']) ? str_replace('Bearer ', '', $headers['Authorization']) : ''
);

if ($providedToken === '') {
    http_response_code(401);
    echo json_encode([
        'status' => 'error',
        'message' => 'Missing authentication token. Include X-ColorJet-Token header or Authorization bearer token.',
        'code' => 'AUTH_MISSING'
    ]);
    exit();
}

if ($providedToken !== MIGRATION_SECURE_TOKEN) {
    http_response_code(401);
    echo json_encode([
        'status' => 'error',
        'message' => 'Unauthorized. Invalid security token.',
        'code' => 'AUTH_INVALID'
    ]);
    exit();
}

try {
    $db = getMigrationDbConnection();
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Database connection failed. Check server configuration.',
        'code' => 'DB_CONNECTION_FAILED'
    ]);
    exit();
}

$jsonInput = file_get_contents('php://input');
$requestData = json_decode($jsonInput, true);
if (!is_array($requestData)) {
    $requestData = [];
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $action = sanitizeAction($_GET['action'] ?? 'pull');
} else {
    $action = sanitizeAction($requestData['action'] ?? 'pull');
}

switch ($action) {
    case 'test':
        echo json_encode([
            'status' => 'success',
            'message' => 'COLORJET VPS REST API connected successfully!',
            'database_provider' => DB_PROVIDER,
            'timestamp' => time(),
            'version' => '1.0.0'
        ]);
        break;

    case 'push':
        if (!isset($requestData['tables']) || !is_array($requestData['tables'])) {
            http_response_code(400);
            echo json_encode([
                'status' => 'error',
                'message' => 'Missing or invalid "tables" dataset.',
                'code' => 'INVALID_TABLES'
            ]);
            exit();
        }

        $results = [];
        $db->beginTransaction();
        try {
            foreach ($requestData['tables'] as $tableName => $records) {
                if (!is_array($records)) {
                    continue;
                }

                $validTables = getAllowedTables();
                if (!in_array($tableName, $validTables, true)) {
                    continue;
                }

                $insertedCount = 0;
                foreach ($records as $record) {
                    if (!is_array($record) || empty($record)) {
                        continue;
                    }

                    $columns = array_keys($record);
                    $escapedColumns = array_map(function ($col) {
                        return '`' . preg_replace('/[^a-zA-Z0-9_]/', '', $col) . '`';
                    }, $columns);
                    $placeholders = array_map(function ($col) {
                        return ':' . preg_replace('/[^a-zA-Z0-9_]/', '', $col);
                    }, $columns);

                    if (isset($record['id'])) {
                        $deleteStmt = $db->prepare("DELETE FROM `{$tableName}` WHERE `id` = :id");
                        $deleteStmt->execute(['id' => $record['id']]);
                    }

                    $sql = "INSERT INTO `{$tableName}` (" . implode(', ', $escapedColumns) . ") VALUES (" . implode(', ', $placeholders) . ")";
                    $stmt = $db->prepare($sql);

                    $bindParams = [];
                    foreach ($record as $key => $value) {
                        $cleanKey = preg_replace('/[^a-zA-Z0-9_]/', '', $key);
                        $bindParams[$cleanKey] = is_bool($value) ? ($value ? 1 : 0) : $value;
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

            $syncUser = preg_replace('/[^a-zA-Z0-9_-]/', '', (string) ($requestData['username'] ?? 'sync_client'));
            $auditStmt = $db->prepare("INSERT INTO `audit_logs` (username, action, details, timestamp) VALUES (:user, :action, :details, :time)");
            $auditStmt->execute([
                ':user' => $syncUser,
                ':action' => 'CLOUDSYNC_PUSH',
                ':details' => 'Client sync completed',
                ':time' => time(),
            ]);

            echo json_encode([
                'status' => 'success',
                'message' => 'Data pushed successfully to COLORJET Enterprise VPS.',
                'sync_summary' => $results,
                'timestamp' => time()
            ]);
        } catch (Exception $e) {
            $db->rollBack();
            http_response_code(500);
            echo json_encode([
                'status' => 'error',
                'message' => 'Sync push failed: ' . $e->getMessage(),
                'code' => 'SYNC_PUSH_FAILED'
            ]);
        }
        break;

    case 'pull':
        $tablesToPull = isset($requestData['tables']) && is_array($requestData['tables'])
            ? $requestData['tables']
            : getAllowedTables();

        $responsePayload = [];
        try {
            foreach ($tablesToPull as $tableName) {
                if (!in_array($tableName, getAllowedTables(), true)) {
                    continue;
                }

                $stmt = $db->query("SELECT * FROM `{$tableName}` LIMIT 10000");
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
                'message' => 'Sync pull failed: ' . $e->getMessage(),
                'code' => 'SYNC_PULL_FAILED'
            ]);
        }
        break;

    default:
        http_response_code(400);
        echo json_encode([
            'status' => 'error',
            'message' => 'Invalid action parameter.',
            'code' => 'INVALID_ACTION'
        ]);
        break;
}
