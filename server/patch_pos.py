import sys
import os

path = '/home/ntvelop/GoldenGooseServer/app/pos.py'
if not os.path.exists(path):
    print(f"File not found: {path}")
    sys.exit(1)

with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    # 1. Update signature
    if 'def submit_order(order_id: str, db: Session = Depends(get_db)):' in line:
        line = line.replace('def submit_order(order_id: str, db: Session = Depends(get_db)):', 
                            'def submit_order(order_id: str, printer_ip: Optional[str] = None, db: Session = Depends(get_db)):')
    
    # 2. Update insert query
    if 'q_job = text("INSERT INTO print_jobs (id, order_id, status, payload_text, created_at, next_retry_at) VALUES (:jid, :oid, \'PENDING\', :txt, NOW(), NOW())")' in line:
        line = line.replace('q_job = text("INSERT INTO print_jobs (id, order_id, status, payload_text, created_at, next_retry_at) VALUES (:jid, :oid, \'PENDING\', :txt, NOW(), NOW())")', 
                            'q_job = text("INSERT INTO print_jobs (id, order_id, status, payload_text, printer_ip, created_at, next_retry_at) VALUES (:jid, :oid, \'PENDING\', :txt, :pip, NOW(), NOW())")')
    
    # 3. Update execute call
    if 'db.execute(q_job, {"jid": job_id, "oid": order_id, "txt": full_text})' in line:
        line = line.replace('db.execute(q_job, {"jid": job_id, "oid": order_id, "txt": full_text})', 
                            'db.execute(q_job, {"jid": job_id, "oid": order_id, "txt": full_text, "pip": printer_ip})')
    
    new_lines.append(line)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
print("Patch applied successfully.")
