from sqlalchemy.orm import Session
from sqlalchemy import text
import json

def get_active_users_with_pin_hash(db: Session):
    result = db.execute(text('SELECT id::text as id, username, pin_hash, role FROM app_users'))
    return [dict(r._mapping) for r in result]

def get_dining_tables(db: Session):
    # Status "OPEN" (Red) only if there's an active order with at least one line (Sent item)
    result = db.execute(text('''
        SELECT 
            t.id::text as id, 
            'T' || (ROW_NUMBER() OVER (ORDER BY t.code))::text as display_name,
            COALESCE(t.area, '') as area,
            CASE 
                WHEN EXISTS (
                    SELECT 1 FROM order_lines ol 
                    WHERE ol.session_id = s.id
                ) THEN 'OPEN' 
                ELSE 'FREE' 
            END as status,
            COALESCE(u.username, '') as active_waiter_name,
            (SELECT id::text FROM orders WHERE session_id = s.id AND status = 'OPEN' LIMIT 1) as active_order_id,
            0.0 as unpaid_total -- placeholder for now
        FROM dining_tables t
        LEFT JOIN table_sessions s ON t.id = s.table_id AND s.closed_at IS NULL
        LEFT JOIN app_users u ON s.opened_by = u.id
        WHERE t.is_active = true
        ORDER BY t.code
    '''))
    return [dict(r._mapping) for r in result]

def get_menu_categories(db: Session):
    # Use sort_order as display_order and filter by is_active
    result = db.execute(text('SELECT id::text as id, name, sort_order as display_order FROM catalog_categories WHERE is_active = true ORDER BY sort_order'))
    categories = [dict(r._mapping) for r in result]
    for cat in categories:
        cat['products'] = get_menu_items_by_category(db, cat['id'])
    return categories

def get_menu_items_by_category(db: Session, category_id: str):
    # Use base_price_cents as price (converted to float/Euro) and filter by is_active
    result = db.execute(text('''
        SELECT id::text as id, name, (base_price_cents / 100.0) as price, category_id::text as category_id 
        FROM catalog_items 
        WHERE category_id = :cid AND is_active = true
        ORDER BY sort_order
    '''), {'cid': category_id})
    return [dict(r._mapping) for r in result]

def get_item_by_id(db: Session, item_id: str):
    result = db.execute(text('SELECT id::text as id, name, (base_price_cents / 100.0) as price, category_id::text as category_id FROM catalog_items WHERE id = :iid'), {'iid': item_id})
    row = result.mappings().first()
    return dict(row) if row else None

def get_option_groups_for_item(db: Session, item_id: str, category_id: str):
    result = db.execute(text('SELECT id::text as id, name, min_select, max_select FROM option_groups WHERE category_id = :cid'), {'cid': category_id})
    return [dict(r._mapping) for r in result]

def get_options_for_group(db: Session, group_id: str):
    result = db.execute(text('SELECT id::text as id, name, price FROM options WHERE group_id = :gid'), {'gid': group_id})
    return [dict(r._mapping) for r in result]

def get_active_session_for_table(db: Session, table_id: str):
    result = db.execute(text('SELECT id::text as id FROM table_sessions WHERE table_id = :tid AND closed_at IS NULL LIMIT 1'), {'tid': table_id})
    row = result.mappings().first()
    return dict(row) if row else None

def create_table_session(db: Session, table_id: str, opened_by: str):
    import uuid
    sess_id = str(uuid.uuid4())
    db.execute(text('INSERT INTO table_sessions (id, table_id, opened_by, opened_at) VALUES (:sid, :tid, :uid, NOW())'), 
               {'sid': sess_id, 'tid': table_id, 'uid': opened_by})
    db.commit()
    return {'id': sess_id}

def get_open_order_for_session(db: Session, session_id: str):
    result = db.execute(text("SELECT id::text as id, status FROM orders WHERE session_id = :sid AND status = 'OPEN' LIMIT 1"), {'sid': session_id})
    row = result.mappings().first()
    return dict(row) if row else None

def create_order(db: Session, session_id: str, created_by: str):
    import uuid
    order_id = str(uuid.uuid4())
    db.execute(text("INSERT INTO orders (id, session_id, created_by, status, created_at) VALUES (:oid, :sid, :uid, 'OPEN', NOW())"),
               {'oid': order_id, 'sid': session_id, 'uid': created_by})
    db.commit()
    return {'id': order_id}

