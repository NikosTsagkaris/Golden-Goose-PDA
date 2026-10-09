#!/bin/bash
set -e

echo "1. Stopping services..."
sudo systemctl stop brikipos-api.service brikipos-worker.service || echo "Services already stopped"

echo "2. Renaming briki_pos to goldengoose_db..."
# Use -c to run the command. Need to ensure no connections are active.
sudo -u postgres psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'briki_pos';"
sudo -u postgres psql -c "ALTER DATABASE briki_pos RENAME TO goldengoose_db;"

echo "3. Renaming ntvelop_db to ntvelop_db_old (to avoid conflict if needed, though plan said drop)..."
# Actually, the plan said drop ntvelop_db and pos_db.
echo "4. Dropping obsolete databases..."
sudo -u postgres psql -c "DROP DATABASE IF EXISTS ntvelop_db;"
sudo -u postgres psql -c "DROP DATABASE IF EXISTS pos_db;"

echo "Database migration complete."
