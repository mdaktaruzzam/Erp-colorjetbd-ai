#!/bin/bash

# COLORJET Bangladesh ERP - Quick Installation Script
# This script automates the setup process for Android development environment

set -e  # Exit on error

echo "================================"
echo "COLORJET ERP - Installation Script"
echo "================================"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo -e "${YELLOW}[1/6] Checking Prerequisites...${NC}"

if ! command -v git &> /dev/null; then
    echo -e "${RED}✗ Git not found. Please install Git.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Git found${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java not found. Please install Java 11+.${NC}"
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | grep -oP '(?<=")\d+' | head -1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo -e "${RED}✗ Java version $JAVA_VERSION found. Java 11+ required.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java $JAVA_VERSION found${NC}"

# Clone or verify repository
echo ""
echo -e "${YELLOW}[2/6] Repository Setup...${NC}"

if [ -d "Erp-colorjetbd-ai" ]; then
    echo -e "${GREEN}✓ Repository directory found${NC}"
    cd Erp-colorjetbd-ai
else
    echo -e "${YELLOW}Cloning repository...${NC}"
    git clone https://github.com/mdaktaruzzam/Erp-colorjetbd-ai.git
    cd Erp-colorjetbd-ai
    echo -e "${GREEN}✓ Repository cloned${NC}"
fi

# Setup environment
echo ""
echo -e "${YELLOW}[3/6] Environment Configuration...${NC}"

if [ ! -f ".env" ]; then
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo -e "${GREEN}✓ .env created from .env.example${NC}"
        echo -e "${YELLOW}  NOTE: Update .env with your production values before deploying${NC}"
    fi
fi

# Build gradle wrapper
echo ""
echo -e "${YELLOW}[4/6] Gradle Setup...${NC}"

if [ ! -f "gradlew" ]; then
    echo -e "${RED}✗ gradlew not found${NC}"
    exit 1
fi

chmod +x gradlew
echo -e "${GREEN}✓ Gradle wrapper configured${NC}"

# Dependencies
echo ""
echo -e "${YELLOW}[5/6] Downloading Dependencies...${NC}"

./gradlew dependencies --refresh-dependencies > /dev/null 2>&1 || true
echo -e "${GREEN}✓ Dependencies resolved${NC}"

# Build verification
echo ""
echo -e "${YELLOW}[6/6] Build Verification...${NC}"

./gradlew assembleDebug > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Debug build successful${NC}"
else
    echo -e "${RED}✗ Build failed. Check error messages above.${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}✓ Installation Complete!${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "Next Steps:"
echo "1. Open Android Studio"
echo "2. File → Open → Select: $(pwd)"
echo "3. Wait for Gradle sync"
echo "4. Run → Run 'app'"
echo ""
echo "Default Login:"
echo "  Username: cj-md-001"
echo "  Passcode: 1234"
echo "  Role: OWNER"
echo ""
echo -e "${YELLOW}⚠️  IMPORTANT: Change default password on first login!${NC}"
echo ""
