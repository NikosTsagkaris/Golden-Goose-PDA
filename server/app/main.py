from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional
from app import models, schemas
from app.database import get_db, engine

models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="Golden Goose POS API")

@app.get("/")
def read_root():
    return {"message": "Golden Goose POS API Running"}

# --- AUTH ---
@app.post("/auth/login")
def login(req: schemas.LoginRequest, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.pin == req.pin).first()
    if not user:
        raise HTTPException(status_code=401, detail="Invalid PIN")
    return {
        "id": user.id,
        "username": user.username,
        "role": user.role,
        "full_name": user.full_name
    }

# --- TABLES ---
@app.get("/tables")
def get_tables(db: Session = Depends(get_db)):
    tables = db.query(models.Table).order_by(models.Table.number).all()
    res = []
    for t in tables:
        open_order = db.query(models.Order).filter(models.Order.table_id == t.id, models.Order.status.in_(["OPEN", "SENT"])).first()
        unpaid = 0.0
        waiter = None
        status_str = "FREE"
        order_id = None
        if open_order:
            status_str = open_order.status or "OPEN"
            order_id = open_order.id
            waiter = getattr(open_order, 'waiter_name', None) or (open_order.waiter.username if getattr(open_order, 'waiter', None) else "Waiter")
            lines = db.query(models.OrderLine).filter(
                models.OrderLine.order_id == open_order.id,
                models.OrderLine.paid_status != "PAID"
            ).all()
            unpaid = sum(((l.unit_price or 0.0) + (l.options_price or 0.0)) * (l.quantity or 1) for l in lines if getattr(l, "is_voided", False) is not True)
        res.append({
            "id": t.id,
            "number": t.number,
            "area": t.area,
            "status": status_str,
            "unpaid_total": unpaid,
            "waiter": waiter,
            "open_order_id": order_id
        })
    return res

@app.post("/tables/{table_id}/orders")
def open_table_order(table_id: int, waiter_name: str = "Waiter", db: Session = Depends(get_db)):
    try:
        order = db.query(models.Order).filter(models.Order.table_id == table_id, models.Order.status.in_(["OPEN", "SENT"])).first()
        if not order:
            order = models.Order(table_id=table_id, waiter_name=waiter_name, status="OPEN")
            db.add(order)
            db.commit()
            db.refresh(order)
        return {"order_id": order.id, "table_id": table_id, "status": order.status}
    except Exception:
        db.rollback()
        raise

@app.post("/setup/tables/sync")
def sync_tables(payload: dict, db: Session = Depends(get_db)):
    try:
        total_tables = payload.get("total_tables", payload.get("count", 10))
        all_tables = db.query(models.Table).order_by(models.Table.number.asc()).all()
        existing_numbers = {t.number for t in all_tables}

        # Create missing tables up to total_tables
        for i in range(1, total_tables + 1):
            if i not in existing_numbers:
                new_table = models.Table(number=i, area="Main" if i <= 5 else "Garden")
                db.add(new_table)

        # Delete extra tables above total_tables if no active orders exist
        if len(all_tables) > total_tables:
            for t in all_tables:
                if t.number > total_tables:
                    has_active = db.query(models.Order).filter(models.Order.table_id == t.id, models.Order.status.in_(["OPEN", "SENT"])).first()
                    if not has_active:
                        db.delete(t)

        db.commit()
        return {"status": "success", "tables_count": total_tables}
    except Exception as e:
        db.rollback()
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

# --- MENU ---
@app.get("/menu/categories")
def get_menu(db: Session = Depends(get_db)):
    categories = db.query(models.Category).order_by(models.Category.display_order).all()
    result = []
    for c in categories:
        products = db.query(models.Product).filter(
            models.Product.category_id == c.id,
            models.Product.is_available.isnot(False)
        ).all()
        result.append({
            "id": c.id,
            "name": c.name,
            "products": [
                {
                    "id": p.id,
                    "name": p.name,
                    "price": p.price,
                    "option_group": getattr(p, "option_group", -1),
                    "option_group_code": str(getattr(p, "option_group_code", getattr(p, "option_group", -1)))
                } for p in products
            ]
        })
    return result

