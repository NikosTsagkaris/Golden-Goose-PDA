from fastapi import APIRouter, Depends, HTTPException, Body
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

# --- CONFIG ---
DEFAULT_USER_ID = "f6335b2c-ddbb-4616-ab72-dd67841843b2"

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

# --- Helper for getting orders by status ---
def fetch_orders_by_status(status_filter: Optional[str], db: Session):
    filter_clause = ""
    params = {}
    if status_filter:
        filter_clause = "AND o.status = :s"
        params["s"] = status_filter
    
    q = text(f"""
        SELECT o.id, t.display_name as table_name, o.status, o.created_at,
               COALESCE(SUM(ol.unit_price_cents * ol.qty), 0) / 100.0 as total,
               json_agg(json_build_object(
                   'id', ol.id, 'order_id', ol.order_id, 'status', ol.status, 'payment_method', ol.payment_method, 
                   'unit_price', ol.unit_price_cents / 100.0,
                   'product_name', ol.item_name, 'quantity', ol.qty
               )) as lines
        FROM orders o
        JOIN table_sessions ts ON ts.id = o.session_id
        JOIN dining_tables t ON t.id = ts.table_id
        LEFT JOIN order_lines ol ON ol.order_id = o.id AND ol.is_voided = FALSE
        WHERE 1=1 {filter_clause}
        GROUP BY o.id, t.display_name, o.status, o.created_at
        ORDER BY o.created_at DESC
        LIMIT 50
    """)
    
    rows = db.execute(q, params).mappings().all()
    res = []
    for r in rows:
        lines_data = r['lines'] if r['lines'] else []
        valid_lines = [l for l in lines_data if l and l.get('id')]
        res.append({
            "id": r['id'],
            "table_id": r['table_name'], 
            "status": r['status'],
            "total": float(r['total']),
            "lines": [
                {
                    "id": l['id'], "order_id": l['order_id'], "product_name": l['product_name'], "quantity": l['quantity'],
                    "unit_price": l['unit_price'], "options_text": "", "options_price": 0.0, "note": "", 
                    "paid_status": (l['status'] == 'PAID'), "payment_method": l.get('payment_method')
                }
                for l in valid_lines
            ], 
            "created_at": str(r['created_at'])
        })
    return res

# --- Endpoints ---

@router.get("/tables")
@router.get("/pos/tables")
def get_tables(db: Session = Depends(get_db)):
    try:
        q = text("""
            SELECT t.id, t.display_name, t.is_active, 
                   (SELECT o.id FROM orders o 
                    JOIN table_sessions ts ON ts.id = o.session_id 
                    WHERE ts.table_id = t.id AND o.status = 'OPEN' AND ts.closed_at IS NULL 
                    LIMIT 1) as active_order_id
            FROM dining_tables t
            ORDER BY t.id
        """)
        res = db.execute(q).mappings().all()
        
        out = []
        for r in res:
            out.append({
                "id": str(r['id']),
                "display_name": r['display_name'],
                "status": "OPEN" if r['is_active'] else "CLOSED",
                "area": "",
                "unpaid_total": 0.0,
                "waiter_username": "Staff",
                "active_order_id": r['active_order_id']
            })
        return out
    except Exception as e:
        print(f"Error in get_tables: {e}")
        return []

@router.post("/tables/{table_id}/orders")
def open_order(table_id: str, db: Session = Depends(get_db)):
    q_sess = text("SELECT id FROM table_sessions WHERE table_id = :tid AND closed_at IS NULL LIMIT 1")
    sess = db.execute(q_sess, {"tid": table_id}).mappings().first()
    
    sess_id = None
    if sess:
        sess_id = sess['id']
    else:
        sess_id = str(uuid.uuid4())
        q_create_sess = text("INSERT INTO table_sessions (id, table_id, opened_at) VALUES (:sid, :tid, NOW())")
        db.execute(q_create_sess, {"sid": sess_id, "tid": table_id})
    
    q_ord = text("SELECT id, status FROM orders WHERE session_id = :sid AND status = 'OPEN' LIMIT 1")
    order = db.execute(q_ord, {"sid": sess_id}).mappings().first()
    
    order_id = None
    status = "OPEN"
    
    if order:
        order_id = order['id']
        status = order['status']
    else:
        order_id = str(uuid.uuid4())
        q_create_order = text("INSERT INTO orders (id, session_id, status, created_by, created_at) VALUES (:oid, :sid, 'OPEN', :uid, NOW())")
        db.execute(q_create_order, {"oid": order_id, "sid": sess_id, "uid": DEFAULT_USER_ID})
        
    db.commit()
    return {"order_id": order_id, "status": status}

# IMPORTANT: Specific routes first
@router.get("/orders/open")
def get_open_orders(db: Session = Depends(get_db)):
    return fetch_orders_by_status("OPEN", db)

@router.get("/orders/paid")
def get_paid_orders(db: Session = Depends(get_db)):
    return fetch_orders_by_status("PAID", db)

