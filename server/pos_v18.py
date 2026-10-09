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

class TableSyncRequest(BaseModel):
    count: int

# --- Helper for getting orders by status ---
def fetch_orders_by_status(status_filter: Optional[str], db: Session):
    filter_clause = ""
    params = {}
    if status_filter:
        filter_clause = "AND o.status = :s"
        params["s"] = status_filter
    
    q = text(f"""
        SELECT o.id, t.code as table_name, o.status, o.created_at,
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
        GROUP BY o.id, t.code, o.status, o.created_at
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
            ORDER BY t.code ASC
        """)
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
        return out
    except Exception as e:
        print(f"Error in get_tables: {e}")
        return []

@router.post("/setup/tables/sync")
def sync_tables(req: TableSyncRequest, db: Session = Depends(get_db)):
    # 1. Mark all as inactive
    db.execute(text("UPDATE dining_tables SET is_active = FALSE"))
    
    # 2. Ensure we have at least 'count' tables active
    for i in range(1, req.count + 1):
        code = f"T{i}"
        # Check if table exists
        q_check = text("SELECT id FROM dining_tables WHERE code = :c")
        res = db.execute(q_check, {"c": code}).mappings().first()
        
        if res:
            db.execute(text("UPDATE dining_tables SET is_active = TRUE WHERE code = :c"), {"c": code})
        else:
            new_id = str(uuid.uuid4())
            db.execute(text("INSERT INTO dining_tables (id, code, display_name, is_active) VALUES (:id, :c, :d, TRUE)"), 
                       {"id": new_id, "c": code, "d": f"Τραπέζι {i}"})
    
    db.commit()
    return {"status": "success", "count": req.count}

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

@router.get("/orders/open")
def get_open_orders(db: Session = Depends(get_db)):
    return fetch_orders_by_status("OPEN", db)

@router.get("/orders/paid")
def get_paid_orders(db: Session = Depends(get_db)):
    return fetch_orders_by_status("PAID", db)

