from pydantic import BaseModel
from typing import List, Optional, Any, Dict
from datetime import datetime

# Token output schema
class TokenOut(BaseModel):
    access_token: str
    token_type: str
    role: str
    username: str
    user_id: str

class LoginRequest(BaseModel):
    pin: str

class LineCreate(BaseModel):
    product_name: str
    quantity: int = 1
    unit_price: float = 0.0
    options_text: Optional[str] = ""
    options_price: Optional[float] = 0.0
    note: Optional[str] = None
    is_plastic_cup: Optional[bool] = False

class TableSyncIn(BaseModel):
    count: int

# Additional schemas required by main.py
class PinLoginIn(BaseModel):
    pin: str

class TableOut(BaseModel):
    id: str
    display_name: str
    area: Optional[str] = None
    status: str
    unpaid_total: float
    waiter_username: Optional[str] = None
    waiter_id: Optional[str] = None
    active_order_id: Optional[str] = None

class CreateOrderOut(BaseModel):
    order_id: str
    status: str

class OrderLineAddIn(BaseModel):
    item_id: str
    product_name: str
    quantity: int
    unit_price: float
    options_json: Optional[Dict[str, Any]] = {}
    options_text: Optional[str] = ""
    options_price: Optional[float] = 0.0
    note: Optional[str] = None

class OrderLineAddOut(BaseModel):
    id: str

class OrderLineViewOut(BaseModel):
    id: str
    order_id: str
    product_name: str
    quantity: int
    unit_price: float
    options_text: Optional[str] = ""
    options_price: Optional[float] = 0.0
    note: Optional[str] = None
    paid_status: bool
    payment_method: Optional[str] = None

class OrderViewOut(BaseModel):
    id: str
    table_id: str
    waiter_id: Optional[str] = None
    status: str
    created_at: str
    table: Optional[TableOut] = None
    lines: List[OrderLineViewOut] = []

class MenuItemOptionsOut(BaseModel):
    id: str
    name: str
    price: float

class OptionGroupOut(BaseModel):
    id: str
    name: str
    options: List[MenuItemOptionsOut]

class OptionOut(BaseModel):
    id: str
    name: str
    price: float

class ProductOut(BaseModel):
    id: str
    name: str
    price: float
    category_id: str
    option_group: Optional[int] = -1

class CategoryOut(BaseModel):
    id: str
    name: str
    display_order: int
    products: List[ProductOut] = []

class DeleteResponse(BaseModel):
    status: str
    order_id: str

