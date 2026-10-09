from sqlalchemy.orm import Session
from . import models, schemas
from datetime import datetime
from sqlalchemy import func, case

# User
def get_user_by_username(db: Session, username: str):
    return db.query(models.User).filter(models.User.username == username).first()

def get_user_by_pin(db: Session, pin: str):
    # Plain text pin check for BrikiPOS setup (as verified in Step 2423)
    return db.query(models.User).filter(models.User.pin == pin).first()

def create_user(db: Session, user: schemas.UserCreate):
    pass

# Categories
def get_categories(db: Session):
    return db.query(models.Category).order_by(models.Category.display_order).all()

def get_products_by_category(db: Session, category_id: int):
    return db.query(models.Product).filter(models.Product.category_id == category_id).all()

# Table
def get_tables(db: Session):
    return db.query(models.Table).all()

def get_table(db: Session, table_id: int):
    return db.query(models.Table).filter(models.Table.id == table_id).first()

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

def get_open_orders(db: Session, waiter_id: int = None):
    query = db.query(models.Order).filter(models.Order.status == models.OrderStatus.OPEN)
    if waiter_id:
        query = query.filter(models.Order.waiter_id == waiter_id)
    return query.order_by(models.Order.created_at).all()

def create_print_job(db: Session, order_id: int = None, content: str = ""):
    # Use None for system-level print jobs (like shift summaries)
    actual_order_id = order_id if order_id != 0 else None
    job = models.PrintJob(order_id=actual_order_id, content=content, status="PENDING")
    db.add(job)
    db.commit()
    return job

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

def pay_order_line(db: Session, line_id: int, payment_method: str):
    """Mark an order line as paid and record in payments table"""
    line = db.query(models.OrderLine).filter(models.OrderLine.id == line_id).first()
    if not line:
        return None
    
    # Mark line as paid
    line.paid_status = True
    line.payment_method = payment_method
    
    # Create payment record
    payment = models.Payment(
        line_id=line_id,
        amount=line.unit_price + line.options_price,
        method=payment_method
    )
    db.add(payment)
    db.commit()
    db.refresh(line)
    
    # Check if order should be closed
    check_and_close_order(db, line.order_id)
    
    return line
    
def check_and_close_order(db: Session, order_id: int):
    """Check if all lines in an order are paid, and close the order if so"""
    order = get_order(db, order_id)
    if not order:
        return
    
    # Check if any line is NOT paid
    has_unpaid = any(not line.paid_status for line in order.lines)
    
    if not has_unpaid and order.lines:  # Must have lines to be closed
        order.status = models.OrderStatus.PAID
        db.commit()

def get_paid_orders(db: Session, waiter_id: int = None):
    """Get all paid orders, most recent first"""
    query = db.query(models.Order).filter(models.Order.status == models.OrderStatus.PAID)
    if waiter_id:
        query = query.filter(models.Order.waiter_id == waiter_id)
    return query.order_by(models.Order.created_at.desc()).all()

def delete_order(db: Session, order_id: int, performer_id: int = None):
    """Delete an order and all its lines, print jobs, and payments"""
    order = get_order(db, order_id)
    if not order:
        return False
    
    # Log the action before deletion
    # If performer_id is provided (e.g. from current_user), use it. 
    # Otherwise fallback to the order's original waiter.
    actual_performer_id = performer_id if performer_id is not None else order.waiter_id
    
    performer = db.query(models.User).filter(models.User.id == actual_performer_id).first()
    waiter_name = performer.username if performer else "N/A"
    
    table_num = (order.table.number if order.table else order.table_id)
    status_desc = "ΠΛΗΡΩΜΕΝΗ" if order.status == "PAID" else "ΑΝΟΙΧΤΗ"
    create_action_log(db, actual_performer_id, "DELETE_ORDER", 
                      f"Διαγραφή {status_desc} παραγγελίας #${order_id} (Τραπέζι {table_num}) από {waiter_name}")

    # Get all line IDs for this order
    line_ids = [l.id for l in order.lines]
    
    # 1. Delete associated payments for these lines
    if line_ids:
        db.query(models.Payment).filter(models.Payment.line_id.in_(line_ids)).delete(synchronize_session=False)

    # 2. Delete all associated print jobs
    db.query(models.PrintJob).filter(models.PrintJob.order_id == order_id).delete()
    
    # 3. Delete all order lines
    db.query(models.OrderLine).filter(models.OrderLine.order_id == order_id).delete()
    
    # 4. Delete the order
    db.delete(order)
    db.commit()
    return True

