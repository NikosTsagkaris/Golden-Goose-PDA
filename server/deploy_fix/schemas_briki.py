from pydantic import BaseModel
from typing import List, Optional, Any, Dict
from datetime import datetime
from .models import UserRole, OrderStatus, PrintJobStatus

# Auth
class Token(BaseModel):
    access_token: str
    token_type: str
    role: str
    username: str
    user_id: int

class LoginRequest(BaseModel):
    pin: str

# Users
class UserBase(BaseModel):
    username: str
    full_name: Optional[str] = None
    role: UserRole

class UserCreate(UserBase):
    pin: str
    password: str

class User(UserBase):
    id: int
    
    class Config:
        from_attributes = True

class UserShort(BaseModel):
    username: str
    class Config:
        from_attributes = True

class OpenTableResponse(BaseModel):
    order_id: str
    status: str

# Products & Categories
class CategoryBase(BaseModel):
    name: str
    display_order: int = 0

class CategoryCreate(CategoryBase):
    pass

class ProductBase(BaseModel):
    name: str
    price: float
    is_available: bool = True
    category_id: int

class ProductCreate(ProductBase):
    pass

class Product(ProductBase):
    id: int
    
    class Config:
        from_attributes = True

class Category(CategoryBase):
    id: int
    products: List[Product] = []
    
    class Config:
        from_attributes = True

# Order Lines
class OrderLineBase(BaseModel):
    product_name: str
    quantity: int
    unit_price: float
    options_json: Dict[str, Any] = {}
    options_text: str = ""
    options_price: float = 0.0
    note: Optional[str] = None

class OrderLineCreate(OrderLineBase):
    pass

class OrderLine(OrderLineBase):
    id: int
    order_id: int
    paid_status: bool
    
    class Config:
        from_attributes = True

# Payments
class PaymentCreate(BaseModel):
    line_id: int
    amount: float
    method: str # CASH / CARD

class Payment(PaymentCreate):
    id: int
    created_at: datetime
    
    class Config:
        from_attributes = True

# Action Logs
class ActionLog(BaseModel):
    id: int
    waiter_id: Optional[int] = None
    action_type: str
    details: str
    created_at: datetime
    waiter: Optional[UserShort] = None
    
    class Config:
        from_attributes = True

# Admin Totals
class WaiterTotal(BaseModel):
    waiter_name: str
    cash: float
    card: float
    total: float
    unpaid_amount: float
    order_count: int

class DeviceActivation(BaseModel):
    device_id: str
    activation_code: str

class LicenseStatus(BaseModel):
    device_id: str
    is_active: bool
    expiry_date: Optional[datetime] = None
    days_remaining: int = 0
    message: str

# Tables
class TableBase(BaseModel):
    number: int
    display_name: str
    area: str

class TableCreate(TableBase):
    pass

class Table(TableBase):
    id: int
    status: str = "FREE" # FREE / OPEN
    active_order_id: Optional[int] = None
    waiter_id: Optional[int] = None
    waiter_username: Optional[str] = None
    unpaid_total: float = 0.0
    
    class Config:
        from_attributes = True

# Orders
class OrderBase(BaseModel):
    table_id: int

class OrderCreate(OrderBase):
    pass

class Order(OrderBase):
    id: int
    waiter_id: int
    status: OrderStatus
    created_at: datetime
    table: Optional[Table] = None
    lines: List[OrderLine] = []
    
    class Config:
        from_attributes = True
