import subprocess

db = "goldengoose_db"

def run_sql(q):
    # Running directly on the server, no SSH
    cmd = ["sudo", "-u", "postgres", "psql", "-d", db, "-t", "-c", q]
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.stdout.strip()

print("Listing all tables and row counts:")
tables_q = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
raw_tables = run_sql(tables_q)
if not raw_tables:
    print("No tables found or error running SQL")
else:
    tables = raw_tables.split('\n')
    for t in tables:
        t = t.strip()
        if not t: continue
        count = run_sql(f"SELECT count(*) FROM {t}")
        print(f"Table: {t:20} Rows: {count}")
