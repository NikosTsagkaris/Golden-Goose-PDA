import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update get_tables function with real logic
old_get_tables_func = """def get_tables(db: Session = Depends(get_db)):
    try:
        q = text(\"\"\"
            SELECT t.id, t.code,
                   (SELECT o.id FROM orders o 
                    JOIN table_sessions ts ON ts.id = o.session_id 
                    WHERE ts.table_id = t.id AND o.status = 'OPEN' AND ts.closed_at IS NULL 
                    LIMIT 1) as active_order_id,
                   (SELECT o.waiter_name FROM orders o 
                    JOIN table_sessions ts ON ts.id = o.session_id 
                    WHERE ts.table_id = t.id AND o.status = 'OPEN' AND ts.closed_at IS NULL 
                    LIMIT 1) as waiter_name
            FROM dining_tables t
            WHERE t.is_active = TRUE
            ORDER BY 
                CASE 
                    WHEN t.code ~ '^T[0-9]+$' THEN CAST(SUBSTRING(t.code FROM 2) AS INTEGER)
                    ELSE 999 
                END ASC
        \"\"\")
        res = db.execute(q).mappings().all()
        
        out = []
        for r in res:
            is_occupied = r['active_order_id'] is not None
            out.append({
                "id": str(r['id']),
                "display_name": r['code'],
                "status": "OCCUPIED" if is_occupied else "FREE",
                "area": "",
                "unpaid_total": 0.0,
                "waiter_username": r['waiter_name'] if r['waiter_name'] else "",
                "active_order_id": r['active_order_id']
            })
        return out"""

new_get_tables_func = """def get_tables(db: Session = Depends(get_db)):
    try:
        q = text(\"\"\"
            SELECT t.id, t.code,
                   (SELECT o.id FROM orders o 
                    JOIN table_sessions ts ON ts.id = o.session_id 
                    WHERE ts.table_id = t.id AND o.status = 'OPEN' AND ts.closed_at IS NULL 
                    LIMIT 1) as active_order_id,
                   (SELECT o.waiter_name FROM orders o 
                    JOIN table_sessions ts ON ts.id = o.session_id 
                    WHERE ts.table_id = t.id AND o.status = 'OPEN' AND ts.closed_at IS NULL 
                    LIMIT 1) as waiter_name,
                   (SELECT o.created_by FROM orders o 
                    JOIN table_sessions ts ON ts.id = o.session_id 
                    WHERE ts.table_id = t.id AND o.status = 'OPEN' AND ts.closed_at IS NULL 
                    LIMIT 1) as waiter_id,
                   (SELECT COALESCE(SUM(ol.unit_price_cents * ol.qty) / 100.0, 0)
                    FROM order_lines ol
                    JOIN table_sessions ts ON ts.id = ol.session_id
                    WHERE ts.table_id = t.id AND ts.closed_at IS NULL 
                    AND ol.payment_method IS NULL AND ol.is_voided = FALSE) as unpaid_total
            FROM dining_tables t
            WHERE t.is_active = TRUE
            ORDER BY 
                CASE 
                    WHEN t.code ~ '^T[0-9]+$' THEN CAST(SUBSTRING(t.code FROM 2) AS INTEGER)
                    ELSE 999 
                END ASC
        \"\"\")
        res = db.execute(q).mappings().all()
        
        out = []
        for r in res:
            is_occupied = r['active_order_id'] is not None
            out.append({
                "id": str(r['id']),
                "display_name": r['code'],
                "status": "OPEN" if is_occupied else "FREE",
                "area": "",
                "unpaid_total": float(r['unpaid_total'] or 0),
                "waiter_username": r['waiter_name'] if r['waiter_name'] else "",
                "waiter_id": str(r['waiter_id']) if r['waiter_id'] else None,
                "active_order_id": str(r['active_order_id']) if r['active_order_id'] else None
            })
        return out"""

if old_get_tables_func in content:
    content = content.replace(old_get_tables_func, new_get_tables_func)
    print("Fixed get_tables in pos.py.")
else:
    # Try a slightly looser match if exact fail
    print("Exact match failed, trying alternative...")
    if 'status": "OCCUPIED" if is_occupied else "FREE"' in content:
        content = content.replace('"status": "OCCUPIED" if is_occupied else "FREE"', '"status": "OPEN" if is_occupied else "FREE"')
        content = content.replace('"unpaid_total": 0.0', '"unpaid_total": float(r["unpaid_total"] or 0)')
        print("Patched status and unpaid_total strings found.")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Pos.py final update applied.")
