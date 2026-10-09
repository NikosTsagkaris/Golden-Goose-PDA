import psycopg2

def update_db():
    try:
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        conn.autocommit = True
        with conn.cursor() as cur:
            # 1. Add option_group_code to catalog_items
            cur.execute("ALTER TABLE catalog_items ADD COLUMN IF NOT EXISTS option_group_code INTEGER DEFAULT -1")
            print("Added option_group_code to catalog_items.")
            
            # 2. Set default codes for existing items based on category names for backward compat
            # Coffee -> 0
            cur.execute("""
                UPDATE catalog_items ci
                SET option_group_code = 0
                FROM catalog_categories cc
                WHERE ci.category_id = cc.id AND cc.name IN ('ΚΑΦΕΣ', 'HOTLY', 'Teabox', 'ΡΟΦΗΜΑΤΑ')
            """)
            print("Set default group 0 for coffee/beverages.")
            
            # Food -> 2 (Example)
            cur.execute("""
                UPDATE catalog_items ci
                SET option_group_code = 2
                FROM catalog_categories cc
                WHERE ci.category_id = cc.id AND cc.name = 'ΦΑΓΗΤΟ'
            """)
            print("Set default group 2 for food.")

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    update_db()
