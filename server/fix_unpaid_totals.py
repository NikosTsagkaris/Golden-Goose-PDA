import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/app/crud.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update get_tables query to calculate real unpaid_total
old_get_tables = """            (SELECT id::text FROM orders WHERE session_id = s.id AND status = 'OPEN' LIMIT 1) as active_order_id,
            0.0 as unpaid_total -- placeholder for now
        FROM dining_tables t
        LEFT JOIN table_sessions s ON t.id = s.table_id AND s.closed_at IS NULL"""

new_get_tables = """            (SELECT id::text FROM orders WHERE session_id = s.id AND status = 'OPEN' LIMIT 1) as active_order_id,
            COALESCE((
                SELECT SUM(ol.unit_price_cents * ol.qty) / 100.0 
                FROM order_lines ol 
                WHERE ol.session_id = s.id AND ol.payment_method IS NULL AND ol.is_voided = FALSE
            ), 0.0) as unpaid_total
        FROM dining_tables t
        LEFT JOIN table_sessions s ON t.id = s.table_id AND s.closed_at IS NULL"""

if old_get_tables in content:
    content = content.replace(old_get_tables, new_get_tables)
    print("Fixed get_tables query.")

# 2. Update get_order_view table object calculation
old_view_unpaid = "        'unpaid_total': 0.0 # placeholder"
new_view_unpaid = """        'unpaid_total': float(db.execute(text(\"\"\"
            SELECT COALESCE(SUM(unit_price_cents * qty) / 100.0, 0) 
            FROM order_lines WHERE order_id = :oid AND payment_method IS NULL AND is_voided = FALSE
        \"\"\"), {"oid": order_id}).scalar() or 0)"""

if old_view_unpaid in content:
    content = content.replace(old_view_unpaid, new_view_unpaid)
    print("Fixed get_order_view query.")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Crud.py updated with real unpaid totals.")
