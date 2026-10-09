import psycopg2
from psycopg2.extras import RealDictCursor

def diag():
    try:
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            # 1. Check order_lines status counts
            cur.execute("SELECT status, count(*) FROM order_lines GROUP BY status")
            print("--- ORDER_LINES STATUS ---")
            for row in cur.fetchall():
                print(row)
            
            # 2. Check recent PAID lines
            cur.execute("SELECT id, order_id, status, payment_method, unit_price_cents, qty FROM order_lines WHERE status = 'PAID' LIMIT 10")
            print("\n--- RECENT PAID LINES ---")
            for row in cur.fetchall():
                print(row)
                
            # 3. Check enum values
            cur.execute("SELECT enumlabel FROM pg_enum JOIN pg_type ON pg_type.oid = pg_enum.enumtypid WHERE typname = 'order_event_type'")
            print("\n--- ORDER_EVENT_TYPE ENUM ---")
            for row in cur.fetchall():
                print(row)

            # 4. Check order_events counts
            cur.execute("SELECT event_type, count(*) FROM order_events GROUP BY event_type")
            print("\n--- ORDER_EVENTS TYPES ---")
            for row in cur.fetchall():
                print(row)
                
            # 4. Check if a SHIFT_END exist
            cur.execute("SELECT created_by, payload, created_at FROM order_events WHERE event_type = 'SHIFT_END' ORDER BY created_at DESC LIMIT 5")
            print("\n--- RECENT SHIFT_END EVENTS ---")
            for row in cur.fetchall():
                print(row)
                
            # 5. Check order counts / user
            cur.execute("SELECT created_by, count(*) FROM orders GROUP BY created_by")
            print("\n--- ORDERS BY USER ---")
            for row in cur.fetchall():
                print(row)

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    diag()
