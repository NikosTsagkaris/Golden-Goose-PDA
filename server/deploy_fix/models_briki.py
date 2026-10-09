from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Boolean, Text, Enum, JSON
from sqlalchemy.orm import relationship
from .database import Base
from sqlalchemy.sql import func
import enum

class UserRole(str, enum.Enum):
    ADMIN = "ADMIN"
    WAITER = "WAITER"

class OrderStatus(str, enum.Enum):
    OPEN = "OPEN"
    PAID = "PAID"

class PrintJobStatus(str, enum.Enum):
    PENDING = "PENDING"
    PRINTED = "PRINTED"
    FAILED = "FAILED"

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    pin = Column(String) 
    role = Column(String)
    full_name = Column(String)
    hashed_password = Column(String)
    
    orders = relationship("Order", back_populates="waiter")

class Category(Base):
    __tablename__ = "categories"
    id = Column(Integer, primary_key=True)
    name = Column(String)
    display_order = Column(Integer)
    
    products = relationship("Product", back_populates="category")

class Product(Base):
    __tablename__ = "products"
    id = Column(Integer, primary_key=True)
    category_id = Column(Integer, ForeignKey("categories.id"))
    name = Column(String)
    price = Column(Float)
    
    category = relationship("Category", back_populates="products")
    
class Table(Base):
    __tablename__ = "tables" 
    id = Column(Integer, primary_key=True)
    number = Column(Integer)
    area = Column(String)
    # name = Column(String)

class Order(Base):
    __tablename__ = "orders"
    id = Column(Integer, primary_key=True, index=True)
    table_id = Column(Integer, ForeignKey("tables.id"))
    waiter_id = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    status = Column(String, default="OPEN")
    
    waiter = relationship("User", back_populates="orders")
    lines = relationship("OrderLine", back_populates="order")
    table = relationship("Table")
    
    @property
    def table_name_display(self):
        # Fallback to number or ID
        if self.table:
            return str(self.table.number)
        return f"#{self.table_id}"

class OrderLine(Base):
    __tablename__ = "order_lines"
    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id"))
    product_name = Column(String)
    quantity = Column(Integer)
    unit_price = Column(Float)
    options_json = Column(JSON)
    options_text = Column(String)
    options_price = Column(Float)
    note = Column(String)
    paid_status = Column(Boolean, default=False)
    is_printed = Column(Boolean, default=False)
    payment_method = Column(String)  # CASH or CARD
    
    order = relationship("Order", back_populates="lines")

class PrintJob(Base):
    __tablename__ = "print_jobs"
    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id"), nullable=True)
    content = Column(Text)
    status = Column(String, default="PENDING")
    created_at = Column(DateTime(timezone=True), server_default=func.now())

class Payment(Base):
    __tablename__ = "payments"
    id = Column(Integer, primary_key=True, index=True)
    line_id = Column(Integer, ForeignKey("order_lines.id"))
    amount = Column(Float)
    method = Column(String)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

class ActionLog(Base):
    __tablename__ = "action_logs"
    id = Column(Integer, primary_key=True, index=True)
    waiter_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    action_type = Column(String) # DELETE_ORDER, END_SHIFT, CLEAR_LOGS
    details = Column(Text)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    waiter = relationship("User")

class DeviceLicense(Base):
    __tablename__ = "device_licenses"
    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String, unique=True, index=True)
    activation_date = Column(DateTime(timezone=True), server_default=func.now())
    expiry_date = Column(DateTime(timezone=True))
    is_active = Column(Boolean, default=True)
