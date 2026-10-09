from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import crud, database

router = APIRouter(prefix="/shifts", tags=["shifts"])

@router.get("/totals")
def get_shift_totals(db: Session = Depends(database.get_db)):
    """Get current shift totals (cash, card, total)"""
    return crud.get_shift_totals(db)

@router.post("/end")
def end_shift(db: Session = Depends(database.get_db)):
    """End shift: clear paid orders and return final totals"""
    totals = crud.clear_paid_orders(db)
    return {
        "status": "shift_ended",
        "final_totals": totals
    }

@router.post("/print-summary")
def print_shift_summary(db: Session = Depends(database.get_db)):
    """Generate print job for shift summary"""
    totals = crud.get_shift_totals(db)
    
    # Format print content
    content = "[LB]ΤΕΛΟΣ ΒΑΡΔΙΑΣ\n"
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
        "totals": totals
    }
