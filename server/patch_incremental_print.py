import sys
import os
import re

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update SQL to fetch only unprinted lines
old_sql = """    q_l = text(\"\"\"
        SELECT item_name, qty, options_json, note 
        FROM order_lines WHERE order_id = :oid AND is_voided = FALSE
    \"\"\")"""

new_sql = """    q_l = text(\"\"\"
        SELECT id, item_name, qty, options_json, note 
        FROM order_lines WHERE order_id = :oid AND is_voided = FALSE AND is_printed = FALSE
    \"\"\")"""

if old_sql in content:
    content = content.replace(old_sql, new_sql)
    print("Updated SQL to filter by is_printed = FALSE.")

# 2. Add empty check and update is_printed status
old_insertion = """    full_text = header + "\\n" + body + "\\n\\n"
    job_id = str(uuid.uuid4())
    q_job = text(\"\"\"
        INSERT INTO print_jobs (id, order_id, status, payload_text, printer_ip, created_at, next_retry_at) 
        VALUES (:jid, :oid, 'PENDING', :txt, :pip, NOW(), NOW())
    \"\"\")
    db.execute(q_job, {"jid": job_id, "oid": order_id, "txt": full_text, "pip": printer_ip})
    db.commit()
    return {"status": "submitted", "job_id": job_id}"""

new_insertion = """    if not lines:
        return {"status": "nothing_new", "message": "All items already printed"}

    full_text = header + "\\n" + body + "\\n\\n"
    job_id = str(uuid.uuid4())
    q_job = text(\"\"\"
        INSERT INTO print_jobs (id, order_id, status, payload_text, printer_ip, created_at, next_retry_at) 
        VALUES (:jid, :oid, 'PENDING', :txt, :pip, NOW(), NOW())
    \"\"\")
    db.execute(q_job, {"jid": job_id, "oid": order_id, "txt": full_text, "pip": printer_ip})
    
    # Mark as printed
    line_ids = [str(l['id']) for l in lines]
    db.execute(text("UPDATE order_lines SET is_printed = TRUE WHERE id IN :ids"), {"ids": tuple(line_ids)})
    
    db.commit()
    return {"status": "submitted", "job_id": job_id}"""

if old_insertion in content:
    content = content.replace(old_insertion, new_insertion)
    print("Added logic to mark lines as printed and avoid printing if empty.")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Patch for incremental printing applied.")
