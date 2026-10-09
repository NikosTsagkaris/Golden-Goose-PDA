import sys
import os
import uuid
import json

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add import if missing
if 'from app.auth import require_user' not in content:
    content = content.replace(
        'from app.db import get_db',
        'from app.db import get_db\nfrom app.auth import require_user'
    )

# 2. Replace the end_shift stub
old_func = """@router.post("/shifts/end")
def end_shift(db: Session = Depends(get_db)):
    return {"status": "success"}"""

new_func = """@router.post("/shifts/end")
def end_shift(db: Session = Depends(get_db), user: dict = Depends(require_user)):
    uid = user["user_id"]
    role = user["role"]
    
    if role in ["MANAGER", "ADMIN"]:
        # Manager clears ALL paid lines
        db.execute(text("UPDATE order_lines SET status = 'CLEARED' WHERE status = 'PAID'"))
    else:
        # Waiter clears only THEIR OWN paid lines
        db.execute(text(\"\"\"
            UPDATE order_lines SET status = 'CLEARED' 
            WHERE status = 'PAID' AND order_id IN (
                SELECT id FROM orders WHERE created_by = :uid
            )
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

if old_func in content:
    content = content.replace(old_func, new_func)
    with open(target_file, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patch applied successfully")
else:
    # Try a more flexible search if exact match fails
    import re
    pattern = r'@router\.post\("/shifts/end"\)\s+def end_shift\(db: Session = Depends\(get_db\)\):\s+return \{"status": "success"\}'
    if re.search(pattern, content):
        content = re.sub(pattern, new_func, content)
        with open(target_file, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Patch applied via regex successfully")
    else:
        print("Error: Could not find the end_shift stub to replace")
        sys.exit(1)
