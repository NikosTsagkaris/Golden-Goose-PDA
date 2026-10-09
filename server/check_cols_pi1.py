import psycopg2
from psycopg2.extras import RealDictCursor

def list_columns():
    try:
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute("SELECT column_name, column_default FROM information_schema.columns WHERE table_name = 'order_lines' AND column_name = 'is_printed'")
            print("\n--- IS_PRINTED DEFAULT ---")
            for row in cur.fetchall():
                print(row)
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    list_columns()
