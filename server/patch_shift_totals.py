import sys
import os
import re

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# Improved end_shift logic that calculates totals before clearing
new_end_shift = """@router.post("/shifts/end")
def end_shift(db: Session = Depends(get_db), user: dict = Depends(require_user)):
    uid = user["user_id"]
    role = user["role"]
    
    # 1. Fetch current totals before clearing
    if role in ["MANAGER", "ADMIN"]:
        q_totals = text(\"\"\"
            SELECT 
                COALESCE(SUM(CASE WHEN payment_method = 'CASH' THEN unit_price_cents * qty ELSE 0 END), 0) / 100.0 as cash,
                COALESCE(SUM(CASE WHEN payment_method = 'CARD' THEN unit_price_cents * qty ELSE 0 END), 0) / 100.0 as card
            FROM order_lines
            WHERE status = 'PAID'
        \"\"\")
        res_t = db.execute(q_totals).mappings().first()
    else:
        q_totals = text(\"\"\"
            SELECT 
                COALESCE(SUM(CASE WHEN ol.payment_method = 'CASH' THEN ol.unit_price_cents * ol.qty ELSE 0 END), 0) / 100.0 as cash,
                COALESCE(SUM(CASE WHEN ol.payment_method = 'CARD' THEN ol.unit_price_cents * ol.qty ELSE 0 END), 0) / 100.0 as card
            FROM order_lines ol
            JOIN orders o ON ol.order_id = o.id
            WHERE ol.status = 'PAID' AND o.created_by = :uid
        \"\"\")
        res_t = db.execute(q_totals, {"uid": uid}).mappings().first()
    
    cash = float(res_t['cash'] or 0)
    card = float(res_t['card'] or 0)
    total = cash + card
    
    # Format the detail string in Greek
    # M: Μετρητά, K: Κάρτα, Σ: Σύνολο
    totals_str = f"M: {cash:.2f}€, K: {card:.2f}€, Σ: {total:.2f}€"
    
    # 2. Clear items
    if role in ["MANAGER", "ADMIN"]:
        db.execute(text("UPDATE order_lines SET status = 'CLEARED' WHERE status != 'CLEARED'"))
        db.execute(text("UPDATE orders SET status = 'CLEARED' WHERE status = 'OPEN'"))
        db.execute(text("UPDATE table_sessions SET closed_at = NOW() WHERE closed_at IS NULL"))
    else:
        db.execute(text(\"\"\"
            UPDATE order_lines SET status = 'CLEARED' 
            WHERE status != 'CLEARED' AND order_id IN (
                SELECT id FROM orders WHERE created_by = :uid
            )
        \"\"\", {"uid": uid}))
        db.execute(text(\"\"\"
            UPDATE orders SET status = 'CLEARED' 
            WHERE status = 'OPEN' AND created_by = :uid
        \"\"\", {"uid": uid}))
    
    # 3. Create an event log with the totals in payload
    event_id = str(uuid.uuid4())
    db.execute(text(\"\"\"
        INSERT INTO order_events (id, created_by, event_type, payload, created_at)
        VALUES (:eid, :uid, 'SHIFT_END', :payload, NOW())
    \"\"\"), {
        "eid": event_id, 
        "uid": uid, 
        "payload": json.dumps({
            "action": "end_shift", 
            "role": role, 
            "totals": totals_str,
            "cash": cash,
            "card": card,
            "grand_total": total
        })
    })
    
    db.commit()
    return {"status": "success", "cleared_by": role, "final_totals": totals_str}"""

# Replace the entire end_shift function from decorator to return
pattern = r'@router\.post\("/shifts/end"\).*?return \{"status": "success", "cleared_by": role\}'
content = re.sub(pattern, new_end_shift, content, flags=re.DOTALL)

# 4. Make sure get_admin_logs includes the "totals" string if available
old_log_mapping = """        out.append({
            "id": r['id'],
            "waiter_id": r['waiter_id'],
            "action_type": r['action_type'],
            "details": str(r['details']),
            "created_at": str(r['created_at']),
            "waiter": {"username": r['waiter_name']}
        })"""

new_log_mapping = """        details_val = r['details']
        try:
            p = json.loads(details_val) if isinstance(details_val, str) else details_val
            if p.get('totals'):
                details_val = f"Τέλος Βάρδιας ({p['totals']})"
        except: pass

        out.append({
            "id": r['id'],
            "waiter_id": r['waiter_id'],
            "action_type": r['action_type'],
            "details": str(details_val),
            "created_at": str(r['created_at']),
            "waiter": {"username": r['waiter_name']}
        })"""

if old_log_mapping in content:
    content = content.replace(old_log_mapping, new_log_mapping)

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Patch with totals applied successfully.")
