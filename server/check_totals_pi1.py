from sqlalchemy import create_engine, text
import json

engine = create_engine('postgresql://ntvelop_app:ntvelop_pass@localhost/goldengoose_db')

with engine.connect() as conn:
    print("--- General Total Logic ---")
    sum_q = text("""
        SELECT 
            payment_method,
            SUM(unit_price_cents * qty) as total_cents,
            COUNT(DISTINCT order_id) as orders
        FROM order_lines
        WHERE status = 'PAID' AND is_voided = FALSE
        GROUP BY payment_method
    """)
    res = conn.execute(sum_q)
    for row in res:
        print(f"Method: {row[0]}, Total: {row[1]/100.0}, Orders: {row[2]}")

    print("\n--- Waiter Total Logic ---")
    waiter_q = text("""
        SELECT 
            u.username,
            ol.payment_method,
            SUM(ol.unit_price_cents * ol.qty) as total_cents,
            COUNT(DISTINCT ol.order_id) as orders
        FROM app_users u
        LEFT JOIN orders o ON o.created_by = u.id
        LEFT JOIN order_lines ol ON ol.order_id = o.id AND ol.is_voided = FALSE
        WHERE ol.status = 'PAID'
        GROUP BY u.username, ol.payment_method
    """)
    res = conn.execute(waiter_q)
    for row in res:
        print(f"Waiter: {row[0]}, Method: {row[1]}, Total: {row[2]/100.0}, Orders: {row[3]}")

    print("\n--- Checking for Lines that might be causing double counting ---")
    # Join that checks if a line is associated with multiple users? (Impossible)
    # Check if a line is associated with multiple orders?
    dup_q = text("""
        SELECT id, count(*) 
        FROM order_lines 
        WHERE status = 'PAID' AND is_voided = FALSE 
        GROUP BY id 
        HAVING count(*) > 1
    """)
    res = conn.execute(dup_q)
    dups = res.fetchall()
    print(f"Duplicate Lines: {len(dups)}")

    print("\n--- Checking for Orders with multiple created_by? (Impossible) ---")
    
    print("\n--- Summary Comparison ---")
    # Totals over all lines
    total_lines = conn.execute(text("SELECT SUM(unit_price_cents * qty) FROM order_lines WHERE status = 'PAID' AND is_voided = FALSE")).scalar() or 0
    # Totals over all waiter joins
    total_waiters = conn.execute(text("""
        SELECT SUM(ol.unit_price_cents * ol.qty) 
        FROM app_users u 
        JOIN orders o ON o.created_by = u.id 
        JOIN order_lines ol ON ol.order_id = o.id 
        WHERE ol.status = 'PAID' AND ol.is_voided = FALSE
    """)).scalar() or 0
    print(f"Total from Lines Directly: {total_lines/100.0}")
    print(f"Total from Waiter Join: {total_waiters/100.0}")
    print(f"Difference: {(total_waiters - total_lines)/100.0}")
