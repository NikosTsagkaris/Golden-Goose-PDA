import psycopg2

def fix_schema():
    try:
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        conn.autocommit = True
        with conn.cursor() as cur:
            cur.execute("ALTER TABLE order_events ALTER COLUMN session_id DROP NOT NULL")
            print("Successfully made order_events.session_id nullable.")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if conn:
            conn.close()

if __name__ == "__main__":
    fix_schema()
