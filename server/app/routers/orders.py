from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from .. import crud, schemas, database, models, auth_utils

router = APIRouter(prefix="/orders", tags=["orders"])

@router.post("/{order_id}/lines", response_model=schemas.OrderLine)
def add_line(
    order_id: int, 
    line: schemas.OrderLineCreate, 
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    if order.status != models.OrderStatus.OPEN:
        raise HTTPException(status_code=400, detail="Order is closed")
        
    return crud.add_line_to_order(db, order_id, line)

@router.get("/{order_id}", response_model=schemas.Order)
def get_order(order_id: int, db: Session = Depends(database.get_db)):
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    return order

@router.post("/{order_id}/submit")
def submit_order(order_id: int, note: str = "", waiter_name: str = "", db: Session = Depends(database.get_db)):
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    # Construct the print content with Markers for the worker
    # [LB] = Large + Bold
    # [B] = Bold
    # [CUT] = Cut paper
    
    waiter_name_from_order = order.waiter.full_name if order.waiter else "N/A"
    display_waiter_name = waiter_name if waiter_name else waiter_name_from_order
    table_num = order.table.number if order.table else order.table_id
    time_str = order.created_at.strftime('%H:%M')
    
    content = "[LB]ΝΕΑ ΠΑΡΑΓΓΕΛΙΑ\n"
    content += f"[LB]{display_waiter_name}\n"
    content += f"[LB]Τραπέζι {table_num}\n"
    content += f"[LB]Ωρα {time_str}\n"
    content += "------------------------------------------\n"
    
    for line in order.lines:
        content += f"[LB]{line.quantity}x {line.product_name}\n"
        if line.options_text:
            content += f"[LB]{line.options_text}\n"
        content += "------------------------------------------\n"
        
    if note:
        content += f"[LB]Σχόλιο: {note}\n"
    
    content += "[CUT]\n"
    
    crud.create_print_job(db, order_id, content)
    
    return {"status": "submitted"}

@router.post("/{order_id}/pay")
def pay_line(order_id: int, payment: schemas.PaymentCreate, db: Session = Depends(database.get_db)):
    # Verify order
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
        
    paid = crud.pay_order_line(db, payment.line_id, payment.amount, payment.method)
    if not paid:
         raise HTTPException(status_code=400, detail="Payment failed or line not found")
         
    # Check close
    crud.check_and_close_order(db, order_id)
    
    return {"status": "paid"}

@router.get("/open", response_model=List[schemas.Order])
def get_open_orders(db: Session = Depends(database.get_db)):
    return crud.get_open_orders(db)

@router.get("/paid", response_model=List[schemas.Order])
def get_paid_orders(db: Session = Depends(database.get_db)):
    return crud.get_paid_orders(db)
