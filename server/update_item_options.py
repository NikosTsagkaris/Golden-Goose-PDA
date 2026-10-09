import psycopg2

def update_items():
    ids = [
        '5b50ed6d-4a46-4bb8-9b47-c38acea48ade', # CHEESEBURGER
        'ac5c1e4e-d005-466b-a291-e6d0c85b88c1', # HAMBURGER
        '284c5978-83c0-4433-8b6c-d855fd7ee647', # SPECIAL BURGER
        '9232522a-0e54-4dac-97ff-5dac234cec99', # CLUB SANDWICH ΖΑΜΠΟΝ / ΓΑΛΟΠΟΥΛΑ
        '2a809fea-cf86-45cb-9585-71cc696b333d'  # CLUB SANDWICH ΚΟΤΟΠΟΥΛΟ
    ]
    
    try:
        conn = psycopg2.connect("dbname=goldengoose_db user=ntvelop_app password=2105135381 host=localhost")
        conn.autocommit = True
        with conn.cursor() as cur:
            cur.execute("""
                UPDATE catalog_items 
                SET option_group_code = 2 
                WHERE id IN %s
            """, (tuple(ids),))
            print(f"Updated {cur.rowcount} items to option_group_code = 2")
            
            # Also ensure Group 0 is set for all items in ΚΑΦΕΣ and ΡΟΦΗΜΑΤΑ just in case
            cur.execute("""
                UPDATE catalog_items ci
                SET option_group_code = 0
                FROM catalog_categories cc
                WHERE ci.category_id = cc.id AND cc.name IN ('ΚΑΦΕΣ', 'ΡΟΦΗΜΑΤΑ')
            """)
            print(f"Verified Group 0 for coffee and beverages.")

        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    update_items()