@app.get("/pos/options/{group_code}")
def get_options(group_code: str, db: Session = Depends(get_db)):
    options = db.query(models.Option).filter(models.Option.group_code == group_code).all()
    return [{"id": o.id, "name": o.name, "price": o.price}] if options else []

# --- ORDERS ---
@app.get("/orders/open")
def get_open_orders(db: Session = Depends(get_db)):
    orders = db.query(models.Order).filter(
        models.Order.status.in_(["OPEN", "SENT"])
    ).all()

    result = []

    for order in orders:
        lines = db.query(models.OrderLine).filter(
            models.OrderLine.order_id == order.id,
            models.OrderLine.is_voided.is_(False)
        ).all()

        line_list = [
            {
                "id": line.id,
                "line_id": line.id,
                "product_name": line.product_name,
                "name": line.product_name,
                "quantity": line.quantity,
                "unit_price": line.unit_price,
                "price": (line.unit_price + line.options_price) * line.quantity,
                "options_text": line.options_text or "",
                "options_price": line.options_price or 0.0,
                "note": line.note,
                "paid_status": line.paid_status,
                "paid": line.paid_status == "PAID",
                "payment_method": getattr(line, "payment_method", None)
            }
            for line in lines
        ]

        result.append({
            "id": order.id,
            "table_id": order.table_id,
            "waiter_name": order.waiter_name,
            "status": order.status,
            "lines": line_list,
            "products": line_list
        })

    return result

@app.get("/orders/paid")
def get_paid_orders(db: Session = Depends(get_db)):
    orders = db.query(models.Order).filter(models.Order.status == "PAID").all()
    result = []
    for order in orders:
        lines = db.query(models.OrderLine).filter(models.OrderLine.order_id == order.id).all()
        line_list = [
            {
                "id": l.id,
                "line_id": l.id,
                "product_name": l.product_name,
                "name": l.product_name,
                "quantity": l.quantity,
                "unit_price": l.unit_price,
                "options_text": l.options_text or "",
                "options_price": l.options_price or 0.0,
                "note": l.note,
                "paid_status": l.paid_status,
                "paid": l.paid_status == "PAID",
                "payment_method": getattr(l, "payment_method", "CASH")
            } for l in lines
        ]
        result.append({
            "id": order.id,
            "table_id": order.table_id,
            "waiter_name": order.waiter_name,
            "status": order.status,
            "lines": line_list,
            "products": line_list
        })
    return result