@router.get("/orders/{order_id}")
def get_order(order_id: str, db: Session = Depends(get_db)):
    # If order_id is 'open' or 'paid' it means routing reached here by mistake, but we handle it just in case
    if order_id in ["open", "paid"]:
        return fetch_orders_by_status(order_id.upper(), db)

    q = text("""
        SELECT o.id, ts.table_id, t.display_name as table_name, o.status, o.created_at 
        FROM orders o 
        JOIN table_sessions ts ON ts.id = o.session_id
        JOIN dining_tables t ON t.id = ts.table_id
        WHERE o.id = :oid
    """)
    order = db.execute(q, {"oid": order_id}).mappings().first()
    if not order: raise HTTPException(404, "Order not found")
    
    q_lines = text("""
        SELECT id, order_id, item_id, item_name, qty, unit_price_cents, options_json, status, note, is_plastic_cup
        FROM order_lines WHERE order_id = :oid AND is_voided = FALSE
    """)
    lines = db.execute(q_lines, {"oid": order_id}).mappings().all()
    
    out_lines = []
    for l in lines:
        price = (l['unit_price_cents'] or 0) / 100.0
        out_lines.append({
            "id": l['id'],
            "order_id": l['order_id'],
            "product_name": l['item_name'],
            "quantity": l['qty'],
            "unit_price": price,
            "options_text": "",
            "options_price": 0.0,
            "note": l['note'],
            "paid_status": (l['status'] == 'PAID'),
            "payment_method": None
        })

    return {
        "id": order['id'],
        "table_id": order['table_name'],
        "status": order['status'],
        "created_at": str(order['created_at']),
        "lines": out_lines,
        "table": {
            "id": order['table_id'], 
            "display_name": order['table_name'],
            "status": order['status']
        }
    }

@router.post("/orders/{order_id}/lines")
def add_line(order_id: str, req: AddLineRequest, db: Session = Depends(get_db)):
    q_sess = text("SELECT session_id FROM orders WHERE id = :oid")
    session_id = db.execute(q_sess, {"oid": order_id}).scalar()
    if not session_id:
        raise HTTPException(404, "Order not found or has no session")

    line_id = str(uuid.uuid4())
    opts_json = {"text": req.options_text, "price_added": req.options_price}
    unit_price_cents = int(req.unit_price * 100)
    
    q_ins = text("""
        INSERT INTO order_lines (id, order_id, session_id, item_id, item_name, qty, unit_price_cents, options_json, note, is_plastic_cup, status, created_at)
        VALUES (:lid, :oid, :sid, :iid, :iname, :qty, :price, :opts, :note, :cup, 'PENDING', NOW())
    """)
    db.execute(q_ins, {
        "lid": line_id, "oid": order_id, "sid": session_id, "iid": req.item_id, "iname": req.product_name,
        "qty": req.quantity, "price": unit_price_cents, "opts": json.dumps(opts_json),
        "note": req.note, "cup": req.is_plastic_cup
    })
    db.commit()
    return {"id": line_id}

@router.post("/orders/{order_id}/submit")
def submit_order(order_id: str, printer_ip: Optional[str] = None, db: Session = Depends(get_db)):
    q_o = text("""
        SELECT o.id, t.display_name as table_name, o.created_at
        FROM orders o
        JOIN table_sessions ts ON ts.id = o.session_id
        JOIN dining_tables t ON t.id = ts.table_id
        WHERE o.id = :oid
    """)
    order = db.execute(q_o, {"oid": order_id}).mappings().first()
    if not order: raise HTTPException(404, "Order not found")
    
    q_l = text("""
        SELECT item_name, qty, options_json, note 
        FROM order_lines WHERE order_id = :oid AND is_voided = FALSE
    """)
    lines = db.execute(q_l, {"oid": order_id}).mappings().all()
    
    time_str = datetime.datetime.now().strftime("%H:%M")
    header = f"TABLE: {order['table_name']}\nWAITER: Staff\nTIME: {time_str}\n"
    body = ""
    for l in lines:
        body += f"{l['qty']}x {l['item_name']}\n"
        try:
            opts = json.loads(l['options_json']) if isinstance(l['options_json'], str) else l['options_json']
            if opts and isinstance(opts, dict) and opts.get('text'):
                body += f"   {opts['text']}\n"
        except: pass
        if l['note']: body += f"   (Note: {l['note']})\n"
            
    full_text = header + "\n" + body + "\n\n"
    job_id = str(uuid.uuid4())
    q_job = text("""
        INSERT INTO print_jobs (id, order_id, status, payload_text, printer_ip, created_at, next_retry_at) 
        VALUES (:jid, :oid, 'PENDING', :txt, :pip, NOW(), NOW())
    """)
    db.execute(q_job, {"jid": job_id, "oid": order_id, "txt": full_text, "pip": printer_ip})
    db.commit()
    return {"status": "submitted", "job_id": job_id}

@router.get("/pos/orders")
def get_orders(status: Optional[str] = None, db: Session = Depends(get_db)):
    return fetch_orders_by_status(status, db)
