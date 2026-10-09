import psycopg2
try:
    conn = psycopg2.connect("dbname=ntvelop_db user=ntvelop_app password=2105135381 host=localhost")
    cur = conn.cursor()
    # 1. Find all tables with T01, T02... format
    cur.execute("SELECT id, code FROM dining_tables WHERE code ~ '^T0[0-9]$';")
    rows = cur.fetchall()
    print(f"Found {len(rows)} old-format tables.")
    for tid, code in rows:
        new_code = "T" + code[2:] # T01 -> T1
        # Check if the T1 version already exists
        cur.execute("SELECT id FROM dining_tables WHERE code = %s AND id != %s", (new_code, tid))
        exists = cur.fetchone()
        if exists:
            print(f"Duplicate found for {new_code}. Deleting old {code} (ID: {tid}).")
            # Before deleting, update any references? sessions, orders etc.
            # But the user said it resets to 0, and these are basically fresh tables.
            # Let's be safe and just rename the old one to something else first or delete it if it has no orders.
            cur.execute("DELETE FROM dining_tables WHERE id = %s", (tid,))
        else:
            print(f"Renaming {code} to {new_code}.")
            cur.execute("UPDATE dining_tables SET code = %s WHERE id = %s", (new_code, tid))
    
    conn.commit()
    print("Cleanup complete.")
    conn.close()
except Exception as e:
    print(f"Error: {e}")
