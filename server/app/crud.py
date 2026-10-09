from sqlalchemy.orm import Session
from sqlalchemy import text

def get_active_users_with_pin_hash(db: Session):
    result = db.execute(text("SELECT id, username, pin_hash, role FROM app_users"))
    return [dict(r._mapping) for r in result]

def get_dining_tables(db: Session):
    sql = """
        SELECT 
            t.id,
            CASE WHEN s.id IS NOT NULL THEN 'OPEN' ELSE 'FREE' END as status,
            COALESCE(SUM(ol.qty * ci.price), 0) as unpaid_total,
            u.username as waiter_username,
            u.id::text as waiter_id,
            o.id::text as active_order_id
        FROM dining_tables t
        LEFT JOIN table_sessions s ON t.id = s.table_id AND s.closed_at IS NULL
        LEFT JOIN orders o ON s.id = o.session_id AND o.status = 'OPEN'
        LEFT JOIN order_lines ol ON o.id = ol.order_id AND ol.status = 'PENDING'
        LEFT JOIN catalog_items ci ON ol.item_id = ci.id
        LEFT JOIN app_users u ON s.opened_by = u.id::text
        GROUP BY t.id, s.id, o.id, u.id, u.username
        ORDER BY t.id
    """
    result = db.execute(text(sql))
    return [
        {
            "id": str(r.id),
            "display_name": f"T{i+1}",
            "area": "Κεντρική",
            "status": r.status,
            "unpaid_total": float(r.unpaid_total),
            "waiter_username": r.waiter_username,
            "waiter_id": r.waiter_id,
            "active_order_id": r.active_order_id
        }
        for i, r in enumerate(result)
    ]

def get_menu_categories(db: Session):
    result = db.execute(text("SELECT id::text, name, display_order FROM catalog_categories ORDER BY display_order"))
    categories = []
    
    for row in result.mappings().all():
        category = dict(row)
        
        prod_result = db.execute(
            text("""
                SELECT id::text, name, price, category_id::text as category_id, option_group_code as option_group 
                FROM catalog_items 
                WHERE category_id = :cid AND is_active = TRUE 
                ORDER BY name
            """),
            {"cid": category["id"]}
        )
        category["products"] = [dict(pr._mapping) for pr in prod_result]
        categories.append(category)
        
    return categories

def get_menu_items_by_category(db: Session, category_id: str):
    result = db.execute(text("SELECT id::text, name, price, category_id::text, option_group_code as option_group FROM catalog_items WHERE category_id = :cid"), {"cid": category_id})
    return [dict(r._mapping) for r in result]

def get_item_by_id(db: Session, item_id: str):
    result = db.execute(text("SELECT id::text, name, price, category_id::text FROM catalog_items WHERE id = :iid"), {"iid": item_id})
    row = result.mappings().first()
    return dict(row) if row else None

def get_option_groups_for_item(db: Session, item_id: str, category_id: str):
    result = db.execute(text("SELECT id::text, name, min_select, max_select FROM option_groups WHERE category_id = :cid"), {"cid": category_id})
    return [dict(r._mapping) for r in result]

def get_options_for_group(db: Session, group_id: str):
    result = db.execute(text("SELECT id::text, name, price FROM options WHERE group_id = :gid"), {"gid": group_id})
    return [dict(r._mapping) for r in result]

def get_active_session_for_table(db: Session, table_id: str):
    result = db.execute(text("SELECT id FROM table_sessions WHERE table_id = :tid AND closed_at IS NULL LIMIT 1"), {"tid": table_id})
    row = result.mappings().first()
    return dict(row) if row else None

def create_table_session(db: Session, table_id: str, opened_by: str):
    import uuid
    sess_id = str(uuid.uuid4())
    db.execute(text("INSERT INTO table_sessions (id, table_id, opened_by, opened_at) VALUES (:sid, :tid, :uid, NOW())"), 
               {"sid": sess_id, "tid": table_id, "uid": opened_by})
    db.commit()
    return {"id": sess_id}

def get_open_order_for_session(db: Session, session_id: str):
    result = db.execute(text("SELECT id, status FROM orders WHERE session_id = :sid AND status = 'OPEN' LIMIT 1"), {"sid": session_id})
    row = result.mappings().first()
    return dict(row) if row else None

