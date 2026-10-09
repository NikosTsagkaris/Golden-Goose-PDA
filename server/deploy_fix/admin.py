from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from .. import crud, schemas, database, models, auth_utils

router = APIRouter(prefix="/admin", tags=["admin"])

@router.get("/totals/waiters", response_model=List[schemas.WaiterTotal])
def get_waiter_totals(
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """Get shift totals broken down by waiter (Admin only)"""
    return crud.get_waiter_totals(db)

@router.get("/logs", response_model=List[schemas.ActionLog])
def get_action_logs(
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """Get activity logs (Admin only)"""
    return crud.get_action_logs(db)

@router.delete("/logs")
def clear_logs(
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """Clear activity logs (Admin only)"""
    crud.clear_action_logs(db)
    # Log the clearing action itself
    crud.create_action_log(db, current_user.id, "CLEAR_LOGS", f"Εκκαθάριση ιστορικού ενεργειών από {current_user.username}")
    return {"status": "success", "message": "Logs cleared"}
