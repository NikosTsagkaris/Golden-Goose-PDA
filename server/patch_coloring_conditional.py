import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update SQL to include has_printed check
old_sql_query = """                   (SELECT COALESCE(SUM(ol.unit_price_cents * ol.qty) / 100.0, 0)
                    FROM order_lines ol
                    JOIN table_sessions ts ON ts.id = ol.session_id
                    WHERE ts.table_id = t.id AND ts.closed_at IS NULL 
                    AND ol.payment_method IS NULL AND ol.is_voided = FALSE) as unpaid_total
            FROM dining_tables t"""

new_sql_query = """                   (SELECT COALESCE(SUM(ol.unit_price_cents * ol.qty) / 100.0, 0)
                    FROM order_lines ol
                    JOIN table_sessions ts ON ts.id = ol.session_id
                    WHERE ts.table_id = t.id AND ts.closed_at IS NULL 
                    AND ol.payment_method IS NULL AND ol.is_voided = FALSE) as unpaid_total,
                   EXISTS (
                       SELECT 1 FROM order_lines ol
                       JOIN table_sessions ts ON ts.id = ol.session_id
                       WHERE ts.table_id = t.id AND ts.closed_at IS NULL AND ol.is_printed = TRUE
                   ) as has_printed
            FROM dining_tables t"""

if old_sql_query in content:
    content = content.replace(old_sql_query, new_sql_query)
    print("Updated SQL query to include has_printed check.")

# 2. Update status determination in the loop
old_status_logic = """        for r in res:
            is_occupied = r['active_order_id'] is not None
            out.append({
                "id": str(r['id']),
                "display_name": r['code'],
                "status": "OPEN" if is_occupied else "FREE","""

new_status_logic = """        for r in res:
            # Table is only "OPEN" (Colored) if items have actually been printed/sent
            is_occupied = r['active_order_id'] is not None
            out.append({
                "id": str(r['id']),
                "display_name": r['code'],
                "status": "OPEN" if (is_occupied and r['has_printed']) else "FREE","""

if old_status_logic in content:
    content = content.replace(old_status_logic, new_status_logic)
    print("Updated status logic to check has_printed.")
else:
    # Try simpler match if needed
    if '"status": "OPEN" if is_occupied else "FREE"' in content:
        content = content.replace('"status": "OPEN" if is_occupied else "FREE"', '"status": "OPEN" if (is_occupied and r["has_printed"]) else "FREE"')
        print("Fallback patch for status logic worked.")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Pos.py coloring logic refined.")
