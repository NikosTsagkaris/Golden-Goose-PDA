import psycopg2

def fix_enum():
    try:
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        conn.autocommit = True
        with conn.cursor() as cur:
            cur.execute("ALTER TYPE order_event_type ADD VALUE 'SHIFT_END'")
            print("Enum value SHIFT_END added successfully.")
    except Exception as e:
        if "already exists" in str(e):
            print("Enum value SHIFT_END already exists.")
        else:
            print(f"Error: {e}")
    finally:
        if conn:
            conn.close()

if __name__ == "__main__":
    fix_enum()
