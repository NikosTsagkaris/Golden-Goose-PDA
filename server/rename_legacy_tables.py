import psycopg2
try:
    conn = psycopg2.connect("dbname=ntvelop_db user=ntvelop_app password=2105135381 host=localhost")
    cur = conn.cursor()
    cur.execute("UPDATE dining_tables SET code = 'LEGACY_' || code WHERE code ~ '^T0[0-9]$';")
    print(f"Renamed {cur.rowcount} legacy tables.")
    conn.commit()
    conn.close()
except Exception as e:
    print(f"Error: {e}")
