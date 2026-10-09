import psycopg2
tables = ['order_events', 'app_users', 'shifts', 'payments']
try:
    conn = psycopg2.connect("dbname=ntvelop_db user=ntvelop_app password=2105135381 host=localhost")
    cur = conn.cursor()
    for table in tables:
        cur.execute(f"SELECT column_name FROM information_schema.columns WHERE table_name = '{table}'")
        print(f"\nColumns in {table}:")
        for row in cur.fetchall():
            print(row[0])
    conn.close()
except Exception as e:
    print(f"Error: {e}")