@router.get("/orders/{order_id}")
def get_order(order_id: str, db: Session = Depends(get_db)):
    if order_id in ["open", "paid"]:
        return fetch_orders_by_status(order_id.upper(), db)

    q = text("""
        SELECT o.id, ts.table_id, t.code as table_name, o.status, o.created_at, o.waiter_name
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
        "waiter_name": order['waiter_name'],
        "lines": out_lines,
        "table": {
            "id": order['table_id'], 
            "display_name": order['table_name'],
            "status": order['status']
        }
    }

@router.delete("/orders/{order_id}")
def delete_order(order_id: str, db: Session = Depends(get_db)):
    q_o = text("SELECT session_id FROM orders WHERE id = :oid")
    res_o = db.execute(q_o, {"oid": order_id}).mappings().first()
    if not res_o:
        raise HTTPException(404, "Order not found")
    
    sid = res_o['session_id']
    db.execute(text("UPDATE orders SET status = 'VOIDED' WHERE id = :oid"), {"oid": order_id})
    db.execute(text("UPDATE table_sessions SET closed_at = NOW() WHERE id = :sid"), {"sid": sid})
    db.commit()
    return {"status": "success", "order_id": order_id}

@router.post("/orders/{order_id}/pay")
def pay_order(order_id: str, req: PayLinesRequest, db: Session = Depends(get_db)):
    for lid in req.line_ids:
        db.execute(text("UPDATE order_lines SET status = 'PAID', payment_method = :m WHERE id = :lid"), {"m": req.method, "lid": lid})
    
    q_all = text("SELECT count(*) FROM order_lines WHERE order_id = :oid AND status != 'PAID' AND is_voided = FALSE")
    unpaid_count = db.execute(q_all, {"oid": order_id}).scalar()
    
    if unpaid_count == 0:
        db.execute(text("UPDATE orders SET status = 'PAID' WHERE id = :oid"), {"oid": order_id})
        sid = db.execute(text("SELECT session_id FROM orders WHERE id = :oid"), {"oid": order_id}).scalar()
        if sid:
            db.execute(text("UPDATE table_sessions SET closed_at = NOW() WHERE id = :sid"), {"sid": sid})

    db.commit()
    return {"status": "success"}

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
def submit_order(order_id: str, waiter_name: Optional[str] = None, printer_ip: Optional[str] = None, db: Session = Depends(get_db)):
    # Store the waiter_name from the app
    if waiter_name:
        db.execute(text("UPDATE orders SET waiter_name = :wn WHERE id = :oid"), {"wn": waiter_name, "oid": order_id})
        db.commit()

    q_o = text("""
        SELECT o.id, t.code as table_name, o.created_at, o.waiter_name
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
    w_name = order['waiter_name'] if order['waiter_name'] else "Staff"
    header = f"TABLE: {order['table_name']}\nWAITER: {w_name}\nTIME: {time_str}\n"
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

# --- SHIFT & ADMIN ---

@router.get("/shifts/totals")
def get_shift_totals(db: Session = Depends(get_db)):
    q = text("""
        SELECT 
            COALESCE(SUM(CASE WHEN payment_method = 'CASH' THEN unit_price_cents * qty ELSE 0 END), 0) / 100.0 as cash,
            COALESCE(SUM(CASE WHEN payment_method = 'CARD' THEN unit_price_cents * qty ELSE 0 END), 0) / 100.0 as card,
            COUNT(DISTINCT order_id) as order_count
        FROM order_lines
        WHERE status = 'PAID' AND is_voided = FALSE
    """)
    res = db.execute(q).mappings().first()
    return {
        "cash": float(res['cash']),
        "card": float(res['card']),
        "total": float(res['cash'] + res['card']),
        "order_count": int(res['order_count'])
    }

@router.get("/admin/totals/waiters")
def get_waiter_totals(db: Session = Depends(get_db)):
    q = text("""
        SELECT 
            u.username as waiter_name,
            COALESCE(SUM(CASE WHEN ol.status = 'PAID' AND ol.payment_method = 'CASH' THEN ol.unit_price_cents * ol.qty ELSE 0 END), 0) / 100.0 as cash,
            COALESCE(SUM(CASE WHEN ol.status = 'PAID' AND ol.payment_method = 'CARD' THEN ol.unit_price_cents * ol.qty ELSE 0 END), 0) / 100.0 as card,
            COALESCE(SUM(CASE WHEN ol.status != 'PAID' THEN ol.unit_price_cents * ol.qty ELSE 0 END), 0) / 100.0 as unpaid_total,
            COUNT(DISTINCT ol.order_id) as order_count
        FROM app_users u
        LEFT JOIN orders o ON o.created_by = u.id
        LEFT JOIN order_lines ol ON ol.order_id = o.id AND ol.is_voided = FALSE
        GROUP BY u.username
    """)
    rows = db.execute(q).mappings().all()
    out = []
    for r in rows:
        out.append({
            "waiter_name": r['waiter_name'],
            "cash": float(r['cash']),
            "card": float(r['card']),
            "total": float(r['cash'] + r['card']),
            "unpaid_amount": float(r['unpaid_total']),
            "order_count": int(r['order_count'])
        })
    return out

@router.get("/admin/logs")
def get_admin_logs(db: Session = Depends(get_db)):
    q = text("""
        SELECT e.id, e.created_by as waiter_id, u.username as waiter_name, e.event_type as action_type, e.payload as details, e.created_at
        FROM order_events e
        JOIN app_users u ON u.id = e.created_by
        ORDER BY e.created_at DESC LIMIT 100
    """)
    rows = db.execute(q).mappings().all()
    out = []
    for r in rows:
        out.append({
            "id": r['id'],
            "waiter_id": r['waiter_id'],
            "action_type": r['action_type'],
            "details": str(r['details']),
            "created_at": str(r['created_at']),
            "waiter": r['waiter_name']
        })
    return out

@router.post("/shifts/end")
def end_shift(db: Session = Depends(get_db)):
    return {"status": "success"}

@router.get("/pos/orders")
def get_orders(status: Optional[str] = None, db: Session = Depends(get_db)):
    return fetch_orders_by_status(status, db)
