#!/bin/bash
set -e

echo "1. Opening firewall port 5432..."
sudo ufw allow 5432/tcp || echo "UFW might be disabled or rule exists"

echo "2. Configuring PostgreSQL to listen on all interfaces..."
CONF_FILE="/etc/postgresql/17/main/postgresql.conf"
HBA_FILE="/etc/postgresql/17/main/pg_hba.conf"

# Update listen_addresses
sudo sed -i "s/^#listen_addresses = 'localhost'/listen_addresses = '*'/g" $CONF_FILE
sudo sed -i "s/^listen_addresses = 'localhost'/listen_addresses = '*'/g" $CONF_FILE

echo "3. Adding authentication rule to pg_hba.conf..."
if ! sudo grep -q "host all all 0.0.0.0/0 md5" "$HBA_FILE"; then
    echo "host all all 0.0.0.0/0 md5" | sudo tee -a "$HBA_FILE"
fi

echo "4. Restarting PostgreSQL..."
sudo systemctl restart postgresql

echo "Success! Remote access configured for 100.103.214.109."