def add_order_line(db: Session, order_id: str, item_id: str, qty: int, selected_options: list, created_by: str):
    import uuid
    # Get session_id from order
    order_res = db.execute(text("SELECT session_id::text as session_id FROM orders WHERE id = :oid"), {'oid': order_id})
    order_row = order_res.mappings().first()
    if not order_row: return None
    session_id = order_row['session_id']
    
    # Get price from item
    item_res = db.execute(text("SELECT base_price_cents FROM catalog_items WHERE id = :iid"), {'iid': item_id})
    item_row = item_res.mappings().first()
    if not item_row: return None
    price = item_row['base_price_cents']
    
    line_id = str(uuid.uuid4())
    db.execute(text("""
        INSERT INTO order_lines (id, order_id, session_id, item_id, qty, unit_price_cents, created_by, created_at, options_json) 
        VALUES (:lid, :oid, :sid, :iid, :qty, :price, :uid, NOW(), :opts)
    """), {
        'lid': line_id, 'oid': order_id, 'sid': session_id, 'iid': item_id, 
        'qty': qty, 'price': price, 'uid': created_by, 'opts': json.dumps(selected_options)
    })
    db.commit()
    return {'id': line_id}

def get_order_view(db: Session, order_id: str):
    # Enriched result with table info
    result = db.execute(text('''
        SELECT 
            o.id::text as id, 
            o.session_id::text as session_id, 
            o.status, 
            TO_CHAR(o.created_at, 'YYYY-MM-DD HH24:MI:SS') as created_at,
            o.created_by::text as waiter_id,
            t.id::text as table_id,
            'T' || (SELECT count(*) + 1 FROM dining_tables t2 WHERE t2.code < t.code) as table_name,
            COALESCE(t.area, '') as table_area
        FROM orders o
        JOIN table_sessions s ON o.session_id = s.id
        JOIN dining_tables t ON s.table_id = t.id
        WHERE o.id = :oid
    '''), {'oid': order_id})
    row = result.mappings().first()
    if not row: return None
    
    data = dict(row)
    # Add table object expected by app
    data['table'] = {
        'id': data['table_id'],
        'display_name': data['table_name'],
        'area': data['table_area'],
        'status': data['status'],
        'unpaid_total': 0.0 # placeholder
    }
    
    # Add lines
    lines_res = db.execute(text('''
        SELECT 
            ol.id::text as id,
            ol.order_id::text as order_id,
            ci.name as product_name,
            ol.qty as quantity,
            (ol.unit_price_cents / 100.0) as unit_price,
            (ol.payment_method IS NOT NULL) as paid_status
        FROM order_lines ol
        JOIN catalog_items ci ON ol.item_id = ci.id
        WHERE ol.order_id = :oid
    '''), {'oid': order_id})
    data['lines'] = [dict(r._mapping) for r in lines_res]
    
    return data

def pay_order_lines(db: Session, order_id: str, line_ids: list, method: str):
    for line_id in line_ids:
        db.execute(text("UPDATE order_lines SET payment_method = :m WHERE id = :lid"), {'lid': line_id, 'm': method})
    db.commit()
    return {'status': 'ok'}

def sync_tables(db: Session, count: int):
    # Get all existing codes
    result = db.execute(text('SELECT code, id FROM dining_tables ORDER BY code'))
    existing_tables = [dict(r._mapping) for r in result]
    existing_codes = {str(t['code']) for t in existing_tables}
    current_count = len(existing_tables)
    
    if count > current_count:
        # Add missing tables by filling gaps
        added = 0
        needed = count - current_count
        i = 1
        while added < needed:
            code = f'T{i:02d}'
            if code not in existing_codes:
                display_name = f'Τραπέζι {i}'
                db.execute(text('INSERT INTO dining_tables (id, code, display_name, is_active) VALUES (gen_random_uuid(), :code, :name, true)'),
                           {'code': code, 'name': display_name})
                added += 1
            i += 1
        db.commit()
    elif count < current_count:
        # Remove highest numbered tables (by code desc)
        num_to_remove = current_count - count
        tables_to_remove = sorted(existing_tables, key=lambda x: str(x['code']), reverse=True)[:num_to_remove]
        for t in tables_to_remove:
            table_id = t['id']
            # Delete related data first
            db.execute(text('DELETE FROM table_sessions WHERE table_id = :id'), {'id': table_id})
            db.execute(text('DELETE FROM dining_tables WHERE id = :id'), {'id': table_id})
        db.commit()
    
    return True
