import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# Add the new endpoint before the last line or after the last @router.get
new_endpoint = '''
@router.get("/pos/options/{group_code}")
def get_options_by_code(group_code: int, db: Session = Depends(get_db)):
    q = text("""
        SELECT o.id::text as id, o.name, (o.price_delta_cents / 100.0) as price
        FROM options o
        JOIN option_groups og ON o.group_id = og.id
        WHERE og.group_code = :gc AND o.is_active = TRUE
        ORDER BY o.sort_order
    """)
    res = db.execute(q, {"gc": group_code}).mappings().all()
    return [dict(r) for r in res]
'''

if "@router.get('/pos/options/{group_code}')" not in content and '@router.get("/pos/options/{group_code}")' not in content:
    # Insert before get_orders or at the end of routing section
    if "@router.get(\"/pos/orders\")" in content:
        content = content.replace("@router.get(\"/pos/orders\")", new_endpoint + "\n@router.get(\"/pos/orders\")")
    else:
        content += "\n" + new_endpoint
    print("Added get_options_by_code endpoint to pos.py.")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Pos.py API updated for dynamic options.")
