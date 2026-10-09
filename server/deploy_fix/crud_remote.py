from sqlalchemy.orm import Session
from . import models, schemas
from datetime import datetime

# User
def get_user_by_username(db: Session, username: str):
    return db.query(models.User).filter(models.User.username == username).first()

def get_user_by_pin(db: Session, pin: str):
    from . import auth_utils
    # Iterate all active users and verify hash
    users = db.query(models.User).filter(models.User.is_active == True).all()
    for user in users:
        if auth_utils.verify_password(pin, user.pin):
            return user
    return None

def create_user(db: Session, user: schemas.UserCreate):
    # In real app hash password
    db_user = models.User(
        username=user.username,
        hashed_password=user.password, # Plain for now
        pin=user.pin,
        role=user.role,
        full_name=user.full_name
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

# Products & Categories
def get_categories(db: Session):
    return db.query(models.Category).order_by(models.Category.display_order).all()

def create_category(db: Session, category: schemas.CategoryCreate):
    db_cat = models.Category(**category.model_dump())
    db.add(db_cat)
    db.commit()
    db.refresh(db_cat)
    return db_cat

def get_products_by_category(db: Session, category_id: int):
    return db.query(models.Product).filter(models.Product.category_id == category_id).all()

def create_product(db: Session, product: schemas.ProductCreate):
    db_prod = models.Product(**product.model_dump())
    db.add(db_prod)
    db.commit()
    db.refresh(db_prod)
    return db_prod

# Table
def get_tables(db: Session):
    return db.query(models.Table).all()

def get_table(db: Session, table_id: int):
    return db.query(models.Table).filter(models.Table.id == table_id).first()

def create_table(db: Session, table: schemas.TableCreate):
    db_table = models.Table(**table.model_dump())
    db.add(db_table)
    db.commit()
    db.refresh(db_table)
    return db_table

# Order
def get_open_order_for_table(db: Session, table_id: int):
    return db.query(models.Order).filter(
        models.Order.table_id == table_id,
        models.Order.status == models.OrderStatus.OPEN
    ).first()

def create_order(db: Session, table_id: int, waiter_id: int):
    db_order = models.Order(table_id=table_id, waiter_id=waiter_id, status=models.OrderStatus.OPEN)
    db.add(db_order)
    db.commit()
    db.refresh(db_order)
    return db_order

def get_order(db: Session, order_id: int):
    return db.query(models.Order).filter(models.Order.id == order_id).first()

def get_open_orders(db: Session):
    return db.query(models.Order).filter(models.Order.status == models.OrderStatus.OPEN).order_by(models.Order.created_at).all()

def get_paid_orders(db: Session):
    return db.query(models.Order).filter(models.Order.status == models.OrderStatus.PAID).order_by(models.Order.created_at.desc()).all()

# Lines
def add_line_to_order(db: Session, order_id: int, line: schemas.OrderLineCreate):
    db_line = models.OrderLine(
        order_id=order_id,
        product_name=line.product_name,
        quantity=line.quantity,
        unit_price=line.unit_price,
        options_json=line.options_json,
        options_text=line.options_text,
        options_price=line.options_price,
        note=line.note
    )
    db.add(db_line)
    db.commit()
    db.refresh(db_line)
    return db_line

# Payment
def pay_order_line(db: Session, line_id: int, amount: float, method: str):
    line = db.query(models.OrderLine).filter(models.OrderLine.id == line_id).first()
    if not line:
        return None
        
    # Mark line paid
    line.paid_status = True
    
    # Record payment
    payment = models.Payment(line_id=line_id, amount=amount, method=method)
    db.add(payment)
    
    # Check if order is fully paid
    # Refresh logic might be needed or handled by caller, but for now simple check:
    # We commit first to save the payment
    db.commit()
    
    return payment

def check_and_close_order(db: Session, order_id: int):
    order = get_order(db, order_id)
    if not order:
        return
        
    # Check if any line is NOT paid
    has_unpaid = any(not line.paid_status for line in order.lines)
    
    if not has_unpaid and order.lines: # Must have lines to be closed
        order.status = models.OrderStatus.PAID
        db.commit()

# Print Job
def create_print_job(db: Session, order_id: int, content: str):
    job = models.PrintJob(order_id=order_id, content=content, status=models.PrintJobStatus.PENDING)
    db.add(job)
    db.commit()
    return job
