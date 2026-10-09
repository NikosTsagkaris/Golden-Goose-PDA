from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime
from .. import crud, schemas, database, models, auth_utils

router = APIRouter(prefix="/orders", tags=["orders"])

# Static routes MUST come before parameterized routes
@router.get("/open", response_model=List[schemas.Order])
def get_open_orders(
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """Get all open orders with unpaid items (filtered by waiter if applicable)"""
    waiter_id = current_user.id if current_user.role == "WAITER" else None
    return crud.get_open_orders(db, waiter_id=waiter_id)

@router.get("/paid", response_model=List[schemas.Order])
def get_paid_orders(
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """Get all paid orders, most recent first (filtered by waiter if applicable)"""
    waiter_id = current_user.id if current_user.role == "WAITER" else None
    return crud.get_paid_orders(db, waiter_id=waiter_id)

def check_ownership(order: models.Order, current_user: models.User):
    """Raise 403 if user is not the owner and not an admin/manager"""
    if current_user.role not in ["ADMIN", "MANAGER"] and order.waiter_id != current_user.id:
        raise HTTPException(status_code=403, detail="Δεν έχετε δικαίωμα πρόσβασης σε αυτό το τραπέζι (ανήκει σε άλλο σερβιτόρο)")

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
    
    check_ownership(order, current_user)
    
    return crud.add_line_to_order(db, order_id, line)

@router.get("/{order_id}", response_model=schemas.Order)
def get_order(order_id: int, db: Session = Depends(database.get_db)):
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    return order

@router.post("/{order_id}/pay")
def pay_order(
    order_id: int,
    request: schemas.PayLinesRequest,
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """Mark multiple order lines as paid"""
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    
    check_ownership(order, current_user)
    
    paid_count = 0
    for line_id_str in request.line_ids:
        try:
            line_id = int(line_id_str)
            result = crud.pay_order_line(db, line_id, request.method)
            if result:
                paid_count += 1
        except ValueError:
            continue
            
    return {"status": "success", "order_id": str(order_id), "paid_lines": paid_count}

@router.post("/{order_id}/submit")
def submit_order(
    order_id: int, 
    note: str = Query(""), 
    waiter_name: str = Query(""), 
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    check_ownership(order, current_user)

    # Filter for UNPRINTED lines
    new_lines = [l for l in order.lines if not l.is_printed]
    
    if not new_lines and not note:
        return {"status": "no_changes"}

    waiter_name_from_order = order.waiter.full_name if order.waiter else (order.waiter.username if order.waiter else "N/A")
    display_waiter_name = waiter_name if waiter_name else waiter_name_from_order
    
    table_num = order.table.number if order.table else order.table_id
    time_str = datetime.now().strftime('%H:%M')
    if hasattr(order, 'created_at') and order.created_at:
        time_str = order.created_at.strftime('%H:%M')
    
    content = "[LB]ΝΕΑ ΠΑΡΑΓΓΕΛΙΑ\n"
    content += f"[LB]{display_waiter_name}\n"
    content += f"[LB]Τραπέζι {table_num}\n"
    content += f"[LB]Ωρα {time_str}\n"
    content += "------------------------------------------\n"
    
    for line in new_lines:
        content += f"[LB]{line.quantity}x {line.product_name}\n"
        
        if line.options_text:
            options = line.options_text.split(',')
            for opt in options:
                clean_opt = opt.strip()
                if clean_opt:
                    content += f"[LB]-{clean_opt}\n"
                    
        content += "------------------------------------------\n"
        
        line.is_printed = True
        
    if note:
        content += f"[LB]Σχόλιο: {note}\n"
    
    content += "[CUT]\n"
    
    # Store print job
    crud.create_print_job(db, order_id, content)
    db.commit()
    
    return {"status": "submitted", "printed_lines": len(new_lines)}

@router.delete("/{order_id}")
def delete_order(
    order_id: int, 
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """Delete an order and all its lines"""
    order = crud.get_order(db, order_id)
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
        
    check_ownership(order, current_user)
    
    success = crud.delete_order(db, order_id, performer_id=current_user.id)
    return {"status": "deleted", "order_id": str(order_id)}
