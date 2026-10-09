#!/bin/bash
echo "--- CATEGORIES ---"
sudo -u postgres psql -d goldengoose_db -t -c "SELECT name FROM categories;"
echo "--- TABLE LIST ---"
sudo -u postgres psql -d goldengoose_db -c "\dt"
echo "--- OPTION_GROUP_CODE CHECK ---"
sudo -u postgres psql -d goldengoose_db -c "SELECT name, option_group_code FROM catalog_items LIMIT 5;"
