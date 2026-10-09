import psycopg2
import uuid

def add_honey_option():
    try:
        # Connect to the database
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        conn.autocommit = True
        with conn.cursor() as cur:
            # 1. Find the group_id for option group code 4
            # Based on the screenshot, option group 4 is assigned to Teabox items.
            # We assume the column name is 'group_code' based on migrate_options_v2.py
            cur.execute("SELECT id, name FROM option_groups WHERE group_code = 4")
            group = cur.fetchone()
            
            if not group:
                print("Error: Could not find option group with code 4.")
                # Fallback: check if it's named 'Teabox' or similar
                cur.execute("SELECT id, name FROM option_groups WHERE name ILIKE '%Teabox%' OR name ILIKE '%Group 4%'")
                group = cur.fetchone()
                
            if group:
                group_id, group_name = group
                print(f"Found group: {group_name} (ID: {group_id})")
                
                # 2. Insert the 'Μέλι' option
                # Note: based on other scripts, price might be in cents or float. 
                # migrate_options_v2.py used 'price_delta_cents' with 100 for 1.00.
                # So for 0.30 we use 30.
                option_id = str(uuid.uuid4())
                cur.execute("""
                    INSERT INTO options (id, group_id, name, price_delta_cents, is_active)
                    VALUES (%s, %s, %s, %s, true)
                    ON CONFLICT (group_id, name) DO UPDATE SET price_delta_cents = EXCLUDED.price_delta_cents
                """, (option_id, group_id, 'Μέλι', 30))
                
                print("Successfully added 'Μέλι' option (+0.30) to group 4.")
            else:
                print("Error: Could not find target option group.")

    except Exception as e:
        print(f"Database error: {e}")
    finally:
        if 'conn' in locals():
            conn.close()

if __name__ == "__main__":
    add_honey_option()
