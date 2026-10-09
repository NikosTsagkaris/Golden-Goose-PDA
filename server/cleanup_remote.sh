#!/bin/bash
echo "Dropping obsolete databases..."
sudo -u postgres psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname IN ('ntvelop_db', 'pos_db');"
sudo -u postgres psql -c "DROP DATABASE IF EXISTS ntvelop_db;"
sudo -u postgres psql -c "DROP DATABASE IF EXISTS pos_db;"
echo "Restarting services..."
sudo systemctl start brikipos-api.service brikipos-worker.service
echo "Done."
