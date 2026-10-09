import psycopg2
try:
    conn = psycopg2.connect("dbname=ntvelop_db user=ntvelop_app password=2105135381 host=localhost")
    cur = conn.cursor()
    cur.execute("ALTER TABLE orders ADD COLUMN IF NOT EXISTS waiter_name VARCHAR(255)")
    conn.commit()
    print("Column waiter_name added to orders table.")
    conn.close()
except Exception as e:
    print(f"Error: {e}")
