@echo off
REM COLORJET Bangladesh ERP - Quick Installation Script for Windows
REM This script automates the setup process for Android development environment

setlocal enabledelayedexpansion

echo ================================
echo COLORJET ERP - Installation Script
echo ================================
echo.

REM Check Java
echo [1/6] Checking Prerequisites...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo X Java not found. Please install Java 11+.
    pause
    exit /b 1
)
echo ✓ Java found

REM Check Git
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo X Git not found. Please install Git.
    pause
    exit /b 1
)
echo ✓ Git found

REM Repository setup
echo.
echo [2/6] Repository Setup...

if exist "Erp-colorjetbd-ai" (
    echo ✓ Repository directory found
    cd /d Erp-colorjetbd-ai
) else (
    echo Cloning repository...
    git clone https://github.com/mdaktaruzzam/Erp-colorjetbd-ai.git
    cd /d Erp-colorjetbd-ai
    echo ✓ Repository cloned
)

REM Environment setup
echo.
echo [3/6] Environment Configuration...

if not exist ".env" (
    if exist ".env.example" (
        copy .env.example .env >nul
        echo ✓ .env created from .env.example
        echo   NOTE: Update .env with your production values before deploying
    )
)

REM Gradle setup
echo.
echo [4/6] Gradle Setup...

if not exist "gradlew.bat" (
    echo X gradlew.bat not found
    pause
    exit /b 1
)
echo ✓ Gradle wrapper found

REM Dependencies
echo.
echo [5/6] Downloading Dependencies...

call gradlew.bat dependencies --refresh-dependencies >nul 2>&1
echo ✓ Dependencies resolved

REM Build verification
echo.
echo [6/6] Build Verification...

call gradlew.bat assembleDebug >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Debug build successful
) else (
    echo X Build failed. Check error messages above.
    pause
    exit /b 1
)

echo.
echo ================================
echo ✓ Installation Complete!
echo ================================
echo.
echo Next Steps:
echo 1. Open Android Studio
echo 2. File ^> Open ^> Select current directory
echo 3. Wait for Gradle sync
echo 4. Run ^> Run 'app'
echo.
echo Default Login:
echo   Username: cj-md-001
echo   Passcode: 1234
echo   Role: OWNER
echo.
echo ⚠  IMPORTANT: Change default password on first login!
echo.
pause
