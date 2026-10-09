import os

file_path = "/home/ntvelop/brikipos/server/app/crud.py"
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
skip = False
in_function = False

# We want to replace the entire get_waiter_totals function
new_func = """def get_waiter_totals(db: Session):
    \"\"\"Get shift totals broken down by waiter, including unpaid amounts\"\"\"
    from sqlalchemy import func, case
    
    # Query for PAID totals - Using OrderLine directly to guarantee consistency with General Total
    paid_query = db.query(
        models.User.username,
        func.sum((models.OrderLine.unit_price + models.OrderLine.options_price) * models.OrderLine.quantity).label("paid_total"),
        func.count(func.distinct(models.Order.id)).label("order_count"),
        func.sum(case((models.OrderLine.payment_method == 'CASH', (models.OrderLine.unit_price + models.OrderLine.options_price) * models.OrderLine.quantity), else_=0)).label("cash_amount"),
        func.sum(case((models.OrderLine.payment_method == 'CARD', (models.OrderLine.unit_price + models.OrderLine.options_price) * models.OrderLine.quantity), else_=0)).label("card_amount")
    ).select_from(models.User)\\
     .outerjoin(models.Order, models.User.id == models.Order.waiter_id)\\
     .outerjoin(models.OrderLine, models.Order.id == models.OrderLine.order_id)\\
     .filter(models.User.role == 'WAITER')\\
     .filter(models.OrderLine.paid_status == True)\\
     .group_by(models.User.username).all()

    # Query for UNPAID totals
    unpaid_query = db.query(
        models.User.username,
        func.sum((models.OrderLine.unit_price * models.OrderLine.quantity) + models.OrderLine.options_price).label("unpaid_total")
    ).select_from(models.User)\\
     .join(models.Order, models.User.id == models.Order.waiter_id)\\
     .join(models.OrderLine, models.Order.id == models.OrderLine.order_id)\\
     .filter(models.OrderLine.paid_status == False)\\
     .filter(models.Order.status == models.OrderStatus.OPEN)\\
     .group_by(models.User.username).all()

    unpaid_map = {r.username: r.unpaid_total for r in unpaid_query}
    paid_map = {r.username: r for r in paid_query}
    
    # Get all waiters to ensure we show 0 count if no sales
    waiters = db.query(models.User).filter(models.User.role == 'WAITER').all()
     
    return [
        {
            "waiter_name": w.username,
            "cash": round(float(paid_map[w.username].cash_amount or 0), 2) if w.username in paid_map else 0.0,
            "card": round(float(paid_map[w.username].card_amount or 0), 2) if w.username in paid_map else 0.0,
            "total": round(float(paid_map[w.username].paid_total or 0), 2) if w.username in paid_map else 0.0,
            "unpaid_amount": round(float(unpaid_map.get(w.username, 0)), 2),
            "order_count": int(paid_map[w.username].order_count or 0) if w.username in paid_map else 0
        } for w in waiters
    ]
"""

for line in lines:
    if line.startswith("def get_waiter_totals(db: Session):"):
        in_function = True
        new_lines.append(new_func)
        continue
    
    if in_function:
        if line.startswith("# Licensing") or line.startswith("class ") or (line.startswith("def ") and not line.startswith("def get_waiter_totals")):
            in_function = False
            new_lines.append("\n" + line)
        else:
            continue
    else:
        new_lines.append(line)

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
print("Successfully replaced get_waiter_totals with unified logic.")
