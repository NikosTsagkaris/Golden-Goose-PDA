from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from pydantic import BaseModel
from .. import crud, schemas, database, models, auth_utils

router = APIRouter(prefix="/tables", tags=["tables"])

class TableSyncRequest(BaseModel):
    count: int

@router.get("/", response_model=List[schemas.Table])
def read_tables(db: Session = Depends(database.get_db)):
    # Get all tables
    db_tables = crud.get_tables(db)
    
    # Enrich with status
    results = []
    for t in db_tables:
        # Find active order
        # Logic: If query is expensive, optimize later. For < 50 tables, this is fine.
        # We look for ANY open order for this table.
        # Ideally we might have 'active_order_id' on the table but we decided against it for normalized state.
        # We'll traverse the relationship or query.
        
        # Optimization: Filter in python from relationship if eager loaded, or query.
        # Given lazy loading default, let's just query to be safe or inspect.
        active_order = crud.get_open_order_for_table(db, t.id)
        
        t_status = "FREE"
        t_active_id = None
        t_unpaid = 0.0
        
        if active_order:
            t_active_id = active_order.id
            # Calculate unpaid total
            for line in active_order.lines:
                if not line.paid_status:
                    line_total = (line.unit_price * line.quantity) + line.options_price
                    t_unpaid += line_total
            
            if t_unpaid > 0:
                t_status = "OPEN"
        
        # We construct the Pydantic model manually or let it validate
        # Because 'status' etc are not on the DB model, we map them.
        results.append({
            "id": t.id,
            "number": t.number,
            "area": t.area,
            "status": t_status,
            "active_order_id": t_active_id,
            "unpaid_total": t_unpaid
        })
    
    return results

@router.post("/{table_id}/orders", response_model=int)
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
        return active_order.id
        
    # Create new
    # We implicitly trust the waiter from the context or the request?
    # Request schema OrderCreate is empty/basic. We should probably get waiter_id from token.
    # For now, let's assume valid waiter_id=1 if not passed, or better, require auth.
    # To follow strict spec, we should use the logged in user.
    # I'll stick to a default or modify schema to accept waiter_id if context missing.
    # But let's assuming we add auth dependency later to all these. 
    # For V1 speed, hardcode waiter_id=1 (Manager) or similar if not in token.
    # I'll modify create_order to take waiter_id.
    
    # TODO: Get real user from token.
    # For now, placeholder '1'.
    new_order = crud.create_order(db, table_id=table_id, waiter_id=current_user.id) 
    return new_order.id

@router.post("/sync")
def sync_tables(
    request: TableSyncRequest,
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """
    Sync the number of tables to match the desired count.
    Only accessible by authenticated users.
    """
    crud.sync_tables(db, request.count)
    return {"status": "ok", "count": request.count}
