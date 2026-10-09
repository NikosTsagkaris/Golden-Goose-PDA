from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
import uuid
from datetime import datetime

try:
    from database import get_db
    from models import Order, PrintJob
except ImportError:
    from ..database import get_db
    from ..models import Order, PrintJob

router = APIRouter(tags=["Printing"])

@router.post("/orders/{order_id}/submit")
def submit_order_to_bar(order_id: int, note: str = Query(""), waiter_name: str = Query(""), db: Session = Depends(get_db)):
    order = db.query(Order).get(order_id)
    if not order:
        raise HTTPException(404, detail="Order not found")

    # Generate Content
    full_text = "[LB]ΝΕΑ ΠΑΡΑΓΓΕΛΙΑ\n"
    
    # Waiter
    w_name = waiter_name if waiter_name else (order.waiter.full_name if order.waiter else "N/A")
    full_text += f"[LB]{w_name}\n"
    
    # Table
    t_num = order.table.number if order.table else order.table_id
    full_text += f"[LB]Τραπέζι {t_num}\n"
    
    # Time
    time_str = order.created_at.strftime('%H:%M')
    full_text += f"[LB]Ωρα {time_str}\n"
    full_text += "------------------------------------------\n"
    
    # Lines
    for l in order.lines:
        # Remote model: item_name, qty, options_text, note
        full_text += f"[LB]{l.qty}x {l.item_name}\n"
        if l.options_text:
            full_text += f"[LB]{l.options_text}\n"
        if l.note:
            full_text += f"[LB]Σχόλιο: {l.note}\n"
        full_text += "------------------------------------------\n"

    if note:
        full_text += f"[LB]Σουλτ: {note}\n" # Just distinguishing note

    full_text += "[CUT]\n"

    # Create Job
    job = PrintJob(
        id=str(uuid.uuid4()),
        order_id=str(order_id), # Remote model implies String for order_id? or Int? Local is int. I'll cast to str to be safe if remote allows.
        status='PENDING',
        payload_text=full_text,
        created_at=datetime.now(),
        next_retry_at=datetime.now(),
        attempts=0
    )
    db.add(job)
    db.commit()
    
    return {"status": "queued", "job_id": job.id}