def get_shift_totals(db: Session, waiter_id: int = None):
    """Calculate shift totals from all paid order lines (optionally filtered by waiter)"""
    # Query for paid lines
    query = db.query(models.OrderLine).filter(models.OrderLine.paid_status == True)
    
    # If waiter_id is provided, join with Order table to filter
    if waiter_id:
        query = query.join(models.Order, models.OrderLine.order_id == models.Order.id)\
                     .filter(models.Order.waiter_id == waiter_id)
    
    paid_lines = query.all()
    
    cash_total = sum(
        (line.unit_price + line.options_price) * line.quantity 
        for line in paid_lines 
        if line.payment_method == "CASH"
    )
    
    card_total = sum(
        (line.unit_price + line.options_price) * line.quantity 
        for line in paid_lines 
        if line.payment_method == "CARD"
    )
    
    return {
        "cash": round(cash_total, 2),
        "card": round(card_total, 2),
        "total": round(cash_total + card_total, 2),
        "order_count": len(set(line.order_id for line in paid_lines))
    }

def clear_paid_orders(db: Session, waiter_id: int = None):
    """Clear paid orders and their dependencies (optionally filtered by waiter)"""
    # Get totals before clearing
    totals = get_shift_totals(db, waiter_id=waiter_id)
    
    # Get IDs of paid orders
    query = db.query(models.Order.id).filter(models.Order.status == "PAID")
    if waiter_id:
        query = query.filter(models.Order.waiter_id == waiter_id)
        
    paid_order_ids = [r[0] for r in query.all()]
    
    if not paid_order_ids:
        return totals

    waiter_name = "Global"
    if waiter_id:
        user = db.query(models.User).filter(models.User.id == waiter_id).first()
        waiter_name = user.username if user else str(waiter_id)

    # Log the action
    create_action_log(db, waiter_id, "END_SHIFT", 
                      f"Τέλος βάρδιας ({waiter_name}): Μηδενισμός {len(paid_order_ids)} παραγγελιών. Σύνολο: €{totals['total']:.2f}")

    # Get all line IDs from paid orders
    paid_line_ids = [r[0] for r in db.query(models.OrderLine.id).filter(
        models.OrderLine.order_id.in_(paid_order_ids)
    ).all()]

    # 1. Delete all payments first
    if paid_line_ids:
        db.query(models.Payment).filter(models.Payment.line_id.in_(paid_line_ids)).delete(synchronize_session=False)

    # 2. Delete all print jobs
    db.query(models.PrintJob).filter(models.PrintJob.order_id.in_(paid_order_ids)).delete(synchronize_session=False)
    
    # 3. Delete all lines
    db.query(models.OrderLine).filter(models.OrderLine.order_id.in_(paid_order_ids)).delete(synchronize_session=False)
    
    # 4. Delete the orders
    db.query(models.Order).filter(models.Order.id.in_(paid_order_ids)).delete(synchronize_session=False)
    
    db.commit()
    return totals
    
def create_action_log(db: Session, waiter_id: int, action_type: str, details: str):
    db_log = models.ActionLog(waiter_id=waiter_id, action_type=action_type, details=details)
    db.add(db_log)
    db.commit()
    return db_log

def get_action_logs(db: Session, limit: int = 100):
    return db.query(models.ActionLog).order_by(models.ActionLog.created_at.desc()).limit(limit).all()