@app.get("/orders/{order_id}")
def get_order(order_id: int, db: Session = Depends(get_db)):
    order = db.query(models.Order).filter(models.Order.id == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    lines = db.query(models.OrderLine).filter(models.OrderLine.order_id == order_id).all()
    return {
        "id": order.id,
        "table_id": order.table_id,
        "waiter_name": order.waiter_name,
        "status": order.status,
        "lines": [
            {
                "id": l.id,
                "product_name": l.product_name,
                "quantity": l.quantity,
                "unit_price": l.unit_price,
                "options_text": l.options_text,
                "options_price": l.options_price,
                "note": l.note,
                "paid_status": l.paid_status,
                "is_voided": l.is_voided
            } for l in lines
        ]
    }

@app.post("/orders/{order_id}/lines")
def add_order_line(order_id: int, line: schemas.LineCreate, db: Session = Depends(get_db)):
    try:
        qty = max(1, line.quantity)
        created_ids = []
        for _ in range(qty):
            db_line = models.OrderLine(
                order_id=order_id,
                product_name=line.product_name,
                quantity=1,
                unit_price=line.unit_price,
                options_text=line.options_text,
                options_price=line.options_price,
                note=line.note,
                is_plastic_cup=line.is_plastic_cup
            )
            db.add(db_line)
            db.flush()
            created_ids.append(db_line.id)
        db.commit()
        return {"status": "success", "line_id": created_ids[0], "line_ids": created_ids}
    except Exception:
        db.rollback()
        raise

@app.post("/orders/{order_id}/submit")
def submit_order(order_id: int, db: Session = Depends(get_db)):
    try:
        order = db.query(models.Order).filter(models.Order.id == order_id).first()
        if not order:
            raise HTTPException(status_code=404, detail="Order not found")

        order.status = "SENT"

        lines = db.query(models.OrderLine).filter(models.OrderLine.order_id == order_id).all()
        payload = f"--- ΠΑΡΑΓΓΕΛΙΑ #{order_id} ---\n"
        for line in lines:
            payload += f"{line.quantity}x {line.product_name} - {line.unit_price}€\n"
            if line.options_text:
                payload += f"   ({line.options_text})\n"

        print_job = models.PrintJob(
            order_id=order_id,
            content=payload,
            status="PENDING"
        )
        db.add(print_job)
        db.commit()

        return {"status": "success", "message": "Order submitted and print job created"}
    except Exception:
        db.rollback()
        raise

@app.post("/orders/{order_id}/pay")
def pay_order(order_id: int, payload: dict, db: Session = Depends(get_db)):
    try:
        line_id = payload.get("line_id")
        line_ids = payload.get("line_ids")
        method = payload.get("method", "CASH")

        target_line_ids = []
        if line_id is not None and str(line_id).strip() != "":
            target_line_ids.append(line_id)
        if line_ids and isinstance(line_ids, list):
            target_line_ids.extend(line_ids)

        if target_line_ids:
            for lid in target_line_ids:
                line = None
                try:
                    lid_int = int(float(str(lid).strip()))
                    line = db.query(models.OrderLine).filter(models.OrderLine.id == lid_int).first()
                except (ValueError, TypeError):
                    line = None

                if line:
                    line.paid_status = "PAID"
                    line.payment_method = method
                    calc = float((line.unit_price or 0.0) + (line.options_price or 0.0)) * int(line.quantity or 1)
                    payment = models.Payment(line_id=line.id, amount=calc, method=method)
                    db.add(payment)
        else:
            unpaid_lines = db.query(models.OrderLine).filter(
                models.OrderLine.order_id == order_id,
                models.OrderLine.paid_status != "PAID",
                models.OrderLine.is_voided.is_(False)
            ).all()
            for l in unpaid_lines:
                l.paid_status = "PAID"
                l.payment_method = method
                calc = float((l.unit_price or 0.0) + (l.options_price or 0.0)) * int(l.quantity or 1)
                payment = models.Payment(line_id=l.id, amount=calc, method=method)
                db.add(payment)

        db.flush()

        # Check if all active order lines are paid
        all_lines = db.query(models.OrderLine).filter(models.OrderLine.order_id == order_id).all()
        active_lines = [l for l in all_lines if getattr(l, 'is_voided', False) is not True]
        unpaid_active = [l for l in active_lines if getattr(l, 'paid_status', 'UNPAID') != "PAID"]

        if active_lines and len(unpaid_active) == 0:
            order = db.query(models.Order).filter(models.Order.id == order_id).first()
            if order:
                order.status = "PAID"

        db.commit()
        return {"status": "success", "message": "Payment processed"}
    except Exception as e:
        db.rollback()
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/orders/{order_id}")
def delete_order(order_id: int, db: Session = Depends(get_db)):
    try:
        order = db.query(models.Order).filter(models.Order.id == order_id).first()
        if not order:
            raise HTTPException(status_code=404, detail="Order not found")

        prev_status = order.status or "OPEN"
        order.status = "VOIDED"

        lines = db.query(models.OrderLine).filter(models.OrderLine.order_id == order_id).all()
        for l in lines:
            l.is_voided = True

        total_amount = sum(((l.unit_price or 0.0) + (l.options_price or 0.0)) * (l.quantity or 1) for l in lines)

        # Record ActionLog for deleted order
        log_entry = models.ActionLog(
            waiter_id=order.waiter_id,
            action_type="DELETE_ORDER",
            details=f"Διαγραφή παραγγελίας #{order.id} (Τραπέζι {order.table_id}, Κατάσταση: {prev_status}) – Σύνολο: €{total_amount:.2f}"
        )
        db.add(log_entry)
        db.commit()
        return {"status": "voided", "message": f"Order {order_id} voided and logged"}
    except Exception as e:
        db.rollback()
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

# --- SHIFTS & ADMIN ---
@app.get("/shifts/totals")
def get_shift_totals(db: Session = Depends(get_db)):
    try:
        paid_lines = db.query(models.OrderLine).filter(
            models.OrderLine.paid_status == "PAID",
            models.OrderLine.is_voided.isnot(True)
        ).all()

        cash = 0.0
        card = 0.0
        for l in paid_lines:
            line_total = float((l.unit_price or 0.0) + (l.options_price or 0.0)) * int(l.quantity or 1)
            method = (getattr(l, "payment_method", None) or "CASH").upper()
            if method == "CARD":
                card += line_total
            else:
                cash += line_total

        total = cash + card
        order_count = db.query(models.Order).count()

        return {
            "cash": cash,
            "card": card,
            "total": total,
            "cash_total": cash,
            "card_total": card,
            "grand_total": total,
            "order_count": order_count
        }
    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/shifts/end")
def end_shift(db: Session = Depends(get_db)):
    try:
        totals = get_shift_totals(db)
        cash_total = totals["cash"]
        card_total = totals["card"]
        grand_total = totals["total"]

        # Record End Shift ActionLog before clearing shift data
        shift_log = models.ActionLog(
            action_type="END_SHIFT",
            details=f"Τέλος Βάρδιας – Σύνολο: €{grand_total:.2f} (Μετρητά: €{cash_total:.2f}, Κάρτα: €{card_total:.2f})"
        )
        db.add(shift_log)
        db.flush()

        # Delete child tables before parent tables to prevent foreign key errors
        db.query(models.Payment).delete(synchronize_session=False)
        db.query(models.PrintJob).delete(synchronize_session=False)
        db.query(models.OrderLine).delete(synchronize_session=False)
        db.query(models.Order).delete(synchronize_session=False)

        db.commit()
        return {"status": "success", "message": "Shift ended and reset complete"}
    except Exception as e:
        db.rollback()
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/shifts/print-summary")
def print_shift_summary():
    return {"status": "success", "message": "Shift summary sent to printer"}

@app.get("/admin/totals/waiters")
def get_waiter_totals(db: Session = Depends(get_db)):
    try:
        orders = db.query(models.Order).all()
        waiter_groups = {}
        for o in orders:
            w_name = o.waiter_name or (o.waiter.username if getattr(o, "waiter", None) else "Σερβιτόρος")
            if w_name not in waiter_groups:
                waiter_groups[w_name] = []
            waiter_groups[w_name].append(o)

        res = []
        for w_name, o_list in waiter_groups.items():
            order_ids = [o.id for o in o_list]
            lines = db.query(models.OrderLine).filter(models.OrderLine.order_id.in_(order_ids)).all() if order_ids else []

            cash = 0.0
            card = 0.0
            unpaid = 0.0

            for l in lines:
                if getattr(l, "is_voided", False) is True:
                    continue
                line_amount = float((l.unit_price or 0.0) + (l.options_price or 0.0)) * int(l.quantity or 1)
                p_status = getattr(l, "paid_status", "UNPAID")
                if p_status == "PAID":
                    p_method = (getattr(l, "payment_method", None) or "CASH").upper()
                    if p_method == "CARD":
                        card += line_amount
                    else:
                        cash += line_amount
                else:
                    unpaid += line_amount

            res.append({
                "waiter_id": w_name,
                "waiter_name": w_name,
                "cash": cash,
                "card": card,
                "total": cash + card,
                "order_count": len(o_list),
                "unpaid_total": unpaid
            })
        return res
    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/admin/logs")
def get_admin_logs(db: Session = Depends(get_db)):
    try:
        logs = db.query(models.ActionLog).order_by(models.ActionLog.created_at.desc()).all()
        res = []
        for l in logs:
            waiter_dict = None
            if l.waiter:
                waiter_dict = {"id": str(l.waiter.id), "username": l.waiter.username, "full_name": l.waiter.full_name}
            res.append({
                "id": str(l.id),
                "action_type": l.action_type,
                "details": l.details,
                "created_at": l.created_at.isoformat() if l.created_at else "",
                "waiter": waiter_dict
            })
        return res
    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/admin/logs")
def clear_admin_logs(db: Session = Depends(get_db)):
    try:
        db.query(models.ActionLog).delete()
        db.commit()
        return {"status": "success", "message": "Logs cleared"}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))
