-- Phase 1: Establish Supply Inventory Database Schema
-- ColorJet ERP Bangladesh

CREATE TABLE IF NOT EXISTS supply_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    stockLevel INTEGER NOT NULL DEFAULT 0,
    threshold INTEGER NOT NULL DEFAULT 0,
    location VARCHAR(100) NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed initial supply inventory matching local enterprise cache
INSERT OR IGNORE INTO supply_items (sku, name, category, stockLevel, threshold, location) VALUES 
('CJ-UV-CYN1L', 'UV-6090 Cyan Ink 1L', 'Inks', 15, 10, 'Shelf A-1'),
('CJ-UV-MAG1L', 'UV-6090 Magenta Ink 1L', 'Inks', 8, 10, 'Shelf A-1'),
('CJ-UV-YEL1L', 'UV-6090 Yellow Ink 1L', 'Inks', 12, 10, 'Shelf A-1'),
('CJ-UV-BLK1L', 'UV-6090 Black Ink 1L', 'Inks', 5, 10, 'Shelf A-1'),
('CJ-UV-WHT1L', 'UV-6090 White Ink 1L', 'Inks', 22, 15, 'Shelf A-2'),
('CJ-DMP-DX5', 'Damper DX5 Single Line', 'Spare Parts', 50, 20, 'Drawer D-4'),
('CJ-SQ-100', 'Squeegee Blade 100mm', 'Spare Parts', 18, 10, 'Drawer D-1');
