import psycopg2

def update_db():
    try:
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        conn.autocommit = True
        with conn.cursor() as cur:
            # 1. Add group_code to option_groups
            cur.execute("ALTER TABLE option_groups ADD COLUMN IF NOT EXISTS group_code INTEGER DEFAULT -1")
            print("Added group_code to option_groups.")
            
            # 2. Create an example "group 2" for potatoes
            import uuid
            group_id = str(uuid.uuid4())
            cur.execute("""
                INSERT INTO option_groups (id, name, group_code, is_active)
                VALUES (%s, 'Επιλογές Φαγητού', 2, true)
                ON CONFLICT DO NOTHING
            """, (group_id,))
            
            cur.execute("""
                INSERT INTO options (id, group_id, name, price_delta_cents, is_active)
                VALUES 
                    (gen_random_uuid(), %s, 'Πατάτες', 100, true),
                    (gen_random_uuid(), %s, 'Σος', 50, true)
                ON CONFLICT DO NOTHING
            """, (group_id, group_id))
            print("Created example group 2 options.")

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    update_db()
