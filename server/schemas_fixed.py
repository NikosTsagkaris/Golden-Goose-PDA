from pydantic import BaseModel, Field, field_validator
from typing import List, Optional, Any, Dict
from datetime import datetime
from .models import UserRole, OrderStatus, PrintJobStatus

# Helper to ensure IDs are strings for the Android app
def ensure_str(v):
    if v is None:
        return None
    return str(v)

# Auth
class Token(BaseModel):
    access_token: str
    token_type: str
    role: str
    username: str
    user_id: str
    
    @field_validator("user_id", mode="before")
    @classmethod
    def validate_user_id(cls, v): return ensure_str(v)

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
    id: str
    
    @field_validator("id", mode="before")
    @classmethod
    def validate_id(cls, v): return ensure_str(v)
    
    class Config:
        from_attributes = True

class UserShort(BaseModel):
    username: str
    class Config:
        from_attributes = True

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
    category_id: str
    
    @field_validator("category_id", mode="before")
    @classmethod
    def validate_category_id(cls, v): return ensure_str(v)

class ProductCreate(ProductBase):
    pass

class Product(ProductBase):
    id: str
    option_group: int = Field(-1, alias="option_group_code")

    @field_validator("id", mode="before")
    @classmethod
    def validate_id(cls, v): return ensure_str(v)

    class Config:
        from_attributes = True
        populate_by_name = True

class Category(CategoryBase):
    id: str
    products: List[Product] = []
    
    @field_validator("id", mode="before")
    @classmethod
    def validate_id(cls, v): return ensure_str(v)
    
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
    id: str
    order_id: str
    paid_status: bool
    
    @field_validator("id", "order_id", mode="before")
    @classmethod
    def validate_ids(cls, v): return ensure_str(v)
    
    class Config:
        from_attributes = True

# Payments
class PaymentCreate(BaseModel):
    line_id: str
    amount: float
    method: str # CASH / CARD
    
    @field_validator("line_id", mode="before")
    @classmethod
    def validate_line_id(cls, v): return ensure_str(v)

class Payment(PaymentCreate):
    id: str
    created_at: datetime
    
    @field_validator("id", mode="before")
    @classmethod
    def validate_id(cls, v): return ensure_str(v)
    
    class Config:
        from_attributes = True

class PayLinesRequest(BaseModel):
    line_ids: List[str]
    method: str

# Action Logs
class ActionLog(BaseModel):
    id: str
    waiter_id: Optional[str] = None
    action_type: str
    details: str
    created_at: datetime
    waiter: Optional[UserShort] = None
    
    @field_validator("id", "waiter_id", mode="before")
    @classmethod
    def validate_ids(cls, v): return ensure_str(v)
    
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
    display_name: Optional[str] = None
    area: str

class TableCreate(TableBase):
    pass

class Table(TableBase):
    id: str
    status: str = "FREE" # FREE / OPEN
    active_order_id: Optional[str] = None
    waiter_id: Optional[str] = None
    waiter_username: Optional[str] = None
    unpaid_total: float = 0.0
    
    @field_validator("id", "active_order_id", "waiter_id", mode="before")
    @classmethod
    def validate_ids(cls, v): return ensure_str(v)
    
    class Config:
        from_attributes = True

class TableOpenOut(BaseModel):
    order_id: str
    status: str
    
    @field_validator("order_id", mode="before")
    @classmethod
    def validate_order_id(cls, v): return ensure_str(v)

# Orders
class OrderBase(BaseModel):
    table_id: str
    
    @field_validator("table_id", mode="before")
    @classmethod
    def validate_table_id(cls, v): return ensure_str(v)

class OrderCreate(OrderBase):
    pass

class Order(OrderBase):
    id: str
    waiter_id: str
    status: OrderStatus
    created_at: datetime
    table: Optional[Table] = None
    lines: List[OrderLine] = []
    
    @field_validator("id", "waiter_id", mode="before")
    @classmethod
    def validate_ids(cls, v): return ensure_str(v)
    
    class Config:
        from_attributes = True

class OptionResponse(BaseModel):
    id: str
    name: str
    price: float
    
    @field_validator("id", mode="before")
    @classmethod
    def validate_id(cls, v): return ensure_str(v)