def clear_action_logs(db: Session):
    db.query(models.ActionLog).delete()
    db.commit()

def get_waiter_totals(db: Session):
    """Get shift totals broken down by waiter, including unpaid amounts"""
    from sqlalchemy import func
    
    # We join User -> Order -> OrderLine -> Payment (for paid items)
    # AND User -> Order -> OrderLine (for unpaid items)
    
    # Query for PAID totals
    paid_query = db.query(
        models.User.username,
        func.sum(models.Payment.amount).label("paid_total"),
        func.count(func.distinct(models.Order.id)).label("order_count"),
        func.sum(case((models.Payment.method == 'CASH', models.Payment.amount), else_=0)).label("cash_amount"),
        func.sum(case((models.Payment.method == 'CARD', models.Payment.amount), else_=0)).label("card_amount")
    ).select_from(models.User)\
     .outerjoin(models.Order, models.User.id == models.Order.waiter_id)\
     .outerjoin(models.OrderLine, models.Order.id == models.OrderLine.order_id)\
     .outerjoin(models.Payment, models.OrderLine.id == models.Payment.line_id)\
     .filter(models.User.role == 'WAITER')\
     .group_by(models.User.username).all()

    # Query for UNPAID totals
    unpaid_query = db.query(
        models.User.username,
        func.sum((models.OrderLine.unit_price * models.OrderLine.quantity) + models.OrderLine.options_price).label("unpaid_total")
    ).select_from(models.User)\
     .join(models.Order, models.User.id == models.Order.waiter_id)\
     .join(models.OrderLine, models.Order.id == models.OrderLine.order_id)\
     .filter(models.OrderLine.paid_status == False)\
     .filter(models.Order.status == models.OrderStatus.OPEN)\
     .group_by(models.User.username).all()

    unpaid_map = {r.username: r.unpaid_total for r in unpaid_query}
     
    return [
        {
            "waiter_name": r.username,
            "cash": round(float(r.cash_amount or 0), 2),
            "card": round(float(r.card_amount or 0), 2),
            "total": round(float(r.paid_total or 0), 2),
            "unpaid_amount": round(float(unpaid_map.get(r.username, 0)), 2),
            "order_count": int(r.order_count or 0)
        } for r in paid_query
    ]

# Licensing
def get_license_status(db: Session, device_id: str):
    return db.query(models.DeviceLicense).filter(models.DeviceLicense.device_id == device_id).first()

def caesar_cipher_decrypt(text: str, shift: int):
    result = ""
    for char in text:
        if char.isalnum():
            # Handle letters and numbers
            if char.isdigit():
                # Numbers 0-9
                new_val = (int(char) - shift) % 10
                result += str(new_val)
            elif char.isupper():
                result += chr((ord(char) - ord('A') - shift) % 26 + ord('A'))
            elif char.islower():
                result += chr((ord(char) - ord('a') - shift) % 26 + ord('a'))
        else:
            result += char
    return result

def activate_device(db: Session, device_id: str, activation_code: str):
    if not activation_code or len(activation_code) < 2:
        return None
        
    try:
        shift_digit = int(activation_code[0])
        encrypted_id = activation_code[1:]
        decrypted_id = caesar_cipher_decrypt(encrypted_id, shift_digit)
        
        if decrypted_id.lower() == device_id.lower():
            # Valid code
            db_license = get_license_status(db, device_id)
            from datetime import timedelta, datetime, timezone
            now = datetime.now(timezone.utc)
            if not db_license:
                db_license = models.DeviceLicense(
                    device_id=device_id,
                    expiry_date=now + timedelta(days=365)
                )
                db.add(db_license)
            else:
                db_license.is_active = True
                db_license.activation_date = now
                db_license.expiry_date = now + timedelta(days=365)
            
            db.commit()
            db.refresh(db_license)
            return db_license
    except Exception as e:
        print(f"Activation error: {e}")
        return None
    
    return None
