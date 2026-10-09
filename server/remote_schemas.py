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

class TableSyncIn(BaseModel):
    count: int

# Additional schemas required by main.py
class PinLoginIn(BaseModel):
    pin: str

class TableOut(BaseModel):
    id: str
    display_name: str
    area: Optional[str] = None
    status: str = 'FREE'
    active_waiter_name: Optional[str] = None
    unpaid_total: float = 0.0
    active_order_id: Optional[str] = None

class CreateOrderOut(BaseModel):
    order_id: str
    status: str

class OrderLineAddIn(BaseModel):
    item_id: str
    qty: int
    selected_options: List[str] = []

class OrderLineAddOut(BaseModel):
    id: str

class OrderViewOut(BaseModel):
    id: str
    session_id: str
    status: str
    created_at: str

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
