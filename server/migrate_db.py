import psycopg2
commands = [
    "ALTER TABLE order_lines ADD COLUMN IF NOT EXISTS item_name TEXT;",
    "ALTER TABLE order_lines ADD COLUMN IF NOT EXISTS note TEXT;",
    "ALTER TABLE order_lines ADD COLUMN IF NOT EXISTS status TEXT DEFAULT 'PENDING';",
    "ALTER TABLE order_lines ADD COLUMN IF NOT EXISTS is_plastic_cup BOOLEAN DEFAULT FALSE;",
    "ALTER TABLE order_lines ADD COLUMN IF NOT EXISTS options_text TEXT;"
]
try:
    conn = psycopg2.connect("dbname=ntvelop_db user=ntvelop_app password=2105135381 host=localhost")
    cur = conn.cursor()
    for cmd in commands:
        print(f"Executing: {cmd}")
        cur.execute(cmd)
    conn.commit()
    print("Migration successful.")
    conn.close()
except Exception as e:
    print(f"Error: {e}")
