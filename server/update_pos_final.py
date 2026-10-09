import sys
import os

path = '/home/ntvelop/GoldenGooseServer/app/pos.py'
if not os.path.exists(path):
    print(f"File not found: {path}")
    sys.exit(1)

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# I will overwrite pos.py with a fully corrected version that I know is robust.
# This is safer than incremental patching at this point.

new_content = """from fastapi import APIRouter, Depends, HTTPException, Body
from sqlalchemy.orm import Session
from sqlalchemy import text
from typing import List, Optional
from pydantic import BaseModel
import uuid
import datetime
import json

try:
    from app.db import get_db
except ImportError:
    def get_db(): yield None

router = APIRouter(tags=["POS"])

# --- DTOs ---
class AddLineRequest(BaseModel):
    item_id: Optional[str] = ""
    product_name: str
    quantity: int
    unit_price: float
    options_text: Optional[str] = ""
    options_json: Optional[dict] = {}
    options_price: float = 0.0
    note: Optional[str] = ""
    is_plastic_cup: bool = False
    selected_options: List[str] = [] 

class PayLinesRequest(BaseModel):
    line_ids: List[str]
    method: str 

# --- Endpoints ---

@router.get("/pos/tables")
def get_tables(db: Session = Depends(get_db)):
    try:
        res = db.execute(text("SELECT id, name, status, active_waiter_name, unpaid_total FROM tables ORDER BY id")).mappings().all()
        return res
    except:
        return []

@router.post("/tables/{table_id}/orders")
def open_order(table_id: str, db: Session = Depends(get_db)):
    # 1. Check Active Session
    q_sess = text("SELECT id FROM table_sessions WHERE table_id = :tid AND closed_at IS NULL LIMIT 1")
    sess = db.execute(q_sess, {"tid": table_id}).mappings().first()
    
    sess_id = None
    if sess:
        sess_id = sess['id']
    else:
        sess_id = str(uuid.uuid4())
        q_create_sess = text("INSERT INTO table_sessions (id, table_id, opened_at) VALUES (:sid, :tid, NOW())")
        db.execute(q_create_sess, {"sid": sess_id, "tid": table_id})
    
    # 2. Check Open Order
    q_ord = text("SELECT id, status FROM orders WHERE session_id = :sid AND status = 'OPEN' LIMIT 1")
    order = db.execute(q_ord, {"sid": sess_id}).mappings().first()
    
    order_id = None
    status = "OPEN"
    
    if order:
        order_id = order['id']
        status = order['status']
    else:
        order_id = str(uuid.uuid4())
        q_create_order = text("INSERT INTO orders (id, session_id, status, created_at) VALUES (:oid, :sid, 'OPEN', NOW())")
        db.execute(q_create_order, {"oid": order_id, "sid": sess_id})
        
    db.commit()
    return {"order_id": order_id, "status": status}

@router.get("/orders/{order_id}")
def get_order(order_id: str, db: Session = Depends(get_db)):
    q = text(\"\"\"
        SELECT o.id, ts.table_id, t.name as table_name, o.status, o.created_at 
        FROM orders o 
        JOIN table_sessions ts ON ts.id = o.session_id
        JOIN tables t ON t.id = ts.table_id
        WHERE o.id = :oid
    \"\"\")
    order = db.execute(q, {"oid": order_id}).mappings().first()
    if not order: raise HTTPException(404, "Order not found")
    
    q_lines = text(\"\"\"
        SELECT id, item_id, item_name, qty, unit_price_cents, options_json, status, note, is_plastic_cup
        FROM order_lines WHERE order_id = :oid AND is_voided = FALSE
    \"\"\")
    lines = db.execute(q_lines, {"oid": order_id}).mappings().all()
    
    out_lines = []
    grand_total = 0.0
    for l in lines:
        price = l['unit_price_cents'] / 100.0
        out_lines.append({
            "id": l['id'],
            "item_id": l['item_id'],
            "product_name": l['item_name'],
            "quantity": l['qty'],
            "unit_price": price,
            "options_text": "",
            "options_price": 0.0,
            "note": l['note'],
            "is_plastic_cup": l['is_plastic_cup'] or False,
            "status": l['status'],
            "total_price": price * l['qty']
        })
        grand_total += (price * l['qty'])

    return {
        "id": order['id'],
        "table_id": order['table_name'],
        "status": order['status'],
        "total": grand_total,
        "paid_total": 0.0,
        "lines": out_lines,
        "created_at": str(order['created_at']),
        "table": {"id": order['table_id'], "name": order['table_name']}
    }

@router.post("/orders/{order_id}/lines")
def add_line(order_id: str, req: AddLineRequest, db: Session = Depends(get_db)):
    line_id = str(uuid.uuid4())
    opts_json = {"text": req.options_text, "price_added": req.options_price}
    unit_price_cents = int(req.unit_price * 100)
    
    q_ins = text(\"\"\"
        INSERT INTO order_lines (id, order_id, item_id, item_name, qty, unit_price_cents, options_json, note, is_plastic_cup, status, created_at)
        VALUES (:lid, :oid, :iid, :iname, :qty, :price, :opts, :note, :cup, 'PENDING', NOW())
    \"\"\")
    db.execute(q_ins, {
        "lid": line_id, "oid": order_id, "iid": req.item_id, "iname": req.product_name,
        "qty": req.quantity, "price": unit_price_cents, "opts": json.dumps(opts_json),
        "note": req.note, "cup": req.is_plastic_cup
    })
    db.commit()
    return {"status": "added", "line_id": line_id}

@router.post("/orders/{order_id}/submit")
def submit_order(order_id: str, printer_ip: Optional[str] = None, db: Session = Depends(get_db)):
    q_o = text(\"\"\"
        SELECT o.id, t.name as table_name, o.created_at
        FROM orders o
        JOIN table_sessions ts ON ts.id = o.session_id
        JOIN tables t ON t.id = ts.table_id
        WHERE o.id = :oid
    \"\"\")
    order = db.execute(q_o, {"oid": order_id}).mappings().first()
    if not order: raise HTTPException(404, "Order not found")
    
    q_l = text(\"\"\"
        SELECT item_name, qty, options_json, note 
        FROM order_lines WHERE order_id = :oid AND is_voided = FALSE
    \"\"\")
    lines = db.execute(q_l, {"oid": order_id}).mappings().all()
    
    time_str = datetime.datetime.now().strftime(\"%H:%M\")
    header = f"TABLE: {order['table_name']}\\nWAITER: Staff\\nTIME: {time_str}\\n"
    body = ""
    for l in lines:
        body += f"{l['qty']}x {l['item_name']}\\n"
        try:
            opts = json.loads(l['options_json']) if isinstance(l['options_json'], str) else l['options_json']
            if opts and isinstance(opts, dict) and opts.get('text'):
                body += f"   {opts['text']}\\n"
        except: pass
        if l['note']: body += f"   (Note: {l['note']})\\n"
            
    full_text = header + \"\\n\" + body + \"\\n\\n\"
    job_id = str(uuid.uuid4())
    q_job = text(\"\"\"
        INSERT INTO print_jobs (id, order_id, status, payload_text, printer_ip, created_at, next_retry_at) 
        VALUES (:jid, :oid, 'PENDING', :txt, :pip, NOW(), NOW())
    \"\"\")
    db.execute(q_job, {"jid": job_id, "oid": order_id, "txt": full_text, "pip": printer_ip})
    db.commit()
    return {"status": "submitted", "job_id": job_id}
\"\"\"

with open(path, 'w', encoding='utf-8') as f:
    f.write(new_content)
print(\"pos.py updated successfully.\")
"

# I'll use a simpler scp approach instead of write_to_file for the final pos.py script because it's long.
