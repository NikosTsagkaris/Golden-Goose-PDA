import requests
import json
import psycopg2
import sys

# 1. Get an open order ID
try:
    conn = psycopg2.connect("dbname=ntvelop_db user=ntvelop_app password=2105135381 host=localhost")
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM orders WHERE status = 'OPEN' LIMIT 1")
        row = cur.fetchone()
        order_id = row[0] if row else None
    conn.close()
except Exception as e:
    print(f"DB Error: {e}")
    order_id = None

if not order_id:
    print("No open orders found in DB. Please open a table in the app first.")
    sys.exit(0)

print(f"Testing with order_id: {order_id}")

# 2. Test Add Line
url = f"http://localhost:8000/orders/{order_id}/lines"
payload = {
    "item_id": "test-id",
    "product_name": "Test Item",
    "quantity": 1,
    "unit_price": 1.5
}
try:
    resp = requests.post(url, json=payload)
    print(f"Response ({resp.status_code}): {resp.text}")
except Exception as e:
    print(f"Request failed: {e}")
