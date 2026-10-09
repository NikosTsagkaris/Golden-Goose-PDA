from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import crud, database, models, auth_utils
from typing import Optional

router = APIRouter(prefix="/shifts", tags=["shifts"])

@router.get("/totals")
def get_shift_totals(
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """Get shift totals (filtered by waiter if applicable)"""
    waiter_id = current_user.id if current_user.role == "WAITER" else None
    return crud.get_shift_totals(db, waiter_id=waiter_id)

@router.post("/end")
def end_shift(
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """End shift: clear paid orders (filtered by waiter if applicable) and return final totals"""
    waiter_id = current_user.id if current_user.role == "WAITER" else None
    totals = crud.clear_paid_orders(db, waiter_id=waiter_id)
    return {
        "status": "shift_ended",
        "scope": "personal" if waiter_id else "global",
        "final_totals": totals
    }

@router.post("/print-summary")
def print_shift_summary(
    db: Session = Depends(database.get_db),
    current_user: models.User = Depends(auth_utils.get_current_user)
):
    """Generate print job for shift summary (filtered by waiter if applicable)"""
    waiter_id = current_user.id if current_user.role == "WAITER" else None
    totals = crud.get_shift_totals(db, waiter_id=waiter_id)
    
    # Format print content
    header = "ΤΕΛΟΣ ΒΑΡΔΙΑΣ"
    if waiter_id:
        header += f"\n[LB]({current_user.username.upper()})"
    
    content = f"[LB]{header}\n"
    content += "[LB]ΑΝΑΛΥΤΙΚΑ ΣΤΟΙΧΕΙΑ\n"
    content += "==========================================\n"
    content += f"[LB]Παραγγελίες: {totals['order_count']}\n"
    content += "==========================================\n"
    content += f"[LB]Μετρητά:  €{totals['cash']:.2f}\n"
    content += f"[LB]Κάρτα:    €{totals['card']:.2f}\n"
    content += "==========================================\n"
    content += f"[LB]ΣΥΝΟΛΟ:   €{totals['total']:.2f}\n"
    content += "==========================================\n"
    content += "[CUT]\n"
    
    # Create print job (order_id=None for shift summary)
    crud.create_print_job(db, order_id=None, content=content)
    
    return {
        "status": "print_queued",
        "scope": "personal" if waiter_id else "global",
        "totals": totals
    }
