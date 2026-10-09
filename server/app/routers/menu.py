from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from .. import crud, schemas, database

router = APIRouter(prefix="/menu", tags=["menu"])

@router.get("/categories", response_model=List[schemas.Category])
def get_menu(db: Session = Depends(database.get_db)):
    """Fetch the full menu structure (categories with products)"""
    return crud.get_categories(db)

@router.get("/products", response_model=List[schemas.Product])
def get_products(category_id: int = None, db: Session = Depends(database.get_db)):
    if category_id:
        return crud.get_products_by_category(db, category_id)
    return [] # Or return all products if needed
