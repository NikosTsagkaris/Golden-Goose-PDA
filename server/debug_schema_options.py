import psycopg2
from psycopg2.extras import RealDictCursor

def list_schema():
    try:
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            # List all tables to see if option_X tables exist
            cur.execute("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")
            print("--- TABLES ---")
            tables = [r['table_name'] for r in cur.fetchall()]
            for t in tables:
                print(f"- {t}")
            
            # Check catalog_items
            cur.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'catalog_items'")
            print("\n--- CATALOG_ITEMS COLUMNS ---")
            for row in cur.fetchall():
                print(f"{row['column_name']}: {row['data_type']}")
                
            # Check option_groups
            if 'option_groups' in tables:
                cur.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'option_groups'")
                print("\n--- OPTION_GROUPS COLUMNS ---")
                for row in cur.fetchall():
                    print(f"{row['column_name']}: {row['data_type']}")
            
            # Check options
            if 'options' in tables:
                cur.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'options'")
                print("\n--- OPTIONS COLUMNS ---")
                for row in cur.fetchall():
                    print(f"{row['column_name']}: {row['data_type']}")

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    list_schema()
