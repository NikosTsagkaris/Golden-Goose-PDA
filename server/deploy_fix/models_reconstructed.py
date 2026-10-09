from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Boolean, Text, Enum
from sqlalchemy.orm import relationship
from .database import Base
from sqlalchemy.dialects.postgresql import UUID, JSONB
import uuid
from sqlalchemy.sql import func
import enum

class UserRole(str, enum.Enum):
    ADMIN = "ADMIN"
    WAITER = "WAITER"

class OrderStatus(str, enum.Enum):
    OPEN = "OPEN"
    PAID = "PAID"
    # Add other statuses if known, but OPEN/PAID are critical

class PrintJobStatus(str, enum.Enum):
    PENDING = "PENDING"
    PRINTED = "PRINTED"
    FAILED = "FAILED"

class User(Base):
    __tablename__ = "app_users"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    username = Column(String, unique=True)
    pin = Column("pin_hash", String) 
    role = Column(String) 
    is_active = Column(Boolean, default=True)
    
    orders = relationship("Order", back_populates="waiter")

class CatalogItem(Base):
    __tablename__ = "catalog_items"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String)
    base_price_cents = Column(Integer)
    is_active = Column(Boolean, default=True)

class Table(Base):
    __tablename__ = "dining_tables"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    display_name = Column(String)
    # ...

class TableSession(Base):
    __tablename__ = "table_sessions"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    table_id = Column(String, ForeignKey("dining_tables.id"))
    table = relationship("Table")

class Order(Base):
    __tablename__ = "orders"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    session_id = Column(String, ForeignKey("table_sessions.id"))
    created_by = Column(String, ForeignKey("app_users.id"))
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    status = Column(String, default="OPEN")
    
    waiter = relationship("User", back_populates="orders")
    lines = relationship("OrderLine", back_populates="order")
    session = relationship("TableSession")
    
    @property
    def table(self):
        return self.session.table if self.session else None
    
    @property
    def table_id(self):
        return self.table.display_name if self.table else "N/A"

class OrderLine(Base):
    __tablename__ = "order_lines"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    order_id = Column(String, ForeignKey("orders.id"))
    item_id = Column(String, ForeignKey("catalog_items.id"))
    qty = Column(Integer)
    unit_price_cents = Column(Integer)
    options_json = Column(JSONB)
    is_printed = Column(Boolean, default=False)
    
    order = relationship("Order", back_populates="lines")
    item = relationship("CatalogItem")
    
    @property
    def product_name(self):
        return self.item.name if self.item else "Unknown"
    
    @property
    def quantity(self):
        return self.qty
    
    @property
    def options_text(self):
        # Flatten options_json to string for printing
        if not self.options_json:
            return ""
        if isinstance(self.options_json, list):
             # Try to extract names if it's list of objects, or just stringify
             names = []
             for o in self.options_json:
                 if isinstance(o, dict) and 'name' in o:
                     names.append(o['name'])
                 elif isinstance(o, str):
                     names.append(o)
                 else:
                     names.append(str(o))
             return ",".join(names)
        return str(self.options_json)

class PrintJob(Base):
    __tablename__ = "print_jobs"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    order_id = Column(String) 
    content = Column("payload_text", Text)
    status = Column(String, default="PENDING")
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    next_retry_at = Column(DateTime(timezone=True), server_default=func.now())
    attempts = Column(Integer, default=0)
    last_error = Column(String)
