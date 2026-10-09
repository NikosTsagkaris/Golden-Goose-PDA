#!/bin/bash
# Fix GoldenGoose Server - Run this on the Raspberry Pi

echo "=== Fixing GoldenGoose Server ==="

cd ~/GoldenGooseServer || exit 1

# Stop any running server
echo "Stopping existing server..."
pkill -f uvicorn

# Fix schemas.py
echo "Creating schemas.py..."
cat > app/schemas.py << 'EOF'
from pydantic import BaseModel
from typing import List, Optional, Any, Dict
from datetime import datetime

# Auth
class Token(BaseModel):
    access_token: str
    token_type: str
    role: str
    username: str

class LoginRequest(BaseModel):
    pin: str

class TableSyncIn(BaseModel):
    count: int

# Additional schemas required by main.py
class PinLoginIn(BaseModel):
    pin: str

class TableOut(BaseModel):
    id: str
    label: str
    status: str
    active_waiter_name: Optional[str] = None
    unpaid_total: float

class CreateOrderOut(BaseModel):
    order_id: str
    status: str

class OrderLineAddIn(BaseModel):
    item_id: str
    qty: int
    selected_options: List[str] = []

class OrderLineAddOut(BaseModel):
    id: str

class OrderViewOut(BaseModel):
    id: str
    session_id: str
    status: str
    created_at: str

class MenuItemOptionsOut(BaseModel):
    id: str
    name: str
    price: float

class OptionGroupOut(BaseModel):
    id: str
    name: str
    options: List[MenuItemOptionsOut]

class OptionOut(BaseModel):
    id: str
    name: str
    price: float
EOF

echo "✓ schemas.py created"

# Verify crud.py exists and has sync_tables
if ! grep -q "def sync_tables" app/crud.py; then
    echo "⚠ crud.py missing sync_tables function - this needs to be added manually"
else
    echo "✓ crud.py has sync_tables function"
fi

# Start server
echo "Starting server..."
nohup .venv/bin/python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 > /tmp/server.log 2>&1 &

sleep 5

# Test server
echo "Testing server..."
if curl -s http://localhost:8000/health > /dev/null; then
    echo "✅ Server is running successfully!"
    echo ""
    echo "Server URL: http://192.168.1.50:8000"
    echo ""
    echo "Updated PINs:"
    echo "  waiter: 0000"
    echo "  waiter2: 5555"
    echo "  manager: 1234"
else
    echo "❌ Server failed to start. Check logs:"
    echo "  tail -50 /tmp/server.log"
fi
