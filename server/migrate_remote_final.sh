#!/bin/bash
set -e

DB="goldengoose_db"

echo "1. Adding columns to products..."
sudo -u postgres psql -d $DB -c "ALTER TABLE products ADD COLUMN IF NOT EXISTS option_group_code INTEGER DEFAULT -1;"

echo "2. Creating option_groups table..."
sudo -u postgres psql -d $DB -c "
CREATE TABLE IF NOT EXISTS option_groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    group_code INTEGER UNIQUE,
    is_active BOOLEAN DEFAULT TRUE
);"

echo "3. Creating options table..."
sudo -u postgres psql -d $DB -c "
CREATE TABLE IF NOT EXISTS options (
    id SERIAL PRIMARY KEY,
    group_id INTEGER REFERENCES option_groups(id),
    name VARCHAR(255),
    price_delta_cents INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0
);"

echo "4. Initializing Option Groups (0, 1, 2)..."
sudo -u postgres psql -d $DB -c "
INSERT INTO option_groups (name, group_code) 
VALUES ('Coffee Options', 0), ('Hot/Cold Options', 1), ('Food Options', 2)
ON CONFLICT (group_code) DO NOTHING;"

echo "5. Setting option_group_code for items..."
# Category names in Greek: 'ΚΑΦΕΣ', 'ΡΟΦΗΜΑΤΑ' -> 0
# 'ΦΑΓΗΤΟ' -> 2
sudo -u postgres psql -d $DB -c "
UPDATE products p
SET option_group_code = 0
FROM categories c
WHERE p.category_id = c.id AND c.name IN ('ΚΑΦΕΣ', 'ΡΟΦΗΜΑΤΑ');"

sudo -u postgres psql -d $DB -c "
UPDATE products p
SET option_group_code = 2
FROM categories c
WHERE p.category_id = c.id AND c.name = 'ΦΑΓΗΤΟ';"

echo "Migration Successful!"
