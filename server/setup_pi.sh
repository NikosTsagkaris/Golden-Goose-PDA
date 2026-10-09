#!/bin/bash
set -e

echo "Starting Pi Setup Script..."

# 1. PostgreSQL User & DB
echo "Configuring PostgreSQL..."
sudo -u postgres psql -c "CREATE USER pi WITH PASSWORD 'raspberry';" || echo "User pi might exist"
sudo -u postgres psql -c "ALTER USER pi WITH PASSWORD 'raspberry';"
sudo -u postgres psql -c "ALTER DATABASE goldengoose_pos OWNER TO pi;"

# 2. CUPS Client
echo "Installing CUPS client utilities..."
sudo apt-get install -y cups-client

echo "Current Printers:"
lpstat -p || echo "No printers found."

echo "Setup script finished."