def create_order(db: Session, session_id: str, created_by: str):
    import uuid
    order_id = str(uuid.uuid4())
    db.execute(text("INSERT INTO orders (id, session_id, created_by, status, created_at) VALUES (:oid, :sid, :uid, 'OPEN', NOW())"),
               {"oid": order_id, "sid": session_id, "uid": created_by})
    db.commit()
    return {"id": order_id}

def add_order_line(db: Session, order_id: str, item_id: str, qty: int, options_text: str = "", options_price: float = 0.0, note: str = None, options_json: dict = None, unit_price: float = 0.0, product_name: str = ""):
    import uuid
    import json
    line_id = str(uuid.uuid4())
    
    if item_id == 'custom-item-id':
        if not options_json:
            options_json = {}
        options_json['custom_name'] = product_name
        options_json['custom_price'] = unit_price
        
    db.execute(
        text("""
            INSERT INTO order_lines (id, order_id, item_id, qty, status, options_text, options_price, note, options_json) 
            VALUES (:lid, :oid, :iid, :qty, 'PENDING', :otext, :oprice, :note, :ojson)
        """),
        {
            "lid": line_id, 
            "oid": order_id, 
            "iid": item_id, 
            "qty": qty, 
            "otext": options_text, 
            "oprice": options_price, 
            "note": note, 
            "ojson": json.dumps(options_json) if options_json else None
        }
    )
    db.commit()
    return {"id": line_id}

def get_order_view(db: Session, order_id: str):
    sql = """
        SELECT 
            o.id::text,
            s.table_id::text as table_id,
            s.opened_by as waiter_id,
            o.status,
            o.created_at::text
        FROM orders o
        JOIN table_sessions s ON o.session_id = s.id
        WHERE o.id = :oid
    """
    result = db.execute(text(sql), {"oid": order_id})
    row = result.mappings().first()
    if not row: return None
    
    order_data = dict(row)
    
    # Get lines
    lines_sql = """
        SELECT 
            ol.id::text,
            ol.order_id::text,
            ci.name as product_name,
            ol.qty as quantity,
            ci.price as unit_price,
            ol.options_text,
            ol.options_price,
            ol.note,
            ol.status = 'PAID' as paid_status,
            ol.options_json
        FROM order_lines ol
        JOIN catalog_items ci ON ol.item_id = ci.id
        WHERE ol.order_id = :oid
    """
    lines_result = db.execute(text(lines_sql), {"oid": order_id})
    lines = []
    for lr in lines_result:
        line_dict = dict(lr._mapping)
        opt_json = line_dict.pop("options_json", None)
        if line_dict["product_name"] == "Ελεύθερο Είδος" or line_dict["options_text"] == "custom-item-id":
            import json
            try:
                p = json.loads(opt_json) if isinstance(opt_json, str) else opt_json
                if p and isinstance(p, dict):
                    if "custom_name" in p:
                        line_dict["product_name"] = p["custom_name"]
                    if "custom_price" in p:
                        line_dict["unit_price"] = float(p["custom_price"])
            except:
                pass
        lines.append(line_dict)
    
    order_data["lines"] = lines
    return order_data

def pay_order_lines(db: Session, order_id: str, line_ids: list, method: str):
    for line_id in line_ids:
        db.execute(text("UPDATE order_lines SET status = 'PAID' WHERE id = :lid"), {"lid": line_id})
        
    q_unpaid = text("SELECT COUNT(*) FROM order_lines WHERE order_id = :oid AND status != 'PAID'")
    unpaid_count = db.execute(q_unpaid, {"oid": order_id}).scalar()
    
    if unpaid_count == 0:
        db.execute(text("UPDATE orders SET status = 'PAID' WHERE id = :oid"), {"oid": order_id})
        session_id = db.execute(text("SELECT session_id FROM orders WHERE id = :oid"), {"oid": order_id}).scalar()
        if session_id:
            db.execute(text("UPDATE table_sessions SET closed_at = NOW() WHERE id = :sid"), {"sid": session_id})
            
    db.commit()
    return {"status": "ok"}

