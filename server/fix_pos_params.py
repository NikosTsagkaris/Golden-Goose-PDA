import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix the broken text() calls
broken1 = 'db.execute(text("""\n            UPDATE order_lines SET status = \'CLEARED\' \n            WHERE status = \'PAID\' AND order_id IN (\n                SELECT id FROM orders WHERE created_by = :uid\n            )\n        """, {"uid": uid}))'

fixed1 = 'db.execute(text("""\n            UPDATE order_lines SET status = \'CLEARED\' \n            WHERE status = \'PAID\' AND order_id IN (\n                SELECT id FROM orders WHERE created_by = :uid\n            )\n        """), {"uid": uid})'

broken2 = 'db.execute(text("""\n        INSERT INTO order_events (id, created_by, event_type, payload, created_at)\n        VALUES (:eid, :uid, \'SHIFT_END\', :payload, NOW())\n    """, {\n        "eid": event_id, \n        "uid": uid, \n        "payload": json.dumps({"action": "end_shift", "role": role})\n    }))'

fixed2 = 'db.execute(text("""\n        INSERT INTO order_events (id, created_by, event_type, payload, created_at)\n        VALUES (:eid, :uid, \'SHIFT_END\', :payload, NOW())\n    """), {\n        "eid": event_id, \n        "uid": uid, \n        "payload": json.dumps({"action": "end_shift", "role": role})\n    })'

if broken1 in content:
    content = content.replace(broken1, fixed1)
    print("Fixed broken1")

if broken2 in content:
    content = content.replace(broken2, fixed2)
    print("Fixed broken2")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Pos.py corrected.")
