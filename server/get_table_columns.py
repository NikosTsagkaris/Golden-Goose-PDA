
import psycopg2

# Correct DB credentials from verify_local_login.py
DB_URL = "postgresql://ntvelop_app:2105135381@localhost:5432/ntvelop_db"

def check():
    try:
        conn = psycopg2.connect(DB_URL)
        cur = conn.cursor()
        
        print("--- COLUMNS FOR dining_tables ---")
        cur.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'dining_tables'")
        rows = cur.fetchall()
        for row in rows:
            print(f"Column: {row[0]} ({row[1]})")
            
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    check()