def sync_tables(db: Session, count: int):
    result = db.execute(text("SELECT id FROM dining_tables ORDER BY id"))
    tables = [r[0] for r in result]
    current_count = len(tables)
    
    if count > current_count:
        for i in range(current_count + 1, count + 1):
            db.execute(text("INSERT INTO dining_tables (id) VALUES (gen_random_uuid())"))
        db.commit()
    elif count < current_count:
        num_to_remove = current_count - count
        tables_to_remove = tables[-num_to_remove:]
        for table_id in tables_to_remove:
            # Delete related sessions first to avoid foreign key constraint
            db.execute(text("DELETE FROM table_sessions WHERE table_id = :id"), {"id": table_id})
            db.execute(text("DELETE FROM dining_tables WHERE id = :id"), {"id": table_id})
        db.commit()
    
    return True

def get_user_by_pin(db: Session, pin: str):
    result = db.execute(text("SELECT id::text, username, role FROM app_users WHERE pin_hash = :pin"), {"pin": pin})
    row = result.mappings().first()
    return dict(row) if row else None

def get_orders_by_status(db: Session, status: str):
    sql = """
        SELECT 
            o.id::text,
            s.table_id::text as table_id,
            s.opened_by as waiter_id,
            o.status,
            o.created_at::text
        FROM orders o
        JOIN table_sessions s ON o.session_id = s.id
        WHERE o.status = :status
        ORDER BY o.created_at DESC
    """
    result = db.execute(text(sql), {"status": status})
    orders_list = []
    
    for row in result.mappings().all():
        order_data = dict(row)
        
        # Get lines for this order
        lines_sql = """
            SELECT 
                ol.id::text,
                ol.order_id::text,
                ci.name as product_name,
                ol.qty as quantity,
                ci.price as unit_price,
                ol.options_text,
                ol.options_price,
                ol.note,
                ol.status = 'PAID' as paid_status,
                ol.options_json
            FROM order_lines ol
            JOIN catalog_items ci ON ol.item_id = ci.id
            WHERE ol.order_id = :oid
        """
        lines_result = db.execute(text(lines_sql), {"oid": order_data["id"]})
        lines = []
        for lr in lines_result:
            line_dict = dict(lr._mapping)
            opt_json = line_dict.pop("options_json", None)
            if line_dict["product_name"] == "Ελεύθερο Είδος" or line_dict["options_text"] == "custom-item-id":
                import json
                try:
                    p = json.loads(opt_json) if isinstance(opt_json, str) else opt_json
                    if p and isinstance(p, dict):
                        if "custom_name" in p:
                            line_dict["product_name"] = p["custom_name"]
                        if "custom_price" in p:
                            line_dict["unit_price"] = float(p["custom_price"])
                except:
                    pass
            lines.append(line_dict)
        order_data["lines"] = lines
        orders_list.append(order_data)
        
    return orders_list

def delete_order(db: Session, order_id: str):
    # Retrieve the order first to find the session
    result = db.execute(text("SELECT session_id FROM orders WHERE id = :oid"), {"oid": order_id})
    row = result.mappings().first()
    if not row:
        return False
    
    session_id = row["session_id"]
    
    # Delete order lines
    db.execute(text("DELETE FROM order_lines WHERE order_id = :oid"), {"oid": order_id})
    # Delete print jobs associated
    db.execute(text("DELETE FROM print_jobs WHERE order_id = :oid"), {"oid": order_id})
    # Delete order
    db.execute(text("DELETE FROM orders WHERE id = :oid"), {"oid": order_id})
    # Close the table session so the table is marked as FREE
    db.execute(text("UPDATE table_sessions SET closed_at = NOW() WHERE id = :sid"), {"sid": session_id})
    db.commit()
    return True

def get_options_by_code(db: Session, group_code: int):
    q = """
        SELECT o.id::text as id, o.name, (o.price_delta_cents / 100.0) as price
        FROM options o
        JOIN option_groups og ON o.group_id = og.id
        WHERE og.group_code = :gc AND o.is_active = TRUE
        ORDER BY o.sort_order
    """
    res = db.execute(text(q), {"gc": group_code}).mappings().all()
    return [dict(r) for r in res]



