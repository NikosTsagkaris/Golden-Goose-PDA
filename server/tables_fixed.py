from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime, timedelta
from .. import crud, schemas, database, models, auth_utils

router = APIRouter(prefix="/tables", tags=["tables"])

@router.get("/", response_model=List[schemas.Table])
def read_tables(db: Session = Depends(database.get_db)):
    # Get all tables
    db_tables = crud.get_tables(db)
    
    # Enrichment
    results = []
    for t in db_tables:
        active_order = crud.get_open_order_for_table(db, t.id)
        
        t_status = "FREE"
        t_active_id = None
        t_unpaid = 0.0
        t_waiter_id = None
        t_waiter_username = None
        
        if active_order:
            t_active_id = str(active_order.id)
            t_waiter_id = str(active_order.waiter_id)
            t_waiter_username = active_order.waiter.username if active_order.waiter else "N/A"
            
            # Calculate unpaid total
            for line in active_order.lines:
                if not line.paid_status:
                    line_total = (line.unit_price * line.quantity) + line.options_price
                    t_unpaid += line_total
            
            if t_unpaid > 0:
                t_status = "OPEN"
        
        results.append({
            "id": str(t.id),
            "number": t.number,
            "display_name": str(t.number),
            "area": t.area,
            "status": t_status,
            "active_order_id": t_active_id,
            "waiter_id": t_waiter_id,
            "waiter_username": t_waiter_username,
            "unpaid_total": t_unpaid
        })
    
    return results

@router.post("/{table_id}/orders", response_model=schemas.TableOpenOut)
def open_table(
    table_id: int, 
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    # Check if table exists
    table = crud.get_table(db, table_id)
    if not table:
         raise HTTPException(status_code=404, detail="Table not found")
         
    # Check if already open
    active_order = crud.get_open_order_for_table(db, table_id)
    if active_order:
        # Check if the order is stale (empty and > 12 hours old)
        is_empty = len(active_order.lines) == 0
        is_stale = (datetime.utcnow() - active_order.created_at.replace(tzinfo=None)) > timedelta(hours=12)
        
        if is_empty and is_stale:
            # Silently clear the stale empty order to allow a new one
            crud.delete_order(db, active_order.id)
            active_order = None
        else:
            return {"order_id": str(active_order.id), "status": "OPEN"}
        
    # Create new with current user as owner
    new_order = crud.create_order(db, table_id=table_id, waiter_id=current_user.id) 
    return {"order_id": str(new_order.id), "status": "OPEN"}
