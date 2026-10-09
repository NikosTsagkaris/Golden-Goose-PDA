import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/app/crud.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update get_tables query to use 'OPEN' instead of 'OCCUPIED' and add waiter info
old_sql_tables = """            COALESCE(t.area, '') as area,
            CASE WHEN s.id IS NOT NULL THEN 'OCCUPIED' ELSE 'FREE' END as status,
            (SELECT id::text FROM orders WHERE session_id = s.id AND status = 'OPEN' LIMIT 1) as active_order_id,"""

new_sql_tables = """            COALESCE(t.area, '') as area,
            CASE WHEN s.id IS NOT NULL THEN 'OPEN' ELSE 'FREE' END as status,
            s.opened_by::text as waiter_id,
            u.username as waiter_username,
            (SELECT id::text FROM orders WHERE session_id = s.id AND status = 'OPEN' LIMIT 1) as active_order_id,"""

if old_sql_tables in content:
    content = content.replace(old_sql_tables, new_sql_tables)
    print("Fixed get_tables query status and waiter info.")

# 2. Update get_order_view table status as well
old_view_status = "            COALESCE(t.area, '') as table_area\n        FROM orders o"
new_view_status = "            COALESCE(t.area, '') as table_area,\n            'OPEN' as status\n        FROM orders o"

if old_view_status in content:
    content = content.replace(old_view_status, new_view_status)
    print("Fixed get_order_view status.")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Crud.py fully aligned with app expectations.")
