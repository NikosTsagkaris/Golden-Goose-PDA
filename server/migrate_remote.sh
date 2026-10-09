#!/bin/bash
# Schema Update
sudo -u postgres psql -d goldengoose_db -c "ALTER TABLE catalog_items ADD COLUMN IF NOT EXISTS option_group_code INTEGER DEFAULT -1;"
sudo -u postgres psql -d goldengoose_db -c "ALTER TABLE option_groups ADD COLUMN IF NOT EXISTS group_code INTEGER DEFAULT -1;"

# List items to identify IDs for Burgers/Snacks
echo "--- FOOD ITEMS ---"
sudo -u postgres psql -d goldengoose_db -t -c "SELECT c.name as cat, i.name as item, i.id FROM catalog_items i JOIN catalog_categories c ON i.category_id = c.id WHERE c.name IN ('BURGERS / PIZZA', 'SNACKS', 'BRUNCH / SALADS') ORDER BY c.name, i.name;"
