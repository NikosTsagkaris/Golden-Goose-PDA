import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
import sys

def rename_database():
    try:
        # Connect to postgres default database to perform management tasks
        conn = psycopg2.connect(
            dbname='postgres', 
            user='postgres',
            host='localhost'
        )
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        cur = conn.cursor()
        
        print("Terminating active connections to briki_pos...")
        # Terminate other connections to the target database
        cur.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'briki_pos' AND pid != pg_backend_pid();")
        
        print("Renaming database from briki_pos to goldengoose_pos...")
        # Rename the database
        cur.execute("ALTER DATABASE briki_pos RENAME TO goldengoose_pos;")
        
        print("Database renamed successfully!")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

if __name__ == '__main__':
    rename_database()
