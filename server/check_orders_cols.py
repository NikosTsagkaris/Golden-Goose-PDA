import psycopg2
try:
    conn = psycopg2.connect("dbname=ntvelop_db user=ntvelop_app password=2105135381 host=localhost")
    cur = conn.cursor()
    cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name = 'orders'")
    print("Columns in orders table:")
    for row in cur.fetchall():
        print(row[0])
    conn.close()
except Exception as e:
    print(f"Error: {e}")
