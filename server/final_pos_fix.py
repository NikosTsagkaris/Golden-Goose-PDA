import sys
import os
import re

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update get_admin_logs to return waiter as object {username: ...}
old_log_item = """        out.append({
            "id": r['id'],
            "waiter_id": r['waiter_id'],
            "action_type": r['action_type'],
            "details": str(r['details']),
            "created_at": str(r['created_at']),
            "waiter": r['waiter_name']
        })"""

new_log_item = """        out.append({
            "id": r['id'],
            "waiter_id": r['waiter_id'],
            "action_type": r['action_type'],
            "details": str(r['details']),
            "created_at": str(r['created_at']),
            "waiter": {"username": r['waiter_name']}
        })"""

if old_log_item in content:
    content = content.replace(old_log_item, new_log_item)
    print("Fixed log format.")

# 2. Update get_waiter_totals to only count PENDING for unpaid_total
old_unpaid = "COALESCE(SUM(CASE WHEN ol.status != 'PAID' THEN ol.unit_price_cents * ol.qty ELSE 0 END), 0) / 100.0 as unpaid_total,"
new_unpaid = "COALESCE(SUM(CASE WHEN ol.status = 'PENDING' THEN ol.unit_price_cents * ol.qty ELSE 0 END), 0) / 100.0 as unpaid_total,"

if old_unpaid in content:
    content = content.replace(old_unpaid, new_unpaid)
    print("Fixed waiter totals query.")

# 3. Update end_shift to clear sessions, orders and all lines
new_end_shift = """@router.post("/shifts/end")
def end_shift(db: Session = Depends(get_db), user: dict = Depends(require_user)):
    uid = user["user_id"]
    role = user["role"]
    
    if role in ["MANAGER", "ADMIN"]:
        # Thorough reset for Manager
        db.execute(text("UPDATE order_lines SET status = 'CLEARED' WHERE status != 'CLEARED'"))
        db.execute(text("UPDATE orders SET status = 'CLEARED' WHERE status = 'OPEN'"))
        db.execute(text("UPDATE table_sessions SET closed_at = NOW() WHERE closed_at IS NULL"))
    else:
        # Waiter clears only their own lines/orders
        # Note: We don't close sessions for waiters to avoid affecting others, but clear the lines
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
    
    # Create an event log
    event_id = str(uuid.uuid4())
    db.execute(text(\"\"\"
        INSERT INTO order_events (id, created_by, event_type, payload, created_at)
        VALUES (:eid, :uid, 'SHIFT_END', :payload, NOW())
    \"\"\"), {
        "eid": event_id, 
        "uid": uid, 
        "payload": json.dumps({"action": "end_shift", "role": role})
    })
    
    db.commit()
    return {"status": "success", "cleared_by": role}"""

content = re.sub(r'@router\.post\("/shifts/end"\).*?return \{"status": "success", "cleared_by": role\}', new_end_shift, content, flags=re.DOTALL)
print("Enhanced end_shift logic.")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Patch applied successfully.")
