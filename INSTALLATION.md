# COLORJET Bangladesh ERP - Installation & Deployment Guide

## Quick Start Installation

### Prerequisites
- Android Studio (Latest)
- Java 11+
- Gradle 7.0+
- Android SDK 24+ (API 24 minimum)
- PHP 7.4+ (for backend)
- MySQL 5.7+ or SQLite 3.x
- Git

---

## 1. DOWNLOAD SOURCE CODE

### Option A: Clone from GitHub
```bash
git clone https://github.com/mdaktaruzzam/Erp-colorjetbd-ai.git
cd Erp-colorjetbd-ai
```

### Option B: Download as ZIP
```bash
# Visit: https://github.com/mdaktaruzzam/Erp-colorjetbd-ai/archive/refs/heads/main.zip
# Extract the ZIP file to your preferred location
unzip Erp-colorjetbd-ai-main.zip
cd Erp-colorjetbd-ai-main
```

---

## 2. ANDROID APP SETUP

### Step 1: Open in Android Studio
1. Launch Android Studio
2. File → Open → Select extracted `Erp-colorjetbd-ai` folder
3. Wait for Gradle to sync (2-5 minutes)

### Step 2: Configure Build Variant
1. Select Build Variant: `Release` or `Debug`
2. Ensure Target SDK: 34+, Min SDK: 24

### Step 3: Run App
```bash
# Via Android Studio: Run → Run 'app'
# Or via terminal:
./gradlew build
./gradlew installDebug
```

### Step 4: First Login
**Default Owner Account:**
- **Username:** `cj-md-001`
- **Passcode:** `1234`
- **Full Name:** Md Aktaruzzaman
- **Role:** OWNER

---

## 3. CHANGE DEFAULT PASSWORD (MANDATORY)

### First Login Flow:
1. Open COLORJET ERP app
2. Login with default credentials above
3. Navigate to **More Screen** → **User Management**
4. Click **Edit Profile** or **Change Password**
5. **OLD PASSWORD:** `1234`
6. **NEW PASSWORD:** Enter secure password (min 8 chars, mixed case, numbers)
7. **CONFIRM:** Re-enter new password
8. Save & Logout
9. Login with NEW password

---

## 4. BACKEND SETUP (Optional - For Cloud Sync)

### Step 1: Backend Files
Located in `/backend/` directory:
- `config.php` - Database & environment configuration
- `api.php` - REST API endpoints
- `AuditLogger.php` - Audit trail logging
- `migration_manager.php` - Database migration manager

### Step 2: Environment Configuration
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

Edit `.env` with production values:
```env
DB_PROVIDER=mysql
DB_HOST=your-db-host.com
DB_PORT=3306
DB_NAME=colorjet_erp_production
DB_USER=colorjet_secure_user
DB_PASS=your-strong-password
COLORJET_API_TOKEN=your-secure-api-token
```

### Step 3: Deploy Backend to VPS
```bash
# Upload to your VPS via SFTP/SCP
sftp user@your-vps.com
put -r backend /var/www/html/erp
```

### Step 4: Configure Web Server
**Apache (.htaccess)**
```apache
<IfModule mod_rewrite.c>
    RewriteEngine On
    RewriteCond %{REQUEST_FILENAME} !-f
    RewriteCond %{REQUEST_FILENAME} !-d
    RewriteRule ^(.*)$ api.php [QSA,L]
</IfModule>
```

**Nginx (nginx.conf)**
```nginx
location /erp/ {
    try_files $uri $uri/ /api.php?$query_string;
}
```

### Step 5: Set Permissions
```bash
chmod 755 /var/www/html/erp
chmod 644 /var/www/html/erp/*.php
chmod 777 /var/www/html/erp/logs/  # If applicable
```

---

## 5. DATABASE INITIALIZATION

### Option A: Auto-Initialization (Recommended)
App automatically creates & seeds database on first launch.

### Option B: Manual MySQL Setup
```sql
-- Create Database
CREATE DATABASE colorjet_erp_production;
USE colorjet_erp_production;

-- Create Users Table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    fullName VARCHAR(255),
    pinCode VARCHAR(32),
    role VARCHAR(50),
    designation VARCHAR(255),
    department VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    status VARCHAR(50),
    INDEX idx_username (username),
    INDEX idx_role (role)
);

-- Insert Default Owner
INSERT INTO users (username, fullName, pinCode, role, designation, department, phone, email, status)
VALUES ('cj-md-001', 'Md Aktaruzzaman', '1234', 'OWNER', 'Founder & CEO', 'Management', '01954100710', 'admin@colorjetbd.com', 'Active');

-- Run other DDL from ErpModels.kt structure
```

