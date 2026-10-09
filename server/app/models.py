from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, Float, DateTime, Text, JSON
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from .database import Base
import enum

class UserRole(str, enum.Enum):
    WAITER = "WAITER"
    MANAGER = "MANAGER"

class OrderStatus(str, enum.Enum):
    OPEN = "OPEN"
    PAID = "PAID"
    CANCELLED = "CANCELLED"

class PrintJobStatus(str, enum.Enum):
    PENDING = "PENDING"
    SENT = "SENT"
    FAILED = "FAILED"

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    hashed_password = Column(String) # In real app, hash this. For V1 simple pin can be stored or hashed.
    pin = Column(String, unique=True) # Simple PIN login
    role = Column(String, default=UserRole.WAITER)
    full_name = Column(String)

class Category(Base):
    __tablename__ = "categories"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)
    display_order = Column(Integer, default=0)
    
    products = relationship("Product", back_populates="category")

class Product(Base):
    __tablename__ = "products"
    id = Column(Integer, primary_key=True, index=True)
    category_id = Column(Integer, ForeignKey("categories.id"))
    name = Column(String, index=True)
    price = Column(Float)
    is_available = Column(Boolean, default=True)
    
    category = relationship("Category", back_populates="products")

class Table(Base):
    __tablename__ = "tables"
    id = Column(Integer, primary_key=True, index=True)
    number = Column(Integer, unique=True, index=True) # Human readable number
    area = Column(String) # e.g. "Main", "Garden"
    # Status is derived but valid to cache if needed. 
    # For now, we trust relationships to determine if occupied.
    
    orders = relationship("Order", back_populates="table")

class Order(Base):
    __tablename__ = "orders"
    id = Column(Integer, primary_key=True, index=True)
    table_id = Column(Integer, ForeignKey("tables.id"))
    waiter_id = Column(Integer, ForeignKey("users.id"))
    waiter_name = Column(String, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    status = Column(String, default=OrderStatus.OPEN)
    
    table = relationship("Table", back_populates="orders")
    waiter = relationship("User")
    lines = relationship("OrderLine", back_populates="order", cascade="all, delete-orphan")
    print_jobs = relationship("PrintJob", back_populates="order")

class OrderLine(Base):
    __tablename__ = "order_lines"
    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id"))
    
    product_name = Column(String)
    quantity = Column(Integer)
    unit_price = Column(Float)
    
    options_json = Column(JSON, default={}) 
    options_text = Column(String)
    options_price = Column(Float, default=0.0)
    
    note = Column(String, nullable=True)
    
    paid_status = Column(String, default="UNPAID")
    payment_method = Column(String, nullable=True)
    is_plastic_cup = Column(Boolean, default=False)
    is_voided = Column(Boolean, default=False)
    
    order = relationship("Order", back_populates="lines")
    payments = relationship("Payment", back_populates="line")

class Payment(Base):
    __tablename__ = "payments"
    id = Column(Integer, primary_key=True, index=True)
    line_id = Column(Integer, ForeignKey("order_lines.id"))
    amount = Column(Float)
    method = Column(String) # CASH / CARD
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    line = relationship("OrderLine", back_populates="payments")

class PrintJob(Base):
    __tablename__ = "print_jobs"
    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id"))
    content = Column(Text) # The raw text/escpos to print
    status = Column(String, default=PrintJobStatus.PENDING)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    order = relationship("Order", back_populates="print_jobs")

class ActionLog(Base):
    __tablename__ = "action_logs"
    id = Column(Integer, primary_key=True, index=True)
    waiter_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    action_type = Column(String)
    details = Column(String)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    waiter = relationship("User")
