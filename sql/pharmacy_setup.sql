-- ============================================================
--  AR Pharmacy System - Database Setup Script v2.0
--  Run this in MySQL before launching the application
-- ============================================================

CREATE DATABASE IF NOT EXISTS ar_pharmacy;
USE ar_pharmacy;

-- ─────────────────────────────────────────────
--  TABLE: users
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id   INT          NOT NULL AUTO_INCREMENT,
    name      VARCHAR(100) NOT NULL,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    role      ENUM('admin','customer') NOT NULL DEFAULT 'customer',
    phone     VARCHAR(20)  DEFAULT '',
    address   VARCHAR(200) DEFAULT '',
    PRIMARY KEY (user_id)
);

-- ─────────────────────────────────────────────
--  TABLE: medicines  (with category + stock_status)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS medicines (
    med_id      INT           NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL,
    category    VARCHAR(60)   DEFAULT 'General',
    price       DOUBLE        NOT NULL DEFAULT 0.00,
    quantity    INT           NOT NULL DEFAULT 0,
    min_stock   INT           NOT NULL DEFAULT 10,
    PRIMARY KEY (med_id)
);

-- ─────────────────────────────────────────────
--  TABLE: bills  (one row per bill)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bills (
    bill_id     INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    bill_date   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DOUBLE      NOT NULL DEFAULT 0.00,
    PRIMARY KEY (bill_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────
--  TABLE: bill_items  (line items per bill)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bill_items (
    item_id    INT    NOT NULL AUTO_INCREMENT,
    bill_id    INT    NOT NULL,
    med_id     INT    NOT NULL,
    med_name   VARCHAR(100) NOT NULL,
    unit_price DOUBLE NOT NULL,
    quantity   INT    NOT NULL,
    subtotal   DOUBLE NOT NULL,
    PRIMARY KEY (item_id),
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (med_id)  REFERENCES medicines(med_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────
--  TABLE: sales  (kept for backward compat)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sales (
    sale_id   INT  NOT NULL AUTO_INCREMENT,
    user_id   INT  NOT NULL,
    med_id    INT  NOT NULL,
    quantity  INT  NOT NULL,
    sale_date DATE NOT NULL,
    PRIMARY KEY (sale_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)    ON DELETE CASCADE,
    FOREIGN KEY (med_id)  REFERENCES medicines(med_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────
--  SEED DATA
-- ─────────────────────────────────────────────
INSERT INTO users (name, username, password, role, phone, address)
VALUES ('Administrator', 'admin', 'admin123', 'admin', '0300-0000000', 'AR Pharmacy HQ')
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO medicines (name, category, price, quantity, min_stock) VALUES
  ('Paracetamol 500mg',   'Analgesic',    5.00, 100, 20),
  ('Amoxicillin 250mg',   'Antibiotic',  15.00,  80, 15),
  ('Ibuprofen 400mg',     'Analgesic',    8.00,  60, 10),
  ('Metformin 500mg',     'Diabetes',    12.00,  90, 20),
  ('Atorvastatin 10mg',   'Cardiac',     20.00,  50, 10),
  ('Omeprazole 20mg',     'GI',          10.00,  40, 10),
  ('Cetirizine 10mg',     'Allergy',      6.00,  70, 15),
  ('Azithromycin 250mg',  'Antibiotic',  25.00,   8,  10)
ON DUPLICATE KEY UPDATE med_id = med_id;

SELECT 'AR Pharmacy DB v2.0 setup complete!' AS Status;