---

## 6. PRODUCTION CONFIGURATION

### Security Checklist
- [ ] Change default passwords
- [ ] Update `.env` with real credentials
- [ ] Enable HTTPS/SSL certificates
- [ ] Configure CORS allowlist in `backend/api.php`
- [ ] Set `RESTRICT_TO_LOCALHOST=true` in `backend/config.php`
- [ ] Update `ALLOW_CLI_EXECUTION=false`
- [ ] Generate strong `COLORJET_API_TOKEN`
- [ ] Enable database backups
- [ ] Configure audit logging

### Update API Token
Edit `backend/api.php`:
```php
$providedToken = $headers['X-ColorJet-Token'] ?? '';
if ($providedToken !== 'your-new-secure-token') {
    http_response_code(401);
    echo json_encode(['status' => 'error', 'message' => 'Unauthorized']);
    exit();
}
```

### Configure CORS Origins
Edit `backend/api.php`:
```php
$allowedOrigins = [
    'https://yourcompany.com',
    'https://app.yourcompany.com',
];
```

---

## 7. TESTING & VALIDATION

### Run App Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Verify Login Roles
1. **Owner (cj-md-001):** Full access to all modules
2. **Admin (cj-mgmt-002):** Admin dashboard + most operations
3. **Staff (cj-tech-007 - Zahed Islam):** Limited service operations
4. **Customer (customer-001):** Customer portal only

### Test Data Included
- 50+ Pre-seeded users
- 30+ Sample customers
- 100+ Products with stock levels
- Sample invoices, payments, machines

---

## 8. TROUBLESHOOTING

### App Won't Start
```bash
# Clear cache
./gradlew clean

# Rebuild
./gradlew build

# Reinstall
./gradlew installDebug
```

### Database Connection Failed
- Verify SQLite exists: `/data/data/com.aistudio.colorjeterp.pxlmjq/databases/colorjet_erp_database`
- Check MySQL credentials in `.env`
- Verify network connectivity if using cloud DB

### API Sync Issues
- Verify backend URL in app settings
- Check `COLORJET_API_TOKEN` matches
- Ensure CORS allowlist includes app origin
- Check server logs: `/var/www/html/erp/logs/`

### Permission Errors
- Verify user role in database
- Check role-based access matrix in `ROLE_PERMISSION_MATRIX.md`
- Ensure user is not deleted/disabled

---

## 9. FIRST-TIME ADMIN SETUP

### Initial Admin Configuration
1. **Login as Owner:** `cj-md-001` / `1234`
2. **Change Owner Password:**
   - Navigate to More → User Management
   - Edit own profile
   - Set strong new password
   - Logout and re-login with new password
3. **Create Additional Admins (Optional):**
   - More → Add User
   - Set Role: ADMIN
   - Assign to departments
4. **Configure Company Settings:**
   - More → Settings
   - Update company name, address
   - Configure payment terms
5. **Seed Master Data (If needed):**
   - More → Products
   - Add product categories
   - Add suppliers
   - Add ledger accounts

---

## 10. DEPLOYMENT CHECKLIST

### Pre-Production
- [ ] All default passwords changed
- [ ] Backend `.env` configured with real values
- [ ] Database backups enabled
- [ ] HTTPS/SSL certificates installed
- [ ] CORS origins configured
- [ ] API token secured
- [ ] Audit logging enabled
- [ ] Test all user roles can login
- [ ] Verify data sync working (if using backend)

### Production Release
- [ ] Build signed APK: `./gradlew assembleRelease`
- [ ] Sign with production keystore
- [ ] Test on multiple devices
- [ ] Upload to Google Play Store (if applicable)
- [ ] Monitor crash reports
- [ ] Enable remote logging

---

## 11. SUPPORT & DOCUMENTATION

- **Module Inventory:** See `MODULE_INVENTORY.md`
- **API Routes:** See `API_ROUTE_MAP.md`
- **Completed Modules:** See `COMPLETED_MODULES.md`
- **Role Permissions:** See `ROLE_PERMISSION_MATRIX.md`
- **Test Results:** See `TEST_RESULTS.md`

---

## License & Ownership

**COLORJET Bangladesh ERP v1.0.23**
- Developer: Md Aktaruzzaman
- Company: COLORJET Bangladesh Ltd.
- Contact: admin@colorjetbd.com
- Hotline: +8809677610610

---

**Installation Complete!** 🎉

For issues or questions, contact the COLORJET Support team.
