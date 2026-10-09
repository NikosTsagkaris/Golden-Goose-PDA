from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Boolean, Text, Enum
from sqlalchemy.orm import relationship
from .database import Base
import enum
from sqlalchemy.sql import func

class OrderStatus(str, enum.Enum):
    OPEN = "OPEN"
    CLOSED = "CLOSED"
    CANCELLED = "CANCELLED"

class PrintJobStatus(str, enum.Enum):
    PENDING = "PENDING"
    SENT = "SENT"
    FAILED = "FAILED"

class UserRole(str, enum.Enum):
    ADMIN = "ADMIN"
    WAITER = "WAITER"

class User(Base):
    __tablename__ = "app_users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    full_name = Column(String)
    pin = Column(String)
    role = Column(String) # ADMIN, WAITER
    is_active = Column(Boolean, default=True)
    
    orders = relationship("Order", back_populates="waiter")

class Category(Base):
    __tablename__ = "categories"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True)
    display_order = Column(Integer, default=0)
    products = relationship("Product", back_populates="category")

class Product(Base):
    __tablename__ = "products"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    price = Column(Float)
    description = Column(String, nullable=True)
    category_id = Column(Integer, ForeignKey("categories.id"))
    category = relationship("Category", back_populates="products")

class Table(Base):
    __tablename__ = "tables"
    id = Column(Integer, primary_key=True, index=True)
    number = Column(Integer, unique=True, index=True)
    status = Column(String, default="FREE") # FREE, OPEN
    x = Column(Float, default=0.0)
    y = Column(Float, default=0.0)
    orders = relationship("Order", back_populates="table")

class Order(Base):
    __tablename__ = "orders"
    id = Column(Integer, primary_key=True, index=True)
    table_id = Column(Integer, ForeignKey("tables.id"))
    waiter_id = Column(Integer, ForeignKey("users.id"))
    status = Column(String, default=OrderStatus.OPEN)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    total_amount = Column(Float, default=0.0)
    paid_amount = Column(Float, default=0.0)
    
    table = relationship("Table", back_populates="orders")
    waiter = relationship("User", back_populates="orders")
    lines = relationship("OrderLine", back_populates="order")
    print_jobs = relationship("PrintJob", back_populates="order")

class OrderLine(Base):
    __tablename__ = "order_lines"
    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id"))
    product_name = Column(String)
    quantity = Column(Integer)
    price = Column(Float)
    options_text = Column(String, nullable=True)
    
    # New column for delta printing
    is_printed = Column(Boolean, default=False)
    
    # Status tracking (for payments)
    paid_status = Column(Boolean, default=False)
    
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
