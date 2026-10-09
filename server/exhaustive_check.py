import subprocess

db = "goldengoose_db"

def run_sql(q):
    cmd = ["ssh", "ntvelop@100.103.214.109", f"sudo -u postgres psql -d {db} -t -c \"{q}\""]
    res = subprocess.run(cmd, capture_output=True, text=True)
    return res.stdout.strip()

print("Listing all tables and row counts:")
tables_q = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
tables = run_sql(tables_q).split('\n')

for t in tables:
    t = t.strip()
    if not t: continue
    count = run_sql(f"SELECT count(*) FROM {t}")
    print(f"Table: {t:20} Rows: {count}")

print("\nSearching for 141 or 144 in payments:")
if 'payments' in tables:
    print(run_sql("SELECT * FROM payments WHERE amount > 0 LIMIT 10"))

print("\nSearching for paid lines:")
if 'order_lines' in tables:
    print(run_sql("SELECT count(*) FROM order_lines WHERE paid_status = true"))
