import psycopg2
from psycopg2.extras import RealDictCursor

def get_items():
    try:
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        cur = conn.cursor(cursor_factory=RealDictCursor)
        
        cur.execute("""
            SELECT c.name as category, i.name as item, i.id, i.option_group_code 
            FROM catalog_items i 
            JOIN catalog_categories c ON i.category_id = c.id 
            WHERE c.name IN ('BURGERS / PIZZA', 'SNACKS', 'BRUNCH / SALADS') 
            ORDER BY c.name, i.name;
        """)
        items = cur.fetchall()
        print("Category | Item | ID | Option Group")
        print("-" * 50)
        for row in items:
            print(f"{row['category']} | {row['item']} | {row['id']} | {row['option_group_code']}")
        
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    get_items()
